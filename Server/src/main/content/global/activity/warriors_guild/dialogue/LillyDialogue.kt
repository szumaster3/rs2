package content.global.activity.warriors_guild.dialogue

import core.api.openNpcShop
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.dialogue.Topic
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs

/**
 * Represents the Lilly dialogue.
 */
@Initializable
class LillyDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        npcl(FaceAnim.HALF_GUILTY, "Uh..... hi... didn't see you there. Can.... I help?")
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> player(FaceAnim.HALF_GUILTY, "Umm... do you sell potions?").also { stage++ }
            1 -> npc(FaceAnim.HALF_GUILTY, "Erm... yes. When I'm not drinking them.").also { stage++ }
            2 -> showTopics(
                Topic("I'd like to see what you have for sale.",openNpcShop(player, npc.id)),
                Topic("That's a pretty wall hanging.", 3),
                Topic("Bye!", 6)
            )
            3 -> npc(FaceAnim.HALF_GUILTY, "Do you think so? I made it my self.").also { stage++ }
            4 -> player(FaceAnim.HALF_GUILTY, "Really? Is that why there's all this cloth and dye around?").also { stage++ }
            5 -> npc(FaceAnim.HALF_GUILTY, "Yes, it's a hobby of mine when I'm.... relaxing.").also { stage++ }
            6 -> npc(FaceAnim.HALF_GUILTY, "Have fun and come back soon!").also { stage = END_DIALOGUE }
        }
        return true
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.LILLY_4294)
}
