package content.region.wilderness.dialogue

import core.api.openNpcShop
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.game.dialogue.Topic
import core.tools.END_DIALOGUE
import shared.consts.NPCs

/**
 * Represents the Fat Tony dialogue.
 */
class FatTonyDialogue : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            0 -> npc(FaceAnim.HALF_GUILTY, "Go away, I'm very busy.").also { stage++ }
            1 -> showTopics(
                Topic("Sorry to disturb you.", END_DIALOGUE, false),
                Topic(FaceAnim.HALF_ASKING,"What are you busy doing?", 2, false),
                Topic(FaceAnim.HALF_ASKING,"Have you anything to sell?", 11, false)
            )
            2 -> npc(FaceAnim.HALF_GUILTY, "I'm cooking pizzas for the people in this camp.").also { stage++ }
            3 -> npc(FaceAnim.HALF_GUILTY, "Not that these louts appreciate my gourmet cooking!").also { stage++ }
            4 -> showTopics(
                Topic(FaceAnim.HALF_ASKING,"So what is a gourmet chef doing cooking for bandits?", 5, false),
                Topic(FaceAnim.HALF_ASKING,"Can I have some pizza too?", 8, false),
                Topic(FaceAnim.NEUTRAL,"Okay, I'll leave you to it.", END_DIALOGUE, false)
            )
            5 -> npc(FaceAnim.HALF_GUILTY, "Well, I'm an outlaw. I was accused of giving the king food", "poisoning!").also { stage++ }
            6 -> npc(FaceAnim.HALF_GUILTY, "The thought of it! I think he just drank too much wine", "that night.").also { stage++ }
            7 -> npc(FaceAnim.HALF_GUILTY, "I had to flee the kingdom of Misthalin. The bandits", "give me refuge here as long as I cook for them.").also { stage = 4 }
            8 -> npc(FaceAnim.HALF_GUILTY, "Well, this pizza is really meant to be for the bandits.", "I guess I could sell you some pizza bases though.").also { stage++ }
            9 -> showTopics(
                Topic("Yes, okay.", 10, false),
                Topic("Oh, if I have to pay I don't want any.", END_DIALOGUE, false),
            )
            10 ->  {
                end()
                openNpcShop(player!!, NPCs.FAT_TONY_596)
            }
            11 -> npc(FaceAnim.HALF_GUILTY, "Well, I guess I can sell you some half-made pizzas.").also { stage = 10 }
        }
    }
}
