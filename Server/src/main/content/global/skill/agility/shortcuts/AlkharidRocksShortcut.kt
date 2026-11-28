package content.global.skill.agility.shortcuts

import core.api.*
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.QueueStrength
import core.game.node.entity.skill.Skills
import core.game.world.map.Direction
import core.game.world.map.Location
import shared.consts.Animations
import shared.consts.Scenery

/**
 * Handles the Al Kharid rocks shortcut.
 */
class AlkharidRocksShortcut : InteractionListener {

    companion object {
        /**
         * Animation used when climbing down.
         */
        private const val CLIMB_DOWN_ANIMATION = Animations.DEATH_HUMAN_CLIMBING_DOWN_1148

        /**
         * Animation used when climbing up.
         */
        private const val CLIMB_UP_ANIMATION = Animations.HUMAN_CLIMBING_DOWN_740

        /**
         * Start location of the shortcut.
         */
        private val START_LOCATION: Location = Location(3307, 3315, 0)

        /**
         * End location of the shortcut.
         */
        private val END_LOCATION: Location = Location(3303, 3315, 0)

        /**
         * Rock object ids for the shortcut.
         */
        private val OBJECTS = intArrayOf(Scenery.ROCKS_9331, Scenery.ROCKS_9332)
    }

    override fun defineListeners() {
        on(OBJECTS, IntType.SCENERY, "climb") { player, node ->
            if (getStatLevel(player, Skills.AGILITY) < 38) {
                sendMessage(player, "You need an Agility level of at least 38 to climb these rocks.")
                return@on true
            }

            queueScript(player, 0, QueueStrength.STRONG)
            {
                val (destination, animation) = when (node.id)
                {
                    OBJECTS[0] -> END_LOCATION   to CLIMB_DOWN_ANIMATION
                    OBJECTS[1] -> START_LOCATION to CLIMB_UP_ANIMATION
                    else -> return@queueScript true
                }

                forceMove(player, node.location, destination, 30, 120, Direction.EAST, animation)
                {
                    resetAnimator(player)
                }
                return@queueScript true
            }

            return@on true
        }
    }

    override fun defineDestinationOverrides() {
        setDest(IntType.SCENERY, OBJECTS, "climb") { _, node ->
            when (node.id) {
                Scenery.ROCKS_9331 -> START_LOCATION
                else -> END_LOCATION
            }
        }
    }
}