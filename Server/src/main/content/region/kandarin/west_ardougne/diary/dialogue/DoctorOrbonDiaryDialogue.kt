package content.region.kandarin.west_ardougne.diary.dialogue

import core.api.*
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.game.dialogue.IfTopic
import core.game.dialogue.Topic
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.link.diary.Diary
import core.game.node.entity.player.link.diary.DiaryType
import shared.consts.NPCs

/**
 * Represents the Ardougne diary dialogue with Doctor Orbon.
 */
class DoctorOrbonDiaryDialogue : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        npc = npc ?: NPC(NPCs.DOCTOR_ORBON_290)
        when (stage) {
            99 -> end()
            0 -> showTopics(
                IfTopic("I've completed all of the Easy tasks in my Ardougne set.", 8, Diary.canClaimLevelRewards(player!!, DiaryType.ARDOUGNE, 0)),
                IfTopic("You again? How did you get on with the cloak?", 15, Diary.canReplaceReward(player!!, DiaryType.ARDOUGNE, 0)),
                Topic("I have a question about my Achievement Diary.", 20)
            )

            20 -> showTopics(
                Topic("What is the Achievement Diary?", 2),
                Topic("What are the rewards?", 5),
                Topic("How do I claim the rewards?", 6),
                Topic("See you later.", 99)
            )

            1 -> when (buttonID) {
                1 -> player("What is the Achievement Diary?").also { stage++ }
                2 -> player("What are the rewards?").also { stage = 5 }
                3 -> player("How do I claim the rewards?").also { stage = 6 }
                4 -> end()
            }

            2 -> npcl(FaceAnim.NEUTRAL, "It's a diary that helps you keep track of particular achievements. Here on Ardougne it can help you, discover some quite useful things. Eventually, with, enough exploration, the people of Ardougne will reward you.").also { stage++ }
            3 -> options("What are the rewards?", "How do I claim the rewards?", "Sorry, I was just leaving.").also { stage++ }
            4 -> when (buttonID) {
                1 -> player("What are the rewards?").also { stage = 5 }
                2 -> player("How do I claim the rewards?").also { stage = 6 }
                3 -> end()
            }
            5 -> npcl(FaceAnim.NEUTRAL, "For completing the Ardougne set, you are presented with a cloak. This cloak will become increasingly useful with each difficulty level of the set that you complete. When you are presented with your rewards, you will be told of their uses.").also { stage = 3 }
            6 -> npc(FaceAnim.NEUTRAL, "You need to complete all of the Diary in a set of a particular difficulty, then you can claim your reward.", "Some of the Ardougne set's Diary are simple, some will require skill levels, and some might require quests to be started or completed.").also { stage++ }
            7 -> npcl(FaceAnim.FRIENDLY, "To claim the Ardougne task rewards, speak to Ardougne's town crier, Aleck in Yanille, or myself.").also { stage = 0 }
            8 -> npcl(FaceAnim.HAPPY, "Ah, that's a relief. You can wear an Ardougne cloak now - I have one for you.").also { stage++ }
            9 -> player("Yes please.").also { stage++ }
            10 -> {
                if (!Diary.flagRewarded(player!!, DiaryType.ARDOUGNE, 0)) { end(); return }
                sendDoubleItemDialogue(player!!, 15345, 15346, "Doctor Orbon hands you a cloak and an old lamp.")
                stage++
            }
            11 -> npcl(FaceAnim.NEUTRAL, "It's strange, that cloak. I'm sure it has magical powers. People like market stallholders don't seem to notice doing certain...things when you wear it.").also { stage++ }
            12 -> npcl(FaceAnim.NEUTRAL, "You'll see what I mean when you try it on. Oh, there's also a lamp here I found in the storeroom - you might find a use for it.").also { stage++ }
            13 -> player(FaceAnim.HALF_ASKING, "Thanks! What else does it do?").also { stage++ }
            14 -> npcl(FaceAnim.NEUTRAL, "Well, now, it seems to be able to teleport you to the monastery south of Ardougne, though why you'd want to visit those drunken monks, I don't know. I'm sure that there are other bonuses that I haven't heard of, but maybe you can find them.").also { stage = 0 }
            15 -> player(FaceAnim.SAD, "I lost it.").also { stage++ }
            16 -> npcl(FaceAnim.ASKING, "Lost it? Well, it just so happens I found another. Take better care of this one.").also { stage++ }
            17 -> {
                if (!Diary.grantReplacement(player!!, DiaryType.ARDOUGNE, 0)) { end(); return }
                sendItemDialogue(player!!, 15345, "Doctor Orbon hands you a cloak.")
                stage = 0
            }
        }
    }
}