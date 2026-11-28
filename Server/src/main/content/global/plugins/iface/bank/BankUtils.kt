package content.global.plugins.iface.bank

import core.api.sendInputDialogue
import core.game.dialogue.InputType
import core.game.node.entity.player.Player

object BankUtils {
    fun transferX(player: Player, slot: Int, withdraw: Boolean, after: (() -> Unit)? = null) {
        val container = if (withdraw) player.bank else player.inventory
        val item = container.get(slot) ?: return
        val itemId = item.id
        val session = player.session
        val ifaceId = if (player.bank.isOpen) 762 else if (!withdraw) 11 else return
        val widget = player.interfaceManager.getComponent(ifaceId) ?: return
        sendInputDialogue(player, InputType.AMOUNT, "Enter the amount:") { value ->
            val number = value.toString().toIntOrNull() ?: return@sendInputDialogue
            if (number <= 0 || player.session !== session ||
                player.interfaceManager.getComponent(ifaceId) !== widget || widget.isHidden ||
                container.get(slot) !== item || item.id != itemId ||
                (ifaceId == 762 && !player.bank.isOpen)) return@sendInputDialogue

            if (withdraw) {
                player.bank.takeItem(slot, number)
            } else {
                player.bank.addItem(slot, number)
            }

            player.bank.updateLastAmountX(number)
            after?.let { it() }
        }
    }
}