package core.game.system.command.rottenpotato

import core.api.removeItem
import core.api.sendMessage
import core.cache.def.impl.ObjType
import core.game.interaction.OptionHandler
import core.game.node.Node
import core.game.node.entity.player.Player
import core.game.node.entity.player.info.Rights
import core.game.system.command.CommandSystem
import core.plugin.Initializable
import core.plugin.Plugin
import core.tools.colorize
import shared.consts.Items

private val ROTTEN_POTATO_OPTIONS = arrayOf("RS HD", "Heal", "Extra", "Commands")

@Initializable
class RottenPotatoOptionHandler : OptionHandler() {

    override fun newInstance(arg: Any?): Plugin<Any> {
        for (option in ROTTEN_POTATO_OPTIONS) {
            ObjType.forId(Items.ROTTEN_POTATO_5733).handlers["option:${option.lowercase()}"] = this
        }
        return this
    }

    override fun handle(player: Player?, node: Node?, option: String?): Boolean {
        player ?: return false
        node ?: return false
        option ?: return false

        if (player.rights != Rights.ADMINISTRATOR) {
            removeItem(player, Items.ROTTEN_POTATO_5733)
            return false
        }

        when (option) {
            "rs hd" -> player.dialogueInterpreter.open(RottenPotatoRSHDDialogue().ID)
            "heal" -> player.fullRestore().also { sendMessage(player, colorize("%RAll healed!")) }
            "extra" -> player.dialogueInterpreter.open(RottenPotatoExtraDialogue().ID)
            "commands" -> {
                CommandSystem.commandSystem.parse(player, "commands")
            }
        }
        return true
    }
}
