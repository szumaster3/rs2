package content.region.fremennik.jatizso.quest.fris.plugin

import core.api.*
import core.game.node.entity.player.Player
import core.game.node.entity.skill.SkillingTask
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import shared.consts.Animations
import shared.consts.Items

class FremennikShieldTask(
    player: Player,
    private var amount: Int
) : SkillingTask<Item>(player, cycleTicks = 3) {

    override fun canStart(): Boolean {
        val hasHammer = inInventory(player, Items.HAMMER_2347)
        val hasNails = inInventory(player, Items.BRONZE_NAILS_4819)
        val hasRope = inInventory(player, Items.ROPE_954)
        val logCount = amountInInventory(player, Items.ARCTIC_PINE_LOGS_10810)

        if (!hasHammer) {
            sendMessage(player, "You need a hammer to force the nails in with.")
            return false
        }
        if (!hasNails) {
            sendMessage(player, "You need bronze nails for this.")
            return false
        }
        if (!hasRope) {
            sendMessage(player, "You will need a rope in order to do this.")
            return false
        }
        if (logCount < 2) {
            sendMessage(player, "You need at least 2 arctic pine logs to do this.")
            return false
        }
        return true
    }

    override fun canContinue(): Boolean {
        return amount > 0 &&
                amountInInventory(player, Items.ARCTIC_PINE_LOGS_10810) >= 2
    }

    override fun playAnimation() {
        animate(player, Animations.TT2_SNAKE_WALK_RIGHT_TURN_5755)
    }

    override fun process(): Boolean {

        if (!player.inventory.containsItems(
                Item(Items.ARCTIC_PINE_LOGS_10810, 2),
                Item(Items.ROPE_954, 1),
                Item(Items.BRONZE_NAILS_4819, 1)
            )
        ) {
            sendMessage(player, "You don't have the required items to make this.")
            return true
        }

        removeItem(player, Item(Items.ARCTIC_PINE_LOGS_10810, 2))
        removeItem(player, Items.ROPE_954)
        removeItem(player, Items.BRONZE_NAILS_4819)

        rewardXP(player, Skills.CRAFTING, 34.0)
        addItem(player, Items.FREMENNIK_ROUND_SHIELD_10826)
        sendMessage(player, "You make a Fremennik round shield.")

        amount--
        return amount <= 0
    }
}