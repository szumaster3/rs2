package content.region.kandarin.seers_village.dialogue

import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import shared.consts.NPCs

@Initializable
class CamelotGuardDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        npc(FaceAnim.HALF_GUILTY, "Welcome to the Seer's Village courthouse. Court", "is not in session today, so you're not allowed downstairs.")
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> end()
        }
        return true
    }

    override fun newInstance(player: Player?): Dialogue = CamelotGuardDialogue(player)

    override fun getIds(): IntArray = intArrayOf(NPCs.KR_COURT_HOUSE_GUARD_01_6183, NPCs.KR_COURT_HOUSE_GUARD_02_6184)
}
