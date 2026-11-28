package content.region.kandarin.west_ardougne.dialogue

import content.region.kandarin.east_ardougne.quest.biohazard.dialogue.MournerHeadquartersDialogue
import content.region.kandarin.west_ardougne.quest.elena.dialogue.HeadMournerDialogue
import content.region.kandarin.west_ardougne.quest.elena.dialogue.MournerArdougneDialogue
import core.api.openDialogue
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs

/**
 * Represents the Mourner dialogue.
 */
@Initializable
class MournerDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        when (npc.id) {
            NPCs.MOURNER2_347,
            NPCs.MOURNER3_348,
            NPCs.MOURNER_ARMED_357,
            NPCs.MOURNERSTEW3_369,
            NPCs.MOURNERSTEW1_371,
            NPCs.MOURNERSTEW2_370 -> openDialogue(player, MournerHeadquartersDialogue())
            NPCs.HEADMOURNER_716 -> openDialogue(player, HeadMournerDialogue())
            NPCs.MOURNER1_717, NPCs.MOURNERTWA_718 -> openDialogue(player, MournerArdougneDialogue())
            NPCs.MOURNERTWB_719 -> playerl(FaceAnim.FRIENDLY, "Hello.")
            else -> playerl(FaceAnim.FRIENDLY, "Hello.").also { stage = 5 }
        }
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> npcl(FaceAnim.HALF_ASKING, "What are you up to?").also { stage++ }
            1 -> playerl(FaceAnim.NEUTRAL, "Nothing.").also { stage++ }
            2 -> npcl(FaceAnim.THINKING, "I don't trust you.").also { stage++ }
            3 -> playerl(FaceAnim.NEUTRAL, "You don't have to.").also { stage++ }
            4 -> npcl(FaceAnim.NEUTRAL, "If I find you attempting to cross the wall I'll make sure you never return.").also { stage = END_DIALOGUE }
            5 -> npcl(FaceAnim.NEUTRAL, "Good day. Are you in need of assistance?").also { stage++ }
            6 -> playerl(FaceAnim.FRIENDLY, "Yes, but I don't think you can help.").also { stage++ }
            7 -> npcl(FaceAnim.NEUTRAL, "You will be surprised at how much help the brute force of the Guard can be.").also { stage++ }
            8 -> playerl(FaceAnim.FRIENDLY, "Well I'll be sure to ask if I'm in need of some muscle.").also { stage = END_DIALOGUE }
        }
        return true
    }

    override fun getIds(): IntArray =
        intArrayOf(
            NPCs.MOURNER2_347,
            NPCs.MOURNER3_348,
            NPCs.MOURNER_ARMED_357,
            NPCs.MOURNERSTEW3_369,
            NPCs.MOURNERSTEW1_371,
            NPCs.MOURNERSTEW2_370,
            NPCs.HEADMOURNER_716,
            NPCs.MOURNER1_717,
            NPCs.MOURNERTWA_718,
            NPCs.MOURNERTWB_719
        )
}
