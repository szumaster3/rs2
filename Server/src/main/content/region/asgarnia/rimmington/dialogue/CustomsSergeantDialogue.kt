package content.region.asgarnia.rimmington.dialogue

import content.data.Quests

import core.api.hasRequirement
import core.api.sendDialogue
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.tools.END_DIALOGUE

/**
 * Represents the Customs Sergeant dialogue.
 */
class CustomsSergeantDialogue : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            0 -> if (!hasRequirement(player!!, Quests.ROCKING_OUT.questName, false)) {
                npc(FaceAnim.SLEEPING, "Zzzzzzzzzzzzzzzzzzz.").also { stage++ }
            } else {
                sendDialogue(player!!, "Customs Sergeant seems too busy to talk.").also { stage = END_DIALOGUE }
            }
            1 -> player(FaceAnim.STRUGGLE, "Ahem.").also { stage++ }
            2 -> npc("Push off, I'm busy.").also { stage++ }
            3 -> player("Okay.").also { stage++ }
            4 -> npc(FaceAnim.ANNOYED, "Now!").also { stage = END_DIALOGUE }
        }
    }
}
