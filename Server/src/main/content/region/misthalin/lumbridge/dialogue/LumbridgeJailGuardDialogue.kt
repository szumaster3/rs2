package content.region.misthalin.lumbridge.dialogue

import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs

/**
 * Represents the Jail guard (Lumbridge) dialogue.
 */
@Initializable
class LumbridgeJailGuardDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        player(FaceAnim.HALF_GUILTY, "Hello.")
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> npc(FaceAnim.HALF_GUILTY, "Why are you here ? You must leave at once.").also { stage++ }
            1 -> player(FaceAnim.HALF_GUILTY, "Err.. Okay.").also { stage = END_DIALOGUE }
        }
        return true
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.JAILGUARD_917, NPCs.JAILGUARD_TWO_PATROL_447, NPCs.JAILGUARD_THREE_PATROL_448, NPCs.JAILGUARD_FOUR_PATROL_449)
}
