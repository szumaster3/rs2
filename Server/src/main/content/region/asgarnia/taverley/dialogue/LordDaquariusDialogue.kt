package content.region.asgarnia.taverley.dialogue

import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.tools.END_DIALOGUE
import shared.consts.NPCs

/**
 * Represents the Lord Daquarius dialogue.
 */
class LordDaquariusDialogue : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            0 -> player(FaceAnim.FRIENDLY, "Hello").also { stage++ }
            1 -> npc(FaceAnim.THINKING, "What want you, with the Lord of the Kinshra?", "Speak!").also { stage++ }
            2 -> player("I am here on behalf of the White Knights...").also { stage++ }
            3 -> npc("Pah!", "Begone, fool, or prepare to taste my blade!").also { stage++ }
            4 -> player("Now wait a minute, this concerns a friend of yours...").also { stage++ }
            5 -> npc(FaceAnim.HALF_ASKING, "A friend of mine?", "What friend of mine would the Temple Knights show", "any intere-").also { stage++ }
            6 -> npc(FaceAnim.HALF_ASKING, "Oh.", "This...", "This is about Solus, isn't it?").also { stage++ }
            7 -> player(FaceAnim.HAPPY, "Bingo.").also { stage++ }
            8 -> npc("Then let me assure you, that monster is no 'friend' of", "mine - if indeed such a creature even has 'friends'!").also { stage++ }
            9 -> npc("No, that man is certainly no friend of mine nor of the", "Kinshra.").also { stage++ }
            10-> player("So you will help me find him?").also { stage++ }
            11 -> npc("No fool...", "You and your White Knight cronies are no friend of", "the Kinshra either.").also { stage++ }
            12 -> npc("I will help neither of you, and I wish you all a slow and", "painful death.").also { stage++ }
            13 -> player("You know...", "I'm sure I could MAKE you tell me where he is...").also { stage++ }
            14 -> npc("Is that a threat, whelp?", "Do your worst.", "I care not for my own life, I stand only for the", "protection of the Kinshra!").also { stage++ }

            15 -> player!!.dialogueInterpreter.sendDialogues(NPCs.WANTED_SAVANT_2748, FaceAnim.NEUTRAL, "${player?.username}?", "I've been monitoring you.", "He's telling the truth, so don't try to bluff him by", "starting a fight.").also { stage++ }
            16 -> player!!.dialogueInterpreter.sendDialogues(NPCs.WANTED_SAVANT_2748, FaceAnim.NEUTRAL, "All of our records show that the only thing he will make", "a stand for is the protection of his men, and he has little", "regard for his own safety in comparison.").also { stage++ }
            17 -> player!!.dialogueInterpreter.sendDialogues(NPCs.WANTED_SAVANT_2748, FaceAnim.NEUTRAL, "You're going to have to find some other way to", "intimidate him into giving you the information we need.").also { stage++ }

            18 -> player("Understood.", "${player?.username} out.").also { stage = END_DIALOGUE }
            19 -> npc(FaceAnim.SAD, "Stop!", "I will tell you what you want!", "Please... leave my men be...").also { stage = END_DIALOGUE }
            20 -> npc("What want you, with the Lord of the Kinshra?", "Speak!").also { stage++ }
            21 -> player("Okay Daquarius, you tell me the whereabouts of Solus", "Dellagar right now, or I will put every Black Knight", "here to their death in front of you!").also { stage++ }
            22 -> npc(FaceAnim.SAD, "*sigh*", "I should have known the White Knights would be my", "ruin once again...").also { stage++ }
            23 -> npc(FaceAnim.SAD, "I do not know his exact whereabouts, and when last we", "met we did not leave...", "on the best of terms.").also { stage++ }
            24 -> npc("All I know is that he left behind some fur when he left,", "I would expect him to be in an area with furred", "creatures of some sort.").also { stage++ }
            25 -> player("What kind of fur?", "Dog? Bear? Wolf?").also { stage++ }
            26 -> npc("I do not know, I am a warrior, not a zookeeper.", "It was not bear fur, I know that much.").also { stage++ }

            27 -> player(FaceAnim.ANGRY, "That's it?", "That's your help?", "You'd better not be lying Daquarius, or...").also { stage++ }
            28 -> player!!.dialogueInterpreter.sendDialogues(NPCs.WANTED_SAVANT_2748, FaceAnim.NEUTRAL, "Calm yourself Player, he is telling the truth.").also { stage++ }

            29 -> player("How do you know?").also { stage++ }
            30 -> player!!.dialogueInterpreter.sendDialogues(NPCs.WANTED_SAVANT_2748, FaceAnim.NEUTRAL, "I am monitoring your conversation through the", "CommOrb, his physiology shows none of the changes we", "usually get when someone lies to us.").also { stage++ }

            31 -> player("So this CommOrb thing can work as a lie-detector?", "That's pretty useful!").also { stage++ }
            32 -> player!!.dialogueInterpreter.sendDialogues(NPCs.WANTED_SAVANT_2748, FaceAnim.NEUTRAL, "Yes, it is, but back to the task at hand.", "I suggest you head to the next subject of our", "observations, he might be more help in locating Solus.").also { stage++ }

            33 -> player("You mean that Zamorakian mage in Varrock?").also { stage++ }
            34 -> player!!.dialogueInterpreter.sendDialogues(NPCs.WANTED_SAVANT_2748, FaceAnim.NEUTRAL, "Yes, he would seem a likely bet.", "Savant out.").also { stage = END_DIALOGUE }
        }
    }
}
