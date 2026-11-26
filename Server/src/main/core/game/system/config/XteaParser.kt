package core.game.system.config

import com.google.gson.Gson
import com.google.gson.JsonObject
import core.ServerConstants
import core.api.log
import core.tools.Log
import java.io.FileReader

class XteaParser {
    companion object {
        val REGION_XTEA = HashMap<Int, IntArray>()
        private val DEFAULT_REGION_KEYS = intArrayOf(0, 0, 0, 0)

        fun getRegionXTEA(regionId: Int): IntArray = REGION_XTEA.getOrDefault(regionId, DEFAULT_REGION_KEYS)
    }

    private val gson = Gson()

    fun load() = load(ServerConstants.CONFIG_PATH + "xteas.json")

    fun load(path: String) {
        var count = 0
        FileReader(path).use { reader ->
            val obj = gson.fromJson(reader, JsonObject::class.java)
            val configList = obj.getAsJsonArray("xteas") ?: return@use
            for (configElement in configList) {
                val e = configElement.asJsonObject
                val id = e.get("regionId")?.asInt ?: continue
                val keysString = e.get("keys")?.asString ?: continue
                val keys = keysString.split(",").map { it.trim().toInt() }
                if (keys.size >= 4) {
                    REGION_XTEA[id] = intArrayOf(keys[0], keys[1], keys[2], keys[3])
                    count++
                }
            }
        }
        log(this::class.java, Log.INFO, "Parsed $count region keys.")
    }
}
