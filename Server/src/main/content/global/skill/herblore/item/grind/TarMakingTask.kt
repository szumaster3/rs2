package content.global.skill.herblore.item.grind

import core.api.*
import core.game.node.entity.player.Player
import core.game.node.entity.skill.SkillingTask
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import shared.consts.Animations
import shared.consts.Items
import shared.consts.Quests
import shared.consts.Sounds

class TarMakingTask(
    player: Player,
    private val tar: TarItem,
    private var amount: Int
) : SkillingTask<Player>(player, cycleTicks = 4) {

    override fun canStart(): Boolean {
        if (!isQuestComplete(player, Quests.DRUIDIC_RITUAL)) {
            sendMessage(player, "You must complete the ${Quests.DRUIDIC_RITUAL} quest before you can use Herblore.")
            return false
        }

        if (getDynLevel(player, Skills.HERBLORE) < tar.level) {
            sendMessage(player, "You need a Herblore level of at least ${tar.level} in order to do this.")
            return false
        }

        if (!inInventory(player, Items.PESTLE_AND_MORTAR_233)) {
            sendMessage(player, "You need pestle and mortar in order to crush the herb.")
            return false
        }

        if (!inInventory(player, Items.SWAMP_TAR_1939, SWAMP_TAR_BATCH)) {
            sendMessage(player, "You need at least 15 swamp tar in order to do this.")
            return false
        }

        if (!hasSpaceFor(player, tar.product)) {
            sendMessage(player, "You don't have enough space in your inventory.")
            return false
        }

        return true
    }

    override fun playAnimation() {
        playAudio(player, Sounds.GRIND_2608)
        animate(player, Animations.HUMAN_USE_PESTLE_AND_MORTAR_364)
    }

    override fun canContinue(): Boolean {
        return amount > 0 &&
            inInventory(player, Items.SWAMP_TAR_1939, SWAMP_TAR_BATCH) &&
            inInventory(player, tar.ingredient)
    }

    override fun process(): Boolean {
        if (!inInventory(player, Items.SWAMP_TAR_1939, SWAMP_TAR_BATCH) ||
            !inInventory(player, tar.ingredient)
        ) {
            return true
        }

        removeItem(player, Item(Items.SWAMP_TAR_1939, SWAMP_TAR_BATCH))
        removeItem(player, Item(tar.ingredient))

        addItem(player, tar.product, SWAMP_TAR_BATCH)
        rewardXP(player, Skills.HERBLORE, tar.experience)

        val ingredientName = getItemName(tar.ingredient)
            .lowercase()
            .replace("clean", "")
            .trim()

        sendMessage(player, "You add the $ingredientName to the swamp tar.")

        amount--
        return amount <= 0
    }

    companion object {
        private const val SWAMP_TAR_BATCH = 15
    }
}
