package content.global.skill.agility.shortcuts

import core.api.*
import core.api.utils.Vector
import core.game.activity.ActivityManager
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.QueueStrength
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.game.world.map.Direction
import core.game.world.map.Location
import shared.consts.Scenery

/**
 * Handles the various stile shortcuts.
 * @author Ceikry
 */
class StileShortcut : InteractionListener {
    val ids = intArrayOf(Scenery.STILE_993, Scenery.STILE_3730, Scenery.STILE_7527, Scenery.SWORD_CASE_12982, Scenery.STILE_19222, Scenery.STILE_22302, Scenery.STILE_29460, Scenery.STILE_33842, Scenery.STILE_34776, Scenery.STILE_39508, Scenery.STILE_39509, Scenery.STILE_39510)
    private val FALCONRY_STILE = Scenery.STILE_19222

    override fun defineListeners() {
        on(ids, IntType.SCENERY, "climb-over") { p, n ->
            if(n.id == Scenery.FENCE_9300 && getStatLevel(p, Skills.AGILITY) < 13) {
                sendDialogue(p, "You need an Agility level of 13 to negotiate this obstacle.")
                return@on true
            }
            val direction = Vector.betweenLocs(p.location, n.location).toDirection()
            val startLoc = p.location.transform(direction, 1)
            val endLoc = p.location.transform(direction, 2)

            closeAllInterfaces(p)
            p.walkingQueue.reset()
            p.walkingQueue.addPath(startLoc.x, startLoc.y)
            forceMove(p, startLoc, endLoc, 0, animationCycles(839), direction, 839)

            queueScript(p, 5, QueueStrength.SOFT) { _ ->
                val end = endLoc.transform(direction, 1)
                p.walkingQueue.reset()
                p.walkingQueue.addPath(end.x, end.y)

                if (n.id == FALCONRY_STILE)
                    handleFalconry(p, endLoc)
                return@queueScript stopExecuting(p)
            }
            return@on true
        }

        setDest(IntType.SCENERY, ids, "climb-over") { e, n ->
            return@setDest getInteractLocation(e.location, n.location, getOrientation(n.direction))
        }
    }


    companion object {
        fun getInteractLocation(pLoc: Location, sLoc: Location, orientation: Orientation): Location {
            when (orientation) {
                Orientation.HORIZONTAL -> {
                    if (pLoc.x <= sLoc.x) return sLoc.transform(-1, 0, 0)
                    else return sLoc.transform(2, 0, 0)
                }

                Orientation.VERTICAL -> {
                    if (pLoc.y <= sLoc.y) return sLoc.transform(0, -1, 0)
                    else return sLoc.transform(0, 2, 0)
                }
            }
        }

        fun getOrientation(rotation: Direction): Orientation {
            when (rotation) {
                Direction.EAST, Direction.WEST -> return Orientation.HORIZONTAL
                else -> return Orientation.VERTICAL
            }
        }

        fun handleFalconry(p: Player, endLoc: Location) {
            if (endLoc.y == 3619)
                ActivityManager.start(p, "falconry", false)
            else
                ActivityManager.getActivity("falconry").leave(p, false)
        }
    }

    enum class Orientation {
        HORIZONTAL,
        VERTICAL
    }
}
