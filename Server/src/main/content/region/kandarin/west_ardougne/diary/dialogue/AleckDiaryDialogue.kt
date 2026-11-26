package content.region.kandarin.west_ardougne.diary.dialogue

import core.api.*
import content.data.GameAttributes
import content.data.Quests
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.game.dialogue.IfTopic
import core.game.dialogue.Topic
import core.game.node.entity.player.link.diary.Diary
import core.game.node.entity.player.link.diary.DiaryType
import core.tools.END_DIALOGUE

class AleckDiaryDialogue : DialogueFile() {
    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            99 -> end()
            0 -> showTopics(
                IfTopic("Are there any more tasks?", 20, Diary.hasCompletedLevel(player!!, DiaryType.ARDOUGNE, 2) && player!!.achievementDiaryManager.getDiary(DiaryType.ARDOUGNE)?.isStarted(3) == false),
                IfTopic("I have completed the Elite tasks.", 21, Diary.canClaimLevelRewards(player!!, DiaryType.ARDOUGNE, 3)), 
                IfTopic("How's the cloak?", 14, Diary.canReplaceReward(player!!, DiaryType.ARDOUGNE, 2)),
                IfTopic("I've completed all of the Hard Tasks in the Ardougne set.", 1, Diary.canClaimLevelRewards(player!!, DiaryType.ARDOUGNE, 2)),
                Topic("Tell me about the Achievement Diary.", 7),
                IfTopic("I'd like to change my Watchtower Teleport point.", 18, isDiaryComplete(player!!, DiaryType.ARDOUGNE, 2)),
            )
            1 -> npcl(FaceAnim.FRIENDLY, "Oh! So you have, well done! I have a lovely cloak here for you.").also { stage++ }
            2 -> {
                if (!Diary.flagRewarded(player!!, DiaryType.ARDOUGNE, 2)) { end(); return }
                sendDoubleItemDialogue(player!!, 15349, 15350, "Aleck hands you a cloak and a lamp.")
                stage++
            }
            3 -> npcl(FaceAnim.FRIENDLY, "There you are. I think you'll find you can now, umm, shall we say 'expropriate' things around the world more easily while wearing that cloak.").also { stage++ }
            4 -> npcl(FaceAnim.FRIENDLY, "Also, you may find Cromperty can help you out with your runecrafting. There may be other effects, too; it's quite an enigma.").also { stage++ }
            5 -> npcl(FaceAnim.HAPPY, "Thanks!-").also { stage++ }
            6 -> npcl(FaceAnim.HAPPY, "You're welcome.").also { stage = END_DIALOGUE }
            7 -> showTopics(
                Topic("What is the Achievement Diary?", 8),
                Topic("What are the rewards?", 11),
                Topic("How do I claim the rewards?", 12),
                Topic("See you later.", 99)
            )
            8 -> npcl(FaceAnim.NEUTRAL, "It's a diary that helps you keep track of particular achievements. Here on Ardougne it can help you, discover some quite useful things. Eventually, with, enough exploration, the people of Ardougne will reward you.").also { stage++ }
            9 -> options("What are the rewards?", "How do I claim the rewards?", "Sorry, I was just leaving.").also { stage++ }
            10 -> when (buttonID) {
                1 -> player("What are the rewards?").also { stage = 11 }
                2 -> player("How do I claim the rewards?").also { stage = 12 }
                3 -> end()
            }
            11 -> npcl(FaceAnim.NEUTRAL, "For completing the Ardougne set, you are presented with a cloak. This cloak will become increasingly useful with each difficulty level of the set that you complete. When you are presented with your rewards, you will be told of their uses.").also { stage = 9 }
            12 -> npc(FaceAnim.NEUTRAL, "You need to complete all of the Diary in a set of a particular difficulty, then you can claim your reward.", "Some of the Ardougne set's Diary are simple, some will require skill levels, and some might require quests to be started or completed.").also { stage++ }
            13 -> npcl(FaceAnim.FRIENDLY, "To claim your Ardougne set rewards, speak to Doctor Orbon in East Ardougne church, Aleck in Yanille, or myself.").also { stage = 7 }
            14 -> player(FaceAnim.SAD, "I lost it.").also { stage++ }
            15 -> npcl(FaceAnim.THINKING, "Really? Here, I found another in the storeroom.").also { stage++ }
            16 -> npcl(FaceAnim.NEUTRAL, "Never mind, I have another. Here...").also { stage++ }
            17 -> {
                if (!Diary.grantReplacement(player!!, DiaryType.ARDOUGNE, 2)) { end(); return }
                sendItemDialogue(player!!, 15349, "Aleck hands you another cloak.")
                stage = 7
            }
            18 -> {
                if (!isDiaryComplete(player!!, DiaryType.ARDOUGNE, 2) || !hasRequirement(player!!, Quests.WATCHTOWER.questName)) { end(); return }
                val alternate = getAttribute(player!!, GameAttributes.ATTRIBUTE_WATCHTOWER_ALT_TELE, false)
                sendOptions(player!!, "Toggle Watchtower Teleport to ${if (alternate) "Watchtower" else "centre of Yanille"}?", "Yes", "No")
                stage = 19
            }
            20 -> {
                val p = player ?: return
                val diary = p.achievementDiaryManager.getDiary(DiaryType.ARDOUGNE) ?: return
                if (Diary.hasCompletedLevel(p, DiaryType.ARDOUGNE, 2)) {
                    diary.levelStarted[3] = true
                    diary.drawStatus(p)
                    npcl(FaceAnim.FRIENDLY, "There are five more tasks for you to complete.")
                    stage = END_DIALOGUE
                } else end()
            }
            21 -> {
                val p = player ?: return
                if (Diary.flagRewarded(p, DiaryType.ARDOUGNE, 3)) {
                    sendItemDialogue(p, 15351, "Aleck hands you four experience lamps.")
                    stage = END_DIALOGUE
                } else end()
            }
            19 -> {
                if (buttonID == 1 && isDiaryComplete(player!!, DiaryType.ARDOUGNE, 2) && hasRequirement(player!!, Quests.WATCHTOWER.questName)) {
                    val alternate = !getAttribute(player!!, GameAttributes.ATTRIBUTE_WATCHTOWER_ALT_TELE, false)
                    setAttribute(player!!, GameAttributes.ATTRIBUTE_WATCHTOWER_ALT_TELE, alternate)
                    sendMessage(player!!, "Watchtower Teleport will now teleport you to the ${if (alternate) "centre of Yanille" else "Watchtower"}.")
                }
                end()
            }
        }
    }
}
