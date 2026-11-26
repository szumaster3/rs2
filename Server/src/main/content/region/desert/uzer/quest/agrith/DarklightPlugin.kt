package content.region.desert.uzer.quest.agrith

import content.data.Quests

import core.api.addItem
import core.api.hasRequirement
import core.api.inInventory
import core.api.removeItem
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import shared.consts.Items

class DarklightPlugin : InteractionListener {

    override fun defineListeners() {

        /*
         * Handles use the black dye on silverlight.
         */

        onUseWith(IntType.ITEM, Items.BLACK_MUSHROOM_INK_4622, Items.SILVERLIGHT_2402) { player, used, with ->
            if (!hasRequirement(player, Quests.SHADOW_OF_THE_STORM.questName) || (!inInventory(player, Items.BLACK_MUSHROOM_INK_4622, 1) && (!inInventory(player, Items.SILVERLIGHT_2402, 1)))) {
                return@onUseWith false
            }
            if (removeItem(player, used.id) && removeItem(player, with.id)) {
                addItem(player, Items.DARKLIGHT_6746)
            }
            return@onUseWith true
        }
    }
}
