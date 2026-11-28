package content.region.kandarin.gnome_stronghold.quest.eaglepeak

import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs

@Initializable
class NickolausDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        player("Hello.")
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> npcl(FaceAnim.NEUTRAL, "Shhhh! You'll scare the kebbits.").also { stage = END_DIALOGUE }
        }
        return true
    }

    override fun newInstance(player: Player?): Dialogue = NickolausDialogue(player)

    override fun getIds(): IntArray = intArrayOf(5125,NPCs.EAGLEPEAK_NICKOLAUS_CRAWL_5126,NPCs.EAGLEPEAK_NICKOLAUS_SHOUT_5127,NPCs.EAGLEPEAK_NICKOLAUS_NORMAL_5128,NPCs.EAGLEPEAK_NICKOLAUS_DISGUISE_5129)
}