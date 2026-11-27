package core.game.world.map.zone.impl

import content.data.GameAttributes
import core.api.getAttribute
import core.game.node.Node
import core.game.node.entity.Entity
import core.game.node.entity.player.Player
import core.game.node.entity.player.info.Rights
import core.game.world.map.zone.MapZone
import core.game.world.map.zone.ZoneBuilder
import core.plugin.Initializable
import core.plugin.Plugin

/**
 * Represents the tutorial zone area.
 */
@Initializable
class TutorialZone : MapZone("tutorial", true), Plugin<Any?> {

    override fun configure() {
        for (regionId in REGIONS) {
            registerRegion(regionId)
        }
    }

    override fun newInstance(arg: Any?): Plugin<Any?> {
        ZoneBuilder.configure(this)
        return this
    }

    override fun teleport(entity: Entity, type: Int, node: Node?): Boolean {
        if (entity is Player) {
            if (entity.rights == Rights.ADMINISTRATOR) {
                return super.teleport(entity, type, node)
            }

            val tutorialComplete = getAttribute(entity, GameAttributes.TUTORIAL_COMPLETE, false)
            val tutorialStage = getAttribute(entity, GameAttributes.TUTORIAL_STAGE, -1)

            if (!tutorialComplete && tutorialStage < 72) {
                return false
            }
        }
        return super.teleport(entity, type, node)
    }

    override fun fireEvent(identifier: String, vararg args: Any): Any? {
        return null
    }

    companion object {
        /**
         * Represents the tutorial region ids.
         */
        private val REGIONS = intArrayOf(12079, 12180, 12592, 12436, 12335, 12336
        )
    }
}