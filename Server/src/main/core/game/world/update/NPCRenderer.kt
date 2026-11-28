package core.game.world.update

import core.net.packet.ServerProt

import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.world.GameWorld
import core.game.world.map.RegionManager
import core.game.world.repository.Repository
import core.ServerConstants
import core.net.packet.IoBuffer
import core.net.packet.PacketHeader
import java.nio.ByteBuffer
import java.nio.BufferOverflowException

/**
 * The NPC renderer.
 * @author Emperor
 */
object NPCRenderer {
    /**
     * Handles the NPC rendering for a player.
     * @param player The player.
     */
    @JvmStatic
    fun render(player: Player) {
        player.session.write(encode(player, RegionManager.getLocalNpcs(player), player.playerFlags.isUpdateSceneGraph))
    }

    // Npc.move uses N,NE,E,SE,S,SW,W,NW; spawn angle = (wire + 4) * 2048 mod 16384.
    private val movementDirections = intArrayOf(7, 0, 1, 6, 2, 5, 4, 3)

    fun encode(player: Player, nearby: List<NPC>, wide: Boolean): IoBuffer {
        val buffer = IoBuffer(if (wide) ServerProt.NPC_INFO_LARGE else ServerProt.NPC_INFO_SMALL,
            PacketHeader.SHORT, ByteBuffer.allocate(ServerProt.MAX_PAYLOAD_SIZE))
        val bits = if (wide) 8 else 5
        val limit = 1 shl (bits - 1)
        val info = player.renderInfo
        val localNPCs = info.localNpcs
        val previous = info.localNpcIndices.toMap()
        val next = LinkedHashMap<Int, NPC>()
        val maskBuffer = IoBuffer(-1, PacketHeader.NORMAL, ByteBuffer.allocate(ServerProt.MAX_PAYLOAD_SIZE))
        val scratch = IoBuffer(-1, PacketHeader.NORMAL, ByteBuffer.allocate(ServerProt.MAX_PAYLOAD_SIZE))
        // Reserve 3 bits for every unprocessed local removal and 15 for the sentinel.
        fun fits(bitsToAdd: Int, blockBytes: Int, remainingLocal: Int): Boolean =
            (buffer.bitPosition + bitsToAdd + 3 * remainingLocal + 15 + 7) / 8 +
                maskBuffer.toByteBuffer().position() + blockBytes <= ServerProt.MAX_PAYLOAD_SIZE
        fun prepareMask(npc: NPC, sync: Boolean): Boolean {
            scratch.clear()
            return try {
                if (npc.updateMasks.isUpdateRequired || (sync && npc.updateMasks.hasSynced())) {
                    if (sync) npc.updateMasks.writeSynced(player, npc, scratch, true)
                    else npc.updateMasks.write(player, npc, scratch)
                }
                true
            } catch (_: BufferOverflowException) {
                false
            }
        }
        fun appendMask() {
            val data = scratch.toByteBuffer().duplicate()
            data.flip()
            maskBuffer.put(data)
        }
        buffer.setBitAccess()
        buffer.putBits(8, previous.size)
        val removed = HashMap<Int, NPC>()
        val budgetRemoved = HashSet<Int>()
        var remainingLocal = previous.size
        for ((slot, npc) in previous) {
            remainingLocal--
            val withinDistance = player.location.withinDistance(npc.location)
            val walk = npc.walkingQueue.walkDir
            val run = npc.walkingQueue.runDir
            val eligible = slot == npc.index && isCurrent(npc) && npc.isActive && !npc.isHidden(player) &&
                withinDistance && !npc.properties.isTeleporting
            val prepared = eligible && prepareMask(npc, false)
            val maskBytes = if (prepared) scratch.toByteBuffer().position() else 0
            val movementBits = when {
                walk in movementDirections.indices && run in movementDirections.indices -> 11
                walk in movementDirections.indices || run in movementDirections.indices -> 7
                maskBytes > 0 -> 3
                else -> 1
            }
            if (!prepared || !fits(movementBits, maskBytes, remainingLocal)) {
                buffer.putBits(1, 1).putBits(2, 3)
                removed[slot] = npc
                if (eligible) budgetRemoved.add(slot)
                npc.aggressiveHandler?.removeTolerance(player.index)
                continue
            }
            next[slot] = npc
            if (walk in movementDirections.indices && run in movementDirections.indices) {
                buffer.putBits(1, 1).putBits(2, 2).putBits(1, 1)
                    .putBits(3, movementDirections[walk]).putBits(3, movementDirections[run])
                buffer.putBits(1, if (maskBytes > 0) 1 else 0)
            } else if (walk in movementDirections.indices || run in movementDirections.indices) {
                // A single queued step is encoded using the one-direction movement form.
                val direction = if (walk in movementDirections.indices) walk else run
                buffer.putBits(1, 1).putBits(2, 1).putBits(3, movementDirections[direction])
                buffer.putBits(1, if (maskBytes > 0) 1 else 0)
            } else if (maskBytes > 0) {
                buffer.putBits(1, 1).putBits(2, 0)
            } else {
                buffer.putBits(1, 0)
            }
            appendMask()
        }
        for (npc in nearby) {
            if (next.size >= 255) {
                break
            }
            // 32767 terminates the new-NPC bit stream; NPC types occupy 14 bits.
            if (!isCurrent(npc) || npc.id !in 0 until 16384 ||
                next.containsKey(npc.index) ||
                // Preserve ordinary same-entity teleport re-adds, but never re-add a budget
                // eviction or a recycled index until the next update.
                npc.index in budgetRemoved || (removed.containsKey(npc.index) && removed[npc.index] !== npc) ||
                !npc.isActive || npc.isHidden(player) ||
                !player.location.withinDistance(npc.location)) {
                continue
            }
            val offsetX = npc.location.x - player.location.x
            val offsetY = npc.location.y - player.location.y
            if (offsetX !in -limit until limit || offsetY !in -limit until limit) continue
            if (!prepareMask(npc, true)) continue
            val maskBytes = scratch.toByteBuffer().position()
            val spawnBits = 15 + 2 * bits + 1 + 14 + 3 + 2 + 1
            if (!fits(spawnBits, maskBytes, 0)) continue
            val dir = movementDirections[npc.direction.ordinal]
            buffer.putBits(15, npc.index).putBits(bits, offsetY).putBits(bits, offsetX)
            buffer.putBits(1, if (maskBytes > 0) 1 else 0)
            buffer.putBits(14, npc.id).putBits(3, dir).putBits(2, npc.location.z)
                .putBits(1, if (npc.properties.isTeleporting) 1 else 0)
            if (npc.aggressiveHandler != null) {
                npc.aggressiveHandler.playerTolerance[player.index] = GameWorld.ticks
            }
            next[npc.index] = npc
            appendMask()
        }
        val masks = maskBuffer.toByteBuffer()
        masks.flip()
        buffer.putBits(15, 32767)
        buffer.setByteAccess()
        buffer.put(masks)
        info.localNpcIndices.clear()
        info.localNpcIndices.putAll(next)
        localNPCs.clear()
        localNPCs.addAll(next.values)
        return buffer
    }

    private fun isCurrent(npc: NPC): Boolean =
        npc.index in 1..minOf(32766, ServerConstants.MAX_NPCS) && Repository.npcs.get(npc.index) === npc

}
