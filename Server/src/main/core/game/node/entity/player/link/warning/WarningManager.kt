package core.game.node.entity.player.link.warning

import core.api.*
import core.game.component.Component
import core.game.node.entity.player.Player
import core.tools.Log
import core.api.sendMessage
import core.api.sendInterfaceConfig

object WarningManager {

    @JvmStatic
    fun trigger(player: Player, warning: WarningType, action: () -> Unit) {
        if (isDisabled(player, warning)) {
            action()
        } else {
            log(this::class.java, Log.INFO, "${player.name}: Trigger warning: $warning")
            player.debug("Trigger warning: $warning")
            player.pendingWarning = PendingWarning(warning) { action() }
            openWarningInterface(player, warning)
        }
    }

    fun handleButton(player: Player, warning: WarningType, buttonId: Int) {
        when (buttonId) {
                        // Ok.
            17, 18 -> {
                closeOverlay(player)
                closeInterface(player)
                player.pendingWarning?.action?.execute(player)
                player.pendingWarning = null
            }
            // Cancel.
            19, 20, 28 -> {
                toggleWarning(player, warning)
                sendToggleMessage(player, warning)
            }
        }
    }

    fun isDisabled(player: Player, warning: WarningType): Boolean =
        getVarbit(player, warning.varbit) == if (warning.binaryToggle) 1 else 7

    fun hasPending(player: Player): Boolean =
        player.pendingWarning != null

    private fun openWarningInterface(player: Player, warning: WarningType) {
        if (isDisabled(player, warning)) return

        player.interfaceManager.open(Component(warning.component))
        incrementWarning(player, warning)
    }

    private fun incrementWarning(player: Player, warning: WarningType) {
        val current = getVarbit(player, warning.varbit)
        val next = (current + 1).coerceAtMost(6)

        if (current < 6) {
            setVarbit(player, warning.varbit, next, true)

            log(this::class.java, Log.INFO, "${player.name}: Warning [$warning] increased to [$next]")
            player.debug("Warning [$warning] increased to [$next]")

            if (next == 6) {
                enableToggleButton(player, warning)
                sendMessage(player, "You can now toggle this warning in settings.")
            }
        } else if (current == 6) {
            enableToggleButton(player, warning)
        }
    }

    fun toggleWarning(player: Player, warning: WarningType) {
        val current = getVarbit(player, warning.varbit)

        val newValue = if (warning.binaryToggle) {
            if (current == 1) 0 else 1
        } else {
            if (current == 7) 6 else 7
        }
        setVarbit(player, warning.varbit, newValue, true)

        log(this::class.java, Log.INFO, "${player.name}: Warning [$warning] toggled to [$newValue]")
        player.debug("Warning [$warning] toggled to [$newValue]")
    }

    private fun sendToggleMessage(player: Player, warning: WarningType) {
        if (isDisabled(player, warning)) {
            sendMessage(player, "You have toggled this warning off. You will no longer see it.")
        } else {
            sendMessage(player, "You have enabled this warning. It will be shown again.")
        }
    }

    private fun enableToggleButton(player: Player, warning: WarningType) {
        val toggleButtonId = when (warning.component) {
            shared.consts.Components.WILDERNESS_WARNING_382 -> 26
            else -> 21
        }
        sendInterfaceConfig(player, warning.component, toggleButtonId, false)
    }
}