package content.global.skill.smithing

import content.data.Quests

import core.api.*
import core.game.node.entity.player.Player
import core.game.node.entity.skill.SkillingTask
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import shared.consts.Animations
import shared.consts.Items
import content.data.GameAttributes
import content.global.skill.smithing.bar.BarItemProduct
import core.game.event.ResourceProducedEvent
import core.game.node.entity.player.link.diary.DiaryType
import core.game.world.map.Location
import core.tools.StringUtils

class SmithingTask(
    player: Player,
    private val item: Item?,
    private val bar: BarItemProduct,
    private var amount: Int,
) : SkillingTask<Item>(player, item, cycleTicks = 4) {

    override fun canStart(): Boolean {
        if (!player.inventory.contains(bar.barType.barType, bar.smithingType.required * amount)) {
            amount = amountInInventory(player, bar.barType.barType)
        }

        player.interfaceManager.close()

        if (!inInventory(player, Items.HAMMER_2347, 1)) {
            sendDialogue(player, "You need a hammer to work the metal with.")
            return false
        }

        if (item?.id != Items.BRONZE_DAGGER_1205 &&
            !getAttribute(player, GameAttributes.TUTORIAL_COMPLETE, false)
        ) {
            sendDialogue(player, "You cannot make this on Tutorial Island.")
            return false
        }

        if (!isQuestComplete(player, Quests.THE_TOURIST_TRAP.questName) && bar.smithingType == SmithingType.TYPE_DART_TIP) {
            sendDialogue(player, "You need to complete Tourist Trap to smith dart tips.")
            return false
        }

        if (!isQuestComplete(player, Quests.DEATH_PLATEAU.questName) && bar.smithingType == SmithingType.TYPE_CLAWS) {
            sendDialogue(player, "You need to complete Death Plateau to smith claws.")
            return false
        }

        if (!isQuestComplete(player, Quests.THE_KNIGHTS_SWORD.questName) && bar.smithingType == SmithingType.TYPE_Crossbow_Bolt) {
            sendDialogue(player, "You need to complete Knights' Sword to smith bolts.")
            return false
        }

        if (!isQuestComplete(player, Quests.THE_KNIGHTS_SWORD.questName) && bar.smithingType == SmithingType.TYPE_Crossbow_Limb) {
            sendDialogue(player, "You need to complete Knights' Sword to smith limb.")
            return false
        }

        if (getDynLevel(player, Skills.SMITHING) < bar.level) {
            sendMessage(
                player,
                "You need a Smithing level of ${bar.level} to make a ${getItemName(bar.product)}."
            )
            return false
        }

        if (!anyInInventory(player, bar.barType.barType, bar.smithingType.required)) {
            sendMessage(
                player,
                "You don't have enough ${
                    getItemName(bar.barType.barType).lowercase()
                }s to make a ${
                    bar.smithingType.name
                        .replace("TYPE_", "")
                        .replace("_", " ")
                        .lowercase()
                }."
            )
            return false
        }

        return true
    }

    override fun playAnimation() {
        animate(player, Animations.SMITH_HAMMER_898)
    }

    override fun canContinue(): Boolean {
        return amount > 0 &&
                anyInInventory(player, bar.barType.barType, bar.smithingType.required)
    }

    override fun process(): Boolean {
        lock(player, 4)
        removeItem(player, Item(bar.barType.barType, bar.smithingType.required))
        val product = Item(node!!.id, bar.smithingType.productAmount)
        player.inventory.add(product)

        player.dispatch(ResourceProducedEvent(product.id, 1, player, bar.barType.barType))
        rewardXP(player, Skills.SMITHING, bar.barType.experience * bar.smithingType.required)

        val message = if (
            StringUtils.isPlusN(getItemName(bar.product).lowercase())
        ) "an" else "a"

        sendMessage(
            player,
            "You hammer the ${
                bar.barType.barName.lowercase().replace("smithing", "")
            }and make $message ${getItemName(bar.product).lowercase()}."
        )

        if (bar == BarItemProduct.BLURITE_CROSSBOW_LIMBS &&
            withinDistance(player, Location(3000, 3145, 0), 10)
        ) {
            finishDiaryTask(player, DiaryType.FALADOR, 1, 9)
            setVarbit(player, 5715, 1, true)
        }

        amount--
        return amount < 1
    }
}