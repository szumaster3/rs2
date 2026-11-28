package content.global.skill.hunter.pitfall

import core.cache.def.impl.NpcType
import core.cache.def.impl.LocType
import core.game.interaction.OptionHandler
import core.game.node.Node
import core.game.node.entity.Entity
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.plugin.Plugin

@Initializable
class PitfallOptionHandler : OptionHandler() {
    val graahkPitIds = intArrayOf(19227, 19268, 19267, 19266, 19264, 19265)
    val graahkIds = 5105
    val levelRequirements = 41

    override fun handle(player: Player?, node: Node?, option: String?): Boolean {
        node ?: return true
        player ?: return true
        when (option) {
            "tease" -> {
                (node as Entity).attack(player)
            }
        }
        return true
    }

    override fun newInstance(arg: Any?): Plugin<Any> {
        for (graahkPit in graahkPitIds) {
            LocType.forId(graahkPit).handlers["option:trap"] = this
            NpcType.forId(graahkIds).handlers["option:tease"] = this
        }
        return this
    }
}
