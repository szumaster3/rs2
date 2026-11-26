package content.global.plugins.iface

import core.game.interaction.InterfaceListener
import shared.consts.Components

/**
 * Handles the welcome screen;
 * - CS2 1187 also sends CloseIface.
 */
class LoginInterface : InterfaceListener {
    override fun defineInterfaceListeners() {
        on(Components.WELCOME_SCREEN_378) { _, _, _, _, _, _ ->
            // The client executes if_close (CS2 1187); the close packet owns the transition.
            return@on true
        }
    }
}
