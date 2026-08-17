package content.region.other.entrana.dialogue

import core.api.openNpcShop
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.dialogue.Topic
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs

/**
 * Represents the Frincos dialogue.
 */
@Initializable
class FrincosDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        npcl(FaceAnim.HALF_ASKING, "Hello, how can I help you?").also { stage++ }
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> showTopics(
                Topic("What are you selling?", openNpcShop(player!!, NPCs.FRINCOS_578)),
                Topic(FaceAnim.NEUTRAL, "You can't; I'm beyond help.", END_DIALOGUE, false),
                Topic(FaceAnim.NEUTRAL, "I'm okay, thank you.", END_DIALOGUE, false),
            )
        }
        return true
    }

    override fun newInstance(player: Player?): Dialogue = FrincosDialogue(player)

    override fun getIds(): IntArray = intArrayOf(NPCs.FRINCOS_578)

}
