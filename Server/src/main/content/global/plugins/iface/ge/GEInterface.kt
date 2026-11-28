package content.global.plugins.iface.ge

import core.api.*
import core.cache.def.impl.CS2Mapping
import core.game.component.Component
import core.game.ge.ExchangeHistory
import core.game.ge.GuidePrices
import core.game.ge.ItemSet
import core.game.interaction.InterfaceListener
import core.game.interaction.InterfaceOperation
import core.game.node.entity.player.Player
import core.game.node.item.Item
import shared.consts.Components
import shared.consts.Sounds

/**
 * Handles the Grand Exchange interface options.
 */
class GEInterface : InterfaceListener {

    companion object {
        /** enum 1080, script662: six offer layers, two actionable children (0,2) each. */
        internal fun collectionOfferIndex(button: Int): Int? =
            intArrayOf(18, 23, 28, 36, 44, 52).indexOf(button).takeIf { it >= 0 }
    }

    override fun defineInterfaceListeners() {

        /*
         * Search interface.
         */

        onNative(Components.EXCHANGE_SEARCH_389) { player, _, operation, button, _, _ ->
            if (operation == InterfaceOperation.OP1 && button == 10) closeChatBox(player)
            true
        }

        /*
         * Collection box
         */

        onNative(Components.STOCKCOLLECT_109) { player, _, operation, button, slot, _ ->
            if (slot != 0 && slot != 2) return@onNative true
            val index = collectionOfferIndex(button) ?: return@onNative true
            val records = ExchangeHistory.getInstance(player)
            val offer = records.getOffer(index) ?: return@onNative true
            StockMarket.withdrawNative(player, offer, slot / 2, operation)
            return@onNative true
        }

        onNative(Components.EXCHANGE_SETS_SIDE_644) { player, component, operation, button, slot, itemId ->
            handleItemSet(player, component, operation, button, slot, itemId)
            return@onNative true
        }

        onNative(Components.EXCHANGE_ITEMSETS_645) { player, component, operation, button, slot, itemId ->
            handleItemSet(player, component, operation, button, slot, itemId)
            return@onNative true
        }

        /*
         * Guide price.
         */

        onNative(GuidePrices.INTERFACE_ID) { player, _, operation, button, slot, itemId ->
            if (operation != InterfaceOperation.OP1 || button != 16 || slot < 0 || slot % 2 != 0) return@onNative true
            val type = getAttribute<GuidePrices.GuideType?>(player, "guide-price", null) ?: return@onNative true
            val id = type.nativeItems().getOrNull(slot / 2) ?: return@onNative true
            if (id != itemId) return@onNative true
            sendMessage(player, itemDefinition(id).examine)
            sendMessage(player, "Guide price: ${core.game.ge.GrandExchange.getRecommendedPrice(id, false)} gp.")
            return@onNative true
        }
        onClose(GuidePrices.INTERFACE_ID) { player, _ ->
            GuidePrices.clear(player)
            return@onClose true
        }
    }

    /**
     * Handles item sets.
     */
    private fun handleItemSet(
        player: Player,
        component: Component,
        opcode: Int,
        button: Int,
        slot: Int,
        itemId: Int,
    ) {
        val inventory = component.id == Components.EXCHANGE_SETS_SIDE_644
        if (button != (if (inventory) 0 else 16)) return
        val main = player.interfaceManager.getComponent(Components.EXCHANGE_ITEMSETS_645) ?: return
        if (main.isHidden) return
        val item: Item
        if (inventory) {
            if (slot !in 0 until player.inventory.capacity()) return
            item = player.inventory[slot] ?: return
            if (item.id != itemId) return
        } else {
            val id = ExchangeItemSets.setId(slot) ?: return
            if (id != itemId) return
            item = Item(id)
        }
        if (opcode == (if (inventory) InterfaceOperation.OP10 else InterfaceOperation.OP3)) {
            sendMessage(player, item.definition.examine)
            return
        }
        if (opcode != InterfaceOperation.OP1 && opcode != InterfaceOperation.OP2) return
        val set = ItemSet.forId(item.id) ?: run {
            sendMessage(player, "This isn't a supported set item.")
            return
        }

        when (opcode) {
            /*
             * Exchange set components.
             */

            InterfaceOperation.OP2 -> {
                if (inventory) {
                    if (freeSlots(player) < set.components.size - 1) {
                        sendMessage(
                            player,
                            "You don't have enough inventory space for the component parts."
                        )
                        return
                    }

                    if (!player.inventory.remove(item, false)) {
                        return
                    }

                    for (id in set.components) {
                        player.inventory.add(Item(id, 1))
                    }

                    refreshInventory(player)

                    sendMessage(player, "You successfully traded your set for its component items!")

                } else {
                    if (!player.inventory.containItems(*set.components)) {
                        sendMessage(player, "You don't have the parts that make up this set.")
                        return
                    }

                    for (id in set.components) {
                        player.inventory.remove(Item(id, 1), false)
                    }

                    player.inventory.add(item)

                    refreshInventory(player)

                    sendMessage(
                        player,
                        "You successfully traded your item components for a set!"
                    )
                }

                playAudio(player, Sounds.GE_TRADE_OK_4044)


            }

            /*
             * Item set description.
             */

            InterfaceOperation.OP1 -> {
                val mapping = CS2Mapping.forId(1089)
                if (mapping != null) {
                    val message = mapping.map?.get(set.itemId) as? String ?: ""
                    sendMessage(player, message)
                }
            }
        }
    }

}
