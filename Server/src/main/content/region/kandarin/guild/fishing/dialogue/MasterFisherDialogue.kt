package content.region.kandarin.guild.fishing.dialogue

import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.global.Skillcape
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs

/**
 * Represents the Master Fisher dialogue.
 */
@Initializable
class MasterFisherDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        if (!Skillcape.isMaster(player, Skills.FISHING)) {
            npc(FaceAnim.FRIENDLY, "Hello, I'm afraid only the top fishers are allowed to use our", "premier fishing facilities.")
        } else {
            npc(FaceAnim.FRIENDLY, "Hello, only the top fishers are allowed to use our", "premier fishing facilities and you seem to meet the", "criteria. Enjoy!")
        }
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> if (Skillcape.isMaster(player, Skills.FISHING)) {
                player("Can I buy a Skillcape of Fishing?").also { stage = 3 }
            } else {
                player("Can you tell me about that Skillcape you're wearing?").also { stage++ }
            }
            1 -> npc(FaceAnim.FRIENDLY, "I'm happy to, my friend. This beautiful cape was", "presented to me in recognition of my skills and", "experience as a fisherman and I was asked to be the", "head of this guild at the same time. As the best").also { stage++ }
            2 -> npc(FaceAnim.FRIENDLY, "fisherman in the guild it is my duty to control who has", "access to the guild and to say who can buy similar", "Skillcapes.").also { stage = END_DIALOGUE }
            3 -> npc(FaceAnim.FRIENDLY, "Certainly! Right when you pay me 99000 coins.").also { stage++ }
            4 -> options("Okay, here you go.", "No, thanks.").also { stage++ }
            5 -> when (buttonId) {
                1 -> {
                    end()
                    if (Skillcape.purchase(player, Skills.FISHING)) {
                        npc(FaceAnim.HAPPY,"There you go! Enjoy.").also { stage = END_DIALOGUE }
                    }
                }
                2 -> player("No, thanks.").also { stage = END_DIALOGUE }
            }
        }
        return true
    }

    override fun newInstance(player: Player?): Dialogue = MasterFisherDialogue(player)

    override fun getIds(): IntArray = intArrayOf(NPCs.MASTER_FISHER_308)
}
