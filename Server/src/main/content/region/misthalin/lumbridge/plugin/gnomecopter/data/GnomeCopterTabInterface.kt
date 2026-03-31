package content.region.misthalin.lumbridge.plugin.gnomecopter.data

import content.region.misthalin.lumbridge.plugin.gnomecopter.GnomeCopterDestination
import core.game.interaction.InterfaceListener
import shared.consts.Components

class GnomeCopterTabInterface : InterfaceListener {

    override fun defineInterfaceListeners() {

        on(Components.CARPET_MAIN_728) { player, _, _, button, _, _ ->

            val destination = player.getAttribute(
                "gc:route",
                GnomeCopterDestination.default()
            )

            var page = player.getAttribute("gc:page", 0)
            val max = destination.tab.pages.size - 1

            when (button) {
                FIRST_PAGE -> page = 0
                LAST_PAGE -> page = max
                NEXT_PAGE -> if (page < max) page++
                PREVIOUS_PAGE -> if (page > 0) page--
            }
            PagedInformationManager.sendPage(player, destination.tab, page)
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
    }
}