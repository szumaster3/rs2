package content.region.tirannwn.dialogue

import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.dialogue.Topic
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs

/**
 * Represents the Tyras guard dialogue.
 */
@Initializable
class TyrasGuardDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        npc(FaceAnim.FRIENDLY, "What is it?")
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> showTopics(
                Topic("What's going on around here?", 10, false),
                Topic("Do you know what's south of here?", 20, false),
                Topic("I'll leave you alone.", END_DIALOGUE, false)
            )
            10 -> npcl(FaceAnim.NEUTRAL, "Sorry, I shouldn't give out sensitive information to civilians. You should go to General Hining, in the camp along the road.").also { stage = 11 }
            11 -> showTopics(
                Topic("Do you know what's south of here?", 20, false),
                Topic("Okay, thanks.", END_DIALOGUE, false)
            )
            20 -> npcl(FaceAnim.NEUTRAL, "No. We sent a scouting party in that direction, when we first established our camp. Some of the men got lost in the swamps. Eventually we listed them as dead.").also { stage++ }
            21 -> npcl(FaceAnim.NEUTRAL, "Then suddenly they returned, with a wild gleam in their eyes, raving about gods and snakes and all kinds of madness.").also { stage++ }
            22 -> npcl(FaceAnim.NEUTRAL, "We had to drive them out, in case their condition infected the rest of the troops. Their wives will be given a full widow pension when we return home.").also { stage++ }
            23 -> npcl(FaceAnim.NEUTRAL, "General Hining concluded that, whatever is down there, it's not affiliated with any of the elf factions, and it should be left alone.").also { stage++ }
            24 -> player(FaceAnim.FRIENDLY, "Thank you.").also { stage = 0 }
        }
        return true
    }

    override fun getIds(): IntArray {
        return intArrayOf(NPCs.TYRAS_GUARD_1203)
    }
}