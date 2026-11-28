package content.global.random.event.pillory

import content.data.GameAttributes
import core.api.*
import core.game.interaction.QueueStrength
import core.game.node.entity.npc.AbstractNPC
import core.game.world.map.Location
import core.game.world.map.RegionManager
import core.game.world.update.flag.context.Animation
import core.tools.RandomFunction
import shared.consts.Animations
import shared.consts.Graphics
import shared.consts.NPCs

/**
 * Represents the Tramp random event NPC.
 *
 * @author szu
 */
class TrampNPC(id: Int = 0, location: Location? = null) : AbstractNPC(id, location) {

    override fun construct(id: Int, location: Location, vararg objects: Any): AbstractNPC =
        TrampNPC(id, location)

    override fun getIds(): IntArray = intArrayOf(NPCs.PILLORY_TRAMP_THROWER_YANILLE_2794, NPCs.PILLORY_TRAMP_THROWER_SEERS_2793, NPCs.PILLORY_TRAMP_THROWER_VARROCK_2792)

    override fun handleTickActions() {
        super.handleTickActions()

        RegionManager.getLocalPlayers(this.asNpc(), 8)
            .firstOrNull { it.getAttribute(GameAttributes.RE_PILLORY_TARGET, false) }
            ?.takeIf { RandomFunction.random(100) == 5 }
            ?.also { player ->
                stopWalk(this)
                faceLocation(this, player.location)
                queueScript(this, 2, QueueStrength.SOFT) {
                    sendChat(this, "Take that, you thief!")
                    this.animate(Animation(Animations.HUMAN_THROW_385))
                    spawnProjectile(this, player, Graphics.VIAL_TRAVEL_1_29)
                    return@queueScript stopExecuting(this)
                }
            }
    }
}
