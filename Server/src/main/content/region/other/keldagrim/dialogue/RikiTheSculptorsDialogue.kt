package content.region.other.keldagrim.dialogue

import content.data.Quests

import core.api.hasRequirement
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs

/**
 * Represents the Riki The Sculptor dialogue.
 */
@Initializable
class RikiTheSculptorsDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        if(!hasRequirement(player, Quests.THE_GIANT_DWARF.questName)){
            player("Hello there, what are you doing here?")
        } else {
            player(FaceAnim.HAPPY, "I'm glad I don't have to talk to you anymore!")
            stage = 6
        }
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> npc(FaceAnim.OLD_DEFAULT,"I... am the sculptor's associate.").also { stage++ }
            1 -> player(FaceAnim.HALF_ASKING,"His associate? What does that mean?").also { stage++ }
            2 -> npc(FaceAnim.OLD_DEFAULT,"I... am the sculptor's associate.").also { stage++ }
            3 -> player(FaceAnim.ASKING,"Are you going to say anything meaningful at all?").also { stage++ }
            4 -> npc(FaceAnim.OLD_DEFAULT,"I...").also { stage++ }
            5 -> player(FaceAnim.NEUTRAL, "No, apparently not.").also { stage = END_DIALOGUE }
            6 -> npc(FaceAnim.OLD_DEFAULT, "Hrm.").also { stage = END_DIALOGUE }
        }
        return true
    }

    override fun newInstance(player: Player?): Dialogue = RikiTheSculptorsDialogue(player)

    override fun getIds(): IntArray =
        intArrayOf(
            NPCs.DWARF_CITY_SHOP_SCULPTURE_MODEL_2143,
            NPCs.DWARF_CITY_SHOP_SCULPTURE_MODEL_BOOTS_2144,
            NPCs.DWARF_CITY_SHOP_SCULPTURE_MODEL_TORSO_2145,
            NPCs.DWARF_CITY_SHOP_SCULPTURE_MODEL_AXE_2146,
            NPCs.DWARF_CITY_SHOP_SCULPTURE_MODEL_BOOTS_AND_TORSO_2147,
            NPCs.DWARF_CITY_SHOP_SCULPTURE_MODEL_BOOTS_AND_AXE_2148,
            NPCs.DWARF_CITY_SHOP_SCULPTURE_MODEL_TORSO_AND_AXE_2149,
            NPCs.DWARF_CITY_SHOP_SCULPTURE_MODEL_FINISHED_2150,
        )
}
