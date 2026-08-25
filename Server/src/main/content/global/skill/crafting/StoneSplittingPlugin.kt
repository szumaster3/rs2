package content.global.skill.crafting

import core.api.*
import core.game.interaction.Clocks
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.QueueStrength
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import core.tools.RandomUtils
import shared.consts.Animations
import shared.consts.Items
import shared.consts.Sounds

class StoneSplittingPlugin : InteractionListener {

    override fun defineListeners() {

        /*
         * Handles stone pillar crafting.
         */

        onUseWith(IntType.ITEM, Items.STONE_SLAB_13245, Items.CHISEL_1755) { player, used, _ ->
            if (!clockReady(player, Clocks.SKILLING)) return@onUseWith true
            if (getDynLevel(player, Skills.CRAFTING) < 20) return@onUseWith true
            if (!inInventory(player, Items.HAMMER_2347)) return@onUseWith true
            if (!inInventory(player, used.id)) return@onUseWith true

            runTask(player, 2) {
                delayClock(player, Clocks.SKILLING, 2)
                playAudio(player, Sounds.HAMMER_STONE_2100)
                animate(player, Animations.USE_HAMMER_CHISEL_11041)
                if (removeItem(player, used.id)) {
                    rewardXP(player, Skills.CRAFTING, 20.0)
                    addItem(player, Items.PILLAR_13246)
                    sendMessage(player, "You craft the stone into a pillar.")
                }
            }
            return@onUseWith true
        }

        /*
         * Handles granite cutting.
         */

        onUseWith(IntType.ITEM, Items.CHISEL_1755, *GRANITE_IDS) { player, _, with ->
            if (!clockReady(player, Clocks.SKILLING)) return@onUseWith true
            if (!inInventory(player, Items.CHISEL_1755)) return@onUseWith true

            setTitle(player, 2)
            sendOptions(player, "What would you like to do?", "Split the block into smaller pieces.", "Nothing.")

            addDialogueAction(player) { _, button ->
                if (button != 2) {
                    closeDialogue(player)
                    return@addDialogueAction
                }

                val graniteId = with.id
                val resultId = if (graniteId == Items.GRANITE_5KG_6983)
                    Items.GRANITE_2KG_6981 else Items.GRANITE_500G_6979

                sendSkillDialogue(player) {
                    withItems(resultId)
                    create { _, amount ->
                        runTask(
                            player = player,
                            amount = amount,
                            requiredItem = graniteId,
                            requiredSlots = 4,
                            animation = Animations.HUMAN_CHISEL_GRANITE_11146,
                            sound = Sounds.CHISEL_2586,
                            missingMessage = "You have run out of granite."
                        ) {
                            when (graniteId) {
                                Items.GRANITE_5KG_6983 -> {
                                    addItem(player, Items.GRANITE_2KG_6981, 2)
                                    addItem(player, Items.GRANITE_500G_6979, 2)
                                }

                                Items.GRANITE_2KG_6981 -> {
                                    addItem(player, Items.GRANITE_500G_6979, 4)
                                }
                            }
                        }
                    }
                    calculateMaxAmount { amountInInventory(player, graniteId) }
                }
            }
            return@onUseWith true
        }

        /*
         * Handles limestone cutting into bricks.
         */

        onUseWith(IntType.ITEM, Items.CHISEL_1755, Items.LIMESTONE_3211) { player, _, with ->
            if (!clockReady(player, Clocks.SKILLING)) return@onUseWith true
            if (getDynLevel(player, Skills.CRAFTING) < 12)  return@onUseWith true
            if (!inInventory(player, Items.CHISEL_1755)) return@onUseWith true

            sendSkillDialogue(player) {
                withItems(Items.LIMESTONE_BRICK_3420)
                create { _, amount ->
                    runTask(
                        player = player,
                        amount = amount,
                        requiredItem = with.id,
                        requiredSlots = 1,
                        animation = Animations.CHISEL_OYSTER_PEARL_4470,
                        sound = Sounds.SPLIT_ROCK_1708,
                        missingMessage = "You have run out of limestone."
                    ) {
                        val successProbability = BASE_SUCCESS_PROBABILITY +
                                getStatLevel(player, Skills.CRAFTING) * SUCCESS_PER_LEVEL

                        if (RandomUtils.randomDouble() <= successProbability) {
                            rewardXP(player, Skills.CRAFTING, 6.0)
                            addItem(player, Items.LIMESTONE_BRICK_3420)
                            sendMessage(player, "You successfully craft ${getItemName(Items.LIMESTONE_BRICK_3420)}.")
                        } else {
                            rewardXP(player, Skills.CRAFTING, 1.5)
                            addItem(player, Items.ROCK_968)
                            sendMessage(player, "You fail to craft ${getItemName(Items.LIMESTONE_BRICK_3420)}.")
                        }
                    }
                }
                calculateMaxAmount { amountInInventory(player, with.id) }
            }
            return@onUseWith true
        }
    }

    private fun runTask(player: Player, amount: Int, requiredItem: Int, requiredSlots: Int, animation: Int, sound: Int, missingMessage: String, onSuccess: () -> Unit) {
        var remaining = amount

        queueScript(player, 0, QueueStrength.WEAK) {
            if (!clockReady(player, Clocks.SKILLING)) return@queueScript stopExecuting(player)
            if (!inInventory(player, requiredItem)) {
                sendMessage(player, missingMessage)
                return@queueScript stopExecuting(player)
            }
            if (remaining <= 0) return@queueScript stopExecuting(player)

            if (freeSlots(player) < requiredSlots) {
                sendDialogue(player, "You'll need $requiredSlots empty inventory space${if (requiredSlots > 1) "s" else ""} to continue.")
                return@queueScript stopExecuting(player)
            }

            animate(player, animation)
            playAudio(player, sound)
            delayClock(player, Clocks.SKILLING, 2)

            if (removeItem(player, Item(requiredItem, 1))) {
                onSuccess()
                remaining--
            }

            if (remaining <= 0) {
                return@queueScript stopExecuting(player)
            }

            delayClock(player, Clocks.SKILLING, 2)
            setCurrentScriptState(player, 0)
            delayScript(player, 2)
        }
    }

    companion object {
        private const val MAXIMUM_SUCCESS_LEVEL = 40
        private const val BASE_SUCCESS_PROBABILITY = 0.75
        private const val MAXIMUM_SUCCESS_PROBABILITY = 1.0
        private const val SPREAD_SUCCESS = MAXIMUM_SUCCESS_PROBABILITY - BASE_SUCCESS_PROBABILITY
        private const val SUCCESS_PER_LEVEL = SPREAD_SUCCESS / MAXIMUM_SUCCESS_LEVEL

        private val GRANITE_IDS = intArrayOf(
            Items.GRANITE_2KG_6981,
            Items.GRANITE_5KG_6983
        )
    }
}