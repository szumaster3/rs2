package content.region.misthalin.varrock.quest.dragon.plugin

import content.data.Quests

import content.region.misthalin.varrock.quest.dragon.DragonSlayer
import core.api.*
import core.game.global.action.ClimbActionHandler
import core.game.global.action.DoorActionHandler
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.player.link.diary.DiaryType
import core.game.node.item.GroundItemManager
import core.game.world.map.Location
import core.game.world.map.RegionManager
import core.game.world.update.flag.context.Animation
import shared.consts.*

class DragonSlayerListener : InteractionListener {

    override fun defineListeners() {
        /*
         * Handles interaction with the locked cell door.
         */

        on(Scenery.CELL_DOOR_40184, IntType.SCENERY, "open") { player, _ ->
            sendMessage(player, "It's locked tight.")
            return@on true
        }

        /*
         * Handles interaction with Wormbrain.
         * Adjusts the player's location based on their proximity to the NPC.
         */

        setDest(IntType.NPC, intArrayOf(NPCs.WORMBRAIN_745), "talk-to") { player, node ->
            val npcLocation = node.asNpc().location
            val playerLocation = player.asPlayer().location
            val isWithinDistance = playerLocation.withinMaxnormDistance(npcLocation, 1)
            val targetLocation = if (isWithinDistance) playerLocation else npcLocation
            return@setDest Location.create(targetLocation.x, targetLocation.y, 0)
        }

        /*
         * Handles diary task.
         */

        on(Scenery.CLIMBING_ROPE_25213, IntType.SCENERY, "climb") { player, _ ->
            ClimbActionHandler.climb(player, Animation(Animations.HUMAN_REACHFORLADDER_828), Location(2834, 3258, 0))
            finishDiaryTask(player, DiaryType.KARAMJA, 1, 2)
            setVarbit(player, 3580,1, true)
            return@on true
        }

        /*
         * Handles entrance from crandor to elvarg dungeon.
         */

        on(Scenery.HOLE_25154, IntType.SCENERY, "enter") { player, _ ->
            ClimbActionHandler.climb(player, null, Location(2833, 9658, 0))
            return@on true
        }

        /*
         * Handles the quest NPC interaction (Dragon slayer).
         */

        on(NPCs.ELVARG_742, IntType.NPC, "attack") { player, node ->
            when {
                getQuestStage(player, Quests.DRAGON_SLAYER.questName) == 40 && inInventory(player, DragonSlayer.ELVARG_HEAD.id) -> {
                    sendMessage(player, "You have already slain the dragon. Now you just need to return to Oziach for your reward!")
                }
                getQuestStage(player, Quests.DRAGON_SLAYER.questName) > 40 -> {
                    sendMessage(player, "You have already slain Elvarg the dragon.")
                }
                else -> {
                    player.attack(node)
                    face(player, node, 3)
                }
            }
            return@on true
        }

        /*
         * Handles climbing over a wall to elvarg NPC.
         */

        on(Scenery.WALL_25161, IntType.SCENERY, "climb-over") { player, _ ->
            if (player.location.x >= 2847) {
                if (getQuestStage(player, Quests.DRAGON_SLAYER.questName) == 40 && !inInventory(player, DragonSlayer.ELVARG_HEAD.id)) {
                    if (player.location.x <= 2845) {
                        val npcs = RegionManager.getLocalNpcs(player)
                        for (n in npcs) {
                            if (n.id == NPCs.ELVARG_742) {
                                n.properties.combatPulse.attack(player)
                                return@on true
                            }
                        }
                    }
                }
                forceMove(player, player.location, player.location.transform(if (player.location.x == 2845) 2 else -2, 0, 0), 30, 90, null, Animations.MDAUGHTER_CLIMB_OVER_ROCKS_10573)
                return@on true
            }
            if (inInventory(player, DragonSlayer.ELVARG_HEAD.id)) {
                sendMessage(player, "You have already slain the dragon. Now you just need to return to Oziach for your reward!")
                return@on true
            }
            if (isQuestComplete(player, Quests.DRAGON_SLAYER.questName)) {
                sendMessage(player, "You have already slain Elvarg the dragon.")
                return@on true
            }
            return@on true
        }

        /*
         * Handles creating a crandor map from pieces.
         */

        onUseWith(IntType.ITEM, mapPieces, *mapPieces) { player, _, _ ->
            if (!allInInventory(player, *mapPieces)) {
                sendMessage(player, "You don't have all the map pieces yet.")
                return@onUseWith false
            }
            if (!player.inventory.removeAll(mapPieces)) return@onUseWith false

            sendItemDialogue(
                player,
                Items.CRANDOR_MAP_1538,
                "You put the three pieces together and assemble a map that shows the route through the reefs to Crandor."
            )
            addItem(player, Items.CRANDOR_MAP_1538, 1)
            return@onUseWith true
        }

        /*
         * Handles interaction for each map pieces.
         */

        on(mapPieces, IntType.ITEM, "study") { player, node ->
            val dialogue = when (node.id) {
                Items.MAP_PART_1535 -> "This is a piece of map that you found in Melzar's Maze. You will need to join it to the other two map pieces before you can see the route to Crandor."
                Items.MAP_PART_1536 -> "This is a piece of map that you got from Wormbrain, the goblin thief. You will need to join it to the other two map pieces before you can see the route to Crandor."
                Items.MAP_PART_1537 -> "This is a piece of map that you found in a secret chest in the Dwarven Mine. You will need to join it to the other two map pieces before you can see the route to Crandor."
                else -> return@on false
            }
            sendItemDialogue(player, node.id, dialogue)
            return@on true
        }

        /*
         * Handles opening crandor map.
         */

        on(Items.CRANDOR_MAP_1538, IntType.ITEM, "study") { player, _ ->
            openInterface(player, Components.DRAGON_SLAYER_QIP_MAP_547)
            return@on true
        }

        /*
         * Handles repair the lady lumbridge gaping hole.
         */

        on(Scenery.HOLE_2589, IntType.SCENERY, "repair", "fix", "use") { player, _ ->

            val memorized = player.getSavedData().questData.getDragonSlayerAttribute("memorized")
            if (memorized) {
                sendDialogueLines(player,
                    "You don't need to mess about with broken ships now that you have",
                    "found the secret passage from Karamja."
                )
                return@on true
            }

            when {
                !player.inventory.containsItem(DragonSlayer.NAILS) -> sendDialogue(
                    player,
                    "You need 30 steel nails to attach the plank with."
                )

                !player.inventory.containsItem(DragonSlayer.PLANK) -> sendDialogue(
                    player,
                    "You'll need to use wooden planks on this hole to patch it up."
                )

                !player.inventory.containsItem(DragonSlayer.HAMMER) -> sendDialogue(
                    player,
                    "You need a hammer to force the nails in with."
                )

                else -> {
                    player.inventory.remove(DragonSlayer.NAILS)
                    player.inventory.remove(DragonSlayer.PLANK)
                    player.animate(Animation(Animations.HUMAN_POH_BUILD_3676))
                    player.getSavedData().questData.dragonSlayerPlanks++

                    if (player.getSavedData().questData.dragonSlayerPlanks < 3) {
                        player.dialogueInterpreter.sendDialogue(
                            "You nail a plank over the hole, but you still need more planks to",
                            "close the hole completely."
                        )
                    } else {
                        player.getSavedData().questData.setDragonSlayerAttribute("repaired", true)
                        setVarbit(player, 1837, 1)
                        player.dialogueInterpreter.sendDialogue(
                            "You nail a final plank over the hole. You have successfully patched",
                            "the hole in the ship."
                        )
                    }
                }
            }
            return@on true
        }

        /*
         * Handles opening the sealed entrance door.
         */

        onUseWith(IntType.SCENERY, magicDoorRequiredItemIds, Scenery.MAGIC_DOOR_25115) { player, used, _ ->
            if (getQuestStage(player, Quests.DRAGON_SLAYER.questName) < 20) {
                return@onUseWith true
            }
            if (!removeItem(player, used)) {
                return@onUseWith true
            }
            sendMessage(player, "You put ${used.name.lowercase()} into the opening in the door.")
            val index = magicDoorRequiredItemIds.indexOf(used.id).takeIf { it >= 0 } ?: 0
            player.savedData.questData.dragonSlayerItems[index] = true
            DragonSlayer.handleMagicDoor(player, false)
            return@onUseWith true
        }

        keyDoors.forEach { (doorId, keyItemId) ->
            on(doorId, IntType.SCENERY, "open") { player, node ->
                if (!removeItem(player, keyItemId)) {
                    sendMessage(player, "This door is securely locked.")
                } else {
                    sendMessage(player, "The key disintegrates as it unlocks the door.")
                    DoorActionHandler.handleAutowalkDoor(player, node.asScenery())
                }
                return@on true
            }
        }

        on(Scenery.DOOR_2595, IntType.SCENERY, "open") { player, node ->
            val isFreePass = player.location.x == 2940 && player.location.y == 3248

            if (isFreePass) {
                DoorActionHandler.handleAutowalkDoor(player, node.asScenery())
                return@on true
            }

            if (player.inventory.containsItem(DragonSlayer.MAZE_KEY)) {

                sendMessage(player, "You use the key and the door opens.")

                if (!player.musicPlayer.hasUnlocked(Music.MELZARS_MAZE_365)) {
                    player.musicPlayer.unlock(Music.MELZARS_MAZE_365)
                }

                DoorActionHandler.handleAutowalkDoor(player, node.asScenery())
                return@on true
            }

            sendMessage(player, "This door is securely locked.")
            return@on true
        }

        /*
         * Handles search & close interaction with end melzar basement chest.
         */

        on(Scenery.CHEST_2603, IntType.SCENERY, "open") { player, node ->
            player.packetDispatch.sendMessage("You open the chest.")
            replaceScenery(node.asScenery(), 2604, -1)
            playAudio(player, Sounds.CHEST_OPEN_52)
            return@on true
        }

        on(Scenery.CHEST_2604, IntType.SCENERY, "search") { player, _ ->
            if (!player.inventory.containsItem(DragonSlayer.MAZE_PIECE)) {
                if (!player.inventory.add(DragonSlayer.MAZE_PIECE))
                    GroundItemManager.create(DragonSlayer.MAZE_PIECE, player)
                player.dialogueInterpreter.sendItemMessage(
                    DragonSlayer.MAZE_PIECE.id, "You find a map piece in the chest."
                )
            } else {
                sendMessage(player, "You find nothing in the chest.")
            }
            return@on true
        }

        on(Scenery.CHEST_2604, IntType.SCENERY, "close") { player, node ->
            sendMessage(player, "You shut the chest.")
            replaceScenery(node.asScenery(), 2603, -1)
            playAudio(player, Sounds.CHEST_CLOSE_51)
            return@on true
        }
    }

    companion object {
        val mapPieces = intArrayOf(Items.MAP_PART_1537, Items.MAP_PART_1536, Items.MAP_PART_1535)
        private val magicDoorRequiredItemIds = intArrayOf(Items.LOBSTER_POT_301, Items.UNFIRED_BOWL_1791, Items.SILK_950, Items.WIZARDS_MIND_BOMB_1907)
        val keyDoors = mapOf(
            2601 to Items.KEY_1548,
            2600 to Items.KEY_1547,
            2609 to Items.KEY_1546,
            2598 to Items.KEY_1545,
            2596 to Items.KEY_1543,
        )
    }
}
