package content.region.kandarin.east_ardougne.dialogue

import content.data.Quests

import core.api.isQuestComplete
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs

/**
 * Represents the Farmer Brumty dialogue.
 */
@Initializable
class FarmerBrumtyDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        if (!isQuestComplete(player, Quests.SHEEP_HERDER.questName)) {
            player("Hello there.")
        } else {
            player(FaceAnim.HALF_GUILTY, "Hello there. Sorry about your sheep...").also { stage = 2 }
        }
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> npcl(FaceAnim.HALF_GUILTY, "I have all the bad luck. My sheep all run off somewhere, and then those mourners tell me they're infected!").also { stage++ }
            1 -> playerl(FaceAnim.HALF_GUILTY, "Well, I hope things start to look up for you.").also { stage = END_DIALOGUE }
            2 -> npcl(FaceAnim.HALF_GUILTY, "That's alright. It had to be done for the sake of the town. I just hope none of my other livestock get infected.").also { stage = END_DIALOGUE }
        }
        return true
    }

    override fun newInstance(player: Player?): Dialogue = FarmerBrumtyDialogue(player)

    override fun getIds(): IntArray = intArrayOf(NPCs.FARMER_BRUMTY_291)
}
