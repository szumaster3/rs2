package content.region.misthalin.dig_site.dialogue

import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import core.tools.START_DIALOGUE
import shared.consts.NPCs

/**
 * Represents Researcher dialogue.
 */
@Initializable
class ResearcherDialogue(player: Player? = null) : Dialogue(player) {

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            START_DIALOGUE -> npcl(FaceAnim.FRIENDLY, "Hello there. What are you doing here?").also { stage++ }
            1 -> playerl(FaceAnim.FRIENDLY, "Just looking around at the moment.").also { stage++ }
            2 -> npcl(FaceAnim.FRIENDLY, "Well, feel free to talk to me should you come across anything you can't figure out.").also { stage = END_DIALOGUE }
        }
        return true
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.QIP_DIGSITE_RESEARCHER_4568)
}
