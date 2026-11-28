package content.region.misthalin.barbarian_village.dialogue

import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs

/**
 * Represents the Barbarian dialogue.
 */
@Initializable
class BarbarianDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        npc(FaceAnim.HALF_ASKING, "What do you want, Outlander?")
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> options("Let's fight!", "Goodbye.").also { stage++ }
            1 -> when (buttonId) {
                1 -> player(FaceAnim.HALF_GUILTY, "Let's fight!").also { stage++ }
                2 -> player(FaceAnim.HALF_GUILTY, "Goodbye.").also { stage = END_DIALOGUE }
            }
            2 -> end().also { npc.attack(player) }
        }
        return true
    }

    override fun newInstance(player: Player?): Dialogue = BarbarianDialogue(player)

    override fun getIds(): IntArray = intArrayOf(
        NPCs.BARBARIAN_12,
        NPCs.FAI_BARBARIAN_1_3246,
        NPCs.FAI_BARBARIAN_2_3247,
        NPCs.FAI_BARBARIAN_3_3248,
        NPCs.FAI_BARBARIAN_4_3249,
        NPCs.FAI_BARBARIAN_5_3250,
        NPCs.FAI_BARBARIAN_6_3251,
        NPCs.FAI_BARBARIAN_7_3252,
        NPCs.FAI_BARBARIAN_8_3253,
        NPCs.FAI_BARBARIAN_10_3255,
        NPCs.FAI_BARBARIAN_11_3256,
        NPCs.FAI_BARBARIAN_12_3257,
        NPCs.FAI_BARBARIAN_13_3258,
        NPCs.FAI_BARBARIAN_14_3259,
        NPCs.FAI_BARBARIAN_FEMALE_1_3260,
        NPCs.FAI_BARBARIAN_FEMALE_2_3261,
        NPCs.FAI_BARBARIAN_FEMALE_3_3262,
        NPCs.FAI_BARBARIAN_FEMALE_4_3263,
        NPCs.BARBARIANWOMAN_VARROCK_5909,
    )
}
