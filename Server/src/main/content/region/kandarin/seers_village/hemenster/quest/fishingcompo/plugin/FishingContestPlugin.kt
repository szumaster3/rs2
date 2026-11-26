package content.region.kandarin.seers_village.hemenster.quest.fishingcompo.plugin

import content.data.Quests

import content.data.GameAttributes
import core.api.*
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.game.dialogue.Topic
import core.game.global.action.DoorActionHandler
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.QueueStrength
import core.game.node.Node
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.game.world.map.Location
import core.game.world.repository.Repository
import core.game.world.update.flag.context.Animation
import shared.consts.*

class FishingContestPlugin : InteractionListener {

    companion object {
        private val VINE_SCENERY = intArrayOf(Scenery.VINE_58, Scenery.VINE_2989, Scenery.VINE_2990, Scenery.VINE_2991, Scenery.VINE_2992, Scenery.VINE_2993, Scenery.VINE_2994, Scenery.VINE_2013)
        private val TUNNEL_STAIRS = intArrayOf(Scenery.STAIRS_55, Scenery.STAIRS_57)
        private val GATES = intArrayOf(Scenery.GATE_47, Scenery.GATE_48, Scenery.GATE_52, Scenery.GATE_53)
    }

    override fun defineListeners() {

        /*
         * Handles interaction with tunnel stairs (White Wolf Mountain shortcut).
         */

        on(TUNNEL_STAIRS, IntType.SCENERY, "climb-down") { player, node ->
            if (!isQuestComplete(player, Quests.FISHING_CONTEST.questName)) {
                when (node.id) {
                    Scenery.STAIRS_55 -> Repository.findNPC(NPCs.VESTRI_3679)?.let {
                        openDialogue(player, NPCs.VESTRI_3679, it)
                    }
                    Scenery.STAIRS_57 -> Repository.findNPC(NPCs.AUSTRI_232)?.let {
                        openDialogue(player, NPCs.AUSTRI_232, it)
                    }
                }
            } else {
                val destination = when (node.id) {
                    Scenery.STAIRS_55 -> Location(2820, 9882, 0)
                    Scenery.STAIRS_57 -> Location(2876, 9879, 0)
                    else -> return@on true
                }
                teleport(player, destination)
            }
            return@on true
        }

        /*
         * Handles interaction with Hemenster fence and McGrubor's gates.
         * Varbit ID: 2053
         */

        on(GATES, IntType.SCENERY, "open") { player, node ->
            when (node.id) {
                Scenery.GATE_47,
                Scenery.GATE_48 -> handleFishingContestGate(player, node)

                Scenery.GATE_52,
                Scenery.GATE_53 -> {
                    if (inBorders(player, 2647, 3468, 2652, 3469)) {
                        lock(player, 1)
                        face(findNPC(NPCs.FORESTER_231)!!, player, 3)
                        sendNPCDialogue(player, NPCs.FORESTER_231, "Hey! You can't come through here! This is private land!", FaceAnim.ANGRY)
                        sendMessage(player, "There might be a gap in the fence somewhere where he wouldn't see you sneak in.")
                        sendMessage(player, "You should look around.")
                    } else {
                        sendDialogue(player, "This gate is locked.")
                    }
                    return@on true
                }

                else -> return@on true
            }
        }

        /*
         * Handles interaction with vines in McGrubor's Wood.
         */

        on(VINE_SCENERY, IntType.SCENERY, "check") { player, _ ->
            if (!inInventory(player, Items.SPADE_952, 1)) {
                sendDialogue(player, "I should probably get a spade before I try digging here.")
                return@on true
            }

            queueScript(player, 1, QueueStrength.WEAK) {
                sendMessage(player, "You dig in amongst the vines.")
                animate(player, Animation(Animations.DIG_SPADE_830))
                sendMessage(player, "You find a red vine worm.")
                addItem(player, Items.RED_VINE_WORM_25, 1, Container.INVENTORY)
                return@queueScript stopExecuting(player)
            }

            return@on true
        }

        /*
         * Handles Bonzo NPC - pay option interaction.
         */

        on(NPCs.BONZO_225, IntType.NPC, "pay") { player, node ->
            if(getQuestStage(player, Quests.FISHING_CONTEST.questName) >= 20) {
                sendMessage(player, "You've already won the Fishing Competition and don't need to enter again.")
                return@on true
            }
            Repository.findNPC(node.id)?.let { openDialogue(player, NPCs.BONZO_225, it) }
            return@on true
        }

        /*
         * Handles interaction with wall pipe.
         */

        onUseWith(IntType.SCENERY, Items.GARLIC_1550, Scenery.WALL_PIPE_41) { player, used, with ->
            val scenery = with.asScenery()
            if (
                scenery.location == Location(2638, 3446, 0) &&
                !getAttribute(player, GameAttributes.QUEST_FISHINGCOMPO_STASH_GARLIC, false)
            ) {
                if(removeItem(player, Items.GARLIC_1550, Container.INVENTORY)) {
                    removeItem(player, Item(Items.GARLIC_1550, 1), Container.INVENTORY)
                    setAttribute(player, GameAttributes.QUEST_FISHINGCOMPO_STASH_GARLIC, true)
                    sendItemDialogue(player, used.asItem(), "You stash the garlic in the pipe.")
                }
            } else {
                faceLocation(player, scenery.location)
                sendDialogue(player, "I shoved garlic up here.")
            }
            return@onUseWith true
        }

        /*
         * Handles the search option for interacting with  wall pipe.
         */

        on(Scenery.WALL_PIPE_41, IntType.SCENERY, "search") { player, node ->
            val scenery = node.asScenery()
            if (scenery.location != Location(2638, 3446, 0)) {
                faceLocation(player, scenery.location)
                sendPlayerDialogue(player, "Ewww - it's a smelly sewage pipe.", FaceAnim.DISGUSTED)
                return@on true
            }
            if (getAttribute(player, GameAttributes.QUEST_FISHINGCOMPO_STASH_GARLIC, false)) {
                sendDialogue(player, "I shoved garlic up here.")
            } else {
                faceLocation(player, scenery.location)
                sendPlayerDialogue(player, "Ewww - it's a smelly sewage pipe.", FaceAnim.DISGUSTED)
            }
            return@on true
        }
    }

    override fun defineDestinationOverrides() {
        setDest(IntType.SCENERY, intArrayOf(Scenery.WALL_PIPE_41), "search", "use") { _, node ->
            return@setDest node.location.transform(0, -1, 0)
        }
    }

    private fun otherGate(id: Int): Int? = when (id) {
        Scenery.GATE_47 -> Scenery.GATE_48
        Scenery.GATE_48 -> Scenery.GATE_47
        else -> null
    }

    private fun walkThroughGate(player: Player, node: Node, otherGate: Int) {
        DoorActionHandler.autowalkFence(
            player,
            node.asScenery(),
            node.id,
            otherGate
        )
    }

    private fun handleFishingContestGate(player: Player, node: Node): Boolean {
        val otherGate = otherGate(node.id) ?: return false

        if(getAttribute(player, GameAttributes.QUEST_FISHINGCOMPO_CONTEST, false)) {
            sendDialogue(player, "You can't do that until the fishing contest is over.")
            return true
        }

        if (
            player.location.x < 2643 &&
            getAttribute(player, GameAttributes.QUEST_FISHINGCOMPO_CONTEST, false)
        ) {
            openDialogue(player, FishingCompetitionExitDialogue(node, node.id to otherGate))
            return true
        }

        val shownPass = getVarbit(player, Vars.VARBIT_FISHING_CONTEST_PASS_SHOWN_2053)

        when {
            shownPass == 0 && player.location.x > 2642 -> {
                if (inInventory(player, Items.FISHING_PASS_27)) {
                    sendMessage(player, "You should give your pass to Morris.")
                } else {
                    sendMessage(player, "You need a fishing pass to fish here.")
                }
            }

            shownPass == 1 && player.location.x > 2642 -> {
                walkThroughGate(player, node, otherGate)
            }

            !inInventory(player, Items.FISHING_ROD_307) -> {
                sendDialogueLines(
                    player,
                    "I should probably get a rod from",
                    "Grandpa Jack before starting."
                )
            }

            else -> {
                walkThroughGate(player, node, otherGate)
            }
        }

        return true
    }

    /**
     * Represents dialogue for leaving the fishing competition through the gate.
     */
    private class FishingCompetitionExitDialogue(private val node: Node, private val gatePair: Pair<Int, Int>?) : DialogueFile() {

        init { stage = 0 }

        override fun handle(componentID: Int, buttonID: Int) {
            when(stage) {
                0 -> sendNPCDialogue(player!!, NPCs.BONZO_225, "So you're calling it quits here for now?").also { stage++ }
                1 -> showTopics(
                    Topic("Yes I'll compete again another day.", 2, false),
                    Topic("Go back and catch more", 3, true)
                )
                2 -> {
                    removeAttribute(player!!, GameAttributes.QUEST_FISHINGCOMPO_CONTEST)
                    runTask(player!!, 3) {
                        gatePair?.let { DoorActionHandler.autowalkFence(player!!, node.asScenery(), it.first, it.second) }
                        sendNPCDialogue(player!!, NPCs.BONZO_225, "Ok, I'll see you again.")
                        end()
                    }
                }
                3 -> {
                    sendPlayerDialogue(player!!, "Actually I'll go back and catch some more.")
                    runTask(player!!, 3) {
                        sendNPCDialogue(player!!, NPCs.BONZO_225, "Good luck!")
                        end()
                    }
                }
            }
        }
    }
}