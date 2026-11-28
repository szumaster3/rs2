package content.region.misthalin.varrock.museum.dialogue

import core.api.sendNPCDialogue
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs

@Initializable
class PupilDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        sendNPCDialogue(player, NPCs.SCHOOLBOY_5949, "Teacher! Sir! I need the toilet!", FaceAnim.CHILD_GUILTY)
        stage = 0
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> {
                sendNPCDialogue(
                    player,
                    NPCs.VM_MALE_SCHOOL_TEACHER_CHATHEAD_DUMMY_5948,
                    "I told you to go before we got here.",
                    FaceAnim.HALF_GUILTY
                )
                stage++
            }

            1 -> {
                sendNPCDialogue(
                    player,
                    NPCs.SCHOOLBOY_5949,
                    "But sir, I didn't need to go then!",
                    FaceAnim.CHILD_GUILTY
                )
                stage++
            }

            2 -> {
                sendNPCDialogue(
                    player,
                    NPCs.VM_MALE_SCHOOL_TEACHER_CHATHEAD_DUMMY_5948,
                    "Alright, come on then.",
                    FaceAnim.HALF_GUILTY
                )
                stage = END_DIALOGUE
            }
        }
        return true
    }

    override fun newInstance(player: Player?): Dialogue = PupilDialogue(player)

    override fun getIds(): IntArray = intArrayOf(NPCs.VM_SCHOOL_TEACHER_MALE_AND_BOY_5944)
}
