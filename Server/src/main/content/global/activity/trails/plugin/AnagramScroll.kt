package content.global.activity.trails.plugin

import content.global.activity.trails.ClueLevel
import content.global.activity.trails.ClueScroll
import content.global.activity.trails.TreasureTrailManager
import core.api.*
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.game.interaction.Option
import core.game.node.Node
import core.game.node.entity.Entity
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.item.Item
import shared.consts.Components
import shared.consts.Items
import shared.consts.NPCs

/**
 * Represents an anagram clue scroll.
 */
abstract class AnagramScroll(
    name: String?,
    clueId: Int,
    private val anagram: String?,
    val npcId: Int,
    level: ClueLevel?,
    val challenge: Int? = null
) : ClueScroll(name, clueId, level, Components.TRAIL_MAP09_345) {

    override fun interact(e: Entity, target: Node, option: Option): Boolean {
        val player = e.asPlayer()
        val npc = target.asNpc()
        return handleClue(player, npc)
    }

    override fun read(player: Player) {
        repeat(8) { sendString(player, "", interfaceId, it + 1) }
        super.read(player)
        sendString(player, "<br><br><br><br>This anagram reveals<br>who to speak to next:<br><br><br>$anagram", interfaceId, 1)
    }

    companion object {

        /**
         * Gets the active anagram scroll for a player and NPC.
         */
        fun getClueForNpc(player: Player, npc: NPC): AnagramScroll? {
            player.inventory.toArray()
                .filterNotNull()
                .mapNotNull { getClueScrolls()[it.id] as? AnagramScroll }
                .firstOrNull { it.npcId == npc.id }
                ?.let { return it }

            val clueId = getAttribute(player, "anagram_clue_active", -1)
            val activeClue = getClueScrolls()[clueId] as? AnagramScroll

            return if (activeClue != null && activeClue.npcId == npc.id && inInventory(player, activeClue.clueId)) {
                activeClue
            } else {
                removeAttribute(player, "anagram_clue_active")
                null
            }
        }

        /**
         * Handles interaction with NPC for clue logic.
         */
        fun handleClue(player: Player, npc: NPC): Boolean {
            val clue = getClueForNpc(player, npc) ?: return false
            val facial = faceAnim(npc.id)

            if (freeSlots(player) == 0) {
                sendNPCDialogue(player, npc.id, "Your inventory is full, make some room first.", facial)
                return true
            }

            val puzzle = clue.challenge?.let { PuzzleBoxPlugin.forId(it) }
            return if (puzzle != null) {
                handlePuzzleBox(player, npc, clue, puzzle, facial)
            } else {
                handleChallengeScroll(player, npc, clue, facial)
            }
        }

        private fun faceAnim(npcId: Int) = when (npcId) {
            NPCs.ZOGRE_UGLUG_NAR_2039, NPCs.GNOME_BALL_COACH_2802, NPCs.GNOMEREFEREE_635 -> FaceAnim.OLD_DEFAULT
            else -> FaceAnim.HALF_ASKING
        }

        /**
         * Handles puzzle box logic.
         */
        private fun handlePuzzleBox(player: Player, npc: NPC, clue: AnagramScroll, puzzle: PuzzleBoxPlugin.PuzzleBox, chatAnim: FaceAnim): Boolean {
            val hasPuzzle = inInventory(player, puzzle.id)
            val isComplete = PuzzleBoxPlugin.isComplete(player, puzzle.type)

            if (hasPuzzle && !isComplete) {
                sendNPCDialogue(player, npc.id, "You haven't completed the puzzle yet!", chatAnim)
                return true
            }

            if (!hasPuzzle) {
                if (!removeItem(player, clue.clueId)) return false
                setAttribute(player, "anagram_clue_active", clue.clueId)
                addItem(player, puzzle.id)
                sendNPCDialogue(player, npc.id, getPuzzleDialogue(npc.id), chatAnim)
                addDialogueAction(player) { p, _ ->
                    sendItemDialogue(p, puzzle.id, "${npc.name} has given you a puzzle box!")
                }
                return true
            }

            if (!removeItem(player, puzzle.id)) return false
            removeAttributes(player, "${puzzle.type}:puzzle:done", "anagram_clue_active")
            sendNPCDialogue(player, npc.id, getPuzzleCompleteDialogue(npc.id), chatAnim)
            addDialogueAction(player) { p, _ ->
                val manager = TreasureTrailManager.getInstance(p)
                getClueScrolls()[clue.clueId]?.reward(p)
                if (manager.isCompleted) {
                    sendItemDialogue(p, Items.CASKET_405, "You've found a casket!")
                    manager.clearTrail()
                } else {
                    clue.level?.let { getClue(it) }?.let { newClue ->
                        sendItemDialogue(p, newClue, "You receive another clue scroll.")
                        addItem(p, newClue.id, 1)
                    }
                }
            }
            return true
        }

        /**
         * Handles challenge scroll logic.
         */
        private fun handleChallengeScroll(player: Player, npc: NPC, clue: AnagramScroll, chatAnim: FaceAnim): Boolean {
            val challengeId = clue.challenge ?: return false
            if (inInventory(player, challengeId, 1)) return false

            val clueId = Item(challengeId)
            openDialogue(player, object : DialogueFile() {
                override fun handle(componentID: Int, buttonID: Int) {
                    when (stage) {
                        0 -> npc(chatAnim, "Ah! Here you go!").also { stage++ }
                        1 -> player("What?").also { stage++ }
                        2 -> npc(chatAnim, "I need you to answer this for me.").also { stage++ }
                        3 -> {
                            end()
                            setAttribute(player, "anagram_clue_active", challengeId)
                            addItem(player, clueId.id, 1)
                            val name = getItemName(challengeId).lowercase()
                            sendItemDialogue(player, clueId, "${npc.name} has given you a $name!")
                        }
                    }
                }
            })

            return true
        }

        /**
         * Gets the unique dialogue assigned to clue npc.
         */
        private fun getPuzzleDialogue(npcId: Int) = when (npcId) {
            NPCs.RAMARA_DU_CROISSANT_3827 -> "I've ze puzzle for you to solve."
            NPCs.ZOGRE_UGLUG_NAR_2039 -> "You want puzzle?"
            NPCs.GENERAL_BENTNOZE_RED_4493 -> "Human do puzzle for me!"
            else -> listOf("Oh, I have a puzzle for you to solve.", "Oh, I've been expecting you.", "The solving of this puzzle could be the key to your treasure.").random()
        }

        /**
         * Gets unique completion dialogue.
         */
        private fun getPuzzleCompleteDialogue(npcId: Int) = when (npcId) {
            NPCs.RAMARA_DU_CROISSANT_3827 -> "Zat's wonderful!"
            NPCs.ZOGRE_UGLUG_NAR_2039 -> "Dere you go!"
            NPCs.GENERAL_BENTNOZE_RED_4493 -> "Thank you human!"
            else -> listOf("Here is your reward!", "Well done, traveller.").random()
        }
    }
}
