package content.region.desert.al_kharid.dialogue

import content.data.Quests

import content.region.desert.al_kharid.quest.feud.dialogue.AliMorrisaneTheFeudDialogue
import core.api.getVarbit
import core.api.hasRequirement
import core.api.openDialogue
import core.api.sendMessage
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.game.dialogue.Topic
import core.game.shops.Shops
import core.tools.END_DIALOGUE
import shared.consts.Vars

/**
 * Represents the Ali Morrisane dialogue.
 */
class AliMorrisaneDialogue : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            0 -> npcl(FaceAnim.FRIENDLY, "Good day and welcome back to Al Kharid.").also { stage++ }
            1 -> playerl(FaceAnim.FRIENDLY, "Hello to you too.").also { stage++ }
            2 -> npc(FaceAnim.FRIENDLY, "My name is Ali Morrisane - the greatest salesman in", "the world.").also { stage++ }
            3 -> showTopics(
                Topic("If you are, then why are you still selling goods from a stall?",5),
                Topic("So what are you selling then?", 4),
            )
            4 -> {
                end()
                if(!hasRequirement(player!!, Quests.THE_FEUD.questName)) {
                //if (getVarbit(player!!, Vars.VARBIT_QUEST_THE_FEUD_PROGRESS_334) < 1) {
                    sendMessage(player!!, "You need to make progress in 'The Feud' quest to trade with Ali Morrisane.")
                    stage = END_DIALOGUE
                    return
                }
                Shops.openId(player!!, 112)
            }
            5 -> {
                end()
                openDialogue(player!!, AliMorrisaneTheFeudDialogue())
            }
        }
    }
}
