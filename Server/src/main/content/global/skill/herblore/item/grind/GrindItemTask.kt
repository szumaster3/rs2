package content.global.skill.herblore.item.grind

import core.api.*
import core.game.node.entity.player.Player
import core.game.node.entity.skill.SkillingTask
import core.game.node.item.Item
import shared.consts.Animations
import shared.consts.Items
import shared.consts.Sounds

class GrindItemTask(
    player: Player,
    private val grind: GrindableItem,
    private val sourceId: Int,
    private var amount: Int
) : SkillingTask<Player>(player, cycleTicks = 2) {

    override fun canStart(): Boolean = amount > 0

    override fun canContinue(): Boolean {
        return amount > 0 && inInventory(player, sourceId)
    }

    override fun playAnimation() {
        playAudio(player, Sounds.GRIND_2608)
        animate(player, Animations.HUMAN_USE_PESTLE_AND_MORTAR_364)
    }

    override fun process(): Boolean {
        if (!inInventory(player, sourceId)) {
            return true
        }

        val quantity = if (sourceId == FISHING_BAIT) {
            minOf(10, amount, amountInInventory(player, sourceId))
        } else {
            1
        }

        if (quantity <= 0 || !removeItem(player, Item(sourceId, quantity))) {
            return true
        }

        addItem(player, grind.product, quantity)
        sendMessage(player, grind.message)

        amount -= quantity
        return amount <= 0
    }

    companion object {
        private const val FISHING_BAIT = Items.FISHING_BAIT_313
    }
}
