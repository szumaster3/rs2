package content.region.misthalin.lumbridge.plugin.gnomecopter.sign.impl

import content.region.misthalin.lumbridge.plugin.gnomecopter.sign.InformationSign
import core.api.openSingleTab
import core.api.sendString
import core.game.node.entity.player.Player
import core.game.world.map.Location
import core.tools.DARK_RED
import shared.consts.Components

// Completed.
enum class CastleWarsInformationSign(
    val location: Location, vararg info: String,
) : InformationSign {
    CENTER_ISLAND_SIGN(
        Location(2028, 5409, 0),
        "In the centre of this island,",
        "trapdoors lead down into the",
        "tunnels below. The tunnels",
        "lead into the basements of",
        "the two castles.",
        "",
        "Players may collapse the",
        "tunnels, forcing the enemy to",
        "mine through the rubble in",
        "order to proceed."
    ),

    WALL_SIGN(
        Location(2039, 5397, 0),
        "A determined player can scale",
        "the castle walls by deftly",
        "throwing a rope over the",
        "battlements, then climbing up.",
        "",
        "Of course, there's a high",
        "chance that someone up there",
        "will swiftly kill them."
    ),

    MAIN_SIGN(
        Location.create(2023, 5392, 0), // Location of sign.
        "~ What is this? ~",
        "This is the storeroom of",
        "the Zamorak castle. Players",
        "collect equipment from here",
        "before running into the",
        "fray",
        "",
        "They also have to fight off",
        "any Saradomin fighters",
        "who've breached the castle.",
        "",
        "Under the area are",
        "tunnels that lead into the",
        "basements of the two",
        "castles. Players will often",
        "defend their tunnel by using",
        "a ${DARK_RED}pickaxe</col> to collapse the",
        "walls.",
        "",
        "The ${DARK_RED}barricades</col> can be used",
        "to block passageways. To",
        "get past a barricade,",
        "players can destroy it with",
        "their weapon, set it on fire",
        "or blow it up with an",
        "explosive potion.",
        "",
        "The ${DARK_RED}explosive potions</col> can",
        "also be used to blow up",
        "the enemy team's catapult",
        "and to clear rockslides in",
        "the tunnels under the",
        "area.",
        "",
        "${DARK_RED}Toolkits</col> can be used to",
        "repair catapults that have",
        "been blown up.",
        "",
        "Once a catapult's working,",
        "players are able to fire",
        "${DARK_RED}rocks</col> into the battlefield,",
        "damaging anyone who gets",
        "in the way.",
        "",
        "${DARK_RED}ropes</col> are handy when you",
        "want to climb up the",
        "enemy castle's walls."
    ),
    MAIN_SIGN_NEXT_FLOOR(
        Location.create(2023, 5392, 0), // Place location of transform to presents 2nd floor.
        "~ What is this? ~",
        "This is the spawning room",
        "of the Saradomin castle.",
        "",
        "Players enter and exit the",
        "game through the round",
        "portal. They also reappear",
        "here if they die while",
        "playing Castle Wars. It's a",
        "safe minigame, so players",
        "don't lose any items when",
        "they die.",
        "",
        "The table in the corner of",
        "the room contains",
        "bandages. Players can take",
        "as many of these as they",
        "like to ."
    ),
    MAIN_SIGN_SPAWN_ROOM_FLOOR(
        Location.create(2023, 5392, 0), // Place location of transform to presents 2nd floor.
        "This is the spawning room",
        "of the Saradomin castle.",
        "",
        "Players enter and exit the",
        "game through the round",
        "portal. They also reappear",
        "here if they die while",
        "playing Castle Wars. It's a",
        "safe minigame, so players",
        "don't lose any items when",
        "they die.",
        "",
        "The table in the corner of",
        "the room contains",
        "bandages. Players can take",
        "as many of these as they",
        "like to heal themselves",
        "when they're fighting.",
        "Bandages can also be used",
        "to heal team-mates."
    ),
    MAIN_SIGN_ROOF_SARADOMIN_EXAMPLE_1(
        Location.create(2023, 5392, 0), // Place location of transform to presents roof area.
        "~ What is this? ~",
        "This is the roof of the",
        "Saradomin castle. The blue",
        "flag is here at the",
        "beginning of the game and",
        "it stays here until one of",
        "the Zamorak team arrives",
        "to capture it.",
        "",
        "In this example, there are",
        "two Saradomin warriors",
        "standing guard, repelling all",
        "attempts to steal the flag.",
        "a ${DARK_RED}binding spell</col> on the flag-",
        "bearer, which prevents the",
        "bearer from walking for a",
        "few seconds. He also sets",
        "up a ${DARK_RED}barricade</col> to block the",
        "staircase.",
        "",
        "Although a second Zamorak",
        "warrior sets the barricade",
        "on fire to destroy it, he is",
        "successfully prevented from",
        "helping his friend to steal",
        "the flag."
    ),

    // Phew it's back!
    CASTLE_WARS_ROOF_ZAMORAK(
        Location.create(2023, 5392, 0), // Place location of transform to presents roof area.
        "This is the roof of the",
        "Zamorak castle. The red flag",
        "is here at the beginning of the",
        "game and it stays here until",
        "one of the Saradomin team",
        "arrives to capture it.",
        "",
        "In this example, the flag is",
        "stolen by a Saradomin",
        "warrior. The Zamorak",
        "defender pursues him,",
        "retrieves the flag when he",
        "dies, then brings it back here."
    );

    private val info: Array<String> = info as Array<String>

    override fun read(player: Player) {
        openSingleTab(player, Components.CARPET_INFO_723)
        val information = info.joinToString("<br>")
        sendString(player, information, Components.CARPET_INFO_723, 10)
    }
}