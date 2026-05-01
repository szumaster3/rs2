package content.region.kandarin.gnome_stronghold.quest.makinghistory.dialogue

import content.region.kandarin.gnome_stronghold.quest.makinghistory.MHUtils
import core.api.*
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.tools.END_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Quests

/**
 * Represents the Silver merchant dialogue.
 */
class SilverMerchantDialogueFile(override var stage: Int) : DialogueFile() {
    override fun handle(
        componentID: Int,
        buttonID: Int,
    ) {
        npc = NPC(NPCs.SILVER_MERCHANT_569)
        when (stage) {
            0 -> playerl(FaceAnim.HALF_ASKING,"Are you Erin?").also { stage++ }
            1 -> npc(FaceAnim.NEUTRAL,"That I am. What do you want? You realise I'm", "working here.").also { stage++ }
            2 -> player(FaceAnim.CALM_TALK,"This shouldn't take too long. I just wanted to ask you a", "bit about your great Grandfather, the one who lived in", "the outpost?").also { stage++ }
            3 -> npcl(FaceAnim.ASKING, "What's it to you?").also { stage++ }
            4 -> player(FaceAnim.NEUTRAL,"I'm doing some research for a man called Jorral.", "Apparently the King is going to make the outpost into", "his very own alchemists' lab.").also { stage++ }
            5 -> npc(FaceAnim.NEUTRAL,"That can only cause chaos! Well, my great Grandfather", "lived and died there according to my mother, but even", "she knows very little about him.").also { stage++ }
            6 -> playerl(FaceAnim.NEUTRAL,"I see.").also { stage++ }
            7 -> npc(FaceAnim.NEUTRAL,"The only thing I have of his is a key. It's a strange", "key, it changes temperature by itself as you walk", "around. I'm afraid I don't know what it's for, though.").also { stage++ }
            8 -> playerl(FaceAnim.NEUTRAL, "No idea at all?").also { stage++ }
            9 -> npc(FaceAnim.NEUTRAL,"Well, I imagine it's to some sort of chest of his", "belongings as it's too small for a door. Perhaps if you", "find some of his belongings you will discover some clues", "amongst them.").also { stage++ }
            10 -> playerl(FaceAnim.NEUTRAL,"It's better than nothing. Will you lend it to me then?").also { stage++ }
            11 -> npc(FaceAnim.NEUTRAL, "Why not, I can't use it anyway. Try feeling its change", "in temperature as you walk around.").also { stage++ }
            12 -> {
                end()
                addItemOrDrop(player!!, Items.ENCHANTED_KEY_6754, 1)
                setVarbit(player!!, MHUtils.ERIN_PROGRESS, 1, true)
                setQuestStage(player!!, Quests.MAKING_HISTORY, 2)
            }

            13 -> playerl(FaceAnim.NEUTRAL, "I haven't found anything yet.").also { stage++ }
            14 -> npcl(FaceAnim.HALF_ASKING, "Keep trying. Have you noticed that the key changes temperature as you walk around?").also { stage++ }
            15 -> if (inInventory(player!!, Items.ENCHANTED_KEY_6754)) {
                player(FaceAnim.HALF_ASKING, "I was just about to. Do you want the key back?").also { stage = 18 }
            } else {
                playerl(FaceAnim.NEUTRAL, "That's kind of hard because I lost the key.").also { stage++ }
            }

            16 -> npcl(FaceAnim.FRIENDLY, "I was waiting for you to say that...because I just found it! Take it and don't lose it!").also { stage++ }
            17 -> if (freeSlots(player!!) == 0) {
                end()
                npcl(FaceAnim.NEUTRAL, "Wait, look how much you're carrying! You can't carry this too.")
            } else {
                end()
                addItemOrDrop(player!!, Items.ENCHANTED_KEY_6754, 1)
            }

            18 -> npc(FaceAnim.NEUTRAL, "Keep it, it may come in handy.").also { stage = END_DIALOGUE }
            19 -> playerl(FaceAnim.HAPPY, "I found a chest!").also { stage++ }
            20 -> npcl(FaceAnim.NEUTRAL, "I wonder what is inside.").also { stage = END_DIALOGUE }
            21 -> playerl(FaceAnim.HALF_ASKING, "Would you like to see the journal I have found?").also { stage++ }
            22 -> npcl(FaceAnim.NEUTRAL, "Let's have a look.").also { stage++ }
            23 -> sendItemDialogue(player!!, Items.JOURNAL_6755, "Erin reads the journal.").also { stage++ }
            24 -> npcl(FaceAnim.HALF_THINKING, "Very interesting. Best you show it to Jorral at the outpost.").also { stage = END_DIALOGUE }
        }
    }
}
