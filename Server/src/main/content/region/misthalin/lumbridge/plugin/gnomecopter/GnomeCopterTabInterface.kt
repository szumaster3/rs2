package content.region.misthalin.lumbridge.plugin.gnomecopter

import core.api.sendString
import core.game.interaction.InterfaceListener
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
    }
}