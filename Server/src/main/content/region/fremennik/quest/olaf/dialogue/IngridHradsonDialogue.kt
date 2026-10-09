package content.region.fremennik.quest.olaf.dialogue

import core.api.*
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Quests
import shared.consts.Vars

/**
 * Represents the Ingrid Hradson dialogue.
 */
@Initializable
class IngridHradsonDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg objects: Any?): Boolean {
        npc = objects[0] as NPC
        if (!isQuestComplete(player, Quests.THE_FREMENNIK_TRIALS)) {
            npcl(FaceAnim.ANNOYED, "Outerlander, I have work to be getting on with... Please stop bothering me.").also { stage = END_DIALOGUE }
        } else if (isQuestComplete(player, Quests.THE_FREMENNIK_TRIALS) && getQuestStage(player, Quests.OLAFS_QUEST) == 0) {
            npc(FaceAnim.FRIENDLY, "Good afternoon! How do you like our village?").also { stage = 10 }
        } else if (isQuestComplete(player, Quests.THE_FREMENNIK_TRIALS) && getQuestStage(player, Quests.OLAFS_QUEST) == 1) {
            npc(FaceAnim.ASKING, "Hello again! Have you any word from my husband?").also { stage = 12 }
        } else {
            player("Excuse me, but are you Ingrid? The same Ingrid that","is married to Olaf Hradson?")
        }
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> npc(FaceAnim.HALF_ASKING, "Yes I am. Why do you ask?").also { stage = 2 }
            2 -> player("I am a friend of your husband. He sent me here with","a gift for you.").also { stage++ }
            3 -> player("An expensive, exotic gift from the far-flung and tropical","shores of the Island of Obscurity.").also { stage++ }
            4 -> npc(FaceAnim.FRIENDLY, "How wonderful! I assumed from the way the big oaf", "always referred to the rigging as 'big strings' that he", "couldn't navigate his way out of a soggy paper page.", "So, can I see the gift he sent?").also { stage++ }
            // If the player has the crude carving.
            5 -> if(removeItem(player, Items.CRUDE_CARVING_11032)) {
                player("Certainly! Here it is. He sends his best wishes along", "with it.")
                stage++
            } else {
            // If the player lost the crude carving.
                player("Well, not at the moment. I seem to have mislaid it.")
                stage = 14
            }
            6 -> npc(FaceAnim.FRIENDLY, "I see. It looks like a log with 'Olaf was here' carved in","it.").also { stage++ }
            7 -> player("It's tribal. Very tribal! I watched the natives carve it","myself.").also { stage++ }
            8 -> {
                npc(FaceAnim.FRIENDLY, "Well, since you have come such a long way, you can","have this. I baked it this morning, so it's still nice and","fresh.")
                addItem(player, Items.BREAD_2309)
                setVarbit(player, Vars.VARBIT_QUEST_OLAFS_QUEST_PROGRESS_3534, 2, true)
                stage++

            }
            9 -> player("Oh, thank you! Enjoy your Obscurian Tribal", "Artifact(tm). It has a million-and-one uses!").also { stage = END_DIALOGUE }

            10 -> playerl(FaceAnim.FRIENDLY, "It's lovely. You have a fine collection of goats.").also { stage++ }
            11 -> npcl(FaceAnim.FRIENDLY, "We polish them every day to get them nice and clean.").also { stage = END_DIALOGUE }
            12 -> playerl(FaceAnim.HALF_GUILTY, "Err, no, not yet. It takes a while for the messages to reach me you know.").also { stage++ }
            13 -> npcl(FaceAnim.FRIENDLY, "Well, when you do, tell him we'll be more than happy to see him again.").also { stage = END_DIALOGUE }

            14 -> npcl(FaceAnim.SAD, "Oh my...I was a little doubtful that you were telling the truth, but since you've lost something valuable, you must be a friend of my husbands.").also { stage++ }
            15 -> npcl(FaceAnim.FRIENDLY, "Come and see me again when you manage to keep a hold of it you poor, poor man.").also { stage = END_DIALOGUE }
        }

        return true
    }

    override fun getIds(): IntArray = intArrayOf(
        NPCs.INGRID_HRADSON_3696
    )
}