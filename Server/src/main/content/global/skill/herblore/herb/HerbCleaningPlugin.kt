package content.global.skill.herblore.herb

import content.data.Quests

import core.api.*
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.skill.Skills
import core.game.node.item.Item

class HerbCleaningPlugin : InteractionListener {

    override fun defineListeners() {
        on(IntType.ITEM, "clean") { player, node ->
            val item = node as? Item ?: return@on true

            if (!isQuestComplete(player, Quests.DRUIDIC_RITUAL.questName)) {
                sendMessage(player, "You must complete the ${Quests.DRUIDIC_RITUAL.questName} to use the Herblore skill.")
                return@on true
            }

            val herb: HerbItem = HerbItem.forItem(item) ?: run {
                sendMessage(player, "This cannot be cleaned.")
                return@on true
            }

            if (getDynLevel(player, Skills.HERBLORE) < herb.level) {
                sendMessage(
                    player,
                    "You cannot clean this herb. You need a Herblore level of ${herb.level} to attempt this."
                )
                return@on true
            }

            if (item.slot < 0) {
                return@on true
            }

            if (removeItem(player, item)) {
                addItem(player, herb.product.id, 1)
                rewardXP(player, Skills.HERBLORE, herb.experience)
                playAudio(player, 3921)

                val herbName = herb.product.name
                    .lowercase()
                    .replace("clean", "")
                    .trim()

                sendMessage(player, "You clean the dirt from the $herbName leaf.")
                delayEntity(player, 1)
            }

            return@on true
        }
    }
}