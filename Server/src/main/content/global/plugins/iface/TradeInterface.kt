package content.global.plugins.iface

import core.api.sendInputDialogue
import core.api.sendMessage
import core.game.container.Container
import core.game.interaction.InterfaceListener
import core.game.interaction.InterfaceOperation
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.request.trade.TradeModule
import core.game.node.entity.player.link.request.trade.TradeModule.Companion.getExtension
import shared.consts.Components

/** Revision 578 trade widgets and their native operation indices. */
class TradeInterface : InterfaceListener {
    override fun defineInterfaceListeners() {
        onNative(Components.TRADECONFIRM_334) { player, _, operation, button, _, _ ->
            val module = getExtension(player) ?: return@onNative true
            if (operation == InterfaceOperation.OP1 && module.isActive(1)) {
                when (button) {
                    20 -> module.setAccepted(true, true)
                    21 -> module.decline()
                }
            }
            true
        }

        onNative(Components.TRADEMAIN_335) { player, _, operation, button, slot, itemID ->
            val module = getExtension(player) ?: return@onNative true
            if (!module.isActive(0)) return@onNative true
            when (button) {
                16, 18 -> if (operation == InterfaceOperation.OP1) {
                    if (button == 16) module.setAccepted(true, true) else module.decline()
                }
                TradeModule.OWN_OFFER_CHILD -> {
                    val container = module.container ?: return@onNative true
                    handleItem(player, module, container, operation, slot, itemID, false)
                }
                TradeModule.OTHER_OFFER_CHILD -> if (operation == InterfaceOperation.OP10) {
                    val item = getExtension(module.target)?.container?.get(slot)
                    if (item != null && item.id == itemID) sendMessage(player, item.definition.examine)
                }
            }
            true
        }

        onNative(Components.TRADESIDE_336) { player, component, operation, button, slot, itemID ->
            val duel = player.getExtension<content.minigame.duel_arena.plugin.DuelSession>(content.minigame.duel_arena.plugin.DuelSession::class.java)
            if (duel != null && duel.staked && player.interfaceManager.getComponent(631) != null)
                return@onNative duel.handleStakeSide(player, component, operation, button, slot, itemID)
            val module = getExtension(player) ?: return@onNative true
            if (button == 0 && module.isActive(0))
                handleItem(player, module, player.inventory, operation, slot, itemID, true)
            true
        }
    }

    private fun handleItem(player: Player, module: TradeModule, source: Container, operation: Int,
                           slot: Int, itemID: Int, offer: Boolean) {
        val item = source.get(slot) ?: return
        if (item.id != itemID) return
        val trade = module.container ?: return
        fun transfer(amount: Int) {
            // The prompt can outlive a trade or its original slot. Never act on a replacement item.
            if (amount <= 0 || !module.isActive(0) || source.get(slot) !== item || item.id != itemID) return
            if (offer) trade.offer(slot, amount) else trade.withdraw(slot, amount)
        }
        when (operation) {
            InterfaceOperation.OP1 -> transfer(1)
            InterfaceOperation.OP2 -> transfer(5)
            InterfaceOperation.OP3 -> transfer(10)
            InterfaceOperation.OP4 -> transfer(source.getAmount(item))
            InterfaceOperation.OP5 -> sendInputDialogue(player, false, "Enter the amount:") { value ->
                val amount = value.toString().replace("k", "000", ignoreCase = true)
                    .replace("m", "000000", ignoreCase = true).toIntOrNull()
                if (amount != null) transfer(amount)
            }
            InterfaceOperation.OP10 -> sendMessage(player, item.definition.examine)
        }
    }
}
