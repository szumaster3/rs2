package content.region.misthalin.lumbridge.plugin.gnomecopter.sign.impl

import content.region.misthalin.lumbridge.plugin.gnomecopter.sign.InformationSign
import core.api.openSingleTab
import core.api.sendString
import core.game.node.entity.player.Player
import core.game.world.map.Location
import core.tools.DARK_RED
import shared.consts.Components
import shared.consts.Quests

enum class  TrollweissAndRellekkaInformationSign(val location: Location, vararg info: String,
) : InformationSign {
    RELLEKKA_EAGLES_SIGN(Location(2543, 5430, 1),
        "In the icy mountain, a small",
        "cavern contains the lair of",
        "some giant eagles.",
        "",
        "After completing the ${DARK_RED}Eagles</col>",
        "${DARK_RED}Peak</col> quest, players may catch",
        "them with a rope and ride",
        "them to other eagle nests,",
        "around the world."
    ),

    EAST_OF_TROLLWEISS_SIGN(Location(2543, 5422, 1),
        "East of the Trollweiss and",
        "Rellekka Hunter area lies a",
        "range of snowy mountains",
        "populated by fearsome ice",
        "trolls and wolves. Players",
        "come here for the ${DARK_RED}Desert</col>",
        "${DARK_RED}Treasure</col> quest and to",
        "enter the insanely",
        "dangerous ${DARK_RED}God Wars</col>",
        "${DARK_RED}Dungeon</col>.",
        "",
        "Not all quests are just",
        "about fighting, however. In",
        "the ${DARK_RED}${Quests.TROLL_ROMANCE}</col> quest,",
        "players are challenged to",
        "help a particularly soppy",
        "troll gain the affections of",
        "their beloved. This involves",
        "building a sled and taking it",
        "for a ride down this",
        "mountain!"
    );

    private val info: Array<String> = info as Array<String>

    override fun read(player: Player) {
        openSingleTab(player, Components.CARPET_INFO_723)
        val information = info.joinToString("<br>")
        sendString(player, information, Components.CARPET_INFO_723, 10)
    }
}