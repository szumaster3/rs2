package content.region.other.keldagrim.dialogue

import core.api.sendNPCDialogue
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs

/**
 * Represents the Gnome traveler dialogue.
 */
@Initializable
class GnomeTravelerDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        if(npc.id == NPCs.GNOME_TRAVELLER_2139) {
            player(FaceAnim.HALF_ASKING,"Hello there! What are you doing in the city?")
        } else {
            player("Hello there mister gnome!")
            stage = 7
        }
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            // Male
            0 -> npcl(FaceAnim.OLD_DEFAULT, "Why do you want to know?").also { stage++ }
            1 -> playerl(FaceAnim.FRIENDLY, "I was just wondering, I thought this was a dwarven city, not a gnome city.").also { stage++ }
            2 -> npcl(FaceAnim.OLD_DEFAULT, "It's not a human city either, but you're here all the same.").also { stage++ }
            3 -> player(FaceAnim.FRIENDLY, "Fair point.").also { stage++ }
            4 -> npcl(FaceAnim.OLD_DEFAULT, "Anyway, we're here to do some business with one of the companies of the Consortium. Getting supplies for our people back at, er, the Grand Tree.").also { stage++ }
            5 -> playerl(FaceAnim.ASKING, "What kind of special supplies do the dwarves have that you need then?").also { stage++ }
            6 -> npcl(FaceAnim.OLD_DEFAULT, "Oh, nothing special, nothing special I assure you... it's just that the dwarves have everything in large supplies.").also { stage = END_DIALOGUE }
            // Female
            7 -> npcl(FaceAnim.OLD_DEFAULT, "Miss gnome to you, thank you.").also { stage++ }
            8 -> playerl(FaceAnim.ASKING, "Oh, sorry about that... So what are you doing in fair Keldagrim?").also { stage++ }
            9 -> npcl(FaceAnim.OLD_DEFAULT, "Fair? Hardly...").also { stage++ }
            10 -> npcl(FaceAnim.OLD_DEFAULT, "We are the companions of the leader of our expedition. We have come here to bargain for much needed supplies for our people.").also { stage++ }
            11 -> player(FaceAnim.HALF_ASKING, "Oh yes, where are you from?").also { stage++ }
            12 -> npcl(FaceAnim.OLD_DEFAULT, "Ehm... from Tree Gnome Village.").also { stage++ }
            13 -> sendNPCDialogue(player, NPCs.GNOME_TRAVELLER_2139, "The Grand Tree!",FaceAnim.OLD_DEFAULT).also { stage++ }
            14 -> npcl(FaceAnim.OLD_DEFAULT, "Oh yes, sorry, that's what I meant to say.").also { stage = END_DIALOGUE }
        }
        return true
    }

    override fun newInstance(player: Player?): Dialogue = GnomeTravelerDialogue(player)

    override fun getIds(): IntArray = intArrayOf(
        NPCs.GNOME_TRAVELLER_2138,
        NPCs.GNOME_TRAVELLER_2139
    )
}
