package core.game.world.update

import core.game.node.entity.player.Player
import core.game.world.map.RegionManager
import core.game.world.repository.Repository
import core.ServerConstants
import java.nio.BufferOverflowException

object PlayerRenderer {
    @JvmStatic
    fun render(player: Player) {
        val session = player.session ?: return
        val view = session.playerInfo ?: return
        val local = player.renderInfo.localPlayers
        val indices = player.renderInfo.localPlayerIndices
        if (view.needsAppearance(player.index)) {
            local.clear()
            indices.clear()
        }
        val previous = indices.toMap()
        val occupiedSlots = HashSet<Int>()
        local.clear()
        for ((slot, other) in previous) {
            if (slot == other.index && isCurrent(other) && slot != player.index &&
                other.isActive && !other.isInvisible && other.location.withinDistance(player.location) &&
                occupiedSlots.add(slot)) {
                local.add(other)
            }
        }
        var added = 0
        for (other in RegionManager.getLocalPlayers(player, 15)) {
            if (local.size >= 255 || added >= 10) break
            if (other === player || !isCurrent(other) || other.index == player.index ||
                !other.isActive || other.isInvisible ||
                // A recycled slot must first be removed from the client's active list.
                previous.containsKey(other.index) || !occupiedSlots.add(other.index)) continue
            local.add(other)
            added++
        }
        val frame = try {
            track(view, player)
            for (other in local) {
                try {
                    track(view, other)
                } catch (_: BufferOverflowException) {
                    // A single oversized remote block cannot be sent: leave it untracked.
                }
            }
            view.encode()
        } catch (_: BufferOverflowException) {
            // The local player must be present; no legal frame can omit its update.
            System.err.println("[Protocol] Local player update exceeds client payload capacity")
            session.disconnect()
            return
        }
        local.removeIf { !view.isLocal(it.index) }
        indices.clear()
        for (other in local) indices[other.index] = other
        session.write(frame)
    }

    private fun isCurrent(player: Player): Boolean =
        player.index in 1..minOf(2047, ServerConstants.MAX_PLAYERS) && Repository.players.get(player.index) === player

    private fun track(view: PlayerInfo, player: Player) {
        val location = player.location
        val traversal = when {
            player.properties.isTeleporting -> 127
            player.walkingQueue.runDir != -1 -> 2
            player.walkingQueue.walkDir != -1 -> 1
            else -> null
        }
        val block = player.updateMasks.playerBlock(player, view.needsAppearance(player.index), traversal)
        view.track(player.index, location.x, location.y, location.z, block)
    }
}
