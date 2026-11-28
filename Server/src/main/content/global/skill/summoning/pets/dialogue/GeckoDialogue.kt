package content.global.skill.summoning.pets.dialogue

import core.api.getRegionBorders
import core.api.inBorders
import core.api.inZone
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs

@Initializable
class GeckoDialogue(player: Player? = null) : Dialogue(player) {

    private val babyGecko = intArrayOf(
        NPCs.LORE_BABY_GECKO_1_6915,
        NPCs.LORE_BABY_GECKO_2_7277,
        NPCs.LORE_BABY_GECKO_3_7278,
        NPCs.LORE_BABY_GECKO_4_7279,
        NPCs.LORE_BABY_GECKO_5_7280
    )

    private val adultGecko = intArrayOf(
        NPCs.LORE_GECKO_1_6916,
        NPCs.LORE_GECKO_2_7281,
        NPCs.LORE_GECKO_3_7282,
        NPCs.LORE_GECKO_4_7283,
        NPCs.LORE_GECKO_5_7284
    )

    private var branch = 0

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC

        branch = (0..5).random()
        stage = 0

        when (branch) {
            0 -> { npc(FaceAnim.CHILD_NORMAL, "Moop, Gecko presko!", "(You're nice to me!)"); stage = END_DIALOGUE }
            1 -> npc(FaceAnim.CHILD_NORMAL, "Gronk, Gecko gronk?", "(When can we go back to the jungle?)")
            2 -> npc(FaceAnim.CHILD_NORMAL, "Hiss gronk. Gecko?", "(Can you carry me?)")
            3 -> npc(FaceAnim.CHILD_NORMAL, "Hiss, gecko. Gecko!", "(I want to rest.)")
            4 -> npc(FaceAnim.CHILD_NORMAL, "Meep, gecko! Gecko!", "(Happy happy!)")
            5 -> {
                if (inBorders(player, getRegionBorders(12598)) || inZone(player, "Desert Zone")) {
                    npc(FaceAnim.CHILD_NORMAL, "Hss! Hsss! Gecko! Hsss!", "(Too hot! Let's go home!)")
                } else {
                    npc(FaceAnim.CHILD_NORMAL, "Meep, gecko! Gecko!", "(Happy happy!)")
                }
            }
        }
        return true
    }

    override fun handle(componentID: Int, buttonID: Int): Boolean {
        when (branch) {
            1 -> when (stage) {
                0 -> { playerl(FaceAnim.FRIENDLY, "When I have a reason to go there."); stage = END_DIALOGUE }
            }
            2 -> when (stage) {
                0 -> { playerl(FaceAnim.FRIENDLY, "Well if you are tired I suppose I could..."); stage++ }
                1 -> { npc(FaceAnim.CHILD_NORMAL, "Gecko! Gronk gronk.", "(It's hard to keep up with you!)"); stage = END_DIALOGUE }
            }
            3 -> when (stage) {
                0 -> { playerl(FaceAnim.FRIENDLY, "I think I could do with a short rest, too."); stage = END_DIALOGUE }
            }
            4 -> when (stage) {
                0 -> { playerl(FaceAnim.FRIENDLY, "Hehe, you seem full of energy today."); stage = END_DIALOGUE }
            }
            5 -> when (stage) {
                0 -> { playerl(FaceAnim.FRIENDLY, "Don't worry, we'll be out of the sun soon."); stage = END_DIALOGUE }
            }
        }
        return true
    }

    override fun getIds(): IntArray = intArrayOf(*babyGecko, *adultGecko)
}
