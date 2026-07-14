package content.global.plugins.item

import content.data.GameAttributes
import core.api.*
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.player.Player
import core.game.node.item.Item
import shared.consts.Items


class ForinthryBraceletPlugin : InteractionListener
{

    override fun defineListeners()
    {

        /*
         * Handles checking charges of forinthry bracelet.
         */

        on(BRACELET_IDS, IntType.ITEM, "operate") { player, node ->
            sendMessage(player, "Your Forinthry bracelet has ${getCharges(node.asItem())} uses left.")
            return@on true
        }
    }

    companion object {
        private const val ABYSS_PROTECTION = "forinthry-abyss-protection"
        private val BRACELET_IDS = intArrayOf(
            Items.FORINTHRY_BRACE5_11095,
            Items.FORINTHRY_BRACE4_11097,
            Items.FORINTHRY_BRACE3_11099,
            Items.FORINTHRY_BRACE2_11101,
            Items.FORINTHRY_BRACE1_11103
        )

        /**
         * Called when entering abyss area.
         */
        fun handleAbyssEntry(player: Player): Boolean
        {
            val bracelet = findBracelet(player) ?: return false
            degrade(player,bracelet)
            setAttribute(player,ABYSS_PROTECTION,true)
            sendMessage(player,"Your Forinthry bracelet prevents you from being skulled.")
            return true
        }

        /**
         * Allows teleport while teleblocked by revenant npc.
         */
        @JvmStatic
        fun canIgnoreTeleblock(player: Player): Boolean
        {
            if (!getAttribute(player,GameAttributes.REVENANT_TELEBLOCK,false))
                return false

            val bracelet = findBracelet(player) ?: return false
            degrade(player,bracelet)
            sendMessage(player,"Your Forinthry bracelet protects you from the teleblock.")
            return true
        }

        /**
         * Removes protection after leaving abyss area.
         */
        fun clearAbyssProtection(player: Player)
        {
            removeAttribute(player,ABYSS_PROTECTION)
        }

        private fun findBracelet(player: Player): Item?
        {
            return player.equipment.toArray().firstOrNull{
                it != null && it.id in BRACELET_IDS
            }
        }

        private fun degrade(player: Player, item: Item)
        {
            when (item.id)
            {
                Items.FORINTHRY_BRACE5_11095 -> replaceSlot(player, item.slot, Item(Items.FORINTHRY_BRACE4_11097),item,Container.EQUIPMENT)
                Items.FORINTHRY_BRACE4_11097 -> replaceSlot(player, item.slot, Item(Items.FORINTHRY_BRACE3_11099),item,Container.EQUIPMENT)
                Items.FORINTHRY_BRACE3_11099 -> replaceSlot(player, item.slot, Item(Items.FORINTHRY_BRACE2_11101),item,Container.EQUIPMENT)
                Items.FORINTHRY_BRACE2_11101 -> replaceSlot(player, item.slot, Item(Items.FORINTHRY_BRACE1_11103),item,Container.EQUIPMENT)
                Items.FORINTHRY_BRACE1_11103 -> {removeItem(player, item, Container.EQUIPMENT);sendMessage(player,"Your Forinthry bracelet crumbles to dust.")}
            }
        }

        private fun getCharges(item: Item): Int
        {
            return when (item.id)
            {
                Items.FORINTHRY_BRACE5_11095 -> 5
                Items.FORINTHRY_BRACE4_11097 -> 4
                Items.FORINTHRY_BRACE3_11099 -> 3
                Items.FORINTHRY_BRACE2_11101 -> 2
                Items.FORINTHRY_BRACE1_11103 -> 1
                else -> 0
            }
        }
    }
}