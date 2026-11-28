package core.game.node.entity.combat.equipment

import core.api.applyPoison
import core.api.registerTimer
import core.api.sendMessage
import core.api.spawnTimer
import core.game.node.Node
import core.game.node.entity.Entity
import core.game.node.entity.impl.Animator.Priority
import core.game.node.entity.player.Player
import core.game.system.task.NodeTask
import core.game.world.update.flag.context.Animation
import shared.consts.Graphics
import kotlin.random.Random

/**
 * The fire types.
 *
 * @author Emperor
 */
enum class FireType(val animation: Animation, val projectileId: Int, val task: NodeTask) {

    /**
     * Fiery breath.
     */
    FIERY_BREATH(Animation(81, Priority.HIGH), Graphics.DRAGON_RANGED_ATTACKS_1_393, object : NodeTask(0) {
        override fun exec(node: Node, vararg others: Node): Boolean = true
    }),

    /**
     * Toxic breath that applies poison to the target.
     */
    TOXIC_BREATH(Animation(82, Priority.HIGH), Graphics.DRAGON_RANGED_ATTACKS_2_394, object : NodeTask(0) {
        override fun exec(node: Node, vararg others: Node): Boolean {
            applyPoison(node as Entity, others[0] as Entity, 40)
            return true
        }
    }),

    /**
     * Icy breath that freezes the target for a short duration.
     */
    ICY_BREATH(Animation(83, Priority.HIGH), Graphics.DRAGON_RANGED_ATTACKS_3_395, object : NodeTask(0) {
        override fun exec(node: Node, vararg others: Node): Boolean {
            registerTimer(node as Entity, spawnTimer("frozen", 7, true))
            return true
        }
    }),

    /**
     * Shocking breath that randomly reduces target skill level.
     */
    SHOCKING_BREATH(Animation(84, Priority.HIGH), Graphics.DRAGON_RANGED_ATTACKS_4_396, object : NodeTask(0) {
        override fun exec(node: Node, vararg others: Node): Boolean {
            val entity = node as Entity
            if (Random.nextInt(10) < 3) {
                entity.getSkills().updateLevel(Random.nextInt(3), -5, 0)
                (entity as? Player)?.let { sendMessage(it, "You have been shocked.") }
            }
            return true
        }
    }),
}