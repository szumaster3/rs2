package content.region.desert.quest.deserttreasure

import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs

/**
 * Represents Ice Troll dialogue.
 */
@Initializable
class IceTrollDialogue(player: Player? = null) : Dialogue(player) {
    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when(stage){
            0 -> npc(FaceAnim.OLD_LAUGH1, "Hur hur hur!", "Well look here, a puny fleshy human!").also { stage++ }
            1 -> npc(FaceAnim.OLD_LAUGH1, "You should beware of the icy wind that runs through", "this valley, it will bring a fleshy like you to a cold end", "indeed!").also{ stage= END_DIALOGUE }
        }
        return false
    }

    override fun newInstance(player: Player?): Dialogue {
        return IceTrollDialogue(player)
    }

    override fun getIds(): IntArray {
        return intArrayOf(NPCs.ICE_TROLL_1935)
    }

}