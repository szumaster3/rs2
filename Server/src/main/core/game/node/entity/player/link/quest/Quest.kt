package core.game.node.entity.player.link.quest

import core.api.*
import content.data.Quests
import core.cache.def.impl.QuestDefinition
import core.game.component.Component
import core.game.node.entity.player.Player
import core.plugin.Plugin
import core.plugin.PluginManifest
import core.plugin.PluginType
import shared.consts.Components

/**
 * A skeleton plugin for a quest.
 * @author Vexia
 */
@PluginManifest(type = PluginType.QUEST)
abstract class Quest(
    val index: Int, vararg configs: Int
) : Plugin<Any> {

    constructor(quest: Quests, vararg configs: Int) : this(quest.id, *configs)

    val configs: IntArray = configs

    val definition: QuestDefinition by lazy { checkNotNull(QuestDefinition.forId(index)) { "Missing cache quest definition $index" } }
    val name: String get() = definition.let { checkNotNull(it.name) }
    val questPoints: Int get() = definition.questPoints

    companion object {
        const val RED = core.tools.DARK_RED
        const val BRIGHT_RED = core.tools.RED
        const val BLUE = core.tools.DARK_BLUE
        const val BLACK = core.tools.BLACK

        const val JOURNAL_COMPONENT = Components.QUESTJOURNAL_SCROLL_275
        const val REWARD_COMPONENT = Components.QUEST_COMPLETE_SCROLL_277
    }

    override fun fireEvent(identifier: String, vararg args: Any): Any? = null

    /**
     * Returns a new instance of the quest.
     */
    abstract override fun newInstance(`object`: Any?): Quest

    open fun start(player: Player) {
        setQuestStage(player, this.name, 10)
        updateQuestTab(player)
    }

    open fun drawJournal(player: Player, stage: Int) {
        QuestJournal.clear(player, "$RED$name</col>")
    }

    open fun finish(player: Player) {
        if (isQuestComplete(player, name)) {
            throw IllegalStateException("Tried to complete quest $name twice, which is not allowed!")
        }

        for (i in 0 until 18) {
            if (i == 9 || i == 3 || i == 6) {
                continue
            }
            sendString(player, "", REWARD_COMPONENT, i)
        }

        setQuestStage(player, this.name, 100)
        player.questRepository.incrementPoints(questPoints)
        updateQuestTab(player)
        player.interfaceManager.open(Component(REWARD_COMPONENT).setUncloseEvent { p, c ->
            questCloseEvent(p, c)
            true
        })
        sendString(player, "${player.questRepository.points}", REWARD_COMPONENT, 7)
        sendString(player, "You have completed the $name!", REWARD_COMPONENT, 4)
        sendMessage(player, "Congratulations! You have completed the $name!")
        playJingle(player, (152..154).random())
    }

    open fun reset(player: Player) {
    }

    open fun questCloseEvent(player: Player, component: Component) {
    }

    /**
     * Draws a line on the journal component.
     * @param player The player.
     * @param message The message.
     * @param line The line.
     */
    fun line(player: Player, message: String, line: Int) {
        var send = BLUE + message.replace("<n>", "<br><br>").replace("<blue>", BLUE).replace("<red>", RED)

        if (send.contains("<br><br>") || send.contains("<n>")) {
            val lines = send.split(if (send.contains("<br><br>")) "<br><br>" else "<n>")
            var currentLine = line

            for (text in lines) {
                line(
                    player, text.replace("<br><br>", "").replace("<n>", ""), currentLine, false
                )
                currentLine++
            }
        } else {
            send = send.replace("!!", RED).replace("??", BLUE).replace("---", "$BLACK<str>").replace("/--", BLUE)
                .replace("%%", BRIGHT_RED).replace("&&", BLUE)

            line(player, send, line, false)
        }
    }

    /**
     * Draws a line on the quest journal component.
     * @param player The player.
     * @param message The message.
     * @param line The line number.
     * @param crossed True if the message should be crossed out.
     */
    fun line(player: Player, message: String, line: Int, crossed: Boolean) {
        val send = if (!crossed) {
            (BLUE + message.replace("<n>", "<br><br>").replace("<blue>", BLUE).replace("<red>", RED)).replace("!!", RED)
                .replace("??", BLUE).replace("%%", BRIGHT_RED).replace("&&", BLUE)
        } else {
            (BLACK + message.replace("<n>", "<br><br>").replace("<blue>", "").replace("<red>", "RED")).replace("!!", "")
                .replace("??", "").replace("%%", "").replace("&&", "")
        }

        writeJournalLine(player, if (crossed) "<str>$send</str>" else send, line)
    }

    /** Writes preformatted journal text using a legacy quest line number. */
    fun writeJournalLine(player: Player, message: String, line: Int) {
        QuestJournal.writeLegacy(player, line, message)
    }

    /**
     * Draws text on the quest reward component.
     * @param player The player.
     * @param string The string to draw.
     * @param line The line number to draw on.
     */
    fun drawReward(player: Player, string: String, line: Int) {
        sendString(player, string, REWARD_COMPONENT, line)
    }

    /**
     * Sets the player instanced stage.
     * @param player The player.
     * @param stage The stage to set.
     */
    open fun setStage(player: Player, stage: Int) {
        setQuestStage(player, this.name, stage)
    }

    /**
     * Gets the config id based on the stage.
     * @param player The player.
     * @param stage The stage.
     * @return The config data.
     */
    open fun getConfig(player: Player, stage: Int): IntArray {
        if (configs.isEmpty()) {
            val def = checkNotNull(definition) { "Missing cache quest definition for $name" }
            val progress = (def.varps + def.varbits).single()
            val value = if (stage == 0) 0 else if (stage >= 100) progress.completed else progress.started
            return if (def.varbits.isNotEmpty()) intArrayOf(0, progress.variable, value)
            else intArrayOf(progress.variable, value)
        }
        if (configs.size < 4) {
            throw IndexOutOfBoundsException(
                "Quest -> $name configs array length was not valid. config length = ${configs.size}!"
            )
        }

        return if (configs.size >= 5) {
            intArrayOf(
                configs[0], configs[1], if (stage == 0) configs[2] else if (stage >= 100) configs[4] else configs[3]
            )
        } else {
            intArrayOf(
                configs[0], if (stage == 0) configs[1] else if (stage >= 100) configs[3] else configs[2]
            )
        }
    }

    /**
     * Limits the quest log scroll to the number of lines minus 9.
     * Assumes that you start at line 11 or line 12.
     * Call this function at the end of drawJournal like: limitScrolling(player, line).
     */
    fun limitScrolling(player: Player, line: Int, startAtTop: Boolean) {
        // The tab listener resizes once after all rows have been written.
        QuestJournal.setScrollPreference(player, startAtTop)
    }

    open fun updateVarps(player: Player) {
    }

    /**
     * Checks if the quest is in progress.
     * @param player The player.
     * @return {@code True} if so.
     */
    fun isStarted(player: Player): Boolean = getStage(player) > 0 && getStage(player) < 100

    /**
     * Checks if the quest is completed.
     * @param player The player.
     * @return {@code True} if so.
     */
    fun isCompleted(player: Player): Boolean = getStage(player) >= 100

    /**
     * Gets the player instanced stage of this quest.
     * @param player The player.
     * @return The stage.
     */
    fun getStage(player: Player): Int = player.questRepository.getStage(this)

    /**
     * Checks the requirements for the quest.
     * @param player The player
     * @return {@code True} if so.
     */
    open fun hasRequirements(player: Player): Boolean = true

    override fun toString(): String =
        "Quest [name=$name, index=$index, questPoints=$questPoints, configs=${configs.contentToString()}]"
}
