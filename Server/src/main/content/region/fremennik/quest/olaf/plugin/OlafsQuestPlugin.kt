package content.region.fremennik.quest.olaf.plugin

import content.data.GameAttributes
import content.region.fremennik.quest.olaf.npc.UlfricNPC
import core.api.*
import core.game.global.action.DoorActionHandler
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.skill.Skills
import core.game.world.map.Location
import shared.consts.*

class OlafsQuestPlugin : InteractionListener {

    companion object {
        val parchmentContent = arrayOf(
            "",
            "Alas, we are lost. Our ship has been",
            "smashed against the rocks and we",
            "are floundering. Sven has ordered us",
            "to make for the hidden caves, but I",
            "do not think we will make it alive.",
            "The other ships have already gone",
            "down, along with the greater share of",
            "that cursed treasure. I commend my",
            "soul to the gods. -Ulfric Longbeard",
        )
    }

    override fun defineListeners() {

        /*
         * Handles opening the svens map.
         */

        on(Items.SVENS_LAST_MAP_11034, IntType.ITEM, "read") { player, _ ->
            openInterface(player, Components.OLAF2_TREASUREMAP_254)
            return@on true
        }

        /*
         * Handles use the damp planks on embers.
         */

        on(Scenery.EMBERS_14171, IntType.SCENERY, "Use-driftwood") { player, node ->
            if(getStatLevel(player, Skills.FIREMAKING) < 40) {
                sendMessage(player, "You need a Firemaking level of at least 40 in order to do this.")
                return@on false
            }

            if(removeItem(player, Items.DAMP_PLANKS_11031)) {
                faceLocation(player, node.asScenery().location)
                animate(player, Animations.TINDERBOX_3658)
                sendDialogue(player, "You managed to get fire going!")
                setQuestStage(player, Quests.OLAFS_QUEST, 3)
                setVarbit(player, 3537, 0)
            } else {
                sendMessage(player, "You need some driftwood to light this fire.")
            }
            return@on true
        }

        /*
         * Handles search the picture wall.
         */

        on(Scenery.PICTURE_WALL_23156, IntType.SCENERY, "search") { player, _ ->
            if(inInventory(player, Items.KEY_11043)) {
                sendDialogueLines(player, "this 'wall' has some disks set into it showing a picture and some", "levers. I should try pulling the levers to see what happens.")
                addDialogueAction(player) { _, _ ->
                    openInterface(player, Components.OLAF2_SKULL_PUZZLE_253)
                }
            }
            return@on true
        }

        /*
         * Handles opening the key gate.
         */

        on(Scenery.GATE_23216, IntType.SCENERY, "open") { player, node ->
            if(player.location== Location(2726, 10168, 0)){
                DoorActionHandler.handleAutowalkDoor(player, node.asScenery())
            } else {
                sendDialogueLines(player, "This gate has several locks, which seem to match with the rusty key's", "handle.")
                addDialogueAction(player) { _, _ ->
                    openInterface(player, Components.OLAF2_LOCK_GATE_252)
                }
            }
            return@on true
        }

        /*
         * Handles opening the chest and spawn Ulfric NPC.
         */

        on(Scenery.CHEST_14197, IntType.SCENERY, "open") { player, node ->
            if (isQuestComplete(player, Quests.OLAFS_QUEST)) {
                sendMessage(player, "The chest is empty.")
                return@on true
            }
            if (getQuestStage(player, Quests.OLAFS_QUEST) != 99) {
                if (UlfricNPC.spawnUlfric(player)) {
                    replaceScenery(node.asScenery(), node.id -1, 3)
                    sendMessage(player, "You open the chest and the vengeful spirit of fremennik captain appears!", 2)
                    player.questRepository.setStageNonmonotonic(player.questRepository.forIndex(137), 98)
                } else {
                    sendMessage(player, "You can't do that right now.")
                }
            } else {
                sendMessage(player, "You find a note in the chest...")
                sendMessage(player, "...and a huge heap of treasure!")
                finishQuest(player, Quests.OLAFS_QUEST)
            }
            return@on true
        }

        /*
         * Handles read the parchment.
         */

        on(Items.PARCHMENT_11036, IntType.ITEM, "read") { player, _ ->
            if(!getAttribute(player, GameAttributes.OLAF_READ_PARCHMENT, false)){
                sendMessage(player, "The scroll tells you that there were more ships washed up, further out to sea.")
                sendMessage(player, "Better yet, they were apparently carrying the better part of the treasure!")
                setAttribute(player, GameAttributes.OLAF_READ_PARCHMENT, true)
            } else {
                openInterface(player, Components.SCROLL_255).also {
                    sendString(player, parchmentContent.joinToString("<br>"), Components.SCROLL_255, 3)
                }
            }
            return@on true
        }
    }
}
