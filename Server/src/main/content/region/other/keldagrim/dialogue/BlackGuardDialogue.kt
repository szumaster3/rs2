package content.region.other.keldagrim.dialogue

import core.api.sendMessage
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import core.tools.START_DIALOGUE
import shared.consts.NPCs

/**
 * Represents the Black Guard (Keldagrim) dialogue.
 */
@Initializable
class BlackGuardDialogue(player: Player? = null) : Dialogue(player) {

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            START_DIALOGUE -> {
                var random = arrayOf(1, 2, 3, 4, 5).random()
                when (random) {
                    1 -> npcl(FaceAnim.OLD_ANGRY3, "Black Guard: Obey the law!").also { stage = END_DIALOGUE }
                    2 -> npcl(FaceAnim.OLD_ANGRY3, "Stay out of trouble!").also { stage = END_DIALOGUE }
                    3 -> npcl(FaceAnim.OLD_ANGRY3, "Out of the way, human!").also { stage = END_DIALOGUE }
                    4 -> npcl(FaceAnim.OLD_ANGRY3, "I'm keeping an eye on you!").also { stage = END_DIALOGUE }
                    5 -> {
                        end()
                        sendMessage(player, "The guard ignores you.")
                    }
                }
            }
        }
        return true
    }

    override fun newInstance(player: Player?): Dialogue = BlackGuardDialogue(player)

    override fun getIds(): IntArray =
        intArrayOf(
            NPCs.DWARF_CITY_BLACK_GUARD1_2130,
            NPCs.DWARF_CITY_BLACK_GUARD2_2131,
            NPCs.DWARF_CITY_BLACK_GUARD3_2132,
            NPCs.DWARF_CITY_BLACK_GUARD4_2133,
            NPCs.DWARF_CITY_BERSERKER1_2134,
            NPCs.DWARF_CITY_BERSERKER2_2135,
            NPCs.DWARF_CITY_BERSERKER3_2136,
        )
}
