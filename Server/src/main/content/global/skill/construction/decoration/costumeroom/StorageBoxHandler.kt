package content.global.skill.construction.decoration.costumeroom

import core.api.*
import core.game.interaction.InteractionListener
import core.game.node.scenery.Scenery
import core.game.node.entity.player.Player
import shared.consts.Animations
import shared.consts.Sounds
import shared.consts.Scenery as Obj

class StorageBoxHandler : InteractionListener {

    private data class StorageBoxInfo(
        val objectIds: IntArray,
        val storableType: StorableType,
        val tier: Int = 0
    )

    private val treasureChestIds = intArrayOf(Obj.TREASURE_CHEST_18804,Obj.TREASURE_CHEST_18805,Obj.TREASURE_CHEST_18806,Obj.TREASURE_CHEST_18807,Obj.TREASURE_CHEST_18808,Obj.TREASURE_CHEST_18809)

    private val storageBoxes = listOf(
        // BOOKCASE
        StorageBoxInfo(intArrayOf(Obj.BOOKCASE_13597,Obj.BOOKCASE_13598,Obj.BOOKCASE_13599),StorableType.BOOK),
        // CAPE RACK
        StorageBoxInfo(intArrayOf(Obj.OAK_CAPE_RACK_18766,Obj.TEAK_CAPE_RACK_18767,Obj.MAHOGANY_CAPE_RACK_18768,Obj.GILDED_CAPE_RACK_18769,Obj.MARBLE_CAPE_RACK_18770,Obj.MAGIC_CAPE_RACK_18771),StorableType.CAPE),
        // FANCY BOX
        StorageBoxInfo(intArrayOf(Obj.FANCY_DRESS_BOX_18772,Obj.FANCY_DRESS_BOX_18773,Obj.FANCY_DRESS_BOX_18774,Obj.FANCY_DRESS_BOX_18775,Obj.FANCY_DRESS_BOX_18776,Obj.FANCY_DRESS_BOX_18777),StorableType.FANCY),
        // TOY BOX
        StorageBoxInfo(intArrayOf(Obj.TOY_BOX_18798,Obj.TOY_BOX_18799,Obj.TOY_BOX_18800,Obj.TOY_BOX_18801,Obj.TOY_BOX_18802,Obj.TOY_BOX_18803),StorableType.TOY),
        // MAGIC WARDROBE
        StorageBoxInfo(intArrayOf(Obj.MAGIC_WARDROBE_18784,Obj.MAGIC_WARDROBE_18785,Obj.MAGIC_WARDROBE_18786,Obj.MAGIC_WARDROBE_18787,Obj.MAGIC_WARDROBE_18788,Obj.MAGIC_WARDROBE_18789,Obj.MAGIC_WARDROBE_18790,Obj.MAGIC_WARDROBE_18791,Obj.MAGIC_WARDROBE_18792,Obj.MAGIC_WARDROBE_18793,Obj.MAGIC_WARDROBE_18794,Obj.MAGIC_WARDROBE_18795,Obj.MAGIC_WARDROBE_18796,Obj.MAGIC_WARDROBE_18797),StorableType.ARMOUR),
        // ARMOUR CASE
        StorageBoxInfo(intArrayOf(Obj.ARMOUR_CASE_18778,Obj.ARMOUR_CASE_18779,Obj.ARMOUR_CASE_18780,Obj.ARMOUR_CASE_18781,Obj.ARMOUR_CASE_18782,Obj.ARMOUR_CASE_18783),StorableType.ARMOUR_CASE)
    )

    /**
     * Represents every storage box object id.
     * except treasure chests (those use a level dialogue).
     */
    private val storageIds: IntArray = storageBoxes.flatMap { it.objectIds.toList() }.toIntArray()

    override fun defineListeners() {
        on(storageIds, SCENERY, "search") { player, node ->
            val box = storageBoxes.firstOrNull { node.id in it.objectIds } ?: return@on true
            val container = player.getCostumeRoomState().getContainer(box.storableType)
            container.setTier(box.storableType, box.tier)
            StorageBoxInterface.openStorage(player, box.storableType)
            return@on true
        }

        on(treasureChestIds, SCENERY, "search") { player, node ->
            handleTreasureChest(player, node.id)
            return@on true
        }

        val allIds = storageIds + treasureChestIds

        on(allIds, SCENERY, "open") { player, node ->
            openBox(player, node.asScenery())
            return@on true
        }

        on(allIds, SCENERY, "close") { player, node ->
            closeBox(player, node.asScenery())
            return@on true
        }
    }

    private fun handleTreasureChest(player: Player, objId: Int) {
        // Chests 18804-18806 offer 2 levels, 18807-18809 offer 3 levels.
        val levels = if (objId <= Obj.TREASURE_CHEST_18806) 2 else 3
        val options = Array(levels) { "Level ${it + 1}" }

        setTitle(player, levels)
        sendOptions(player, "Take which level of Treasure Trail reward?", *options)
        addDialogueAction(player) { p, button ->
            // Dialogue buttons start at 2.
            val tier = button - 2
            if (tier in 0 until levels) {
                val container = p.getCostumeRoomState().getContainer(StorableType.TRAILS)
                container.setTier(StorableType.TRAILS, tier)
                StorageBoxInterface.openStorage(p, StorableType.TRAILS)
            }
        }
    }

    private fun openBox(player: Player, obj: Scenery) {
        playAudio(player, Sounds.CHEST_OPEN_52)
        animate(player, Animations.HUMAN_OPEN_CHEST_536)
        replaceScenery(obj, obj.id + 1, -1)
    }

    private fun closeBox(player: Player, obj: Scenery) {
        playAudio(player, Sounds.CHEST_CLOSE_51)
        animate(player, Animations.HUMAN_CLOSE_CHEST_538)
        replaceScenery(obj, obj.id - 1, -1)
    }
}