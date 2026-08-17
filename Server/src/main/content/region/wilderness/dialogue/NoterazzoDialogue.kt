package content.region.wilderness.dialogue

import core.api.openNpcShop
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.game.dialogue.Topic
import core.tools.END_DIALOGUE
import shared.consts.NPCs

/**
 * Represents the Noterazzo dialogue.
 */
class NoterazzoDialogue : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            0 -> npcl(FaceAnim.HALF_ASKING, "Hey, wanna trade? I'll give the best deals you can find.").also { stage++ }
            1 -> showTopics(
                Topic("Yes please.", 2),
                Topic("No thanks.", END_DIALOGUE),
                Topic(FaceAnim.HALF_ASKING,"How can you afford to give such good deals?", 3)
            )
            2 -> end().also { openNpcShop(player!!, NPCs.NOTERAZZO_597) }
            3 -> npc(FaceAnim.NEUTRAL, "The general stores in Asgarnia and Misthalin are heavily", "taxed.").also { stage++ }
            4 -> npc(FaceAnim.NEUTRAL, "It really makes it hard for them to run an effective", "business. For some reason taxmen don't visit my store.").also { stage = END_DIALOGUE }
        }
    }
}
