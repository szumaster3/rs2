package core.game.world.update.flag.context

import core.game.world.map.Direction
import core.game.world.map.Location

/**
 * Represents a forced movement update for an entity.
 *
 * @property start The starting location of the movement.
 * @property dest The destination location of the movement.
 * @property startArrive The delay (in client cycles) before the movement starts.
 * @property destArrive The duration (in client cycles) it takes to reach the destination.
 * @property direction The direction the entity should face during the movement.
 */
data class ForceMoveCtx (val start: Location, val dest: Location, val startArrive: Int, val destArrive: Int, val direction: Direction) {
    // Protocol.readPlayerUpdateMasks: four signed bytes, two unsigned absolute
    // cycle offsets relative to receipt, and a cardinal facing (Static277/297).
    fun isEncodableFrom(location: Location): Boolean =
        startArrive >= 0 && destArrive >= 0 &&
            startArrive.toLong() + maxOf(1, destArrive) <= 65535L &&
            direction.toInteger() in 0..3 &&
            listOf(start.x - location.x, start.y - location.y,
                dest.x - location.x, dest.y - location.y).all { it in -128..127 }

    fun snapshot(): ForceMoveCtx = copy(start = Location.create(start), dest = Location.create(dest))
}