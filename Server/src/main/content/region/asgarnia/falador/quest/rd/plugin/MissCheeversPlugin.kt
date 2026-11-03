package content.region.asgarnia.falador.quest.rd.plugin

import content.region.asgarnia.falador.quest.rd.RDUtils
import core.api.*
import core.game.dialogue.DialogueFile
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.QueueStrength
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.game.world.update.flag.context.Graphics
import core.tools.START_DIALOGUE
import shared.consts.Items
import shared.consts.Scenery
import shared.consts.Sounds

class MissCheeversPlugin : InteractionListener {

    companion object {
        /**
         * Varbit used to track the state of the door.
         */
        const val doorVarbit = 686

        /**
         * Attributes representing various tools and items.
         */
        const val book = "rd:book"
        const val magnet = "rd:magnet"
        const val tin = "rd:tin"
        const val chisel = "rd:chisel"
        const val wire = "rd:wire"
        const val knife = "rd:knife"
        const val shears = "rd:shears"
        const val vials = "rd:3vialsofliquid"

        /**
         * Item ids for the tools that can be used by the player.
         */
        val toolIDs = intArrayOf(Items.BRONZE_WIRE_5602, Items.CHISEL_5601, Items.KNIFE_5605)

        /**
         * Item ids for potion ingredients or powders.
         */
        val potionIDs = intArrayOf(Items.TIN_ORE_POWDER_5583, Items.CUPRIC_ORE_POWDER_5584)

        /**
         * Enum representing all vials used in the Recruitment Drive quest.
         */
        enum class Vials(val itemId: Int, val attribute: String) {
            CUPRIC_SULPHATE_5577(Items.CUPRIC_SULPHATE_5577, "rd:cupricsulphate"),
            ACETIC_ACID_5578(Items.ACETIC_ACID_5578, "rd:aceticacid"),
            GYPSUM_5579(Items.GYPSUM_5579, "rd:gypsum"),
            SODIUM_CHLORIDE_5580(Items.SODIUM_CHLORIDE_5580, "rd:sodiumchloride"),
            NITROUS_OXIDE_5581(Items.NITROUS_OXIDE_5581, "rd:nitrousoxide"),
            VIAL_OF_LIQUID_5582(Items.VIAL_OF_LIQUID_5582, "rd:vialofliquid"),
            TIN_ORE_POWDER_5583(Items.TIN_ORE_POWDER_5583, "rd:tinorepowder"),
            CUPRIC_ORE_POWDER_5584(Items.CUPRIC_ORE_POWDER_5584, "rd:cupricorepowder");

            companion object {
                /**
                 * Map of vial item ids to their corresponding [Vials] enum instances.
                 */
                val vialMap = values().associateBy { it.itemId }
            }
        }

        /**
         * Enum representing vials required specifically for the door puzzle.
         */
        enum class DoorVials(val itemId: Int, val attribute: String) {
            CUPRIC_SULPHATE_5577(Items.CUPRIC_SULPHATE_5577, "rd:doorcupricsulphate"),
            ACETIC_ACID_5578(Items.ACETIC_ACID_5578, ""),
            SODIUM_CHLORIDE_5580(Items.SODIUM_CHLORIDE_5580, ""),
            VIAL_OF_LIQUID_5582(Items.VIAL_OF_LIQUID_5582, "rd:doorvialofliquid");

            companion object {
                /**
                 * Array of item ids corresponding to all [DoorVials].
                 */
                val doorVialsArray = values().map { it.itemId }.toIntArray()

                /**
                 * Map of door vial item ids to their corresponding DoorVials enum instances.
                 */
                val doorVialsMap = values().associateBy { it.itemId }

                /**
                 * Map of door vial item ids that are actually required for the door puzzle.
                 */
                val doorVialsRequiredMap = values()
                    .associateBy { it.itemId }
                    .filter { it.value.attribute.isNotEmpty() }
            }
        }
    }

    override fun defineListeners() {
        val searchActions =
            mapOf(
                Scenery.OLD_BOOKSHELF_7327 to { player: Player ->
                    RDUtils.searchingHelper(
                        player,
                        magnet,
                        Items.MAGNET_5604,
                        "You search the bookshelves...",
                        "Hidden amongst the books you find a magnet."
                    )
                },
                Scenery.OLD_BOOKSHELF_7328 to { player: Player ->
                            if (getAttribute(player, "/save:rd:help", -1) < 3) {
                                sendMessage(player, "You search the bookshelves...")
                                sendMessage(player, "You find nothing of interest.", 1)
                            } else {
                                RDUtils.searchingHelper(
                                    player,
                                    book,
                                    Items.ALCHEMICAL_NOTES_5588,
                                    "You search the bookshelves...",
                                    "You find a book that looks like it might be helpful."
                                )
                            }
                        },
                Scenery.OLD_BOOKSHELF_7329 to { player: Player ->
                    RDUtils.searchingHelper(
                        player,
                        knife,
                        Items.KNIFE_5605,
                        "You search the bookshelves...",
                        "Hidden amongst the books you find a knife."
                    )
                },
                Scenery.OLD_BOOKSHELF_7330 to { player: Player ->
                    RDUtils.searchingHelper(
                        player,
                        "",
                        0,
                        "You search the bookshelves...",
                        ""
                    )
                })

        searchActions.forEach { (scenery, action) ->
            on(scenery, IntType.SCENERY, "search") { player, _ ->
                action(player)
                return@on true
            }
        }

        val vialItems =
            listOf(
                Items.ACETIC_ACID_5578,
                Items.CUPRIC_SULPHATE_5577,
                Items.GYPSUM_5579,
                Items.SODIUM_CHLORIDE_5580,
                Items.NITROUS_OXIDE_5581,
                Items.TIN_ORE_POWDER_5583,
                Items.CUPRIC_ORE_POWDER_5584
            )
        val sceneryIDs = (Scenery.SHELVES_7333..Scenery.SHELVES_7339).toList()

        vialItems.forEachIndexed { index, item ->
            on(sceneryIDs[index], IntType.SCENERY, "search") { player, _ ->
                val vialList = mutableListOf<Int>()
                if (!getAttribute(player, Vials.vialMap[item]?.attribute ?: return@on false, false)) {
                    vialList.add(item)
                }
                openDialogue(player, VialShelfDialogueFile(vialList.toIntArray()))
                return@on true
            }
        }

        on(Scenery.SHELVES_7340, IntType.SCENERY, "search") { player, _ ->
            val vialCount = getAttribute(player, vials, 3)
            val vialList = List(vialCount) { Items.VIAL_OF_LIQUID_5582 }
            openDialogue(player, VialShelfDialogueFile(vialList.toIntArray(), vials))
            return@on true
        }

        val crateInteractions =
            mapOf(
                Scenery.CRATE_7347 to Pair(tin, Items.TIN_5600),
                Scenery.CRATE_7348 to Pair(chisel, Items.CHISEL_5601),
                Scenery.CRATE_7349 to Pair(wire, Items.BRONZE_WIRE_5602),
            )

        crateInteractions.forEach { (scenery, attributes) ->
            on(scenery, IntType.SCENERY, "search") { player, node ->
                val (attribute, item) = attributes
                if (node.location == RDUtils.getLocationForScenery(node.asScenery())) {
                    RDUtils.searchingHelper(
                        player,
                        attribute,
                        item,
                        "You search the crate...",
                        "Inside the crate you find a ${getItemName(item).lowercase()}."
                    )
                } else {
                    RDUtils.searchingHelper(player, "", 0, "You search the crate...", "")
                }
                return@on true
            }
        }

        on(Scenery.CLOSED_CHEST_7350, IntType.SCENERY, "open") { _, node ->
            replaceScenery(node.asScenery(), Scenery.OPEN_CHEST_7351, 100)
            return@on true
        }

        on(Scenery.OPEN_CHEST_7351, IntType.SCENERY, "search") { player, _ ->
            RDUtils.searchingHelper(
                player,
                shears,
                Items.SHEARS_5603,
                "You search the chest...",
                "Inside the chest you find some shears."
            )
            return@on true
        }

        on(Scenery.OPEN_CHEST_7351, IntType.SCENERY, "close") { _, node ->
            replaceScenery(node.asScenery(), Scenery.CLOSED_CHEST_7350, -1)
            return@on true
        }

        onUseWith(IntType.ITEM, Items.TIN_5600, Items.GYPSUM_5579) { player, used, with ->
            RDUtils.processItemUsageAndReturn(player, used.asItem(), with.asItem(), Item(Items.TIN_5592))
            return@onUseWith true
        }

        onUseWith(IntType.ITEM, Items.TIN_5592, Items.VIAL_OF_LIQUID_5582) { player, used, with ->
            RDUtils.processItemUsage(player, used.asItem(), with.asItem(), Item(Items.TIN_5593))
            sendMessage(player, "You notice the tin gets quite warm as you do this.")
            sendMessage(player, "A lumpy white mixture is made, that seems to be hardening.", 1)
            return@onUseWith true
        }

        onUseWith(IntType.SCENERY, Items.TIN_5593, Scenery.KEY_7346) { player, used, _ ->
            sendMessage(player, "You make an impression of the key as the white mixture hardens.")
            replaceSlot(player, slot = used.index, Item(Items.TIN_5594))
            return@onUseWith true
        }

        onUseWith(IntType.ITEM, Items.TIN_5594, *potionIDs) { player, used, with ->
            RDUtils.processItemUsageAndReturn(player, used.asItem(), with.asItem(), Item(Items.TIN_5595))
            return@onUseWith true
        }

        onUseWith(IntType.ITEM, Items.TIN_5595, *potionIDs) { player, used, with ->
            RDUtils.processItemUsageAndReturn(player, used.asItem(), with.asItem(), Item(Items.TIN_5596))
            return@onUseWith true
        }

        onUseWith(IntType.SCENERY, Items.TIN_5596, Scenery.BUNSEN_BURNER_7332) { player, used, _ ->
            if (removeItem(player, used.id)) {
                sendMessage(player, "You heat the two powdered ores together in the tin.")
                sendMessage(player, "You make a duplicate of the key in bronze.")
                playAudio(player, Sounds.BUNSEN_BURNER_2595)
                addItemOrDrop(player, Items.TIN_5597)
            }
            return@onUseWith true
        }

        onUseWith(IntType.ITEM, Items.TIN_5597, *toolIDs) { player, used, _ ->
            if (removeItem(player, used.id)) {
                sendMessage(player, "You prise the duplicate key out of the tin.")
                addItemOrDrop(player, Items.TIN_5600)
                addItemOrDrop(player, Items.BRONZE_KEY_5585)
            }
            return@onUseWith true
        }

        onUseWith(IntType.SCENERY, Items.METAL_SPADE_5586, Scenery.BUNSEN_BURNER_7332) { player, _, _ ->
            if (!inInventory(player, Items.METAL_SPADE_5586)) {
                return@onUseWith true
            }

            lock(player, 3)
            sendMessage(player, "You burn the wooden handle away from the spade...")

            queueScript(player, 1, QueueStrength.WEAK) { stage: Int ->
                if (!inInventory(player, Items.METAL_SPADE_5586)) {
                    return@queueScript stopExecuting(player)
                }

                when (stage) {
                    0 -> {
                        playAudio(player, Sounds.BUNSEN_BURNER_2595)
                        delayScript(player, 1)
                    }

                    1 -> {
                        visualize(player, -1, Graphics(157, 96))
                        playAudio(player, Sounds.FIREWAVE_HIT_163)
                        removeItem(player, Items.METAL_SPADE_5586)
                        delayScript(player, 1)
                    }

                    2 -> {
                        addItem(player, Items.METAL_SPADE_5587)
                        addItem(player, Items.ASHES_592)
                        sendMessage(player, "...and are left with a metal spade with no handle.")
                        stopExecuting(player)
                    }

                    else -> stopExecuting(player)
                }
            }

            return@onUseWith true
        }

        on(Scenery.STONE_DOOR_7343, SCENERY, "study") { player, _ ->
            sendDialogueLines(player, "There is a stone slab here obstructing the door.", "There is a small hole in the slab that looks like it might be for a handle.")
            return@on true
        }

        onUseWith(IntType.SCENERY, Items.METAL_SPADE_5587, Scenery.STONE_DOOR_7343) { player, used, _ ->
            if (removeItem(player, used.id)) {
                playAudio(player, Sounds.RECRUIT_SPADE_1742)
                sendMessage(player, "You slide the spade into the hole in the stone...")
                sendMessage(player, "It's nearly a perfect fit!", 1)
                setVarbit(player, doorVarbit, 1)
            }
            return@onUseWith true
        }

        onUseWith(IntType.SCENERY, DoorVials.doorVialsArray, Scenery.STONE_DOOR_7344) { player, used, _ ->
            RDUtils.handleVialUsage(player, used.asItem())
            return@onUseWith true
        }

        on(Scenery.STONE_DOOR_7344, SCENERY, "pull-spade") { player, _ ->
            RDUtils.handleSpadePull(player)
            return@on true
        }

        on(Scenery.OPEN_DOOR_7345, SCENERY, "walk-through") { player, _ ->
            RDUtils.handleDoorWalkThrough(player)
            return@on true
        }
    }

}


private class VialShelfDialogueFile(
    private val flaskIdsArray: IntArray,
    private val specialAttribute: String? = null
) : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            START_DIALOGUE -> {
                when (flaskIdsArray.size) {
                    3 -> {
                        dialogue("There are three vials on this shelf.")
                        stage = 1
                    }

                    2 -> {
                        dialogue("There are two vials on this shelf.")
                        stage = 1
                    }

                    1 -> {
                        dialogue("There is a vial on this shelf.")
                        stage = 1
                    }

                    0 -> {
                        dialogue("There is nothing of interest on these shelves.")
                        stage = 100
                    }

                    else -> endFile()
                }
            }

            1 -> {
                when (flaskIdsArray.size) {
                    3 -> options(
                        "Take one vial.",
                        "Take two vials.",
                        "Take all three vials.",
                        "Don't take a vial.",
                        title = "Take the vials?"
                    )

                    2 -> options(
                        "Take the first vial.",
                        "Take the second vial.",
                        "Take both vials.",
                        title = "Take the vials?"
                    )

                    1 -> options(
                        "YES",
                        "NO",
                        title = "Take the vial?"
                    )

                    else -> end()
                }

                stage = 2
            }

            2 -> {
                when (flaskIdsArray.size) {
                    3 -> {
                        when (buttonID) {
                            1 -> {
                                addItemOrDrop(player!!, flaskIdsArray[0])

                                if (specialAttribute != null) {
                                    setAttribute(
                                        player!!,
                                        specialAttribute,
                                        getAttribute(player!!, specialAttribute, 3) - 1
                                    )
                                    print(getAttribute(player!!, specialAttribute, 3))
                                }
                            }

                            2 -> {
                                addItemOrDrop(player!!, flaskIdsArray[0])
                                addItemOrDrop(player!!, flaskIdsArray[1])

                                if (specialAttribute != null) {
                                    setAttribute(
                                        player!!,
                                        specialAttribute,
                                        getAttribute(player!!, specialAttribute, 3) - 2
                                    )
                                }
                            }

                            3 -> {
                                addItemOrDrop(player!!, flaskIdsArray[0])
                                addItemOrDrop(player!!, flaskIdsArray[1])
                                addItemOrDrop(player!!, flaskIdsArray[2])

                                if (specialAttribute != null) {
                                    setAttribute(
                                        player!!,
                                        specialAttribute,
                                        getAttribute(player!!, specialAttribute, 3) - 3
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        when (buttonID) {
                            1 -> {
                                addItemOrDrop(player!!, flaskIdsArray[0])

                                if (specialAttribute != null) {
                                    setAttribute(
                                        player!!,
                                        specialAttribute,
                                        getAttribute(player!!, specialAttribute, 2) - 1
                                    )
                                } else {
                                    setAttribute(
                                        player!!,
                                        MissCheeversPlugin.Companion.Vials.vialMap[flaskIdsArray[0]]!!.attribute,
                                        true
                                    )
                                }
                            }

                            2 -> {
                                addItemOrDrop(player!!, flaskIdsArray[1])

                                if (specialAttribute != null) {
                                    setAttribute(
                                        player!!,
                                        specialAttribute,
                                        getAttribute(player!!, specialAttribute, 2) - 1
                                    )
                                } else {
                                    setAttribute(
                                        player!!,
                                        MissCheeversPlugin.Companion.Vials.vialMap[flaskIdsArray[1]]!!.attribute,
                                        true
                                    )
                                }
                            }

                            3 -> {
                                addItemOrDrop(player!!, flaskIdsArray[0])
                                addItemOrDrop(player!!, flaskIdsArray[1])

                                if (specialAttribute != null) {
                                    setAttribute(
                                        player!!,
                                        specialAttribute,
                                        getAttribute(player!!, specialAttribute, 2) - 2
                                    )
                                } else {
                                    setAttribute(
                                        player!!,
                                        MissCheeversPlugin.Companion.Vials.vialMap[flaskIdsArray[0]]!!.attribute,
                                        true
                                    )
                                    setAttribute(
                                        player!!,
                                        MissCheeversPlugin.Companion.Vials.vialMap[flaskIdsArray[1]]!!.attribute,
                                        true
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        if (buttonID == 1) {
                            addItemOrDrop(player!!, flaskIdsArray[0])

                            if (specialAttribute != null) {
                                setAttribute(
                                    player!!,
                                    specialAttribute,
                                    getAttribute(player!!, specialAttribute, 1) - 1
                                )
                            } else {
                                setAttribute(
                                    player!!,
                                    MissCheeversPlugin.Companion.Vials.vialMap[flaskIdsArray[0]]!!.attribute,
                                    true
                                )
                            }
                        }
                    }
                }

                end()
            }

            100 -> endFile()
        }
    }
}