package core.game.node.entity.player.link.diary

import com.google.gson.JsonObject
import core.api.*
import core.cache.def.impl.NPCDefinition
import core.game.diary.DiaryLevel
import core.game.node.entity.player.Player
import core.game.node.item.Item
import shared.consts.Components
import java.util.*

/**
 * Represents an Achievement Diary.
 *
 * @property type The type of the diary.
 */
class Diary(
    /**
     * The type of this diary.
     */
    val type: DiaryType
) {
    /**
     * Flags indicating whether each diary level has been started.
     * Indexed by level ordinal.
     */
    val levelStarted: BooleanArray = BooleanArray(type.achievements.size)

    /**
     * Flags indicating whether the reward for each diary level has been claimed.
     * Indexed by level ordinal.
     */
    val levelRewarded: BooleanArray = BooleanArray(type.achievements.size)

    /**
     * Flags indicating whether each task at each level has been completed.
     * Indexed as [level][taskIndex].
     */
    val taskCompleted: Array<BooleanArray> = Array(type.achievements.size) { BooleanArray(type.achievements[it].size) }

    // Reconstructed bit assignment; native server-side meanings are not available.
    var ardougneCrossbowMask: Int = 0
        private set

    enum class CrossbowStep(val bit: Int) {
        SINEW(1), STRING(2), STOCK(4), LIMBS(8), ASSEMBLE(16), STRING_CROSSBOW(32)
    }

    /** Call only after a successful production action in a verified task location. */
    fun recordArdougneCrossbowStep(player: Player, step: CrossbowStep) {
        if (type != DiaryType.ARDOUGNE || !isStarted(3) || isComplete(3, 4)) return
        if (ardougneCrossbowMask and step.bit != 0) return
        ardougneCrossbowMask = ardougneCrossbowMask or step.bit
        if (ardougneCrossbowMask == 63) finishTask(player, 3, 4)
        else updateTask(player, 3, 4, false)
    }

    /**
     * Updates the diary interface with the current completion status of each level.
     *
     * @param player The player whose diary interface will be updated.
     */
    fun drawStatus(player: Player) {
        val startedBit = when (type) {
            DiaryType.KARAMJA -> 3576
            DiaryType.VARROCK -> 3981
            DiaryType.LUMBRIDGE -> 4940
            DiaryType.FREMENNIK -> 5644
            DiaryType.FALADOR -> 5687
            DiaryType.SEERS_VILLAGE -> 5778
            DiaryType.ARDOUGNE -> 6597
        }
        setVarbit(player, startedBit, if (isStarted) 1 else 0)
        // Aggregate completion flags are independent of individual task bits.
        val completionBits = when (type) {
            DiaryType.KARAMJA -> intArrayOf(3578, 3599, 3611)
            DiaryType.VARROCK -> intArrayOf(3982, 3983, 3984)
            DiaryType.LUMBRIDGE -> intArrayOf(4942, 4944, 4945)
            DiaryType.FALADOR -> intArrayOf(5688, 5689, 5690)
            DiaryType.SEERS_VILLAGE -> intArrayOf(5779, 5780, 5781)
            DiaryType.FREMENNIK, DiaryType.ARDOUGNE -> intArrayOf()
        }
        // Karamja's client expects a sum of six; other aggregate flags are one bit.
        for ((level, bit) in completionBits.withIndex()) {
            setVarbit(player, bit, if (isComplete(level)) {
                if (type == DiaryType.KARAMJA) 2 else 1
            } else 0)
        }
        if (type == DiaryType.ARDOUGNE) {
            setVarbit(player, 6657, if (isStarted(3)) 1 else 0)
            for ((level, bits) in ARDOUGNE_TIER_BITS.withIndex()) {
                for ((index, bit) in bits.withIndex()) {
                    setVarbit(player, bit, if (isComplete(level, index)) 1 else 0)
                }
                setVarbit(player, ARDOUGNE_COMPLETION_BITS[level], if (isComplete(level)) 1 else 0)
            }
            for (index in 0..3) setVarbit(player, 6658 + index, if (isComplete(3, index)) 1 else 0)
            setVarbit(player, 6662, if (isComplete(3, 4)) 63 else ardougneCrossbowMask)
            setVarbit(player, 6663, if (isComplete(3)) 1 else 0)
        }

    }

    /**
     * Opens the achievement diary interface for the given player and displays all tasks.
     *
     * @param player The player for whom the diary interface is opened.
     */
    fun open(player: Player) {
        // Revision 578 journal has 300 text rows at components 16..315.
        val lines = mutableListOf<String>()
        lines.add("${type.displayName} Area Tasks")
        if (type.info.isNotEmpty() && !isStarted) {
            lines.addAll(type.info.split("<br><br>"))
        }
        lines.add("")
        type.achievements.forEachIndexed { level, achievements ->
            if (type == DiaryType.ARDOUGNE && level == 3 && !isStarted(3)) return@forEachIndexed
            lines.add(getLevel(level))
            lines.add("")
            achievements.forEachIndexed { index, task ->
                task.split("<br><br>").filter { it.isNotBlank() }.forEach { line ->
                    lines.add(if (isComplete(level, index)) "<str>$line</str>" else line)
                }
                lines.add("")
            }
        }
        require(lines.size <= 300) { "Diary journal exceeds revision 578 row capacity: ${type.name}" }
        if (player.interfaceManager.opened?.id != DIARY_COMPONENT) {
            openInterface(player, DIARY_COMPONENT)
        }
        if (player.interfaceManager.opened?.id != DIARY_COMPONENT) return
        sendString(player, "Achievement Diary - ${type.displayName}", 2)
        setComponentVisibility(player, DIARY_COMPONENT, 11, true)
        setComponentVisibility(player, DIARY_COMPONENT, 316, true)
        for (row in 0 until 300) {
            sendString(player, lines.getOrElse(row) { "" }, 16 + row)
        }
        // CS2 1207 takes row count first, then the reset-to-top flag.
        player.packetDispatch.sendRunScript(1207, "ii", lines.size, 1)
    }

    /**
     * Parses saved diary data from JSON and updates the diary's internal state.
     *
     * @param data The JSON object containing startedLevels, completedLevels, and rewardedLevels.
     */
    fun parse(data: JsonObject) {
        data.getAsJsonArray("startedLevels")?.forEachIndexed { i, element ->
            if (i in levelStarted.indices) levelStarted[i] = element.asBoolean
        }

        data.getAsJsonArray("completedLevels")?.forEachIndexed { i, levelArray ->
            levelArray.asJsonArray.forEachIndexed { j, task ->
                if (i in taskCompleted.indices && j in taskCompleted[i].indices) taskCompleted[i][j] = task.asBoolean
            }

        }

        if (type == DiaryType.ARDOUGNE) {
            ardougneCrossbowMask = if (isComplete(3, 4)) 63 else
                (data.get("ardougneCrossbowMask")?.takeIf { it.isJsonPrimitive }?.asInt ?: 0) and 63
        }

        data.getAsJsonArray("rewardedLevels")?.forEachIndexed { i, element ->
            if (i in levelRewarded.indices) levelRewarded[i] = element.asBoolean
        }
    }

    /**
     * Updates the task status in the interface.
     *
     * @param player The player whose task is being updated.
     * @param level The level index of the task.
     * @param index The task index within the level.
     * @param complete True if the task has been completed, false if only updated.
     */
    fun updateTask(player: Player, level: Int, index: Int, complete: Boolean) {
        if (level !in taskCompleted.indices || index !in taskCompleted[level].indices) return
        if (complete && taskCompleted[level][index]) return
        if (type == DiaryType.ARDOUGNE && level == 3 && !isStarted(3)) return
        if (!levelStarted[level]) levelStarted[level] = true

        if (!complete) {
            sendMessage(player, "Well done! A ${type.displayName} task has been updated.")
        } else {
            taskCompleted[level][index] = true
            val tempLevel = if (type == DiaryType.LUMBRIDGE) level - 1 else level
            sendMessages(player,
                "Well done! You have completed ${
                    when (tempLevel) {
                        -1 -> "a beginner"
                        0 -> "an easy"
                        1 -> "a medium"
                        3 -> if (type == DiaryType.ARDOUGNE) "an elite" else "a hard"
                        else -> "a hard"
                    }
                } task in the ${type.displayName} area. Your", "Achievement Diary has been updated."
            )
        }

        if (isComplete(level)) {
            val npcName = if (type == DiaryType.ARDOUGNE && level == 1) "the Ardougne town crier" else NPCDefinition.forId(type.getNpc(level)).name
            val msg =
                "Congratulations! You have completed all of the ${getLevel(level).lowercase()} tasks in the ${type.displayName} area."
            sendMessage(player, "$msg Speak to $npcName to claim your reward.")
            sendDialogue(player, "$msg Speak to $npcName to claim your reward.")
        }
        drawStatus(player)
    }

    /**
     * Marks a task as finished for the player and updates the completion status of the level.
     *
     * @param player The player whose task is finished.
     * @param level The level index of the task.
     * @param index The task index within the level.
     */
    fun finishTask(player: Player, level: Int, index: Int) {
        if (!isComplete(level, index)) {
            updateTask(player, level, index, true)

        }
    }

    /**
     * Resets a specific task to incomplete and updates level start/reward status.
     *
     * @param player The player whose task is being reset.
     * @param level The level index of the task.
     * @param index The task index within the level.
     */
    fun resetTask(player: Player, level: Int, index: Int) {
        if (level !in taskCompleted.indices || index !in taskCompleted[level].indices) return
        if (type == DiaryType.ARDOUGNE && level == 3 && index == 4) ardougneCrossbowMask = 0
        taskCompleted[level][index] = false
        if (taskCompleted[level].none { it } && !(type == DiaryType.ARDOUGNE && level == 3)) levelStarted[level] = false
        if (!isComplete(level)) levelRewarded[level] = false
        drawStatus(player)
    }

    /**
     * Sends a formatted string to the diary interface for a specific child component.
     *
     * @param player The player to whom the string will be sent.
     * @param string The text to display.
     * @param child The interface child component index.
     */
    private fun sendString(player: Player, string: String, child: Int) {
        sendString(player, string.replace("<blue>", BLUE).replace("<red>", RED), DIARY_COMPONENT, child)
    }

    /**
     * Checks if a level has been started.
     *
     * @param level The level index.
     * @return True if the level has been started, false otherwise.
     */
    fun isStarted(level: Int): Boolean = levelStarted.getOrNull(level) == true

    /**
     * Checks if any level in this diary has been started.
     */
    val isStarted: Boolean
        get() = type.levelNames.indices.any { isStarted(it) }

    /**
     * Checks if a specific task has been completed.
     *
     * @param level The level index.
     * @param index The task index.
     * @return True if the task is complete.
     */
    fun isComplete(level: Int, index: Int): Boolean = taskCompleted.getOrNull(level)?.getOrNull(index) == true

    /**
     * Checks if all tasks in a level are complete.
     *
     * @param level The level index.
     * @return True if the level is complete.
     */
    fun isComplete(level: Int): Boolean = taskCompleted.getOrNull(level)?.let { it.isNotEmpty() && it.all { done -> done } } == true

    /**
     * Checks if a level is complete, optionally checking cumulatively previous levels.
     *
     * @param level The level index.
     * @param cumulative Whether to include previous levels in the check.
     * @return True if the level (and optionally previous levels) are complete.
     */
    fun isComplete(level: Int, cumulative: Boolean): Boolean =
        isComplete(level) && (!cumulative || level <= 0 || isComplete(level - 1, true))


    /**
     * Checks if all levels in this diary are complete.
     */
    val isComplete: Boolean
        get() = taskCompleted.indices.all { isComplete(it) }

    /**
     * Returns the highest completed level, or -1 if none.
     */
    val level: Int
        get() = taskCompleted.indices.reversed().firstOrNull { isComplete(it) } ?: -1

    /**
     * Returns the highest rewarded level, or -1 if none.
     */
    val reward: Int
        get() = levelRewarded.indices.reversed().firstOrNull { isLevelRewarded(it) } ?: -1

    /**
     * Gets the name of a level.
     *
     * @param level The level index.
     * @return The display name of the level.
     */
    fun getLevel(level: Int): String = type.levelNames[level]

    /**
     * Returns the color code representing the status of a level.
     *
     * @param level The level index.
     * @return GREEN if complete, YELLOW if started, RED if not started.
     */
    fun getStatus(level: Int): String = when {
        !isStarted(level) -> RED
        isComplete(level) -> GREEN
        else -> YELLOW
    }

    /**
     * Marks a level as rewarded.
     *
     * @param level The level index.
     */
    fun setLevelRewarded(level: Int) {
        levelRewarded[level] = true
    }

    /**
     * Checks if a level has been rewarded.
     *
     * @param level The level index.
     * @return True if the level reward has been claimed.
     */
    fun isLevelRewarded(level: Int): Boolean = levelRewarded.getOrNull(level) == true

    companion object {
        private val ARDOUGNE_TIER_BITS = arrayOf(
            (6598..6609).toList() + 1421 + (6611..6620).toList(),
            (6622..6627).toList() + 6650 + (6629..6637).toList(),
            (6639..6649).toList() + 6628 + (6651..6655).toList()
        )
        private val ARDOUGNE_COMPLETION_BITS = intArrayOf(6621, 6638, 6656)

        /**
         * The interface component for the diary.
         * */
        const val DIARY_COMPONENT: Int = Components.QUESTJOURNAL_SCROLL_275


        private const val RED = "<col=8A0808>"
        private const val BLUE = "<col=08088A>"
        private const val YELLOW = "<col=F7FE2E>"
        private const val GREEN = "<col=3ADF00>"

        /**
         * Removes the reward for a specific level from the player.
         *
         * @param player The player.
         * @param type The diary type.
         * @param level The level index.
         * @return True if a reward was removed.
         */
        fun removeRewardsFor(player: Player, type: DiaryType, level: Int): Boolean {
            val reward = type.rewards.getOrNull(level)?.firstOrNull() ?: return false // Only first reward is removable.
            val removed =
                removeItem(player,reward, Container.INVENTORY) || removeItem(player,reward, Container.BANK) || removeItem(player,reward, Container.EQUIPMENT)

            if (removed) player.debug("Removed previous reward")
            return removed
        }

        /**
         * Adds rewards for a specific level to the player's inventory.
         *
         * @param player The player.
         * @param type The diary type.
         * @param level The level index.
         * @return True if all rewards were successfully added.
         */
        fun addRewardsFor(player: Player, type: DiaryType, level: Int): Boolean {
            if (level !in type.rewards.indices) return false
            val rewards = type.getRewards(level)
            if (rewards.isEmpty() || rewards.any { it.amount <= 0 }) return false
            // Container.add may silently truncate a requested quantity. Preflight
            // all rewards together, including multiple non-stackable lamps.
            val quantities = rewards.groupBy { it.id }.mapValues { (_, items) ->
                items.sumOf { it.amount.toLong() }
            }
            var slots = 0L
            for ((id, amount) in quantities) {
                if (amount > Int.MAX_VALUE) return false
                val item = Item(id, amount.toInt())
                if (!player.inventory.hasSpaceFor(item)) return false
                slots += if (item.definition.isStackable) {
                    if (amountInInventory(player, id) > 0) 0L else 1L
                } else amount
            }
            if (slots > freeSlots(player)) return false

            val before = quantities.keys.associateWith { amountInInventory(player, it) }
            for (reward in rewards) {
                val previous = amountInInventory(player, reward.id)
                val success = addItem(player, reward.id, reward.amount)
                if (!success || amountInInventory(player, reward.id) - previous != reward.amount) {
                    for ((id, count) in before) {
                        val added = amountInInventory(player, id) - count
                        if (added > 0) removeItem(player, Item(id, added), Container.INVENTORY)
                    }
                    return false
                }
            }
            return true
        }

        /**
         * Flags a level as rewarded and handles reward replacement.
         *
         * @param player The player.
         * @param type The diary type.
         * @param level The level index.
         * @return True if the reward was successfully granted.
         */
        fun flagRewarded(player: Player, type: DiaryType, level: Int): Boolean {
            if (level !in type.rewards.indices) return false
            if (!canClaimLevelRewards(player, type, level)) return false

            return if (addRewardsFor(player, type, level)) {
                if (type != DiaryType.ARDOUGNE || level != 3) {
                    for (previous in 0 until level) removeRewardsFor(player, type, previous)
                }
                player.achievementDiaryManager.getDiary(type)?.let {
                    it.setLevelRewarded(level)
                    it.drawStatus(player)
                }
                true
            } else {
                sendMessage(player, "You do not have enough space in your inventory to claim these rewards.")
                false
            }
        }

        /**
         * Checks if the reward for a level can be replaced.
         *
         * @param player The player.
         * @param type The diary type.
         * @param level The level index.
         * @return True if replacement is possible.
         */
        fun canReplaceReward(player: Player, type: DiaryType, level: Int): Boolean {
            if (type == DiaryType.ARDOUGNE && level == 3) return false
            val reward = type.rewards.getOrNull(level)?.firstOrNull() ?: return false
            val claimed = hasCompletedLevel(player, type, level) && hasClaimedLevelRewards(
                player,
                type,
                level
            ) && !player.hasItem(reward)

            val replacementLimit = if (type == DiaryType.ARDOUGNE) 3 else type.rewards.size
            return claimed && (level + 1 until replacementLimit).none { hasClaimedLevelRewards(player, type, it) }
        }

        /**
         * Grants a replacement reward if possible.
         *
         * @param player The player.
         * @param type The diary type.
         * @param level The level index.
         * @return True if the replacement was successfully granted.
         */
        fun grantReplacement(player: Player, type: DiaryType, level: Int): Boolean {
            val reward = type.rewards.getOrNull(level)?.firstOrNull() ?: return false
            return canReplaceReward(player, type, level) && addItem(player, reward.id)
        }

        /**
         * Checks if a level has been completed.
         *
         * @param player The player.
         * @param type The diary type.
         * @param level The level index.
         * @return True if the level is complete.
         */
        fun hasCompletedLevel(player: Player, type: DiaryType, level: Int): Boolean {
            if (level !in type.levelNames.indices) return false
            return player.achievementDiaryManager.getDiary(type)?.isComplete(level, cumulative = true) == true
        }

        /**
         * Checks if a level's reward has been claimed.
         *
         * @param player The player.
         * @param type The diary type.
         * @param level The level index.
         * @return True if the reward has been claimed.
         */
        fun hasClaimedLevelRewards(player: Player, type: DiaryType?, level: Int): Boolean {
            return player.achievementDiaryManager.getDiary(type)?.isLevelRewarded(level) == true
        }

        /**
         * Checks if a level's rewards can be claimed.
         *
         * @param player The player.
         * @param type The diary type.
         * @param level The level index.
         * @return True if rewards can be claimed.
         */
        fun canClaimLevelRewards(player: Player, type: DiaryType, level: Int): Boolean {
            if (level !in type.rewards.indices) return false
            val nextLevelClaimed = (level + 1 until type.rewards.size).any { hasClaimedLevelRewards(player, type, it) }
            val currentClaimed = hasClaimedLevelRewards(player, type, level)
            val completed = hasCompletedLevel(player, type, level)
            return completed && !currentClaimed && !nextLevelClaimed
        }

        /**
         * Returns the rewards for a given diary type and level.
         *
         * @param type The diary type.
         * @param level The level index.
         * @return Array of items rewarded at this level.
         */
        fun getRewards(type: DiaryType, level: Int): Array<Item> = type.getRewards(level)
    }
}
