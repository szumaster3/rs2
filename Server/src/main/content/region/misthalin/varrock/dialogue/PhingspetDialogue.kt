package content.region.misthalin.varrock.dialogue

import core.api.sendNPCDialogue
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs

@Initializable
class PhingspetDialogue(player: Player? = null) : Dialogue(player) {

    private var npcId = 0

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC

        npcId =
            if (npc.id == NPCs.VC_PHINGSPET_2947)
                NPCs.VC_GRIMESQUIT_2946
            else
                NPCs.VC_PHINGSPET_2947

        npc(FaceAnim.ASKING, "What's you want?")
        stage = 0
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> sendNPCDialogue(player, npcId, "Ya what's zey want?").also { stage++ }
            1 -> npc(FaceAnim.HALF_THINKING, "I don't know it's why I is asking 'em.").also { stage++ }
            2 -> sendNPCDialogue(player, npcId, "Why do we care what zey wants let's get back to rat splattin'.").also { stage++ }
            3 -> npc("Ya good plan.").also { stage++ }
            4 -> player(FaceAnim.ASKING, "Em... Ahemm.. Excuse me?").also { stage = END_DIALOGUE }
        }
        return true
    }

    override fun newInstance(player: Player?): Dialogue = PhingspetDialogue(player)

    override fun getIds(): IntArray = intArrayOf(NPCs.VC_PHINGSPET_2947, NPCs.VC_GRIMESQUIT_2946)
}