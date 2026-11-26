package content.global.plugins.iface

import content.data.GameAttributes
import core.api.getAttribute
import core.api.openInterface
import core.api.sendMessage
import core.game.component.Component
import core.game.interaction.InterfaceListener
import core.game.node.entity.player.Player
import shared.consts.Components

class MainGameInterface : InterfaceListener {
    val TOPLEVEL = Components.TOPLEVEL_548
    val TOPLEVEL_FS = Components.TOPLEVEL_FULLSCREEN_746
    val RUN_BUTTON = Components.TOPSTAT_RUN_750
    val FILTER_BUTTONS = Components.FILTERBUTTONS_751
    val REPORT_ABUSE = 594

    override fun defineInterfaceListeners() {
        on(FILTER_BUTTONS){player, _, _, buttonID, _, _ ->
            if(buttonID == 28)
                openReport(player)
            return@on true
        }

        on(RUN_BUTTON){player, _, opcode, buttonID, _, _ ->
            if (buttonID == 1 && opcode == 155) player.settings.toggleRun()
            return@on true
        }

        on(TOPLEVEL_FS){player, _, _, buttonID, _, _ ->
            if (buttonID == 174)
                if(!getAttribute(player, GameAttributes.TUTORIAL_COMPLETE, false)) {
                    openInterface(player, 764)
                } else {
                    configureWorldMap(player)
                }
            return@on true
        }

        on(TOPLEVEL){player, _, _, buttonID, _, _ ->
            if (buttonID == 130) {
                if (!getAttribute(player, GameAttributes.TUTORIAL_COMPLETE, false)) {
                    openInterface(player, 764)
                } else {
                    configureWorldMap(player)
                }
            }
            return@on true
        }
    }

    fun openReport(player: Player) {
        // snapshot_main_load is the revision 578 on-load script on component 32.
        player.interfaceManager.openTemporaryRoot(Component(REPORT_ABUSE))
    }

    private fun configureWorldMap(player: Player) {
        if (player.inCombat()) {
            sendMessage(player, "It wouldn't be very wise opening the world map during combat.")
            return
        }
        if (player.locks.isInteractionLocked() || player.locks.isMovementLocked()) {
            sendMessage(player, "You can't do this right now.")
            return
        }
        val posHash = player.location.z shl 28 or (player.location.x shl 14) or player.location.y
        player.packetDispatch.sendVarcUpdate(622.toShort(), posHash)
        player.interfaceManager.openTemporaryRoot(Component(755))
    }
}