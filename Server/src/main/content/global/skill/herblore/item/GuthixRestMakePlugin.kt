
package content.global.skill.herblore.item

import core.api.*
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import shared.consts.Items
import shared.consts.Quests

class GuthixRestMakePlugin : InteractionListener {

    override fun defineListeners() {

        /*
         * Handles creating guthix rest potion.
         */

        onUseWith(IntType.ITEM, herbsArray, *teaMixes) { player, used, base ->
            handleMix(player, used.asItem(), base.asItem())
            return@onUseWith true
        }
    }

    private fun handleMix(player: Player, from: Item, to: Item): Boolean {
        if (!hasRequirement(player, Quests.DRUIDIC_RITUAL) || !hasRequirement(player, Quests.ONE_SMALL_FAVOUR)) {
            return false
        }

        if (getDynLevel(player, Skills.HERBLORE) < 18) {
            sendMessage(player, "You need a Herblore level of at least 18 to mix a Guthix Rest Tea.")
            return false
        }

        val herb: Item
        val mix: Item

        if (from.id in herbIds) {
            herb = from
            mix = to
        } else {
            herb = to
            mix = from
        }

        val existingIngredients = GuthixRest.byTeaId[mix.id]?.ingredients
            ?: emptyList()

        val newIngredients = (existingIngredients + herb.id).sorted()

        val upgradedTea = GuthixRest.byIngredients[newIngredients]
        if (upgradedTea == null) {
            sendMessage(player, "Nothing interesting happens.")
            return false
        }

        val mixSlot = mix.slot
        player.inventory.replace(Item(upgradedTea.product), mixSlot, true)

        val herbSlot = herb.slot
        player.inventory.remove(herb, herbSlot, true)

        sendMessage(
            player,
            "You place the ${herb.name.lowercase().replace(" leaf", "")} into the steamy mixture" +
                    if (upgradedTea == GuthixRest.COMPLETE_MIX) {
                        " and make Guthix Rest Tea."
                    } else {
                        "."
                    }
        )

        rewardXP(player, Skills.HERBLORE, 13.5 + newIngredients.size * 0.5)
        return true
    }

    companion object {
        private val herbIds = setOf(
            Items.CLEAN_GUAM_249,
            Items.CLEAN_MARRENTILL_251,
            Items.CLEAN_HARRALANDER_255
        )

        val herbsArray = herbIds.toIntArray()

        val teaMixes = GuthixRest.values()
            .map { it.product }
            .toIntArray() + Items.CUP_OF_HOT_WATER_4460
    }
}