package content.global.skill.herblore.item.grind

import core.api.amountInInventory
import core.game.dialogue.SkillDialogueHandler
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.item.Item
import shared.consts.Items

class GrindItemPlugin : InteractionListener {

    override fun defineListeners() {
        onUseWith(IntType.ITEM, GrindableItem.itemIds, Items.PESTLE_AND_MORTAR_233) { player, used, _ ->
            val sourceId = used.id
            val grind = GrindableItem.forID(sourceId) ?: return@onUseWith true
            val max = amountInInventory(player, sourceId)

            if (max <= 1) {
                GrindItemTask(player, grind, sourceId, 1).start()
                return@onUseWith true
            }

            object : SkillDialogueHandler(
                player,
                SkillDialogue.ONE_OPTION,
                Item(grind.product)
            ) {

                override fun create(amount: Int, index: Int) {
                    val available = amountInInventory(player, sourceId)
                    if (available > 0) {
                        GrindItemTask(player, grind, sourceId, amount.coerceAtMost(available)).start()
                    }
                }

                override fun getAll(index: Int): Int = max
            }.open()

            return@onUseWith true
        }
    }
}
