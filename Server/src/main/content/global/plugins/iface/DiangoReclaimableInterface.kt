package content.global.plugins.iface

import content.global.skill.construction.decoration.costumeroom.Storable
import content.global.skill.construction.decoration.costumeroom.StorableType
import core.api.openInterface
import core.api.sendMessage
import core.api.setAttribute
import core.game.container.access.InterfaceContainer.generateItems
import core.game.interaction.InterfaceListener
import core.game.node.entity.player.Player
import core.game.node.item.Item
import shared.consts.Components

/**
 * Represents interface listener for the Diango Reclaimable.
 * @author Ceikry
 */
class DiangoReclaimableInterface : InterfaceListener {

    override fun defineInterfaceListeners() {

        ITEMS.addAll(getEligibleItemsList(StorableType.TOY))

        on(COMPONENT_ID) { player, _, opcode, _, slot, _ ->

            val reclaimable =
                player.getAttribute<Array<Item?>>("diango-reclaimables", null)
                    ?: getEligibleItems(player)

            val reclaimItem = reclaimable?.getOrNull(slot)

            if (reclaimItem == null) {
                sendMessage(player, "Something went wrong there. Please try again.")
                return@on true
            }

            when (opcode) {
                155 -> {
                    if (player.inventory.freeSlots() <= 0) {
                        sendMessage(player, "You don't have enough space in your inventory.")
                        return@on true
                    }

                    player.inventory.add(reclaimItem)
                    refresh(player)
                }

                9 -> {
                    sendMessage(player, reclaimItem.definition.examine)
                }
            }

            return@on false
        }
    }

    companion object {
        private const val COMPONENT_ID = Components.DIANGO_RECLAIMABLE_468
        private val ITEMS: MutableList<Item> = mutableListOf()

        /**
         * Returns a list of eligible items that can be reclaimed.
         *
         * @return List of eligible reclaimable items.
         */
        private fun getEligibleItemsList(type: StorableType): List<Item> {
            return Storable.values()
                .filter { it.type == type && it.takeIds.isNotEmpty() }
                .map { Item(it.takeIds.first()) }
        }

        /**
         * Opens the reclaim interface for the specified player.
         *
         * @param player The player opening the interface.
         */
        @JvmStatic
        fun open(player: Player) {
            val curOpen = player.interfaceManager.opened
            curOpen?.close(player)
            val reclaimable = getEligibleItems(player)
            setAttribute(player, "diango-reclaimables", reclaimable)
            if (reclaimable!!.isNotEmpty()) {
                player.generateItems(
                    reclaimable.toList(),
                    COMPONENT_ID,
                    2,
                    listOf("Take"),
                    8,
                    8
                )
            }
            openInterface(player, COMPONENT_ID)
        }

        /**
         * Refreshes the reclaim interface.
         *
         * @param player The player refreshing the interface.
         */
        private fun refresh(player: Player) {
            player.interfaceManager.close()
            open(player)
        }

        /**
         * Returns an array of items that the player is eligible to reclaim.
         *
         * @param player The player whose eligibility is checked.
         * @return Array of reclaimable items.
         */
        fun getEligibleItems(player: Player): Array<Item?>? = ITEMS.filter { item ->
            !player.equipment.containsItem(item) && !player.inventory.containsItem(item) && !player.bank.containsItem(item)
        }.toTypedArray()
    }
}