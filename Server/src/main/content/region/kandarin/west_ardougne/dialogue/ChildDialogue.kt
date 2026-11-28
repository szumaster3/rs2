package content.region.kandarin.west_ardougne.dialogue

import content.global.skill.construction.decoration.costumeroom.Storable
import content.global.skill.construction.decoration.costumeroom.Storable.Companion.hasStorableEquipped
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs

/**
 * Represents the Child dialogue.
 */
@Initializable
class ChildDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        playerl(FaceAnim.FRIENDLY, "Hello there.")
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> if(!hasStorableEquipped(player, Storable.MournerGear)) {
                npcl(FaceAnim.CHILD_NEUTRAL, "I'm not allowed to speak with strangers.").also { stage = END_DIALOGUE }
            } else {
                npcl(FaceAnim.CHILD_NEUTRAL, "I'm not allowed to speak with Mourners.").also { stage = END_DIALOGUE }
            }
        }
        return true
    }

    override fun getIds(): IntArray = intArrayOf(
        NPCs.DSKIN_W_ARDOUNGECHILD7_6345,
        NPCs.W_ARDOUNGECHILD1_355,
        NPCs.DSKIN_W_ARDOUNGECHILD1_356,
        NPCs.W_ARDOUNGECHILD2_6334,
        NPCs.W_ARDOUNGECHILD3_6335,
        NPCs.W_ARDOUNGECHILD4_6336,
        NPCs.W_ARDOUNGECHILD5_6337,
        NPCs.W_ARDOUNGECHILD6_6338,
        NPCs.W_ARDOUNGECHILD7_6339,
        NPCs.DSKIN_W_ARDOUNGECHILD2_6340,
        NPCs.DSKIN_W_ARDOUNGECHILD3_6341,
        NPCs.DSKIN_W_ARDOUNGECHILD4_6342,
        NPCs.DSKIN_W_ARDOUNGECHILD5_6343,
    )
}
