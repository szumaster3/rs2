package content.region.desert.nardah.dialogue

import content.data.Quests

import core.api.hasRequirement
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.tools.END_DIALOGUE

/**
 * Represents the Zahra dialogue.
 */
class ZahraDialogue : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            0 -> if (!hasRequirement(player!!, Quests.SPIRITS_OF_THE_ELID.questName, false)) {
                player(FaceAnim.FRIENDLY, "Good day to you.").also { stage++ }
            } else {
                playerl(FaceAnim.FRIENDLY, "How's life now the curse has been lifted?").also { stage = 6 }
            }
            1 -> npc(FaceAnim.FRIENDLY, "Hello.").also { stage++ }
            2 -> player("You don't look too happy.").also { stage++ }
            3 -> npc("True. We've not fallen on the best of times here.").also { stage++ }
            4 -> player("Any way that I can help?").also { stage++ }
            5 -> npcl(FaceAnim.HALF_GUILTY, "Possibly. I'd go talk to Awusah the Mayor of Nardah. He's in the big house on the east side of the town square.").also { stage = END_DIALOGUE }
            6 -> npcl(FaceAnim.HALF_GUILTY, "Much better thanks to you. We're all very impressed.").also { stage = END_DIALOGUE }
        }
    }
}
