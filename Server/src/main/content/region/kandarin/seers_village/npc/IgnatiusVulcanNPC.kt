package content.region.kandarin.seers_village.npc

import core.game.node.entity.impl.Animator.Priority
import core.game.node.entity.npc.AbstractNPC
import core.game.node.entity.npc.NPC
import core.game.node.scenery.Scenery
import core.game.node.scenery.SceneryBuilder
import core.game.world.GameWorld.ticks
import core.game.world.map.Location
import core.game.world.map.RegionManager.getObject
import core.game.world.update.flag.context.Animation
import core.plugin.Initializable
import core.tools.RandomFunction
import shared.consts.Animations
import shared.consts.NPCs
import shared.consts.Scenery as Objects

/**
 * Handles Ignatius Vulcan NPC.
 */
@Initializable
class IgnatiusVulcanNPC(id: Int = 0, location: Location? = null) : AbstractNPC(id, location) {

    /**
     * The game tick after which the next fire can be created.
     */
    private var lastFire = 0

    override fun construct(id: Int, location: Location, vararg objects: Any): AbstractNPC = IgnatiusVulcanNPC(id, location)

    override fun tick() {
        if (lastFire < ticks) {
            createFire(this, location)
            lastFire = ticks + RandomFunction.random(50, 200)
        }
        super.tick()
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.IGNATIUS_VULCAN_4946)

    companion object {

        /**
         * Represents the animation used when creating a fire.
         */
        private val ANIMATION =
            Animation(
                Animations.HUMAN_CREATEFIRE_733,
                Priority.HIGH
            )

        /**
         * Creates a temporary fire scenery at the given location.
         */
        fun createFire(npc: NPC, location: Location?) {
            npc.walkingQueue.reset()
            npc.animator.forceAnimation(ANIMATION)

            if (location != null && getObject(location) == null) {
                val fire = Scenery(Objects.FIRE_2732, location)
                SceneryBuilder.add(fire, RandomFunction.random(100, 130))
                npc.faceLocation(fire.location)
            }
        }
    }
}