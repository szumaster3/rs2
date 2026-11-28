package content.region.fremennik.rellekka.dialogue

import content.data.Quests

import content.region.fremennik.rellekka.quest.viking.FremennikTrials
import core.api.isQuestComplete
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs

/**
 * Represents the Reeso dialogue.
 */
@Initializable
class ReesoDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        if (!isQuestComplete(player, Quests.THE_FREMENNIK_TRIALS.questName)) {
            npcl(FaceAnim.ANNOYED, "Please do not disturb me, outerlander. I have much to do.").also { stage = END_DIALOGUE }
        } else {
            npcl(FaceAnim.STRUGGLE, "Sorry, ${FremennikTrials.getFremennikName(player)}, I must get on with my work.").also { stage = END_DIALOGUE }
        }
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean = true

    override fun newInstance(player: Player?): Dialogue = ReesoDialogue(player)

    override fun getIds(): IntArray = intArrayOf(NPCs.HANDSAND_REESO_3116)
}
