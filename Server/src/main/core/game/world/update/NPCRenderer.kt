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
        val buffer = IoBuffer(if (wide) ServerProt.NPC_INFO_LARGE else ServerProt.NPC_INFO_SMALL, PacketHeader.SHORT)
        val bits = if (wide) 8 else 5
        val limit = 1 shl (bits - 1)
        val info = player.renderInfo
        val localNPCs = info.localNpcs
        val previous = info.localNpcIndices.toMap()
        val next = LinkedHashMap<Int, NPC>()
        val maskBuffer = IoBuffer(-1, PacketHeader.NORMAL, ByteBuffer.allocate(1 shl 16))
        buffer.setBitAccess()
        buffer.putBits(8, previous.size)
        val removed = HashMap<Int, NPC>()
        for ((slot, npc) in previous) {
            val withinDistance = player.location.withinDistance(npc.location)
            if (slot != npc.index || !isCurrent(npc) || !npc.isActive || npc.isHidden(player) || !withinDistance || npc.properties.isTeleporting) {
                buffer.putBits(1, 1).putBits(2, 3)
                removed[slot] = npc
                npc.aggressiveHandler?.removeTolerance(player.index)
                continue
            }
            next[slot] = npc
            val walk = npc.walkingQueue.walkDir
            val run = npc.walkingQueue.runDir
            if (walk in movementDirections.indices && run in movementDirections.indices) {
                buffer.putBits(1, 1).putBits(2, 2).putBits(1, 1)
                    .putBits(3, movementDirections[walk]).putBits(3, movementDirections[run])
                flagMaskUpdate(player, buffer, maskBuffer, npc, false)
            } else if (walk in movementDirections.indices || run in movementDirections.indices) {
                // A single queued step is encoded using the one-direction movement form.
                val direction = if (walk in movementDirections.indices) walk else run
                buffer.putBits(1, 1).putBits(2, 1).putBits(3, movementDirections[direction])
                flagMaskUpdate(player, buffer, maskBuffer, npc, false)
            } else if (npc.updateMasks.isUpdateRequired) {
                buffer.putBits(1, 1).putBits(2, 0)
                writeMaskUpdates(player, maskBuffer, npc, false)
            } else {
                buffer.putBits(1, 0)
            }
        }
        for (npc in nearby) {
            if (next.size >= 255) {
                break
            }
            // 32767 terminates the new-NPC bit stream; NPC types occupy 14 bits.
            if (!isCurrent(npc) || npc.id !in 0 until 16384 ||
                next.containsKey(npc.index) ||
                // The client does not destroy an NPC removed and re-added in this same update.
                (removed.containsKey(npc.index) && removed[npc.index] !== npc) || !npc.isActive || npc.isHidden(player) ||
                !player.location.withinDistance(npc.location)) {
                continue
            }
            val offsetX = npc.location.x - player.location.x
            val offsetY = npc.location.y - player.location.y
            if (offsetX !in -limit until limit || offsetY !in -limit until limit) continue
            val dir = movementDirections[npc.direction.ordinal]
            buffer.putBits(15, npc.index).putBits(bits, offsetY).putBits(bits, offsetX)
            flagMaskUpdate(player, buffer, maskBuffer, npc, true)
            buffer.putBits(14, npc.id).putBits(3, dir).putBits(2, npc.location.z)
                .putBits(1, if (npc.properties.isTeleporting) 1 else 0)
            if (npc.aggressiveHandler != null) {
                npc.aggressiveHandler.playerTolerance[player.index] = GameWorld.ticks
            }
            next[npc.index] = npc
        }
        val masks = maskBuffer.toByteBuffer()
        masks.flip()
        if (masks.hasRemaining()) {
            buffer.putBits(15, 32767)
            buffer.setByteAccess()
            buffer.put(masks)
        } else {
            buffer.setByteAccess()
        }
        info.localNpcIndices.clear()
        info.localNpcIndices.putAll(next)
        localNPCs.clear()
        localNPCs.addAll(next.values)
        return buffer
    }

    private fun isCurrent(npc: NPC): Boolean =
        npc.index in 1..minOf(32766, ServerConstants.MAX_NPCS) && Repository.npcs.get(npc.index) === npc

    /**
     * Sets the mask update flag.
     * @param player The player.
     * @param buffer The buffer to write on.
     * @param maskBuffer The mask buffer for NPC updates.
     * @param npc The NPC.
     * @param sync Whether this update is during synchronization.
     */
    private fun flagMaskUpdate(player: Player, buffer: IoBuffer, maskBuffer: IoBuffer, npc: NPC, sync: Boolean) {
        if (npc.updateMasks.isUpdateRequired || (sync && npc.updateMasks.hasSynced())) {
            buffer.putBits(1, 1)
            writeMaskUpdates(player, maskBuffer, npc, sync)
        } else {
            buffer.putBits(1, 0)
        }
    }

    /**
     * Writes the mask updates.
     * @param player The player.
     * @param maskBuffer The mask buffer to write on.
     * @param npc The NPC to update.
     * @param sync If called upon synchronization.
     */
    private fun writeMaskUpdates(player: Player?, maskBuffer: IoBuffer?, npc: NPC, sync: Boolean) {
        if (sync) {
            npc.updateMasks.writeSynced(player, npc, maskBuffer!!, true)
        } else {
            npc.updateMasks.write(player, npc, maskBuffer!!)
        }
    }
}
