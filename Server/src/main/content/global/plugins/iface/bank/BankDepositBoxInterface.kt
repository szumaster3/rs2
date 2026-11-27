package content.global.plugins.iface.bank

import core.api.*
import core.game.component.Component
import core.game.dialogue.InputType
import core.game.interaction.InterfaceListener
import core.game.interaction.InterfaceOperation
import core.game.node.entity.player.Player
import shared.consts.Animations
import shared.consts.Components

class BankDepositBoxInterface : InterfaceListener {
    companion object {
        private const val ITEM_LAYER = 17
        private const val BUTTON_DEPOSIT_BOB = 22
        private const val OP_AMOUNT_ONE = InterfaceOperation.OP1
        private const val OP_AMOUNT_FIVE = InterfaceOperation.OP2
        private const val OP_AMOUNT_TEN = InterfaceOperation.OP3
        private const val OP_AMOUNT_ALL = InterfaceOperation.OP4
        private const val OP_AMOUNT_X = InterfaceOperation.OP5
        private const val OP_EXAMINE = InterfaceOperation.OP10
    }

    private fun handleDepositBoxMenu(player: Player, component: Component, opcode: Int, buttonID: Int, slot: Int, itemID: Int): Boolean {
        if (buttonID != ITEM_LAYER) return true
        val item = player.inventory.get(slot) ?: return true

        if (item.id != itemID) return true

        if (opcode == OP_EXAMINE) {
            sendMessage(player, item.definition.examine)
            return true
        }

        runWorldTask {
            if (player.inventory.get(slot) !== item) return@runWorldTask
            when (opcode) {
                OP_AMOUNT_ONE -> player.bank.addItem(slot, 1)
                OP_AMOUNT_FIVE -> player.bank.addItem(slot, 5)
                OP_AMOUNT_TEN -> player.bank.addItem(slot, 10)
                OP_AMOUNT_X -> BankUtils.transferX(player, slot, false, player.bank::refreshDepositBoxInterface)
                OP_AMOUNT_ALL -> player.bank.addItem(slot, player.inventory.getAmount(item))
                else -> player.debug("Unknown deposit box menu opcode $opcode")
            }
            player.bank.refreshDepositBoxInterface()
            animate(player, Animations.HUMAN_BANK_DEPOSIT_BOX_834)
        }

        return true
    }

    override fun defineInterfaceListeners() {
        onNative(Components.BANK_DEPOSIT_BOX_11, ::handleDepositBoxMenu)
        onNative(Components.BANK_DEPOSIT_BOX_11, BUTTON_DEPOSIT_BOB) { player, _, opcode, _, _, _ ->
            if (opcode == InterfaceOperation.OP1) dumpBeastOfBurden(player)
            return@onNative true
        }
    }
}
