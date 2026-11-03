package content.region.asgarnia.falador.quest.rd.dialogue

import core.api.openDialogue
import core.game.dialogue.Dialogue
import core.game.node.entity.player.Player
import core.plugin.Initializable
import shared.consts.NPCs

@Initializable
class SirTinleyDialogue(player: Player? = null) : Dialogue(player) {
    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        openDialogue(player, SirTinleyDialogueFile(), npc)
        return true
    }

    override fun newInstance(player: Player): Dialogue {
        return SirTinleyDialogue(player)
    }

    override fun getIds(): IntArray {
        return intArrayOf(NPCs.SIR_TINLEY_2286)
    }
}