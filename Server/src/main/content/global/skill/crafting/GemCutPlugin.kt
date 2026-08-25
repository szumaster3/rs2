package content.global.skill.crafting

import core.api.*
import core.game.interaction.Clocks
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.QueueStrength
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.tools.RandomFunction.random
import shared.consts.Animations
import shared.consts.Items
import shared.consts.Sounds

class GemCutPlugin : InteractionListener {

    override fun defineListeners() {

        /*
         * Handles cutting gems using chisel.
         */

        onUseWith(IntType.ITEM, Items.CHISEL_1755, *UNCUT_GEMS) { player, used, with ->
            if (!clockReady(player, Clocks.SKILLING)) return@onUseWith true

            val gemId = if (used.id == Items.CHISEL_1755) with.id else used.id
            val gem = CraftingDefinition.Gem.forId(gemId) ?: return@onUseWith true

            fun handleCut(amount: Int) {
                runTask(
                    player = player,
                    amount = amount,
                    requiredItem = gem.uncut,
                    animation = gem.animation,
                    sound = Sounds.CHISEL_2586,
                    missingMessage = "You have run out of gems.",
                    preCheck = {
                        if (getStatLevel(player, Skills.CRAFTING) < gem.level) {
                            sendDialogue(player, "You need a Crafting level of ${gem.level} to cut this gem.")
                            false
                        } else true
                    }
                ) {
                    val craftingLevel = getStatLevel(player, Skills.CRAFTING)
                    val crushed = when (gem.uncut) {
                        Items.UNCUT_OPAL_1625 ->
                            random(100) < getGemCrushChance(7.42, 0.0, craftingLevel)

                        Items.UNCUT_JADE_1627 ->
                            random(100) < getGemCrushChance(9.66, 0.0, craftingLevel)

                        Items.UNCUT_RED_TOPAZ_1629 ->
                            random(100) < getGemCrushChance(9.2, 0.0, craftingLevel)

                        else -> false
                    }

                    if (crushed) {
                        addItem(player, Items.CRUSHED_GEM_1633)
                        rewardXP(
                            player,
                            Skills.CRAFTING,
                            when (gem.uncut) {
                                Items.UNCUT_OPAL_1625 -> 3.8
                                Items.UNCUT_RED_TOPAZ_1629 -> 6.3
                                else -> 5.0
                            }
                        )
                        sendMessage(player, "You mis-hit the chisel and smash the gem to pieces!")
                    } else {
                        addItem(player, gem.cut)
                        rewardXP(player, Skills.CRAFTING, gem.xp)
                        sendMessage(player, "You cut the ${getItemName(gem.cut)}.")
                    }
                }
            }

            val amount = amountInInventory(player, gem.uncut)

            if (amount == 1) {
                handleCut(1)
                return@onUseWith true
            }

            sendSkillDialogue(player) {
                withItems(gem.cut)
                create { _, amountChosen -> handleCut(amountChosen) }
                calculateMaxAmount { amount }
            }

            return@onUseWith true
        }


        /*
         * Handles crushing semi-precious gems using a hammer.
         * Patch: 27 January 2009
         */

        onUseWith(IntType.ITEM, Items.HAMMER_2347, *SEMIPRECIOUS_GEMS) { player, used, with ->
            if (!clockReady(player, Clocks.SKILLING)) return@onUseWith true

            val gemId = if (used.id == Items.HAMMER_2347) with.id else used.id

            fun handleCrush(amount: Int) {
                runTask(
                    player = player,
                    amount = amount,
                    requiredItem = gemId,
                    animation = Animations.USE_HAMMER_CHISEL_11041,
                    sound = null,
                    missingMessage = "You have run out of gems."
                ) {
                    addItem(player, Items.CRUSHED_GEM_1633)
                    sendMessage(player, "You deliberately crush the gem with the hammer.")
                }
            }

            val amount = amountInInventory(player, gemId)

            if (amount == 1) {
                handleCrush(1)
                return@onUseWith true
            }

            sendSkillDialogue(player) {
                withItems(Items.CRUSHED_GEM_1633)
                create { _, amountChosen -> handleCrush(amountChosen) }
                calculateMaxAmount { amount }
            }

            return@onUseWith true
        }

    }

    private fun runTask(
        player: Player,
        amount: Int,
        requiredItem: Int,
        animation: Int,
        sound: Int?,
        missingMessage: String,
        preCheck: () -> Boolean = { true },
        onRemoved: () -> Unit
    ) {
        var remaining = amount

        queueScript(player, 0, QueueStrength.WEAK) {
            if (remaining <= 0) return@queueScript stopExecuting(player)

            if (!inInventory(player, requiredItem)) {
                sendMessage(player, missingMessage)
                return@queueScript stopExecuting(player)
            }

            if (!preCheck()) return@queueScript stopExecuting(player)

            animate(player, animation)
            sound?.let { playAudio(player, it) }

            if (removeItem(player, requiredItem)) {
                onRemoved()
                remaining--
            }

            if (remaining <= 0) return@queueScript stopExecuting(player)

            delayClock(player, Clocks.SKILLING, 1)
        }
    }

    companion object {
        /**
         * Represents the uncut gems.
         */
        private val UNCUT_GEMS = intArrayOf(
            CraftingDefinition.Gem.OPAL.uncut,
            CraftingDefinition.Gem.JADE.uncut,
            CraftingDefinition.Gem.RED_TOPAZ.uncut,
            CraftingDefinition.Gem.SAPPHIRE.uncut,
            CraftingDefinition.Gem.EMERALD.uncut,
            CraftingDefinition.Gem.RUBY.uncut,
            CraftingDefinition.Gem.DIAMOND.uncut,
            CraftingDefinition.Gem.DRAGONSTONE.uncut,
            CraftingDefinition.Gem.ONYX.uncut,
        )

        /**
         * Low-tier gems that can be crushed with a hammer instead of cut.
         */
        private val SEMIPRECIOUS_GEMS = intArrayOf(
            CraftingDefinition.Gem.OPAL.uncut,
            CraftingDefinition.Gem.JADE.uncut,
            CraftingDefinition.Gem.RED_TOPAZ.uncut
        )

        /**
         * Calculates the % chance of crushing a gem when cutting it.
         * @param low The base chance of crushing at level 1.
         * @param high The base chance of crushing at level 49.
         * @param level The player crafting level.
         * @return The chance `(0.0 to 100.0)` that the gem will be crushed.
         */
        private fun getGemCrushChance(low: Double, high: Double, level: Int): Double {
            if (level >= 50) return 0.0
            val clamped = level.coerceIn(1, 49)
            val chance = low * ((50 - clamped) / 49.0) + high * ((clamped - 1) / 49.0)
            return chance.coerceIn(0.0, 100.0)
        }
    }
}