package content.region.kandarin.camelot.ctr

import core.api.*
import core.game.activity.ActivityManager
import core.game.activity.ActivityPlugin
import core.game.node.entity.Entity
import core.game.node.entity.player.Player
import core.game.world.map.Location
import core.game.world.map.zone.ZoneBorders
import core.game.world.map.zone.ZoneRestriction
import core.plugin.Initializable

/**
 * Represents the Training Grounds activity.
 */
@Initializable
class CamelotTrainingRoomActivity : ActivityPlugin("Knight's training", true, false, true, ZoneRestriction.CANNON, ZoneRestriction.RANDOM_EVENTS, ZoneRestriction.FOLLOWERS) {

    init {
        ActivityManager.register(this)
    }

    override fun enter(e: Entity): Boolean {
        val result = super.enter(e)

        if (e is Player) {
            CamelotSession.create(e).start()
        }

        return result
    }

    override fun leave(e: Entity, logout: Boolean): Boolean {
        val result = super.leave(e, logout)

        if (e is Player) {
            CamelotSession.getSession(e)?.close()
        }

        return result
    }

    override fun death(entity: Entity, killer: Entity): Boolean {
        if (entity is Player) {
            CamelotSession.getSession(entity)?.death()
                ?: teleport(entity, Location.create(2750, 3507, 2))
        }
        return true
    }

    override fun configure() {
        register(ZoneBorders(2752, 3502, 2764, 3513, 2, true))
    }

    override fun newInstance(p: Player?): ActivityPlugin =
        CamelotTrainingRoomActivity()

    override fun getSpawnLocation(): Location =
        Location.create(2750, 3507, 2)
}