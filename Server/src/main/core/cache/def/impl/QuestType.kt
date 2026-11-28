package core.cache.def.impl

import core.cache.Cache
import core.cache.misc.buffer.ByteBufferUtils
import java.nio.ByteBuffer

/** Revision 578 config index 2, group 35 (QuestType / QuestTypeList). */
class QuestType private constructor(val id: Int) {
    var name: String? = null
        private set
    var sortName: String? = null
        private set
    var questPoints = 0
        private set
    var members = false
        private set
    var icon = -1
        private set
    data class Progress(val variable: Int, val started: Int, val completed: Int)
    data class SkillRequirement(val skill: Int, val level: Int)
    data class RangeRequirement(val variable: Int, val minimum: Int, val maximum: Int, val description: String)
    var varps = emptyList<Progress>()
        private set
    var varbits = emptyList<Progress>()
        private set
    var prerequisiteQuests = emptyList<Int>()
        private set
    var skills = emptyList<SkillRequirement>()
        private set
    var varpRequirements = emptyList<RangeRequirement>()
        private set
    var varbitRequirements = emptyList<RangeRequirement>()
        private set
    var requiredQuestPoints = 0
        private set
    var category = 0
        private set
    var difficulty = 0
        private set
    var opcode5Value = 0
        private set
    var opcode10Values = emptyList<Int>()
        private set
    var opcode12Value = 0
        private set
    private val parameters = mutableMapOf<Int, Any>()
    val params: Map<Int, Any> get() = parameters.toMap()

    private fun decode(buffer: ByteBuffer) {
        fun u1() = buffer.get().toInt() and 255
        fun u2() = buffer.short.toInt() and 65535
        fun string() = ByteBufferUtils.getString(buffer)
        fun versionedString(): String { require(u1() == 0); return string() }
        fun progress() = List(u1()) { Progress(u2(), buffer.int, buffer.int) }
        fun ranges() = List(u1()) { RangeRequirement(buffer.int, buffer.int, buffer.int, string()) }
        while (true) {
            when (val opcode = u1()) {
                0 -> { if (sortName == null) sortName = name; require(!buffer.hasRemaining()); return }
                1 -> name = versionedString()
                2 -> sortName = versionedString()
                3 -> varps = progress()
                4 -> varbits = progress()
                5 -> opcode5Value = u2()
                6 -> category = u1()
                7 -> difficulty = u1()
                8 -> members = true
                9 -> questPoints = u1()
                10 -> opcode10Values = List(u1()) { buffer.int }
                12 -> opcode12Value = buffer.int
                13 -> prerequisiteQuests = List(u1()) { u2() }
                14 -> skills = List(u1()) { SkillRequirement(u1(), u1()) }
                15 -> requiredQuestPoints = u2()
                17 -> icon = u2()
                18 -> varpRequirements = ranges()
                19 -> varbitRequirements = ranges()
                249 -> repeat(u1()) {
                    val isString = u1() == 1
                    val key = (u1() shl 16) or (u1() shl 8) or u1()
                    parameters[key] = if (isString) string() else buffer.int
                }
                else -> error("Unknown quest opcode $opcode for quest $id at ${buffer.position() - 1}")
            }
        }
    }

    companion object {
        @JvmStatic fun fromBytes(id: Int, data: ByteArray): QuestType =
            QuestType(id).apply { decode(ByteBuffer.wrap(data)) }

        @JvmStatic fun forId(id: Int): QuestType? {
            if (id < 0) return null
            return definitions[id]
        }

        private val definitions by lazy { decodeAll() }

        private val definitionsByName by lazy {
            definitions.values.filter { it.name != null }.groupBy { it.name!! }
        }

        @JvmStatic fun forName(name: String): QuestType? =
            definitionsByName[when (name) {
            "Elemental Workshop I" -> "The Elemental Workshop"
            "Elemental Workshop II" -> "The Elemental Workshop 2"
            "Romeo & Juliet" -> "Romeo and Juliet"
            "Watchtower" -> "The Watchtower"
            "Rum Deal" -> "A Rum Deal"
            "The Dig Site" -> "Dig Site"
            "The Fremennik Isles" -> "Fremennik Isles"
            else -> name
        }]?.singleOrNull()

        @JvmStatic fun nameOf(id: Int): String = requireNotNull(forId(id)?.name) { "Missing quest name for cache ID $id" }

        @JvmStatic fun loadAll(): Map<Int, QuestType> = definitions

        private fun decodeAll(): Map<Int, QuestType> {
            val index = checkNotNull(Cache.getIndexes().getOrNull(2)) { "Config index is unavailable" }
            val ids = checkNotNull(index.getFileIds(35)) { "Quest config group is unavailable" }
            return ids.associateWith { fromBytes(it, checkNotNull(index.getFileData(35, it)) { "Missing quest definition $it" }) }
        }
    }
}
