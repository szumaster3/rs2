package core.game.system.command.oldsys

import core.cache.def.impl.ObjType
import core.cache.def.impl.NpcType
import core.cache.def.impl.LocType
import core.game.node.entity.player.Player
import core.game.system.command.CommandPlugin
import core.game.system.command.CommandSet
import core.plugin.Initializable
import core.plugin.Plugin
import java.io.File

@Initializable
class DumpCommand : CommandPlugin() {

    override fun parse(player: Player?, name: String?, args: Array<String?>?): Boolean {
        if (name != "make") return false

        handleMake(player, args?.filterNotNull() ?: emptyList())
        return true
    }

    private fun handleMake(player: Player?, args: List<String>) {
        if (args.size < 3) {
            player?.sendMessage("Usage: ::make item|object|npc list|doc")
            return
        }

        val dataType = args[1].lowercase()
        val outputType = args[2].lowercase()

        when (outputType) {
            "list" -> {
                player?.sendMessage("Creating $dataType dump list...")
                makeDumpList(dataType)
            }
            "doc" -> {
                player?.sendMessage("Creating $dataType dump doc...")
                makeDumpDoc(dataType)
            }
            else -> player?.sendMessage("Unknown output type: $outputType. Use 'list' or 'doc'.")
        }
    }

    private fun makeDumpList(type: String) {
        val file = File("${System.getProperty("user.dir")}${File.separator}${type}list.txt")
        file.bufferedWriter().use { writer ->
            getDefinitions(type).forEach { def ->
                writer.writeLn("${def.name}(${def.id}) - ${def.examine}")
            }
        }
    }

    private fun makeDumpDoc(type: String) {
        val file = File("${System.getProperty("user.dir")}${File.separator}${type}list.html")
        file.bufferedWriter().use { writer ->
            writer.writeLn("<head>")
            writer.writeLn("<style>")
            writer.writeLn("""
                td { border-right: 1px solid black; border-bottom: 1px solid black; }
                tr { border-bottom: 1px solid black; }
                th { border: 1px solid black; }
                .item-id { color: #FF0004; }
                table { width: 100%; border-collapse: collapse; }
            """.trimIndent())
            writer.writeLn("</style>")
            writer.writeLn("</head>")
            writer.writeLn("<table>")
            writer.writeLn("<tr><th>$type name</th><th>$type ID</th><th>Examine Text</th></tr>")

            getDefinitions(type).forEach { def ->
                writer.writeLn("<tr><td>${def.name}</td><td class=\"item-id\">${def.id}</td><td>${def.examine}</td></tr>")
            }

            writer.writeLn("</table>")
        }
        println("HTML dump created at: ${file.absolutePath}")
    }

    private fun getDefinitions(type: String) = when (type) {
        "item" -> ObjType.getDefinitions().values
        "object" -> LocType.getDefinitions().values
        "npc" -> NpcType.getDefinitions().values
        else -> emptyList()
    }

    override fun newInstance(arg: Any?): Plugin<Any?> {
        link(CommandSet.ADMINISTRATOR)
        return this
    }

    private fun java.io.BufferedWriter.writeLn(line: String) {
        this.write(line)
        this.newLine()
    }
}
