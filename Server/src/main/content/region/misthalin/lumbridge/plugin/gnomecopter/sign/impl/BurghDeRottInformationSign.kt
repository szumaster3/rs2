package content.region.misthalin.lumbridge.plugin.gnomecopter.sign.impl

import content.region.misthalin.lumbridge.plugin.gnomecopter.sign.InformationSign
import core.api.openSingleTab
import core.api.sendString
import core.game.component.Component
import core.game.node.entity.player.Player
import core.game.world.map.Location
import core.tools.DARK_RED
import shared.consts.Components
import shared.consts.Quests

// Completed
enum class BurghDeRottInformationSign(
    val location: Location, vararg info: String,
) : InformationSign {
    GENERAL_STORE(
        Location(2153, 5431, 0),
        "This player is stocking the",
        "general store with useful",
        "items.",
        "",
        "Down in swampy Morytania,",
        "the locals' idea of a tasty",
        "meal is a giant snail, so the",
        "shop needs plenty of those.",
        "There's also plenty of demand",
        "for basic tools such as axes",
        "and tinderboxes."
    ),
    GLOBAL_SIGN(
        Location(2160, 5427, 0),
        "Vampyres can only be hunt",
        "with silver weapons, such as",
        "blessed silver sickles and",
        "silver crossbow bolts.",
        "",
        "Once they are sufficiently",
        "weakened, they can be",
        "finished off with the use of a",
        "special silver rod and a dose",
        "of a certain kind of potion.",
        "You'll learn all about fighting",
        "vampyres if you complete the",
        "${DARK_RED}${Quests.IN_AID_OF_THE_MYREQUE}</col> quest."
    ),
    FURNACE_SIGN(
        Location(2162, 5409, 0),
        "The furnace in burgh de Rott",
        "needs to be repaired and",
        "stocked with coal before it",
        "can be used.",
        "",
        "This player is using it to",
        "make a special rod out of a",
        "silver and mithril alloy, to help",
        "when fighting vampyres."
    ),
    BANK_SIGN(
        Location(2140, 5408, 0),
        "Every town needs a bank.",
        "The bank in Burgh de Rott is",
        "badly in need of repair, so",
        "this player is fixing it. The",
        "hole in the bank's wall also",
        "needs patching."
    );

    private val info: Array<String> = info as Array<String>

    override fun read(player: Player) {
        openSingleTab(player, Components.CARPET_INFO_723)
        val information = info.joinToString("<br>")
        sendString(player, information, Components.CARPET_INFO_723, 10)
    }
}