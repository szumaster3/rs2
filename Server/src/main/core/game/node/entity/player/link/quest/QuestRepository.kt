package core.game.node.entity.player.link.quest

import com.google.gson.JsonObject
import core.cache.def.impl.VarpDefinition
import core.api.log
import core.api.getVarp
import core.api.getVarbit
import core.cache.def.impl.QuestDefinition
import core.api.setVarbit
import core.api.setVarp
import core.game.node.entity.player.Player
import core.tools.Log

/**
 * Manages the systems/players quest repository.
 * @author Vexia
 */
class QuestRepository(private val player: Player) {

    companion object {
        private val QUESTS: MutableMap<String, Quest> = sortedMapOf()

        @JvmStatic
        fun register(quest: Quest) {
            check(QUESTS.values.none { it.index == quest.index }) { "Duplicate quest definition ${quest.index}" }
            QUESTS[quest.name] = quest
        }

        @JvmStatic
        fun getQuests(): Map<String, Quest> = QUESTS
    }

    private val quests: MutableMap<Int, Int> = HashMap()
    var points: Int = 0

    init {
        for (quest in QUESTS.values) {
            quests[quest.index] = 0
        }
    }

    /**
     * Parse quest data.
     */
    fun parse(questData: JsonObject) {
        points = questData.get("points").asInt
        val questArray = questData.getAsJsonArray("questStages")

        for (i in 0 until questArray.size()) {
            val quest = questArray[i].asJsonObject
            val questId = quest.get("questId").asInt
            val questStage = quest.get("questStage").asInt
            quests[questId] = questStage
        }

        syncPoints()
    }

    /**
     * Synchronize the quest tab.
     */
    fun syncronizeTab(player: Player) {
        setVarp(player, 101, points)
        setVarp(player, 904, QuestDefinition.loadAll().values.sumOf { it.questPoints })

        for (quest in QUESTS.values) {
            val stage = getStage(quest)
            val definition = checkNotNull(quest.definition)
            val progress = definition.varps + definition.varbits
            // Shared/multi-variable quests require their existing explicit gameplay configuration.
            val native = progress.singleOrNull()
            val shared = native != null && QuestDefinition.loadAll().values.any { other ->
                other.id != definition.id && (if (definition.varbits.isNotEmpty()) other.varbits else other.varps).any { it.variable == native.variable }
            }
            val custom = quest.javaClass.getMethod("getConfig", Player::class.java, Int::class.javaPrimitiveType!!).declaringClass != Quest::class.java
            if (native != null && !shared && !custom) {
                val bit = definition.varbits.isNotEmpty()
                val current = if (bit) getVarbit(player, native.variable) else getVarp(player, native.variable)
                val value = when {
                    stage == 0 -> 0
                    stage >= 100 -> maxOf(current, native.completed)
                    current in native.started until native.completed -> current
                    else -> native.started
                }
                if (bit) setVarbit(player, native.variable, value) else setVarp(player, native.variable, value)
                quest.updateVarps(player)
                continue
            }
            val config = quest.getConfig(player, stage)

            // {questVarpId, questVarbitId, valueToSet}
            if (config.size == 3) {
                // Set quests with VARPBIT, ignoring the VARP value.
                setVarbit(player, config[1], config[2])
            } else {
                // Original VARP quests: {questVarpId, valueToSet}
                if (!VarpDefinition.exists(config[0])) {
                    log(javaClass, Log.WARN, "Quest ${quest.name}: unmapped varp ${config[0]}")
                } else {
                    setVarp(player, config[0], config[1])
                }
            }

            quest.updateVarps(player)
        }
        // ql4_init registers this varp as the quest list refresh trigger.
        setVarp(player, 1384, getVarp(player, 1384) xor 1)
    }

    /**
     * Sets the quest stage, allowing only forward progression.
     */
    fun setStage(quest: Quest, stage: Int) {
        val oldStage = getStage(quest)

        if (oldStage < stage) {
            quests[quest.index] = stage
        } else {
            log(
                this.javaClass,
                Log.WARN,
                "Nonmonotonic QuestRepository.setStage call for player " + "\"${player.name}\", quest \"${quest.name}\", " + "old stage $oldStage, new stage $stage"
            )
        }
    }

    /**
     * Sets the quest stage without progression restrictions.
     */
    fun setStageNonmonotonic(quest: Quest, stage: Int) {
        quests[quest.index] = stage
    }

    /**
     * Increments quest points.
     */
    fun incrementPoints(value: Int) {
        points += value
    }

    /**
     * Synchronizes quest points with completed quests.
     */
    fun syncPoints() {
        var total = 0

        for (quest in QUESTS.values) {
            if (getStage(quest) >= 100) {
                total += quest.questPoints
            }
        }

        points = total
    }

    /**
     * Gets the total number of available quest points.
     */
    fun getAvailablePoints(): Int {
        var total = 0

        for (quest in QUESTS.values) {
            total += quest.questPoints
        }

        return total
    }


    /**
     * Finds a quest by its index.
     */
    fun forIndex(index: Int): Quest? {
        return QUESTS.values.firstOrNull {
            it.index == index
        }
    }

    /**
     * Checks whether all quests have been completed.
     */
    fun hasCompletedAll(): Boolean {
        return points >= getAvailablePoints()
    }

    /**
     * Checks whether a quest is complete.
     */
    fun isComplete(name: String): Boolean {
        val quest = getQuest(name)

        if (quest == null) {
            log(
                this.javaClass, Log.ERR, "Error can't check if quest is complete for $name"
            )
            return false
        }

        return quest.getStage(player) >= 100
    }

    /**
     * Checks whether a quest has been started.
     */
    fun hasStarted(name: String): Boolean {
        val quest = getQuest(name)

        if (quest == null) {
            log(
                this.javaClass, Log.ERR, "Error can't check if quest is complete for $name"
            )
            return false
        }

        return quest.getStage(player) > 0
    }

    /**
     * Gets the stage of a quest by name.
     */
    fun getStage(name: String): Int {
        val quest = getQuest(name) ?: return 0
        return getStage(quest)
    }

    /**
     * Gets the stage of a quest.
     */
    fun getStage(quest: Quest): Int {
        return quests[quest.index] ?: 0
    }

    /**
     * Gets the saved quest stages.
     * @return A map of quest indices to their current stages.
     */
    fun getQuestStages(): Map<Int, Int> = quests

    /**
     * Gets a quest by name.
     */
    fun getQuest(name: String): Quest? {
        return QUESTS[name] ?: QuestDefinition.forName(name)?.let { forIndex(it.id) }
    }
}