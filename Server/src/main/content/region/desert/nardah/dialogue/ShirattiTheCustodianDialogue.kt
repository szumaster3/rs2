package content.region.desert.nardah.dialogue

import content.data.Quests

import core.api.hasRequirement
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.tools.END_DIALOGUE

/**
 * Represents the Shiratti The Custodian dialogue.
 */
class ShirattiTheCustodianDialogue : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            0 -> if (!hasRequirement(player!!, Quests.SPIRITS_OF_THE_ELID.questName, false)) {
                options("Good day to you.", "What is this building?")
            } else {
                npcl(FaceAnim.FRIENDLY, "Thanks for returning the statuette. You have freed us of our curse. May you forever be considered a friend of Elidinis and her followers.").also { stage = END_DIALOGUE }
            }
            1 -> when (buttonID) {
                1 -> player("Good day to you.").also { stage++ }
                2 -> player("What is this building?").also { stage = 5 }
            }
            2 -> npcl(FaceAnim.FRIENDLY, "Be Careful in here and don't touch anything! There's some old and valuable artefacts around here. Careless outsiders.. you're always coming in and making a mess of things!").also { stage++ }
            3 -> playerl(FaceAnim.HALF_GUILTY, "Ok calm down, I'm not going to touch anything.").also { stage++ }
            4 -> npc("Yes, good don't.").also { stage = END_DIALOGUE }
            5 -> npcl(FaceAnim.FRIENDLY, "This is our local shrine to She.").also { stage++ }
            6 -> playerl(FaceAnim.HALF_GUILTY, "What do you mean by She?").also { stage++ }
            7 -> npcl(FaceAnim.FRIENDLY, "Such ignorance! I suppose I should come to expect it. People are forgetting the old ways of the desert around here. But then..").also { stage++ }
            8 -> npcl(FaceAnim.FRIENDLY, "What do those bureaucratic fools in Menaphos expect! If they're going to take so long to send us a new priestess of She. Do you know we haven't had one for five years now!").also { stage++ }
            9 -> playerl(FaceAnim.HALF_GUILTY, "Errm you still haven't told me what you mean by She.").also { stage++ }
            10 -> npcl(FaceAnim.FRIENDLY, "She! Otherwise known as Elidinis, Goddess of growth and fertility, Lady of the river, Wife of Tumeken, Flower of the desert, Mother of Itchlarin.").also { stage++ }
            11 -> playerl(FaceAnim.HALF_GUILTY, "That all sounds very confusing, I might just call her Elidinis for short.").also { stage = END_DIALOGUE }
        }
    }
}
