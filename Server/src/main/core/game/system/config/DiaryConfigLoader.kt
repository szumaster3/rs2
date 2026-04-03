package core.game.system.config

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import core.ServerConstants
import core.api.log
import core.tools.Log
import java.io.File
import java.io.FileReader

class DiaryConfigLoader {
    private val gson = Gson()

    data class DiaryTask(
        val varbit: Int,
        val task: String
    )

    companion object
    {
        @JvmStatic
        val DIARIES: MutableMap<String, MutableMap<String, MutableList<DiaryTask>>> = mutableMapOf()

        @JvmStatic
        val TASK_BY_VARBIT: MutableMap<Int, DiaryTask> = mutableMapOf()
    }

    fun load()
    {

        var count = 0

        val data = listOf(
            "ardougne_diary.json",
            "karamja_diary.json",
            "varrock_diary.json",
            "fremennik_diary.json",
            "falador_diary.json",
            "seers_village_diary.json",
            "lumbridge_diary.json"
        )

        for (file in data)
        {
            val filePath = "${ServerConstants.CONFIG_PATH}${File.separator}diaries${File.separator}$file"
            FileReader(filePath).use { reader ->
                val obj = gson.fromJson(reader, com.google.gson.JsonObject::class.java)
                val map = mutableMapOf<String, MutableList<DiaryTask>>()

                parse(obj, "beginner", map)
                parse(obj, "easy",     map)
                parse(obj, "medium",   map)
                parse(obj, "hard",     map)

                DIARIES[file] = map

                for ((_, list) in map) {
                    for (task in list) {
                        TASK_BY_VARBIT[task.varbit] = task
                        count++
                    }
                }
            }
        }

        log(this::class.java, Log.INFO, "Loaded $count diary tasks.")
    }

    private fun parse(obj: JsonObject, tier: String, map: MutableMap<String, MutableList<DiaryTask>>)
    {
        val array: JsonArray = obj.getAsJsonArray(tier) ?: return
        val list = mutableListOf<DiaryTask>()
        for (element: JsonElement in array) {
            val e = element.asJsonObject

            list.add(
                DiaryTask(
                    varbit = e.get("varbit").asInt,
                    task = e.get("task").asString // todo remove.
                )
            )
        }

        map[tier] = list
    }
}