package content.region.misthalin.lumbridge.plugin.gnomecopter.sign.impl

import content.region.misthalin.lumbridge.plugin.gnomecopter.sign.InformationSign
import core.api.openSingleTab
import core.api.sendString
import core.game.node.entity.player.Player
import core.game.world.map.Location
import shared.consts.Components

enum class LletyaInformationSign(val location: Location, vararg info: String) : InformationSign {

    UNKNOWN_SIGN(Location(0, 0, 0), "Not Implemented yet.");

    private val info: Array<String> = info as Array<String>

    override fun read(player: Player) {
        openSingleTab(player, Components.CARPET_INFO_723)
        val information = info.joinToString("<br>")
        sendString(player, information, Components.CARPET_INFO_723, 10)
    }
}