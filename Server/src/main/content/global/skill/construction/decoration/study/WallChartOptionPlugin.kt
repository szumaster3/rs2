package content.global.skill.construction.decoration.study

import core.api.*
import core.cache.def.impl.LocType
import core.game.interaction.OptionHandler
import core.game.node.Node
import core.game.node.entity.player.Player
import core.game.world.update.flag.context.Animation
import core.plugin.Initializable
import core.plugin.Plugin
import shared.consts.Scenery

/**
 * Handles interaction with the Wall charts.
 */
@Initializable
class WallChartOptionPlugin : OptionHandler() {

    companion object {
        private val STUDY_ANIMATION = Animation(3653)
    }

    override fun newInstance(arg: Any?): Plugin<Any> {
        LocType.forId(Scenery.ALCHEMICAL_CHART_13662).handlers["option:study"] = this
        LocType.forId(Scenery.ASTRONOMICAL_CHART_13663).handlers["option:study"] = this
        LocType.forId(Scenery.INFERNAL_CHART_13664).handlers["option:study"] = this
        return this
    }

    override fun handle(player: Player?, node: Node?, option: String?): Boolean {
        val def = node?.let { LocType.forId(it.id) }
        if (player != null) {
            lockInteractions(player, 1)
            player.animate(STUDY_ANIMATION)
            def?.examine?.let { sendDialogue(player, it) }
        }
        return true
    }

}