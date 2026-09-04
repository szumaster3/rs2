package content.region.morytania.mort_myre.plugin

import core.api.findLocalNPC
import core.api.sendMessage
import core.api.sendNPCDialogue
import core.game.global.action.DoorActionHandler
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.player.link.warning.WarningManager
import core.game.node.entity.player.link.warning.WarningType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import shared.consts.NPCs
import shared.consts.Quests
import shared.consts.Scenery

class MortMyreSwampPlugin : InteractionListener {

    override fun defineListeners() {

        /*
         * Handles interaction with the Mort Myre gates.
         */

        on(intArrayOf(Scenery.GATE_3506, Scenery.GATE_3507), IntType.SCENERY, "open") { player, node ->
            val scenery = node.asScenery()

            if (player.location.y == 3457) {
                DoorActionHandler.handleAutowalkDoor(player, scenery)

                GlobalScope.launch {
                    findLocalNPC(player, NPCs.ULIZIUS_1054)
                        ?.sendChat("Oh my! You're still alive!", 2)
                }

                return@on true
            }

            if (player.location.y == 3458) {
                if (!player.questRepository.hasStarted(Quests.NATURE_SPIRIT)) {
                    sendNPCDialogue(
                        player,
                        NPCs.ULIZIUS_1054,
                        "I'm sorry, but I'm afraid it's too dangerous to let you through this gate right now."
                    )
                    return@on true
                }

                WarningManager.trigger(player, WarningType.MORT_MYRE) {
                    DoorActionHandler.handleAutowalkDoor(player, scenery)
                    sendMessage(player, "You walk into the gloomy atmosphere of Mort Myre.",3)
                }

                return@on true
            }

            return@on true
        }
    }
}