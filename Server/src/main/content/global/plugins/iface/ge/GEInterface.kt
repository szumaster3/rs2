package content.global.plugins.iface.ge

import core.api.*
import core.cache.def.impl.CS2Mapping
import core.game.component.Component
import core.game.ge.ExchangeHistory
import core.game.ge.GuidePrices
import core.game.ge.ItemSet
import core.game.interaction.InterfaceListener
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.game.system.task.Pulse
import core.game.world.GameWorld
import core.net.packet.PacketRepository
import core.net.packet.context.ContainerContext
import core.net.packet.out.ContainerPacket
import shared.consts.Components
import shared.consts.Sounds

/**
 * Handles the Grand Exchange interface options.
 */
class GEInterface : InterfaceListener {

    override fun defineInterfaceListeners() {

        /*
         * Search interface.
         */

        on(Components.EXCHANGE_SEARCH_389) { player, _, _, button, _, _ ->
            when (button) {
                10 -> {
                    closeChatBox(player)
                    true
                }
                else -> true
            }
        }

        /*
         * Collection box
         */

        on(Components.STOCKCOLLECT_109) { player, _, opcode, button, slot, _ ->
            GameWorld.Pulser.submit(object : Pulse(1, player) {
                override fun pulse(): Boolean {
                    var index = -1

                    when (button) {
                        18, 23, 28 -> index = (button - 18) shr 2
                        36, 44, 52 -> index = 3 + ((button - 36) shr 3)
                    }

                    val records = ExchangeHistory.getInstance(player)
                    val offer = if (index > -1) {
                        records.getOffer(records.offerRecords[index])
                    } else {
                        null
                    }

                    if (offer != null) {
                        StockMarket.withdraw(player, offer, slot shr 1, opcode)
                    }
                    return true
                }
            })
            return@on true
        }

        /*
         * Item sets - inventory.
         */

        on(Components.EXCHANGE_SETS_SIDE_644) { player, component, opcode, button, slot, itemId ->
            GameWorld.Pulser.submit(object : Pulse(1, player) {
                override fun pulse(): Boolean {
                    handleItemSet(player, component, opcode, button, slot, itemId)
                    return true
                }
            })
            return@on true
        }

        /*
         * Item sets - selection list.
         */

        on(Components.EXCHANGE_ITEMSETS_645) { player, component, opcode, button, slot, itemId ->
            GameWorld.Pulser.submit(object : Pulse(1, player) {
                override fun pulse(): Boolean {
                    handleItemSet(player, component, opcode, button, slot, itemId)
                    return true
                }
            })
            return@on true
        }

        /*
         * Guide price.
         */

        on(Components.EXCHANGE_GUIDE_PRICE_642) { player, _, opcode, button, slot, itemId ->
            GameWorld.Pulser.submit(object : Pulse(1, player) {
                override fun pulse(): Boolean {
                    handleGuidePrice(player, opcode, button, slot, itemId)
                    return true
                }
            })
            return@on true
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
        if (button != 16 && button != 0) {
            return
        }

        val inventory = component.id == Components.EXCHANGE_SETS_SIDE_644
        if (slot < 0 || slot >= if (inventory) 28 else ItemSet.values().size) {
            return
        }

        val item: Item
        val set: ItemSet

        if (inventory) {
            val inventoryItem = player.inventory[slot] ?: return

            item = inventoryItem
            set = ItemSet.forId(item.id) ?: run {
                if (opcode == 127) return
                sendMessage(player, "This isn't a set item.")
                return
            }
        } else {
            set = ItemSet.values()[slot]
            item = Item(set.itemId)
        }

        when (opcode) {
            /*
             * Examine.
             */

            9 -> {
                sendMessage(player, item.definition.examine)
            }

            /*
             * Exchange set components.
             */

            196 -> {
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

                PacketRepository.send(
                    ContainerPacket::class.java,
                    ContainerContext(
                        player,
                        -1,
                        -2,
                        player.getAttribute("container-key", 93),
                        player.inventory,
                        false
                    )
                )
            }

            /*
             * Item set description.
             */

            155 -> {
                val mapping = CS2Mapping.forId(1089)
                if (mapping != null) {
                    val message = mapping.map?.get(set.itemId) as? String ?: ""
                    sendMessage(player, message)
                }
            }
        }
    }

    /**
     * Handles the guide price interface.
     */
    private fun handleGuidePrice(
        player: Player,
        opcode: Int,
        buttonId: Int,
        slot: Int,
        itemId: Int,
    ) {
        if (opcode != 155) {
            return
        }

        val type = player.getAttribute<GuidePrices.GuideType>(
            "guide-price",
            null
        ) ?: return

        val subtract = when (buttonId) {
            in 15..23   -> 15
            in 43..57   -> 43
            in 89..103  -> 89
            in 135..144 -> 135
            in 167..182 -> 167
            else -> return
        }

        val index = buttonId - subtract

        if (index < 0 || index >= type.items.size) {
            return
        }

        sendMessage(
            player,
            itemDefinition(type.items[index].item).examine
        )
    }
}