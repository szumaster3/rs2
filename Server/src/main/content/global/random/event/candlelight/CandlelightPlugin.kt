package content.global.random.event.candlelight

import core.api.openDialogue
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import shared.consts.NPCs

class CandlelightPlugin : InteractionListener {
    override fun defineListeners() {
        on(intArrayOf(NPCs.PIOUS_PETE_3207, NPCs.NULL_6564), IntType.NPC, "talk-to") { player, node ->
            openDialogue(player, PiousPeteDialogue(), node.asNpc())
            return@on true
        }
    }
}