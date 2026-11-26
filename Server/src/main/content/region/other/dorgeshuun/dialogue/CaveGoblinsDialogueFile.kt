package content.region.other.dorgeshuun.dialogue

import content.data.Quests

import content.data.LightSources
import core.api.addItemOrDrop
import core.api.anyInInventory
import core.api.isQuestComplete
import core.api.sendMessage
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.tools.END_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs

class CaveGoblinsDialogueFile : DialogueFile() {

    private var convsersation = -1

    override fun handle(componentID: Int, buttonID: Int) {
        val p = player ?: return
        npc = NPC(NPCs.CAVE_GOBLIN_MINER_2069)

        if (!isQuestComplete(p, Quests.THE_LOST_TRIBE.questName)) {
            if (LightSources.hasActiveLightSource(p)) {
                npcl(FaceAnim.OLD_NORMAL, "Watch out! You don't want to let a naked flame near swamp gas! Look out for the warning marks.").also { stage = END_DIALOGUE }
                return
            }
            if (anyInInventory(p, Items.LIT_BLACK_CANDLE_32, Items.LIT_CANDLE_33)) {
                npcl(FaceAnim.OLD_NORMAL, "Don't shine that thing in my eyes!").also { stage = END_DIALOGUE }
                return
            }
            sendMessage(p, "Cave goblin is not interested in talking.")
            return
        }

        if (stage == 0) {
            convsersation = (0..5).random()
        }

        when (convsersation) {

            0 -> when (stage) {
                0 -> npcl(FaceAnim.OLD_NORMAL, "What are you doing down here without a lamp?").also { stage++ }
                1 -> npcl(FaceAnim.OLD_NORMAL, "Here, I have a spare torch.").also { stage++ }
                2 -> {
                    addItemOrDrop(p, Items.LIT_TORCH_594)
                    stage = END_DIALOGUE
                }
            }

            1 -> when (stage) {
                0 -> npcl(FaceAnim.OLD_NORMAL, "Where did you come from?").also { stage++ }
                1 -> playerl(FaceAnim.NEUTRAL, "From above ground.").also { stage++ }
                2 -> npcl(FaceAnim.OLD_NORMAL, "Above ground? Where is that?").also { stage++ }
                3 -> playerl(FaceAnim.NEUTRAL, "Out of caves, in the open air...").also { stage++ }
                4 -> npcl(FaceAnim.OLD_NORMAL, "Ick. Sounds horrible.").also { stage = END_DIALOGUE }
            }

            2 -> when (stage) {
                0 -> npcl(FaceAnim.OLD_NORMAL, "Don't tread on my feet!").also { stage++ }
                1 -> playerl(FaceAnim.NEUTRAL, "I'm not going to tread on your feet.").also { stage = END_DIALOGUE }
            }

            3 -> when (stage) {
                0 -> npcl(FaceAnim.OLD_NORMAL, "Beware of swamp gas! Look out for the warning marks!").also { stage++ }
                1 -> playerl(FaceAnim.NEUTRAL, "Um, thanks.").also { stage = END_DIALOGUE }
            }

            4 -> when (stage) {
                0 -> playerl(FaceAnim.NEUTRAL, "Hello, how are you?").also { stage++ }
                1 -> npcl(FaceAnim.OLD_NORMAL, "I'm a bit worried about humans these days.").also { stage++ }
                2 -> npcl(FaceAnim.OLD_NORMAL, "Present company excluded, of course!").also { stage = END_DIALOGUE }
            }

            5 -> when (stage) {
                0 -> npcl(FaceAnim.OLD_NORMAL, "Nice weather we're having!").also { stage++ }
                1 -> playerl(FaceAnim.NEUTRAL, "But you live underground...").also { stage++ }
                2 -> npcl(FaceAnim.OLD_NORMAL, "Yes, it's always nice!").also { stage = END_DIALOGUE }
            }
        }
    }
}