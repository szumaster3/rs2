package content.region.desert.uzer.quest.golem

import core.api.finishQuest
import core.api.getQuestStage
import core.api.isQuestComplete
import core.api.setQuestStage
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs
import shared.consts.Quests

@Initializable
class ClayGolemDialogue(player: Player? = null) : Dialogue(player) {

    /**
     * Represents the internal dialogue flow branches for the Clay Golem.
     */
    private enum class Flow {
        NONE,
        FIRST_MEET,
        NEEDS_REPAIR,
        AFTER_REPAIR,
        NOT_BELIEVE,
        REPEATING,
        PROGRAM_UPDATE,
        AFTER_QUEST
    }

    private var flow = Flow.NONE

    override fun open(vararg objects: Any?): Boolean {
        npc = objects[0] as NPC
        val p = player!!

        flow = when {
            isQuestComplete(p, Quests.THE_GOLEM) -> Flow.AFTER_QUEST
            else -> when (getQuestStage(p, Quests.THE_GOLEM)) {
                0 -> Flow.FIRST_MEET
                1 -> Flow.NEEDS_REPAIR
                2, 3 -> Flow.AFTER_REPAIR
                4, 5 -> Flow.NOT_BELIEVE
                6, 7 -> Flow.REPEATING
                8 -> Flow.PROGRAM_UPDATE
                else -> Flow.NONE
            }
        }

        stage = 0
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (flow) {
            Flow.AFTER_QUEST -> handleAfterQuest()
            Flow.FIRST_MEET -> handleFirstMeet(buttonId)
            Flow.NEEDS_REPAIR -> handleNeedsRepair()
            Flow.AFTER_REPAIR -> handleAfterRepair()
            Flow.NOT_BELIEVE -> handleDoesntBelieve()
            Flow.REPEATING -> handleRepeating()
            Flow.PROGRAM_UPDATE -> handleProgramUpdate()
            else -> end()
        }
        return true
    }

    private fun handleAfterQuest() {
        npc(FaceAnim.OLD_BOWS_HEAD_SAD, "Thank you for helping me. A golem can have no greater satisfaction than knowing that its task is complete.")
        stage = END_DIALOGUE
    }

    private fun handleFirstMeet(buttonId: Int) {
        val p = player!!
        when (stage) {
            0 -> npc(FaceAnim.OLD_BOWS_HEAD_SAD, "Damage... severe...", "task... incomplete...").also { stage++ }
            1 -> options("Select an option", "Shall I try to repair you?", "I'm not going to find a conversation here!")
            2 -> when (buttonId) {
                1 -> {
                    if (p.questRepository.getQuest(Quests.THE_GOLEM)?.hasRequirements(p) == true) {
                        player("Shall I try to repair you?")
                        stage++
                    } else {
                        player("I'm not going to find a conversation here!")
                        stage = END_DIALOGUE
                    }
                }
                2 -> {
                    player("I'm not going to find a conversation here!")
                    stage = END_DIALOGUE
                }
            }
            3 -> npc(FaceAnim.OLD_BOWS_HEAD_SAD, "Repairs... needed...").also {
                if (getQuestStage(p, Quests.THE_GOLEM) < 1) setQuestStage(p, Quests.THE_GOLEM, 1)
                stage = END_DIALOGUE
            }
        }
    }

    private fun handleNeedsRepair() {
        npc(FaceAnim.OLD_BOWS_HEAD_SAD, "Repairs... needed...")
        stage = END_DIALOGUE
    }

    private fun handleAfterRepair() {
        val p = player!!
        when (stage) {
            0 -> npc(FaceAnim.OLD_BOWS_HEAD_SAD, "Damage repaired...").also { stage++ }
            1 -> npc(FaceAnim.OLD_BOWS_HEAD_SAD, "Thank you. My body and mind are fully healed.").also { stage++ }
            2 -> npc(FaceAnim.OLD_BOWS_HEAD_SAD, "Now I must complete my task by defeating the great enemy.").also { stage++ }
            3 -> player("What enemy?").also { stage++ }
            4 -> npc(FaceAnim.OLD_BOWS_HEAD_SAD, "A great demon. It broke through from its dimension to attack the city.").also { stage++ }
            5 -> npc(FaceAnim.OLD_BOWS_HEAD_SAD, "The golem army was created to fight it. Many were destroyed, but we drove the demon back!").also { stage++ }
            6 -> npc(FaceAnim.OLD_BOWS_HEAD_SAD, "The demon is still wounded. You must open the portal so that I can strike the final blow and complete my task.").also {
                setQuestStage(p, Quests.THE_GOLEM, 3)
                stage = END_DIALOGUE
            }
        }
    }

    private fun handleDoesntBelieve() {
        val p = player!!
        when (stage) {
            0 -> npc(FaceAnim.OLD_BOWS_HEAD_SAD, "My task is incomplete. You must open the portal so I can defeat the great demon.").also { stage++ }
            1 -> player("It's ok, the demon is dead!").also { stage++ }
            2 -> npc(FaceAnim.OLD_BOWS_HEAD_SAD, "The demon must be defeated...").also { stage++ }
            3 -> player("No, you don't understand. I saw the demon's skeleton. It must have died of its wounds.").also { stage++ }
            4 -> npc(FaceAnim.OLD_BOWS_HEAD_SAD, "Demon must be defeated! Task incomplete.").also {
                setQuestStage(p, Quests.THE_GOLEM, 5)
                stage = END_DIALOGUE
            }
        }
    }

    private fun handleRepeating() {
        val p = player!!
        when (stage) {
            0 -> npc(FaceAnim.OLD_BOWS_HEAD_SAD, "My task is incomplete. You must open the portal so I can defeat the great demon.").also { stage++ }
            1 -> player("I already told you, he's dead!").also { stage++ }
            2 -> npc(FaceAnim.OLD_BOWS_HEAD_SAD, "Task incomplete.").also { stage++ }
            3 -> player("Oh, how am I going to convince you?").also {
                if (getQuestStage(p, Quests.THE_GOLEM) < 7) setQuestStage(p, Quests.THE_GOLEM, 7)
                stage = END_DIALOGUE
            }
        }
    }

    private fun handleProgramUpdate() {
        val p = player!!
        when (stage) {
            0 -> npc(FaceAnim.OLD_BOWS_HEAD_SAD, "New instructions...", "Updating program...").also { stage++ }
            1 -> npc(FaceAnim.OLD_BOWS_HEAD_SAD, "Task complete!").also { stage++ }
            2 -> npc(FaceAnim.OLD_BOWS_HEAD_SAD, "Thank you. Now my mind is at rest.").also {
                finishQuest(p, Quests.THE_GOLEM)
                stage = END_DIALOGUE
            }
        }
    }

    override fun getIds(): IntArray = intArrayOf(
        NPCs.CLAY_GOLEM_1907,
        NPCs.BROKEN_CLAY_GOLEM_1908,
        NPCs.DAMAGED_CLAY_GOLEM_1909,
        NPCs.CLAY_GOLEM_1910
    )
}
