package content.global.plugins.iface.bank

import content.global.dialogue.BankDepositDialogue
import content.global.dialogue.BankHelpDialogue
import core.ServerConstants
import core.api.*
import core.game.component.Component
import core.game.container.Container
import core.game.interaction.InterfaceListener
import core.game.node.entity.player.Player
import shared.consts.Components
import shared.consts.Items

/**
 * Handles the bank interface interactions.
 * @author vddCore
 * @refactor steven
 * @date 15.09.2025
 */
class BankInterface : InterfaceListener {

    companion object {
        private const val BANK_V2_MAIN_BUTTON_CLOSE = 10
        private const val BANK_V2_MAIN_BUTTON_INSERT_MODE = 15
        private const val BANK_V2_MAIN_BUTTON_NOTE_MODE = 19
        private const val BANK_V2_MAIN_BUTTON_BOB_DEPOSIT = 31
        private const val BANK_V2_MAIN_BUTTON_SEARCH_BANK = 17
        private const val BANK_V2_MAIN_BUTTON_HELP = 38
        private const val BANK_V2_MENU_ELEMENT = 87

        private const val THRESHOLD_EXACT_QUANTITY = 100_000

        private val BANK_TABS = intArrayOf(56, 54, 52, 50, 48, 46, 44, 42, 40)
    }

    /**
     * Opcodes for bank item interactions.
     */
    private enum class BankItemAction(val opcode: Int) {
        WITHDRAW_ONE(155),
        WITHDRAW_FIVE(196),
        WITHDRAW_TEN(124),
        WITHDRAW_LAST_X(199),
        WITHDRAW_X(234),
        WITHDRAW_ALL(168),
        WITHDRAW_ALL_BUT_ONE(166),
        EXAMINE(9);

        companion object {
            private val lookup = values().associateBy(BankItemAction::opcode)
            fun fromOpcode(opcode: Int): BankItemAction? = lookup[opcode]
        }
    }

    /**
     * Handles opening of the main bank interface.
     */
    private fun handleBankOpen(player: Player, component: Component): Boolean {
        val settings = IfaceSettingsBuilder()
            .enableOptions(0..6)
            .enableOption(9)
            .enableSlotSwitch()
            .setInterfaceEventsDepth(2)
            .build()

        player.packetDispatch.sendIfaceSettings(
            settings,
            BANK_V2_MENU_ELEMENT,
            Components.BANK_V2_MAIN_762,
            0,
            ServerConstants.BANK_SIZE - 1
        )

        resetSearch(player)
        return true
    }

    /**
     * Handles tab interactions in the bank.
     */
    private fun handleTabInteraction(player: Player, opcode: Int, buttonID: Int): Boolean {
        resetSearch(player)
        val clickedTabIndex = -((buttonID - BANK_TABS.first()) / 2)

        when (opcode) {
            155 -> if (player.bank.getTabIndex() != (if (clickedTabIndex == 0) 10 else clickedTabIndex)) player.bank.setTabIndex(clickedTabIndex)
            196 -> if (clickedTabIndex in 1..8) player.bank.collapseTab(clickedTabIndex)
        }

        return true
    }

    /**
     * Handles menu interactions (withdraw/deposit/examine).
     */
    private fun handleContainerMenu(
        player: Player,
        opcode: Int,
        slot: Int,
        from: Container,
        to: Container,
        isWithdraw: Boolean
    ): Boolean {
        val item = from.get(slot) ?: return true
        resetSearch(player)
        if (getVarbit(player, 4895) == 2 && getVarp(player, 281) < 1000 && getVarbit(player, 4931) < 30) {
            return false
        }
        when (BankItemAction.fromOpcode(opcode)) {
            BankItemAction.WITHDRAW_ONE -> transfer(player, slot, 1, isWithdraw)
            BankItemAction.WITHDRAW_FIVE -> transfer(player, slot, 5, isWithdraw)
            BankItemAction.WITHDRAW_TEN -> transfer(player, slot, 10, isWithdraw)
            BankItemAction.WITHDRAW_LAST_X -> transfer(player, slot, player.bank.lastAmountX, isWithdraw)
            BankItemAction.WITHDRAW_X -> BankUtils.transferX(player, slot, isWithdraw)
            BankItemAction.WITHDRAW_ALL -> transfer(player, slot, from.getAmount(item), isWithdraw)
            BankItemAction.WITHDRAW_ALL_BUT_ONE -> if (isWithdraw) transfer(player, slot, from.getAmount(item) - 1, true)
            BankItemAction.EXAMINE -> sendExamine(player, item.id, item.definition.name, item.definition.examine, from)
            null -> player.debug("Unknown container menu opcode $opcode")
        }

        return true
    }

    /**
     * Transfers an amount between bank and inventory.
     */
    private fun transfer(player: Player, slot: Int, amount: Int, isWithdraw: Boolean) {
        if (isWithdraw) player.bank.takeItem(slot, amount) else player.bank.addItem(slot, amount)
    }

    /**
     * Sends examine message for an item, shows coins if large stack.
     */
    private fun sendExamine(player: Player, id: Int, name: String, examine: String, container: Container) {
        val text = if (id == Items.COINS_995 && container.getAmount(id) >= THRESHOLD_EXACT_QUANTITY)
            "${container.getAmount(id)} x $name."
        else examine
        sendMessage(player, text)
    }

    /**
     * Resets search state and last tab.
     */
    private fun resetSearch(player: Player) {
        val lastTab = getAttribute(player, "bank:lasttab", 0)
        player.bank.setTabIndex(lastTab)
        setVarc(player, 190, 1)
    }

    override fun defineInterfaceListeners() {

        /*
         * Handles opening main bank interface
         */

        onOpen(Components.BANK_V2_MAIN_762, ::handleBankOpen)

        /*
         * Handles button interactions in main bank
         */

        on(Components.BANK_V2_MAIN_762) { player, _, opcode, buttonID, slot, itemID ->
            if (getVarbit(player, 4895) == 2 && getVarp(player, 281) < 1000 && getVarbit(player, 4931) < 30) {
                return@on false
            }
            when (buttonID) {
                BANK_V2_MAIN_BUTTON_HELP -> openDialogue(player, BankHelpDialogue())
                BANK_V2_MAIN_BUTTON_BOB_DEPOSIT -> openDialogue(player, BankDepositDialogue())
                BANK_V2_MAIN_BUTTON_INSERT_MODE -> player.bank.isInsertItems = !player.bank.isInsertItems
                BANK_V2_MAIN_BUTTON_NOTE_MODE -> player.bank.isNoteItems = !player.bank.isNoteItems
                BANK_V2_MAIN_BUTTON_SEARCH_BANK -> setAttribute(player, "search", true)
                BANK_V2_MENU_ELEMENT -> if (player.bank.get(slot)?.id == itemID) handleContainerMenu(player, opcode, slot, player.bank, player.inventory, true)
                in BANK_TABS -> handleTabInteraction(player, opcode, buttonID)
            }
            return@on true
        }

        /*
         * Handles bank help interface (close button)
         */

        on(Components.BANK_V2_HELP_767) { player, _, _, buttonID, _, _ ->
            if (buttonID == BANK_V2_MAIN_BUTTON_CLOSE) openBankAccount(player)
            return@on true
        }

        /*
         * Handles depositing items from side inventory panel
         */

        on(Components.BANK_V2_SIDE_763) { player, _, opcode, child, slot, itemID ->
            if (child == 0 && player.inventory.get(slot)?.id == itemID) handleContainerMenu(player, opcode, slot, player.inventory, player.bank, false)
            return@on true
        }
    }
}
