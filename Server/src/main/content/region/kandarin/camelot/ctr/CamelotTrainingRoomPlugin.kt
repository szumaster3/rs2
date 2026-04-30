package content.region.kandarin.camelot.ctr

import content.data.GameAttributes
import core.api.*
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.world.map.Location
import shared.consts.NPCs
import shared.consts.Quests
import shared.consts.Scenery

class CamelotTrainingRoomPlugin : InteractionListener {
    override fun defineListeners() {
        /*
         * Handles start of activity.
         */

        on(intArrayOf(Scenery.LARGE_DOOR_25594, Scenery.LARGE_DOOR_25595), IntType.SCENERY, "open") { player, _ ->
            if (!hasRequirement(player, Quests.KINGS_RANSOM, false)) {
                sendMessage(player, "You need to have completed the King's Ransom Quest to gain access.")
                return@on true
            }

            val kwComplete = getAttribute(player, GameAttributes.KW_COMPLETE, false)
            val kwBegin = getAttribute(player, GameAttributes.KW_BEGIN, false)

            val insideArea = player.location.x >= 2752

            if (insideArea) {
                CamelotSession.getSession(player)?.close()
                teleport(player, Location.create(2751, 3507, 2))
                clearLogoutListener(player, "Knight's training")
                return@on true
            }

            if (kwComplete || !kwBegin) {
                openDialogue(player, NPCs.SQUIRE_6169)
                return@on false
            }

            if (player.familiarManager.hasFamiliar()) {
                sendMessage(player,"Followers are not allowed in the Knight Waves. You'll need to put it away if you wish to enter.")
                return@on true
            }

            teleport(player, Location.create(2753, 3507, 2))

            registerLogoutListener(player, "Knight's training") {
                removeAttributes(
                    player,
                    GameAttributes.PRAYER_LOCK,
                    GameAttributes.KW_SPAWN,
                    GameAttributes.KW_TIER,
                    GameAttributes.KW_BEGIN
                )
            }

            CamelotSession.create(player).start()
            sendMessage(player, "Remember, only melee combat is allowed in here.", 1)
            return@on true
        }
    }
}