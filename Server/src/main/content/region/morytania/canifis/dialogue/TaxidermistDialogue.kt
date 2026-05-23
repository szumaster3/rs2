package content.region.morytania.canifis.dialogue

import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.dialogue.Topic
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs

/**
 * Represents the Taxidermist dialogue.
 */
@Initializable
class TaxidermistDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        npc(FaceAnim.FRIENDLY, "Oh, hello. Have you got something you want", "preserving?")
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> showTopics(
                Topic("Yes please", 4, false),
                Topic("Not right now", 1, false),
                Topic("What?", 2, false)
            )
            1 -> npcl(FaceAnim.HALF_GUILTY, "Well, you go kill things so I can stuff them, eh?").also { stage = END_DIALOGUE }
            2 -> npcl(FaceAnim.HALF_GUILTY, "If you bring me a monster head or a very big fish, I can preserve it for you so you can mount it in your house.").also { stage = 4 }
            3 -> npcl(FaceAnim.HALF_GUILTY, "I hear there are all sorts of exotic creatures in the Slayer Tower - I'd like a chance to stuff one of them!").also { stage = END_DIALOGUE }
            4 -> npc(FaceAnim.HAPPY, "Give it to me to look at then.").also { stage = END_DIALOGUE }
        }
        return true
    }

    override fun newInstance(player: Player?): Dialogue = TaxidermistDialogue(player)

    override fun getIds(): IntArray = intArrayOf(NPCs.TAXIDERMIST_4246)
}
