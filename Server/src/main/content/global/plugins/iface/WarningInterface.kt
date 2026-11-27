package content.global.plugins.iface

import core.api.getVarbit
import core.api.sendMessage
import core.game.interaction.InterfaceListener
import core.game.node.entity.player.link.warning.WarningManager
import core.game.node.entity.player.link.warning.WarningType
import shared.consts.Components

/**
 * Represents the warning interface.
 * @author szu
 */
class WarningInterface : InterfaceListener {

    override fun defineInterfaceListeners() {
        /*
         * Handles toggle of warnings.
         */

        on(Components.CWS_DOOMSAYER_583) { player, _, _, buttonID, _, _ ->
            val warning = WarningType.byButton[buttonID] ?: return@on true

            if (warning == WarningType.PVP_WORLDS) {
                sendMessage(player, "This option is only available on PvP worlds.")
                return@on true
            }

            val timesSeen = getVarbit(player, warning.varbit)
            if (!warning.binaryToggle && timesSeen < 6) {
                sendMessage(
                    player,
                    "You cannot toggle this warning screen on or off. You need to go to the area it is linked to enough times to have the option to do so."
                )
                return@on true
            }

            val wasDisabled = WarningManager.isDisabled(player, warning)
            WarningManager.toggleWarning(player, warning)

            val status = if (wasDisabled) "enabled" else "disabled"
            sendMessage(player, "You have $status the ${warning.name.lowercase().replace('_', ' ')} warning.")

            return@on true
        }
    }
}
