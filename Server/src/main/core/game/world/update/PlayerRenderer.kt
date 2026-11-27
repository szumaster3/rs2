package core.game.world.update

import core.game.node.entity.player.Player
import core.game.world.map.RegionManager

object PlayerRenderer {
    @JvmStatic
    fun render(player: Player) {
        val session = player.session ?: return
        val view = session.playerInfo ?: return
        val local = player.renderInfo.localPlayers
        if (view.needsAppearance(player.index)) local.clear()
        val previous = local.associateBy { it.index }
        val occupiedSlots = HashSet<Int>()
        local.clear()
        for (other in previous.values) {
            if (other.index in 1..2047 && other.index != player.index &&
                other.isActive && !other.isInvisible && other.location.withinDistance(player.location) &&
                occupiedSlots.add(other.index)) {
                local.add(other)
            }
        }
        var added = 0
        for (other in RegionManager.getLocalPlayers(player, 15)) {
            if (local.size >= 255 || added >= 10) break
            if (other === player || other.index !in 1..2047 || other.index == player.index ||
                !other.isActive || other.isInvisible ||
                // A recycled slot must first be removed from the client's active list.
                previous.containsKey(other.index) || !occupiedSlots.add(other.index)) continue
            local.add(other)
            added++
        }
        track(view, player)
        for (other in local) track(view, other)
        session.write(view.encode())
    }

    private fun track(view: PlayerInfo578, player: Player) {
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
