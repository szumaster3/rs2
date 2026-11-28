package content.global.skill.farming

import content.minigame.vinesweeper.plugin.VinesweeperPlugin
import core.cache.def.impl.NpcType
import core.game.component.Component
import core.game.interaction.OptionHandler
import core.game.node.Node
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.plugin.Plugin
import shared.consts.Components
import shared.consts.NPCs

val TL_IDS = arrayOf(NPCs.FARMING_TOOLS_LEPRECHAUN_3021, NPCs.SUM1_GOTH_LEPRECHAUN_8000, NPCs.MYARM_LEPRECHAUN_4965, NPCs.TECLYN_2861)

@Initializable
class ToolLeprechaunHandler : OptionHandler() {

    override fun newInstance(arg: Any?): Plugin<Any> {
        for (id in TL_IDS) {
            val def = NpcType.forId(id)
            def.handlers["option:exchange"] = this
            def.handlers["option:teleport"] = this
        }
        return this
    }

    override fun handle(player: Player?, node: Node?, option: String?): Boolean {
        node ?: return false
        when (option) {
            "exchange" -> player?.interfaceManager?.open(Component(Components.FARMING_TOOLS_125))
            "teleport" -> VinesweeperPlugin.Companion.VinesweeperTeleport.teleport(node as NPC, player!!)
        }
        return true
    }
}
