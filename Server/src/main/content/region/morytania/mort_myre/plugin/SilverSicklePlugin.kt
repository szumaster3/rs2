package content.region.morytania.mort_myre.plugin

import content.data.Quests

import content.region.morytania.mort_myre.quest.druidspirit.plugin.NSUtils.castBloom
import core.api.*
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import shared.consts.Animations
import shared.consts.Items

class SilverSicklePlugin : InteractionListener {

    private val sickleIDs = intArrayOf(Items.SILVER_SICKLE_B_2963, Items.ENCHANTED_SICKLE_EMERALD_B_13156, Items.SILVER_SICKLE_EMERALD_B_13155)

    override fun defineListeners() {
        on(sickleIDs, IntType.ITEM, "operate", "cast bloom") { player, node ->
            val questStage = getQuestStage(player, Quests.NATURE_SPIRIT.questName)
            if (questStage < 75) {
                sendDialogue(player, "You need to start the Nature Spirit to use this.")
                return@on true
            }

            if (!inBorders(player, getRegionBorders(13620)) || inBorders(player, getRegionBorders(13621))) {
                sendMessage(player, "You can only cast the spell in the Mort Myre Swamp.")
                return@on true
            }

            if (inEquipment(player, Items.ENCHANTED_SICKLE_EMERALD_B_13156)) {
                return@on true
            }

            if (node.name.contains("emerald", ignoreCase = true)) {
                animate(player, Animations.DRUIDICSPIRIT_HUMAN_BLOOM_EMERALD_SICKLE_9021)
            } else {
                animate(player, Animations.DRUIDICSPIRIT_HUMAN_BLOOM_1100)
            }

            castBloom(player)
            return@on true
        }

        onEquip(Items.ENCHANTED_SICKLE_EMERALD_B_13156) { _, _ ->
            // emerald upsets the balance of the sickle.
            return@onEquip false
        }

        onUseWith(IntType.ITEM, Items.EMERALD_1605, Items.SILVER_SICKLE_B_2963) { player, used, with ->
            val itemSlot = with.asItem().slot

            if (!inInventory(player, Items.CHISEL_1755)) {
                sendMessage(player, "You'll need a chisel to work these items into something useful.")
                return@onUseWith true
            }

            if (!removeItem(player, used.asItem())) {
                return@onUseWith true
            }

            replaceSlot(player, itemSlot, Item(Items.SILVER_SICKLE_EMERALD_B_13155))

            player.dialogueInterpreter.sendItemMessage(
                Items.SILVER_SICKLE_EMERALD_B_13155,
                "You carefully and skilfully construct an emerald-",
                "adorned blessed silver sickle .",
            )

            rewardXP(player, Skills.CRAFTING, 20.0)

            return@onUseWith true
        }

        /*
         * Handles make a Ivandis flail.
         */

        onUseWith(IntType.ITEM, Items.ENCHANTED_SICKLE_EMERALD_B_13156, Items.CHAIN_LINK_MOULD_13153) { player, used, with ->
            val itemSlot = with.asItem().slot

            if (!inInventory(player, Items.CHISEL_1755)) {
                sendMessage(player, "You'll need a chisel to work these items into something useful.")
                return@onUseWith true
            }

            if (!inInventory(player, Items.MITHRIL_BAR_2359)) {
                sendMessage(player, "You'll need a mithril bar to work these items into something useful.")
                return@onUseWith true
            }

            if (!inInventory(player, Items.CHAIN_LINK_MOULD_13153)) {
                sendMessage(player, "You'll need a chain link mould to work these items into something useful.")
                return@onUseWith true
            }

            if (!hasRequirement(player, Quests.LEGACY_OF_SEERGAZE.questName)) {
                return@onUseWith true
            }

            val rods = intArrayOf(
                Items.ROD_OF_IVANDIS_10_7639,
                Items.ROD_OF_IVANDIS_9_7640,
                Items.ROD_OF_IVANDIS_8_7641,
                Items.ROD_OF_IVANDIS_7_7642,
                Items.ROD_OF_IVANDIS_6_7643,
                Items.ROD_OF_IVANDIS_5_7644,
                Items.ROD_OF_IVANDIS_4_7645,
                Items.ROD_OF_IVANDIS_3_7646,
                Items.ROD_OF_IVANDIS_2_7647,
                Items.ROD_OF_IVANDIS_1_7648
            )

            if (!anyInInventory(player, *rods)) {
                sendMessage(player, "You'll need a rod to work these items into something useful.")
                return@onUseWith true
            }

            if (!removeItem(player, used.asItem())) return@onUseWith true

            removeItem(player, Items.MITHRIL_BAR_2359)
            removeItem(player, Items.CHAIN_LINK_MOULD_13153)

            // TODO: correct animation.

            animate(player, Animations.HUMAN_PICKUPFLOOR_827)
            replaceSlot(player, itemSlot, Item(Items.IVANDIS_FLAIL_30_13117))

            player.dialogueInterpreter.sendItemMessage(
                Items.IVANDIS_FLAIL_30_13117,
                "You construct a wonderfully attractive Ivandis Flail."
            )

            rewardXP(player, Skills.CRAFTING, 20.0)

            return@onUseWith true
        }
    }
}
