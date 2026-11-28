package content.minigame.mage_training.plugin

import core.api.getUsedOption
import core.api.openDialogue
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.world.map.Direction
import core.game.world.map.Location
import shared.consts.NPCs
import shared.consts.Scenery

class MTAPlugin : InteractionListener {

    override fun defineListeners() {

        /*
         * Handles talking to the Maze Guardian NPC.
         */

        on(NPCs.MAGICTRAINING_GUARD_MAZE_3102, IntType.NPC, "talk-to") { player, node ->
            openDialogue(player, node.id, node)
            return@on true
        }

        /*
         * Handles interacting with the Progress Hat item.
         */

        on(ProgressHat.hatIds, IntType.ITEM, "talk-to", "destroy") { player, node ->
            val option = getUsedOption(player)
            if (option == "destroy") {
                openDialogue(player, NPCs.MAGICTRAINING_PIZZAZ_HAT_3096, node, true, true)
            } else {
                openDialogue(player, NPCs.MAGICTRAINING_PIZZAZ_HAT_3096)
            }
            return@on true
        }

        /*
         * Handles destination to Maze guardian.
         */

        setDest(IntType.NPC, NPCs.MAGICTRAINING_GUARD_MAZE_3102) { player, node ->
            val dir = Direction.getDirection(player.location, node.location)
            return@setDest node.location.transform(dir, -1)
        }

        /*
         * Handles the teleport destinations for the MTA tps.
         */

        setDest(IntType.SCENERY, mtaTeleports, "enter") { _, node ->
            when (node.id) {
                Scenery.TELEKINETIC_TELEPORT_10778 -> return@setDest Location.create(3363, 3316, 0)
                Scenery.ENCHANTERS_TELEPORT_10779 -> return@setDest Location.create(3361, 3318, 0)
                Scenery.ALCHEMISTS_TELEPORT_10780 -> return@setDest Location.create(3363, 3320, 0)
                else -> return@setDest Location.create(3365, 3318, 0)
            }
        }
    }

    companion object {
        /**
         * Represents the scenery objects for all MTA teleport portals.
         */
        val mtaTeleports = intArrayOf(
            Scenery.TELEKINETIC_TELEPORT_10778,
            Scenery.ENCHANTERS_TELEPORT_10779,
            Scenery.ALCHEMISTS_TELEPORT_10780,
            Scenery.GRAVEYARD_TELEPORT_10781
        )
    }
}
