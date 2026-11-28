package content.global.dialogue

import core.api.produceGroundItem
import core.api.sendMessage
import core.api.setAttribute
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.diary.DiaryType
import core.game.node.item.Item
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import core.tools.RandomFunction
import shared.consts.Items
import shared.consts.NPCs

/**
 * Handles the man dialogues.
 * @author Vexia
 */
@Initializable
class ManDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        if (npc == null) return false
        if ((args.size > 1 && args[1] is Item) && (args[1] as Item) == Item(Items.CIDER_5763) && player.inventory.remove(Item(Items.CIDER_5763))) {
            if (!player.achievementDiaryManager.getDiary(DiaryType.SEERS_VILLAGE)!!.isComplete(0, 6)) {
                if (player.getAttribute("diary:seers:pub-cider", 0) >= 4) {
                    setAttribute(player, "/save:diary:seers:pub-cider", 5)
                    player.achievementDiaryManager.getDiary(DiaryType.SEERS_VILLAGE)!!.updateTask(player, 0, 6, true)
                } else {
                    setAttribute(player, "/save:diary:seers:pub-cider", player.getAttribute("diary:seers:pub-cider", 0) + 1)
                }
            }
            npc("Ah, a glass of cider, that's very generous of you. I", "don't mind if I do. Thanks!").also {
                stage = END_DIALOGUE
            }
            return true
        }
        player(FaceAnim.HALF_GUILTY, "Hello, how's it going?")
        stage = RandomFunction.random(0, 5)
        if (stage == 1) {
            stage = 0
        }
        val rand = RandomFunction.random(0, 1000)
        if (rand == 1) {
            sendMessage(player, "Something drops out of Man's pocket onto the floor.")
            sendMessage(player, "It looks like a piece of paper.")
            produceGroundItem(player, Items.FLIER_956, 1, npc.location)
        }
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> npc(FaceAnim.HALF_GUILTY, "I'm very well thank you.").also { stage = END_DIALOGUE }
            2 -> npc(FaceAnim.HALF_GUILTY, "Who are you?").also { stage = 6 }
            3 -> npc(FaceAnim.HALF_GUILTY, "I'm fine, how are you?").also { stage = 8 }
            4 -> npc(FaceAnim.HALF_GUILTY, "No, I don't want to buy anything!").also { stage = END_DIALOGUE }
            5 -> npc(FaceAnim.HALF_GUILTY, "I think we need a new king. The one we've got isn't", "very good.").also { stage = END_DIALOGUE }
            6 -> player(FaceAnim.HALF_GUILTY, "I'm a bold adventurer.").also { stage++ }
            7 -> npc(FaceAnim.HALF_GUILTY, "Ah, a very noble profession.").also { stage = END_DIALOGUE }
            8 -> player(FaceAnim.HALF_GUILTY, "Very well thank you.").also { stage = END_DIALOGUE }
        }
        return true
    }

    override fun newInstance(player: Player?): Dialogue = ManDialogue(player)

    override fun getIds(): IntArray = intArrayOf(
        NPCs.MAN_1,
        NPCs.MAN2_2,
        NPCs.MAN3_3,
        NPCs.WOMAN_4,
        NPCs.WOMAN2_5,
        NPCs.WOMAN3_6,
        NPCs.AL_KHARID_MAN_16,
        NPCs.MAN_24,
        NPCs.WOMAN_25,
        NPCs.MAN4_FOR_MUSA_POINT_170,
        NPCs.MAN_351,
        NPCs.WOMAN_352,
        NPCs.WOMAN_353,
        NPCs.WOMAN_354,
        NPCs.MAN_359,
        NPCs.WOMAN_360,
        NPCs.WOMAN_361,
        NPCs.WOMAN_362,
        NPCs.WOMAN_363,
        NPCs.REGISTRAR_1_726,
        NPCs.REGISTRAR_2_727,
        NPCs.MAN_728,
        NPCs.MAN_729,
        NPCs.REGISTRAR_3_730,
        NPCs.MAN4_1086,
        NPCs.MAN_2675,
        NPCs.WANTED_SOLUS_FAKE_WOMAN_BASE_CONFIG_2776,
        NPCs.MAN_3224,
        NPCs.FALADOR_MAN4_3225,
        NPCs.DELL_MONTI_3227,
        NPCs.VARROCK_MAN1_5923,
        NPCs.VARROCK_WOMAN1_5924,
        NPCs.FAI_LUMBRIDGE_MAN1_7873,
        NPCs.FAI_LUMBRIDGE_MAN2_7874,
        NPCs.FAI_LUMBRIDGE_MAN3_7875,
        NPCs.FAI_LUMBRIDGE_MAN4_7876,
        NPCs.FAI_LUMBRIDGE_MAN5_7877,
        NPCs.FAI_LUMBRIDGE_MAN6_7878,
        NPCs.FAI_LUMBRIDGE_MAN7_7879,
        NPCs.FAI_LUMBRIDGE_WOMAN1_7880,
        NPCs.FAI_LUMBRIDGE_WOMAN2_7881,
        NPCs.FAI_LUMBRIDGE_WOMAN3_7882,
        NPCs.FAI_LUMBRIDGE_WOMAN4_7883,
        NPCs.FAI_LUMBRIDGE_WOMAN5_7884,
        NPCs.WOMAN_7925,
    )
}
