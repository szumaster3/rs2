package content.region.fremennik.jatizso.quest.fris.plugin

import core.api.*
import core.game.node.entity.player.Player
import core.game.node.entity.skill.SkillingTask
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import shared.consts.Animations
import shared.consts.Items

class LogCuttingTask(
    player: Player,
    private var amount: Int
) : SkillingTask<Item>(player, cycleTicks = 5) {

    private val log = Items.ARCTIC_PINE_LOGS_10810

    override fun canStart(): Boolean = true

    override fun canContinue(): Boolean {
        return amount > 0 && inInventory(player, log)
    }

    override fun playAnimation() {
        animate(player, Animations.TT2_SNAKE_WALK_RIGHT_TURN_5755)
    }

    override fun process(): Boolean {
        if (!removeItem(player, Item(log))) {
            sendMessage(player, "You have run out of Arctic pine logs.")
            return true
        }

        addItem(player, Items.SPLIT_LOG_10812)
        rewardXP(player, Skills.WOODCUTTING, 42.5)
        sendMessage(player, "You make a split log of Arctic pine.")

        amount--
        return amount <= 0
    }
}