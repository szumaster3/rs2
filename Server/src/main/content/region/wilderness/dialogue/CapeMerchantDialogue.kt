package content.region.wilderness.dialogue

import core.api.openNpcShop
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.game.dialogue.Topic
import core.tools.END_DIALOGUE

/**
 * Represents the Cape Merchant dialogue.
 */
class CapeMerchantDialogue : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            0 -> npc(FaceAnim.HAPPY, "Hello there, are you interested in buying one of my", "special capes?").also { stage++ }
            1 -> showTopics(
                Topic(FaceAnim.ASKING, "What's so special about your capes?", 2),
                Topic("Yes please!", 6, true),
                Topic(FaceAnim.HALF_GUILTY, "No thanks.", END_DIALOGUE)
            )
            2 -> npc(FaceAnim.HALF_THINKING, "Ahh well they make it less likely that you'll accidentally", "attack anyone wearing the same cape as you and easier", "to attack everyone else. They also make it easier to", "distinguish people who're wearing the same cape as you").also { stage++ }
            3 -> npc(FaceAnim.FRIENDLY, "from everyone else. They're very useful when out in", "the wilderness with friends or anyone else you don't", "want to harm.").also { stage++ }
            4 -> npc(FaceAnim.HALF_ASKING, "So would you like to buy one?").also { stage++ }
            5 -> showTopics(
                Topic("Yes please!", 6, true),
                Topic(FaceAnim.HALF_GUILTY, "No thanks.", END_DIALOGUE)
            )
            6 -> {
                end()
                openNpcShop(player!!, npc!!.id)
            }
        }
    }
}
