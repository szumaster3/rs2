package content.global.skill.smithing

import content.data.Quests

import content.global.skill.smithing.bar.BarItem
import content.global.skill.smithing.bar.BarType
import content.global.skill.smithing.bar.BarItemProduct
import core.api.*
import core.game.container.access.InterfaceContainer.generateItems
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import core.tools.StringUtils.formatDisplayName
import shared.consts.*

class SmithingPlugin : InteractionListener {

    companion object {

        private val ANVIL = intArrayOf(
            Scenery.ANVIL_2782,
            Scenery.ANVIL_2783,
            Scenery.ANVIL_4306,
            Scenery.ANVIL_6150,
            Scenery.ANVIL_22725,
            Scenery.LATHE_26817,
            Scenery.ANVIL_26822,
            Scenery.ANVIL_37622
        )

        private val BARS = intArrayOf(
            Items.BRONZE_BAR_2349,
            Items.IRON_BAR_2351,
            Items.STEEL_BAR_2353,
            Items.MITHRIL_BAR_2359,
            Items.ADAMANTITE_BAR_2361,
            Items.RUNITE_BAR_2363,
            Items.BLURITE_BAR_9467
        )

        private val DRAGON = intArrayOf(
            Items.SHIELD_LEFT_HALF_2366,
            Items.SHIELD_RIGHT_HALF_2368
        )

        private val DRACONIC = intArrayOf(
            Items.DRACONIC_VISAGE_11286,
            Items.ANTI_DRAGON_SHIELD_1540
        )

        private val GODSWORD = intArrayOf(
            Items.GODSWORD_SHARDS_11692,
            Items.GODSWORD_SHARD_1_11710,
            Items.GODSWORD_SHARDS_11688,
            Items.GODSWORD_SHARD_2_11712,
            Items.GODSWORD_SHARDS_11686,
            Items.GODSWORD_SHARD_3_11714
        )

        private val HILT = intArrayOf(
            Items.BANDOS_HILT_11704,
            Items.ARMADYL_HILT_11702,
            Items.ZAMORAK_HILT_11708,
            Items.SARADOMIN_HILT_11706
        )

        private val BLURITE_VALUES = intArrayOf(
            17, 25, 33, 41, 57, 65, 73,
            105, 113, 129, 137, 145, 153,
            177, 185, 193, 201, 217, 225, 233, 241
        )

        /**
         * Build smithing interface components.
         */
        fun buildComponents(player: Player, item: Item) {

            val type = BarType.getBarTypeForId(item.id)
                ?: return

            player.removeAttribute("smith-type")
            player.setAttribute("smith-type", type)

            val isBlurite = type.name == "BLURITE"

            if (isBlurite) {
                BLURITE_VALUES.forEach {
                    sendInterfaceConfig(player, Components.SMITHING_NEW_300, it, true)
                }
            } else {
                sendInterfaceConfig(player, Components.SMITHING_NEW_300, 267, false)
            }

            val bars = BarItemProduct.getBars(type)

            bars.forEachIndexed { index, bar ->

                val smithingType = bar?.smithingType ?: return@forEachIndexed

                when (smithingType) {
                    SmithingType.TYPE_GRAPPLE_TIP -> sendInterfaceConfig(player, Components.SMITHING_NEW_300, 169, false)
                    SmithingType.TYPE_DART_TIP -> sendInterfaceConfig(player, Components.SMITHING_NEW_300, 65, false)
                    SmithingType.TYPE_WIRE -> sendInterfaceConfig(player, Components.SMITHING_NEW_300, 81, false)
                    SmithingType.TYPE_SPIT_IRON -> sendInterfaceConfig(player, Components.SMITHING_NEW_300, 89, false)
                    SmithingType.TYPE_BULLSEYE -> sendInterfaceConfig(player, Components.SMITHING_NEW_300, 161, false)
                    SmithingType.TYPE_CLAWS -> sendInterfaceConfig(player, Components.SMITHING_NEW_300, 209, false)
                    SmithingType.TYPE_OIL_LANTERN -> sendInterfaceConfig(player, Components.SMITHING_NEW_300, 161, false)
                    SmithingType.TYPE_STUDS -> sendInterfaceConfig(player, Components.SMITHING_NEW_300, 97, false)

                    else -> {
                        if (isBlurite &&
                            (smithingType == SmithingType.TYPE_Crossbow_Bolt ||
                                    smithingType == SmithingType.TYPE_Crossbow_Limb)
                        ) {
                            sendInterfaceConfig(player, Components.SMITHING_NEW_300, 249, false)
                            sendInterfaceConfig(player, Components.SMITHING_NEW_300, 15, true)
                        }
                    }
                }

                val level = bar.level
                val hasLevel = player.getSkills().getLevel(Skills.SMITHING) >= level

                val color = if (hasLevel) "<col=FFFFFF>" else ""

                sendString(
                    player,
                    color + formatDisplayName(smithingType.name.replace("TYPE_", "")),
                    Components.SMITHING_NEW_300,
                    smithingType.displayName
                )

                val hasItems = player.inventory.contains(
                    bar.barType.barType,
                    smithingType.required
                )

                val reqColor = if (hasItems) "<col=2DE120>" else null

                if (reqColor != null) {
                    val suffix = if (smithingType.required > 1) "s" else ""
                    sendString(
                        player,
                        reqColor + smithingType.required + " Bar" + suffix,
                        Components.SMITHING_NEW_300,
                        smithingType.displayName + 1
                    )
                }

                player.generateItems(
                    listOf(Item(bar.product, smithingType.productAmount)),
                    Components.SMITHING_NEW_300,
                    smithingType.child - 1
                )
            }

            sendString(player, type.barName, Components.SMITHING_NEW_300, 14)
            openInterface(player, Components.SMITHING_NEW_300)
        }
    }

    override fun defineListeners() {

        /*
         * Handles creating dragon square shield.
         */

        onUseWith(IntType.SCENERY, DRAGON, *ANVIL) { player, _, _ ->
            if (getDynLevel(player, Skills.SMITHING) < 60) {
                sendDialogue(player, "You need to have a Smithing level of at least 60 to do this.")
                return@onUseWith false
            }
            if (!inInventory(player, Items.HAMMER_2347, 1)) {
                sendDialogue(player, "You need a hammer to work the metal with.")
                return@onUseWith false
            }

            sendPlainDialogue(
                player,
                false,
                "You set to work trying to fix the ancient shield. It's seen some",
                "heavy reward and needs some serious work doing to it.",
            )

            addDialogueAction(player) { _, _ ->
                closeDialogue(player)
                player.dialogueInterpreter.open(82127843, 1)
                return@addDialogueAction
            }

            return@onUseWith true
        }

        /*
         * Handles creating draconic visage shield.
         */

        onUseWith(IntType.SCENERY, DRACONIC, *ANVIL) { player, _, _ ->
            if (!inInventory(player, Items.ANTI_DRAGON_SHIELD_1540)) {
                sendDialogue(player, "You need to have an anti-dragon-shield to attach the visage onto.")
                return@onUseWith false
            }
            if (!inInventory(player, Items.DRACONIC_VISAGE_11286)) {
                sendDialogue(player, "You need to have a draconic visage so it can be attached on a shield.")
                return@onUseWith false
            }
            if (getDynLevel(player, Skills.SMITHING) < 90) {
                sendDialogue(player, "You need to have a Smithing level of at least 90 to do this.")
                return@onUseWith false
            }
            if (!inInventory(player, Items.HAMMER_2347, 1)) {
                sendDialogue(player, "You need a hammer to work the metal with.")
                return@onUseWith false
            }
            sendPlainDialogue(
                player,
                false,
                "You set to work trying to fix the ancient shield. It's seen some",
                "heavy reward and needs some serious work doing to it.",
            )

            addDialogueAction(player) { _, _ ->
                closeDialogue(player)
                player.dialogueInterpreter.open(82127843, 2)
                return@addDialogueAction
            }

            return@onUseWith true
        }

        /*
         * Handles fuse godsword shards at an anvil to get godsword blade.
         */

        onUseWith(IntType.SCENERY, GODSWORD, *ANVIL) { player, used, _ ->
            if (!inInventory(player, Items.HAMMER_2347, 1)) {
                sendDialogue(player, "You need a hammer to work the metal with.")
                return@onUseWith false
            }
            if (getDynLevel(player, Skills.SMITHING) < 80) {
                sendDialogue(player, "You need to have a Smithing level of at least 80 to do this.")
                return@onUseWith false
            }
            player.dialogueInterpreter.open(62362, used.id)
            return@onUseWith true
        }

        onUseWith(IntType.ITEM, HILT, *GODSWORD) { player, _, _ ->
            sendMessage(player, "The hilt of the godsword can only be connected to a completely reforged blade.")
            return@onUseWith true
        }

        onUseWith(IntType.ITEM, GODSWORD, *GODSWORD) { player, _, _ ->
            sendMessage(player, "Those pieces of the godsword can't be joined together like that - try forging them.")
            return@onUseWith true
        }

        /*
         * Handles using the bars on anvil.
         */

        onUseWith(IntType.SCENERY, BARS, *ANVIL) { player, used, with ->
            if (!inInventory(player, Items.HAMMER_2347, 1)) {
                sendDialogue(player, "You need a hammer to work the metal with.")
                return@onUseWith false
            }
            if (!isQuestComplete(player, Quests.DORICS_QUEST.questName) && with.asScenery().id == Scenery.ANVIL_2782) {
                sendDialogue(player, "Property of Doric the Dwarf.")
                return@onUseWith false
            }
            if (!isQuestComplete(player, Quests.THE_KNIGHTS_SWORD.questName) && used.id == Items.BLURITE_BAR_9467) {
                sendDialogue(player, "You need complete the Knights' Sword to work the metal with.")
                return@onUseWith false
            }

            val barItem = BarItem.forId(used.asItem().id)
            var item = used.asItem()

            if (used.asItem().id == Items.HAMMER_2347) {
                for (i in player.inventory.toArray()) {
                    if (i == null) {
                        continue
                    }
                    val barItem = BarItem.forId(i.id)
                    if (barItem != null) {
                        item = i
                        break
                    }
                }
            }
            if (barItem == null) {
                return@onUseWith false
            }
            if (getStatLevel(player, Skills.SMITHING) < barItem.level) {
                sendDialogue(player, "You need a Smithing level of at least " + barItem.level + " to work " + getItemName(barItem.product.id).lowercase() + "s.")
                return@onUseWith false
            }

            buildComponents(player, item!!)
            return@onUseWith true
        }
    }
}
