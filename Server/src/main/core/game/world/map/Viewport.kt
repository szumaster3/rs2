package core.game.world.map

import core.game.node.entity.Entity
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.world.map.RegionManager.getRegionChunk
import java.util.*

/**
 * Represents an entity's viewport.
 *
 * @author Emperor
 */
class Viewport {
    /**
     * The region.
     */
    var region: Region? = null

    /**
     * The region chunks.
     */
    var chunks: Array<Array<RegionChunk?>> = Array(CHUNK_SIZE) { arrayOfNulls(CHUNK_SIZE) }

    /**
     * The region plane.
     */
    var currentPlane: RegionPlane? = null

    /**
     * The region planes the entity can see.
     */
    var viewingPlanes: List<RegionPlane> = LinkedList()

    /**
     * Updates the entity's viewport.
     *
     * @param entity The entity.
     */
    fun updateViewport(entity: Entity) {
        val chunk = getRegionChunk(entity.location)
        val center = chunks.size shr 1
        val offset = center * -8
        val l = chunk.getCurrentBase()
        for (x in chunks.indices) {
            for (y in chunks[x].indices) {
                val tile = l.transform(offset + (8 * x), offset + (8 * y), 0)
                chunks[x][y] = if (tile.x in 0..16383 && tile.y in 0..16383) getRegionChunk(tile) else null
            }
        }
    }

    /**
     * Removes the entity from the viewingPlanes.
     *
     * @param entity The entity.
     */
    fun remove(entity: Entity?) {
        if (region == null) {
            return
        }
        if (entity is Player) {
            region!!.remove(entity)
            for (viewed in viewingPlanes.map { it.region }.distinct()) {
                viewed.decrementViewAmount()
                viewed.checkInactive()
            }
        } else {
            region!!.remove(entity as NPC?)
        }
    }

    companion object {
        /**
         * The amount of chunks in a viewport.
         */
        const val CHUNK_SIZE: Int = 5
    }
}
