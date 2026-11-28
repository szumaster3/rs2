package content.global.skill.herblore.potion

import content.region.kandarin.baxtorian.BarbarianTraining
import core.api.*
import core.game.dialogue.SkillDialogueHandler
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.Node
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import shared.consts.Animations
import shared.consts.Items

class PotionMakePlugin : InteractionListener {

    override fun defineListeners() {

        /*
         * Handles creating unfinished potions
         * by combining a herb with a base vial.
         */

        onUseWith(IntType.ITEM, UNF_INGREDIENTS, *POTION_BASE) { player, used, with ->
            val unf = UnfinishedPotion.forID(used.id, with.id) ?: return@onUseWith false
            val potion = GenericPotion.transform(unf)
            val base = potion.base
            val product = potion.product ?: return@onUseWith false

            val amount = amountInInventory(player, base)

            val handler = object : SkillDialogueHandler(player, SkillDialogue.ONE_OPTION, Item(product)) {
                override fun create(amount: Int, index: Int) {
                    player.pulseManager.run(PotionMakingPulse(player, base.asItem(), amount, potion))
                }

                override fun getAll(index: Int): Int = amountInInventory(player, base)
            }

            if (amount == 1) {
                handler.create(0, 1)
            } else {
                handler.open()
            }

            return@onUseWith true
        }

        /*
         * Handles creating finished potions
         * by combining an unfinished potion with its secondary ingredient.
         */

        onUseWith(IntType.ITEM, INGREDIENTS, *UNF_PRODUCT) { player, used, with ->
            val (unfId, ingId) = if (used.name.contains("(unf)")) used.id to with.id else with.id to used.id

            val finished = FinishedPotion.getPotion(unfId, ingId) ?: return@onUseWith false
            val potion = GenericPotion.transform(finished)
            val base = potion.base
            val product = potion.product ?: return@onUseWith false

            val amount = amountInInventory(player, base)

            val handler = object : SkillDialogueHandler(player, SkillDialogue.ONE_OPTION, Item(product)) {
                override fun create(amount: Int, index: Int) {
                    player.pulseManager.run(PotionMakingPulse(player, base.asItem(), amount, potion))
                }

                override fun getAll(index: Int): Int = amountInInventory(player, base)
            }

            if (amount == 1) {
                handler.create(0, 1)
            } else {
                handler.open()
            }

            return@onUseWith true
        }

        for (potion in BarbarianMixes.values()) {

            /*
             * Handle cases where both items are needed.
             */

            if (potion.both) {
                onUseWith(IntType.ITEM, potion.base, Items.ROE_11324) { player, used, with ->
                    handleBarbarianMixes(player, used, with)
                    return@onUseWith true
                }
            }

            /*
             * Handle cases where one of the items is caviar.
             */

            onUseWith(IntType.ITEM, potion.base, Items.CAVIAR_11326) { player, used, with ->
                handleBarbarianMixes(player, used, with)
                return@onUseWith true
            }
        }
    }

    /**
    * Handles the barbarian potion mixing process.
    */
    private fun handleBarbarianMixes(player: Player, inputPotion: Node, egg: Node): Boolean {
        val potion = BarbarianMixes.forId(inputPotion.id) ?: return false
        if (!getAttribute(player, BarbarianTraining.HERBLORE_START, false)) {
            sendDialogue(player, "You must begin the relevant section of Otto Godblessed's barbarian training.")
            return true
        }
        if (!hasLevelStat(player, Skills.HERBLORE, potion.level)) {
            sendMessage(player, "You need a Herblore level of ${potion.level} to make this mix.")
            return true
        }
        if (!removeItem(player, potion.base)) {
            return false
        }

        if (!removeItem(player, egg.id)) {
            addItem(player, potion.base)
            return false
        }

        animate(player, Animations.HUMAN_HERBING_GRIND_364)
        addItem(player, potion.product)
        rewardXP(player, Skills.HERBLORE, potion.xp)
        sendMessage(player, "You combine your potion with the ${getItemName(egg.id).lowercase()}.")

        if (!getAttribute(player, BarbarianTraining.HERBLORE_FULL, false)) {
            sendDialogueLines(player, "You feel you have learned more of barbarian ways. Otto might wish", "to talk to you more.")
            setAttribute(player, BarbarianTraining.HERBLORE_FULL, true)
        }
        return true
    }

    companion object {
        val UNF_PRODUCT = UnfinishedPotion.values().map { it.product }.distinct().toIntArray()
        val INGREDIENTS = FinishedPotion.values().map { it.ingredient }.distinct().toIntArray()
        val POTION_BASE = intArrayOf(Items.VIAL_OF_WATER_227, Items.COCONUT_MILK_5935)
        val UNF_INGREDIENTS = UnfinishedPotion.values().map { it.ingredient }.distinct().toIntArray()
    }
}