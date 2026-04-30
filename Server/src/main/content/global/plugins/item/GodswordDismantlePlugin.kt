package content.global.plugins.item

import core.api.sendMessage
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.item.Item
import shared.consts.Items

class GodswordDismantlePlugin : InteractionListener {

    private val godswordItem = intArrayOf(
        Items.ARMADYL_GODSWORD_11694,
        Items.BANDOS_GODSWORD_11696,
        Items.SARADOMIN_GODSWORD_11698,
        Items.ZAMORAK_GODSWORD_11700
    )
    private val bladeItem: Item = Item(Items.GODSWORD_BLADE_11690)

    override fun defineListeners() {

        /*
         * Handles dismantle option for godswords.
         */

        on(godswordItem, IntType.ITEM, "dismantle") { player, node ->
            val item = node.asItem()
            if (item.slot < 0 || player.inventory.getNew(item.slot).id != item.id) {
                return@on true
            }

            if (player.inventory.freeSlot() == -1) {
                sendMessage(player, "Not enough space in your inventory.")
                return@on true
            }

            sendMessage(player, "You detach the hilt from the blade.")
            player.inventory.replace(null, item.slot, false)
            player.inventory.add(Item(Items.ARMADYL_HILT_11702 + (item.id - Items.ARMADYL_GODSWORD_11694)), bladeItem)
            return@on true
        }
    }
}