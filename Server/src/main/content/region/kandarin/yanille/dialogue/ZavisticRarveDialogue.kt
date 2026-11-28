package content.region.kandarin.yanille.dialogue

import content.region.kandarin.feldip.quest.zogre.dialogue.ZavisticRarveDialogues
import core.api.openDialogue
import core.game.dialogue.Dialogue
import core.game.node.entity.player.Player
import core.plugin.Initializable
import shared.consts.NPCs

/**
 * Represents the Zavistic Rarve dialogue.
 */
@Initializable
class ZavisticRarveDialogue(player: Player? = null) : Dialogue(player) {
    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        openDialogue(player, ZavisticRarveDialogues(), npc)
        return false
    }
    override fun newInstance(player: Player?): Dialogue = ZavisticRarveDialogue(player)
    override fun getIds(): IntArray = intArrayOf(NPCs.ZOGRE_HUMAN_ZAVISTIC_RARVE_2059)
}