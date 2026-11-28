package content.global.skill.summoning.familiar.npc

import core.cache.def.impl.NpcType
import core.game.interaction.OptionHandler
import core.game.node.Node
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.plugin.Plugin
import shared.consts.NPCs

@Initializable
class SpiritGraahkOptionPlugin : OptionHandler() {
    @Throws(Throwable::class)
    override fun newInstance(arg: Any?): Plugin<Any> {
        NpcType.forId(NPCs.LORE_GRAAHK_COMBAT_7364).handlers["option:interact"] = this
        NpcType.forId(NPCs.LORE_GRAAHK_7363).handlers["option:interact"] = this
        return this
    }

    override fun handle(player: Player, node: Node, option: String): Boolean {
        player.dialogueInterpreter.open(NPCs.LORE_GRAAHK_7363, node.asNpc())
        return true
    }
}
