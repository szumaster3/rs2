package content.global.skill.summoning.familiar.npc

import core.cache.def.impl.NpcType
import core.game.interaction.OptionHandler
import core.game.node.Node
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.plugin.Plugin
import shared.consts.NPCs

@Initializable
class LavaTitanOptionPlugin : OptionHandler() {
    @Throws(Throwable::class)
    override fun newInstance(arg: Any?): Plugin<Any> {
        NpcType.forId(NPCs.LAVA_TITAN_7341).handlers["option:interact"] = this
        return this
    }

    override fun handle(player: Player, node: Node, option: String): Boolean {
        player.dialogueInterpreter.open(NPCs.LAVA_TITAN_7341, node.asNpc())
        return true
    }
}
