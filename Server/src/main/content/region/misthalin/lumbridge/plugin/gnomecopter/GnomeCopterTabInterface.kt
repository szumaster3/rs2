package content.region.misthalin.lumbridge.plugin.gnomecopter

import core.api.sendString
import core.game.interaction.InterfaceListener
import core.tools.DARK_BLUE
import core.tools.DARK_RED
import shared.consts.Components

class GnomeCopterTabInterface : InterfaceListener{


    override fun defineInterfaceListeners() {
        on(Components.CARPET_MAIN_728) { player, _, _, _, _, _ ->
            val destination = player.getAttribute("gc:route", GnomeCopterDestination.CASTLE_WARS)
            sendString(player, "Welcome to", Components.CARPET_MAIN_728,4)
            sendString(player, destination.displayName, Components.CARPET_MAIN_728,5)
            return@on true
        }
    }


    companion object {
        val TOGGLE_AUTO_PILOT = 17
        val RETURN_TO_LUMBRIDGE = 18
        val FIRST_PAGE = 20
        val PREVIOUS_PAGE = 21
        val NEXT_PAGE = 22
        val LAST_PAGE = 23

        val CASTLE_WARS_PAGE_0 = arrayOf(
            "In Castle Wars, two teams of",
            "players indulge in no-holds-",
            "barred battle across this huge",
            "arena.",
            "",
            "The saradomin team, wearing",
            "${DARK_BLUE}blue</col> hoods, must attempt to",
            "breach the Zamorak castle,",
            "seize the ${DARK_RED}red flag</col> from the",
            "roof and survive to drag it",
        )

        val CASTLE_WARS_PAGE_1 = arrayOf(
            "back to their own castle",
            "without letting their own flag",
            "be stolen.",
            "",
            "The Zamorak team, in ${DARK_RED}red</col>",
            "hoods, must breach the",
            "Saradomin castle seize the",
            "${DARK_BLUE}blue flag</col> from the roof and",
            "survive to drag it back to",
            "their own castle."
        )

        val CASTLE_WARS_PAGE_2 = arrayOf(
            "This minigame is always",
            "popular, offering players the",
            "chance to fight tactically and",
            "use their skills to pass their",
            "enemies' defences or just to",
            "vent some frustration by",
            "unleashing all their rage on",
            "their opponents.",
        )

        val CASTLE_WARS_PAGE_3 = arrayOf(
            "The teams win points by",
            "getting their opponents' flag",
            "home to their castle.",
            "",
            "After 25 minutes, the game",
            "ends and everyone on the",
            "winning team is presented",
            "with ${DARK_RED}Castle Wars tickets</col>.",
            "",
            "There's a man outside",
        )

        val CASTLE_WARS_PAGE_4 = arrayOf(
            "skirt the action and hop",
            "across the stepping-stones in",
            "the river.",
            "",
            "Either of these approaches",
            "would leave the player with",
            "the challenge of breaking into",
            "the enemy castle once they've",
            "arrived. Check out the",
            "noticeboards around the",
        )

        val CASTLE_WARS_PAGE_5 = arrayOf(
            "castles to see how this may",
            "be achieved.",
            "",
            "A more subtle approach is to",
            "use the tunnels under the",
            "battlefield. The tunnels run",
            "between the two castles,",
            "allowing sneak attacks on the",
            "enemy stronghold through the",
            "floor.",
        )


    }

}