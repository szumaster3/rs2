package content.global.skill.herblore.item.grind

import core.api.*
import core.game.dialogue.SkillDialogueHandler
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.item.Item
import shared.consts.Items

class TarMakePlugin : InteractionListener {

    override fun defineListeners() {
        onUseWith(IntType.ITEM, TarItem.ingredientIds, Items.SWAMP_TAR_1939) { player, used, _ ->
            val tar = TarItem.forId(used.id) ?: return@onUseWith true
            val max = maxCraftableAmount(player, used.id)

            if (max <= 1) {
                TarMakingTask(player, tar, 1).start()
                return@onUseWith true
            }

            object : SkillDialogueHandler(
                player,
                SkillDialogue.MAKE_SET_ONE_OPTION,
                Item(tar.product)
            ) {

                override fun create(amount: Int, index: Int) {
                    val available = maxCraftableAmount(player, used.id)
                    if (available > 0) {
                        TarMakingTask(player, tar, amount.coerceAtMost(available)).start()
                    } else {
                        TarMakingTask(player, tar, 1).start()
                    }
                }

                override fun getAll(index: Int): Int = max
            }.open()

            return@onUseWith true
        }
    }

    private fun maxCraftableAmount(player: core.game.node.entity.player.Player, ingredientId: Int): Int {
        val ingredientCount = amountInInventory(player, ingredientId)
        val tarCount = amountInInventory(player, Items.SWAMP_TAR_1939)
        return minOf(ingredientCount, tarCount / SWAMP_TAR_BATCH)
    }

    companion object {
        private const val SWAMP_TAR_BATCH = 15
    }
}
