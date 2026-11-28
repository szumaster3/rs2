package content.minigame.puro_puro.plugin

import core.api.addItemOrDrop
import core.api.openInterface
import core.api.sendNPCDialogue
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import shared.consts.Components
import shared.consts.Items
import shared.consts.NPCs

class ElnockOptionPlugin : InteractionListener {
    override fun defineListeners() {
        on(NPCs.II_ELNOCK_6070, IntType.NPC, "trade") { player, _ ->
            openInterface(player, Components.ELNOCK_EXCHANGE_540)
            return@on true
        }

        on(NPCs.II_ELNOCK_6070, IntType.NPC, "quick-start") { player, _ ->
            if (!player.savedData.activityData.elnockNPCSupplies) {
                player.savedData.activityData.elnockNPCSupplies = true
                addItemOrDrop(player, Items.BUTTERFLY_NET_10010, 1)
                addItemOrDrop(player, Items.IMP_REPELLENT_11262, 1)
                addItemOrDrop(player, Items.IMPLING_JAR_11260, 6)
                sendNPCDialogue(player, NPCs.II_ELNOCK_6070, "Here you go!")
            } else {
                sendNPCDialogue(player, NPCs.II_ELNOCK_6070, "Since I have already given you some equipment for free, I'll be willing to sell you some now.")
            }
            return@on true
        }
    }
}
