package content.region.asgarnia.rimmington.plugin

import content.region.asgarnia.rimmington.dialogue.*
import core.api.animate
import core.api.openDialogue
import core.api.replaceScenery
import core.api.sendMessage
import core.game.global.action.DoorActionHandler
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import shared.consts.Animations
import shared.consts.NPCs
import shared.consts.Scenery

class RimmingtonPlugin : InteractionListener {


    override fun defineListeners() {

        /*
         * Handles Rimmik store doors.
         */

        on(Scenery.DOOR_1534, IntType.SCENERY, "close", "open") { player, node ->
            if (node.location.x == 2950 && node.location.y == 3207) {
                sendMessage(player, "The doors appear to be stuck.")
                return@on false
            }
            DoorActionHandler.handleDoor(player, node.asScenery().wrapper)
            return@on true
        }

        /*
         * Handles opening wardrobe at Melzar's Maze.
         */

        on(Scenery.WARDROBE_33963, IntType.SCENERY, "open") { player, node ->
            animate(player, Animations.HUMAN_OPENBIGCUPBOARD_545)
            replaceScenery(node.asScenery(), Scenery.WARDROBE_35133, -1)
            return@on true
        }

        on(Scenery.WARDROBE_35133, IntType.SCENERY, "close") { player, node ->
            animate(player, Animations.HUMAN_CLOSEHIGHCUPBOARD_541)
            replaceScenery(node.asScenery(), Scenery.WARDROBE_33963, -1)
            return@on true
        }

        /*
         * Handles talking to NPCs around village.
         */

        on(NPCs.RIMMINGTON_ANJA_2684, IntType.NPC, "talk-to") { player, node ->
            openDialogue(player, AnjaDialogue(), node.asNpc())
            return@on true
        }

        on(NPCs.BRIAN_1860, IntType.NPC, "talk-to") { player, node ->
            openDialogue(player, BrianDialogue(), node.asNpc())
            return@on true
        }

        on(NPCs.RIMMINGTON_HENGEL_2683, IntType.NPC, "talk-to") { player, node ->
            openDialogue(player, HengelDialogue(), node.asNpc())
            return@on true
        }

        on(Scenery.CUSTOMS_SERGEANT_31459, IntType.SCENERY, "talk-to") { player, node ->
            if (player.location.x >= 2963) {
                openDialogue(player, CustomsSergeantDialogue(), node.asNpc())
            }
            return@on true
        }

        on(NPCs.ROMMIK_585, IntType.NPC, "talk-to") { player, node ->
            openDialogue(player, RommikDialogue(), node.asNpc())
            return@on true
        }
    }
}
