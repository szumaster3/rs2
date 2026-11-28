package content.global.skill.agility.shortcuts

import content.global.skill.agility.AgilityHandler
import content.global.skill.agility.AgilityShortcut
import core.game.node.entity.player.Player
import core.game.node.scenery.Scenery
import core.game.system.task.Pulse
import core.game.world.GameWorld
import core.game.world.map.Direction
import core.game.world.update.flag.context.Animation
import core.plugin.Initializable
import shared.consts.Animations

/**
 * Handles the wall shortcut.
 */
@Initializable
class McGruborShortcut : AgilityShortcut(intArrayOf(51), 1, 0.0, "squeeze-through") {

    override fun run(player: Player, scenery: Scenery, option: String, failed: Boolean) {
        player.lock(3)
        GameWorld.Pulser.submit(object : Pulse(1, player) {
            override fun pulse(): Boolean {
                AgilityHandler.forceWalk(
                    player,
                    -1,
                    player.location,
                    player.location.transform(if (player.location.x < 2662) Direction.EAST else Direction.WEST, 1),
                    Animation(Animations.RAILING_SQUEEZE_3844),
                    5,
                    0.0,
                    "You squeeze through the loose railing.",
                    0
                )
                return true
            }
        })
    }
}