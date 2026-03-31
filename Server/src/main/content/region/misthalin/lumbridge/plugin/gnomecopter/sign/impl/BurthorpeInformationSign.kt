package content.region.misthalin.lumbridge.plugin.gnomecopter.sign.impl

import content.region.misthalin.lumbridge.plugin.gnomecopter.sign.InformationSign
import core.api.openSingleTab
import core.api.sendString
import core.game.node.entity.player.Player
import core.game.world.map.Location
import shared.consts.Components

enum class BurthorpeInformationSign(val location: Location, vararg info: String) : InformationSign {
    CORRIDOR_SIGN(Location(2399, 5396, 0),
    "This corridor leads to a",
        "selection of waiting rooms,",
        "one for each boardgame.",
        "",
        "Players wishing to play a",
        "specific game can wait in",
        "the appropriate room,",
        "making it easier for them",
        "to arrange a match.",
        "",
        "Additional waiting rooms",
        "are provided upstairs for",
        "the sole use of players",
        "with high ranks in the",
        "boardgames. For the elite",
        "gamer, that's the place",
        "where any challenge will be",
        "from a worthy foe."
    );

    private val info: Array<String> = info as Array<String>

    override fun read(player: Player) {
        openSingleTab(player, Components.CARPET_INFO_723)
        val information = info.joinToString("<br>")
        sendString(player, information, Components.CARPET_INFO_723, 10)
    }
}