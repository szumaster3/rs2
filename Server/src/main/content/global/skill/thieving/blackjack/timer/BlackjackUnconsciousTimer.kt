package content.global.skill.thieving.blackjack.timer

import core.game.node.entity.Entity
import core.game.node.entity.npc.NPC
import core.game.system.timer.RSTimer
import core.game.world.update.flag.context.Animation

/**
 * Keeps an NPC "unconscious" for [remainingTicks] ticks or until it was pickpocketed [pickpocketsLeft] times.
 * Create it with `spawnTimer<BlackjackUnconsciousTimer>(ticks, pickpockets)`.
 */
class BlackjackUnconsciousTimer : RSTimer(
    runInterval = 1,
    identifier = BLACKJACK_UNCONSCIOUS,
    isSoft = false
) {

    var remainingTicks: Int = DEFAULT_TICKS
    var pickpocketsLeft: Int = DEFAULT_PICKPOCKETS

    override fun getInitialRunDelay(): Int = 1

    override fun onRegister(entity: Entity) {
        if (entity !is NPC) return
        entity.animator.animate(Animation(838))
        entity.sendChat("Zzzzzzz")
        entity.walkingQueue.reset()
    }

    override fun run(entity: Entity): Boolean {
        if (entity !is NPC) return false
        remainingTicks--
        return remainingTicks > 0
    }

    override fun onRemoval(entity: Entity) {
        if (entity !is NPC) return
        entity.animator.reset()
        entity.sendChat("Arghh my head.")
    }

    override fun getTimer(vararg args: Any): RSTimer = BlackjackUnconsciousTimer().also {
        it.remainingTicks = args.getOrNull(0) as? Int ?: DEFAULT_TICKS
        it.pickpocketsLeft = args.getOrNull(1) as? Int ?: DEFAULT_PICKPOCKETS
    }

    companion object {
        const val BLACKJACK_UNCONSCIOUS = "blackjack_unconscious"
        const val DEFAULT_TICKS = 8
        const val DEFAULT_PICKPOCKETS = 2
    }
}
