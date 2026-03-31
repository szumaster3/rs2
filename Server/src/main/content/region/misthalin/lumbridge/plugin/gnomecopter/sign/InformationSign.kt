package content.region.misthalin.lumbridge.plugin.gnomecopter.sign

import core.game.node.entity.player.Player

interface InformationSign {
    fun read(player: Player)
}