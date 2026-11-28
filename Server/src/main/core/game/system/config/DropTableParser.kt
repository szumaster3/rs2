package core.game.system.config

import com.google.gson.Gson
import com.google.gson.JsonArray
import core.ServerConstants
import core.api.exceptionToString
import core.api.log
import core.api.utils.ConfigParseException
import core.api.utils.NPCDropTable
import core.api.utils.WeightedItem
import core.cache.def.impl.NpcType
import core.tools.Log
import java.io.FileReader

class DropTableParser {
    private val gson = Gson()

    fun load() {
        var count = 0
        FileReader(ServerConstants.CONFIG_PATH + "drop_tables.json").use { reader ->
            val obj = gson.fromJson(reader, JsonArray::class.java)
            for (element in obj) {
                val tab = element.asJsonObject
                val ids = tab.get("ids").asString.split(",")

                try {
                    val table = NPCDropTable()
                    parseTable(tab.getAsJsonArray("main"), table, isAlways = false)
                    parseTable(tab.getAsJsonArray("charm"), table, isAlways = false, isCharms = true)
                    tab.getAsJsonArray("tertiary")?.let { parseTable(it, table, isAlways = false, isTertiary = true) }
                    parseTable(tab.getAsJsonArray("default"), table, true)

                    for (n in ids) {
                        val def = NpcType.forId(n.toInt()).dropTables
                        def.table = table
                        count++
                    }
                } catch (e: ConfigParseException) {
                    val npcName = NpcType.forId(ids[0].toInt()).name
                    log(this::class.java, Log.ERR, "Error parsing drop tables for NPC $npcName: ${exceptionToString(e)}")
                }
            }
        }
        log(this::class.java, Log.INFO, "Parsed $count drop tables.")
    }

    private fun parseTable(
        data: JsonArray?,
        destinationTable: NPCDropTable,
        isAlways: Boolean,
        isTertiary: Boolean = false,
        isCharms: Boolean = false,
    ) {
        if (data == null) return

        for (element in data) {
            val item = element.asJsonObject
            val id = item.get("id").asInt
            val minAmount = item.get("minAmount").asInt
            val maxAmount = item.get("maxAmount").asInt

            if (minAmount > maxAmount) {
                throw ConfigParseException("Table is invalid! Specified minimum amount is > specified maximum amount.")
            }

            val weight = item.get("weight").asDouble
            val newItem = WeightedItem(id, minAmount, maxAmount, weight, isAlways)

            when {
                isCharms -> destinationTable.addToCharms(newItem)
                isTertiary -> destinationTable.addToTertiary(newItem)
                else -> destinationTable.add(newItem)
            }
        }
    }
}
