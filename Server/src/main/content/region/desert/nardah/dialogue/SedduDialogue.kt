package content.region.desert.nardah.dialogue

import core.api.openNpcShop
import core.game.dialogue.DialogueFile
import core.tools.END_DIALOGUE
import shared.consts.NPCs

/**
 * Represents the Seddu dialogue.
 */
class SedduDialogue : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            0 -> npcl("I buy and sell adventurer's equipment, do you want to trade?").also { stage++ }
            1 -> options("Yes, please", "No, thanks").also { stage++ }
            2 -> when (buttonID) {
                1 -> {
                    end()
                    openNpcShop(player!!, NPCs.SEDDU_3038)
                }
                2 -> player("No, thanks.").also { stage = END_DIALOGUE }
            }
        }
    }
}
