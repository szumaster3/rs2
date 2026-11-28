package content.minigame.sc.plugin

import core.api.addDialogueAction
import core.api.openInterface
import core.api.sendNPCDialogue
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import shared.consts.Components
import shared.consts.NPCs
import shared.consts.Scenery

class StealingCreationMinigame : InteractionListener {
    override fun defineListeners() {
        on(Scenery.ENTRY_PORTAL_39515, IntType.SCENERY, "enter") { player, _ ->
            sendNPCDialogue(
                player,
                NPCs.SC_HEAD_MYSTIC_8227,
                "No. The other end of the portal is shifting. You would be horribly injured if you entered.",
            )
            addDialogueAction(player) { _, _ ->
                sendNPCDialogue(
                    player,
                    NPCs.SC_HEAD_MYSTIC_8227,
                    "If you'd like to be trained so you can understand what is happening here, come and speak to me.",
                )
                return@addDialogueAction
            }
            return@on true
        }

        on(NPCs.SC_REWARDS_MYSTIC_8228, IntType.NPC, "exchange") { player, _ ->
            openInterface(player, Components.SC_REWARD_SHOP_811)
            return@on true
        }
    }
}
