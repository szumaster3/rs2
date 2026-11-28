package core.game.system.config

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import core.ServerConstants
import core.api.log
import core.cache.def.impl.LocType
import core.tools.Log
import java.io.FileReader

class SceneryConfigParser {
    private val gson = Gson()

    fun load() {
        val reader = FileReader("${ServerConstants.CONFIG_PATH}object_configs.json")
        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
        val configList: List<Map<String, Any>> = gson.fromJson(reader, type)

        var count = 0

        configList.forEach { config ->
            val ids = (config["ids"] as String).split(",").map { it.toInt() }
            ids.forEach { id ->
                val def = LocType.forId(id)
                val handlers = def.handlers

                (config["examine"] as? String)?.takeIf { it.isNotBlank() && it != "null" }?.let {
                    handlers["examine"] = it
                }

                (config["render_anim"] as? Number)?.toInt()?.let {
                    def.animationId = it
                }

                count++
            }
        }

        log(this::class.java, Log.INFO, "Parsed $count object configs.")
    }
}
