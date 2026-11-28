package content.global.skill.agility.shortcuts

import core.cache.def.impl.LocType
import core.game.interaction.OptionHandler
import core.game.node.Node
import core.game.node.entity.impl.ForceMovement
import core.game.node.entity.player.Player
import core.game.world.map.Location
import core.game.world.update.flag.context.Animation
import core.plugin.Initializable
import core.plugin.Plugin
import shared.consts.Animations
import shared.consts.Scenery

/**
 * Handles the squeeze under lumberyard wall shortcut.
 */
@Initializable
class LumberYardShortcut : OptionHandler() {

    override fun handle(player: Player, node: Node, option: String): Boolean {
        if (option != "squeeze-under") return true

        val right = player.location.x > node.location.x

        val start = Location(if (right) 3296 else player.location.x, 3498, player.location.z)
        val dest  = Location(if (right) 3295 else 3296, 3498, player.location.z)

        ForceMovement.run(
            player,
            start,
            dest,
            Animation(Animations.PLAYER_SNEAKS_THROUGH_FENCE_REVERSED_9221)
        )

        return true
    }

    @Throws(Throwable::class)
    override fun newInstance(arg: Any?): Plugin<Any> {
        LocType.forId(Scenery.FENCE_31149).handlers["option:squeeze-under"] = this
        return this
    }
}
