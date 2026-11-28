package content.region.kandarin.yanille.dialogue

import content.region.kandarin.feldip.quest.zogre.dialogue.BartenderDialogues
import core.api.openDialogue
import core.game.dialogue.Dialogue
import core.game.node.entity.player.Player
import core.plugin.Initializable
import shared.consts.NPCs

/**
 * Represents the Bartender dialogue (Yanille).
 */
@Initializable
class BartenderDialogue(player: Player? = null) : Dialogue(player) {
    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        openDialogue(player, BartenderDialogues(), npc)
        return false
    }

    override fun newInstance(player: Player?): Dialogue = BartenderDialogue(player)
    override fun getIds(): IntArray = intArrayOf(NPCs.DRAGON_BARTENDER_739)
}