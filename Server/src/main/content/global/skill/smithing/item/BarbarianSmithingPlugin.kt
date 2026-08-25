package content.global.skill.smithing.item

import content.region.kandarin.baxtorian.BarbarianTraining
import core.api.*
import core.game.interaction.Clocks
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.QueueStrength
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import shared.consts.Animations
import shared.consts.Items
import shared.consts.Scenery
import kotlin.math.min

class BarbarianSmithingPlugin : InteractionListener {
    private val BAR_ITEM_IDS = BarbarianWeapon.values().map { it.requiredBar }.toIntArray()
    override fun defineListeners() {
        onUseWith(IntType.SCENERY, BAR_ITEM_IDS, Scenery.BARBARIAN_ANVIL_25349) { player, used, _ ->
            val weapon = BarbarianWeapon.product[used.id] ?: return@onUseWith true

            val canMakeSpear = player.savedData.activityData.barbarianSpearSmithing
            val canMakeHasta = player.savedData.activityData.barbarianHastaeSmithing

            if (!canMakeSpear && !canMakeHasta) {
                sendMessage(player, "You haven't learned to smith these weapons yet.")
                return@onUseWith true
            }

            sendSkillDialogue(player) {
                val items = mutableListOf<Item>()
                if (canMakeSpear) items.add(weapon.spearId.asItem())
                if (canMakeHasta) items.add(weapon.hastaId.asItem())

                withItems(*items.toTypedArray())

                create { button, amount ->
                    val selectedButton = button
                    val selectedAmount = amount
                    barbarianSmithingTask(player, weapon, selectedAmount, selectedButton)
                }

                calculateMaxAmount { amountInInventory(player, used.id) }
            }

            return@onUseWith true
        }
    }

    private fun barbarianSmithingTask(player: Player, weapon: BarbarianWeapon, amount: Int, button: Int) {
        val productId = if (button == 0) weapon.spearId else weapon.hastaId
        val attribute = if (button == 0) BarbarianTraining.SPEAR_FULL else BarbarianTraining.HASTA_FULL

        val barsItem = weapon.requiredBar
        val woodItem = weapon.requiredWood

        var remaining =
            min(amountInInventory(player, barsItem), amountInInventory(player, woodItem)).coerceAtMost(amount)
        if (remaining <= 0) return

        queueScript(player, 0, QueueStrength.WEAK) {
            if (remaining <= 0) return@queueScript stopExecuting(player)

            if (getStatLevel(player, Skills.SMITHING) < weapon.requiredLevel) {
                sendMessage(player, "You need a Smithing level of ${weapon.requiredLevel} to make this.")
                return@queueScript stopExecuting(player)
            }

            if (!inInventory(player, Items.HAMMER_2347)) {
                sendDialogue(player, "You need a hammer to work the metal with.")
                return@queueScript stopExecuting(player)
            }

            if (!inInventory(player, weapon.requiredBar)) {
                sendDialogue(player, "You don't have the necessary material for the weapon.")
                return@queueScript stopExecuting(player)
            }

            if (!inInventory(player, weapon.requiredWood)) {
                sendDialogue(player, "You don't have the necessary logs for the weapon.")
                return@queueScript stopExecuting(player)
            }

            animate(player, Animations.HAMMER_6712)

            if (removeItem(player, barsItem) && removeItem(player, woodItem)) {
                addItem(player, productId, 1)
                rewardXP(player, Skills.SMITHING, weapon.experience)
                sendMessage(player, "You make a ${getItemName(productId)}.")

                if (!getAttribute(player, attribute, false)) {
                    sendDialogueLines(
                        player,
                        "You feel you have learned more of barbarian ways. Otto might wish",
                        "to talk to you more."
                    )
                    setAttribute(player, attribute, true)
                }
                remaining--
            } else {
                return@queueScript stopExecuting(player)
            }
            return@queueScript delayClock(player, Clocks.SKILLING, 1, true)
        }
    }
}