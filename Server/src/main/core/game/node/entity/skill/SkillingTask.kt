package core.game.node.entity.skill

import core.api.*
import core.game.interaction.Clocks
import core.game.interaction.QueueStrength
import core.game.node.Node
import core.game.node.entity.player.Player

/**
 * Clean skilling task (stateless cycle-based design).
 */
abstract class SkillingTask<T : Node>(
    protected val player: Player,
    protected val node: T? = null,
    private val cycleTicks: Int = 3,
    private val strength: QueueStrength = QueueStrength.NORMAL,
    private val persist: Boolean = false
) {

    /**
     * Starts task in queue.
     */
    fun start() {
        queueScript(entity = player, delay = 1, strength = strength, persist = persist) { _ ->
            if (node != null && !node.isActive) {
                stopTask()
                return@queueScript true
            }

            if (!canStart()) {
                stopTask()
                return@queueScript true
            }

            if (!canContinue()) {
                stopTask()
                return@queueScript true
            }

            if (!clockReady(player, Clocks.SKILLING)) {
                return@queueScript false
            }

            delayClock(player, Clocks.SKILLING, cycleTicks)
            playAnimation()

            val finished = process()

            if (finished) {
                stopTask()
                return@queueScript true
            }

            delayScript(player, cycleTicks)
            return@queueScript false
        }
    }

    /**
     * Can this task start?
     */
    abstract fun canStart(): Boolean

    /**
     * Can continue next cycle?
     */
    open fun canContinue(): Boolean = true

    /**
     * Animation per cycle (optional).
     */
    open fun playAnimation() {}

    /**
     * Core cycle logic.
     * @return true if task should finish.
     */
    abstract fun process(): Boolean

    /**
     * Cleanup after finish or cancel.
     */
    open fun stopTask() {
        stopExecuting(player)
        //resetAnimator(player)
    }

}