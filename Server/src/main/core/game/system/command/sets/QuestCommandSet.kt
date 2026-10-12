package core.game.system.command.sets

import core.api.sendMessage
import core.api.sendString
import core.api.updateQuestTab
import core.game.component.Component
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.quest.QuestRepository
import core.game.system.command.Privilege
import core.game.world.repository.Repository
import core.plugin.Initializable
import shared.consts.Components
import java.io.File

@Initializable
class QuestCommandSet : CommandSet(Privilege.ADMIN) {

    override fun defineCommands() {

        /*
         * Command for completing all quests for the player.
         */

        define(name = "allquest", privilege = Privilege.ADMIN, usage = "::allquest", description = "Completing all quests") { player, _ ->
            for (quest in QuestRepository.getQuests().values) {
                quest.finish(player)
            }
        }

        /*
         * Command for showing quest debug info.
         * Can be used to check quests for yourself or another player.
         */

        define(name = "quest", privilege = Privilege.ADMIN, usage = "::quest", description = "Shows quest debug info") { player, args ->
            if (args.size < 3) {
                val lookupP =
                    if (args.size == 1) {
                        player
                    } else if (Repository.getPlayerByName(args[1]) != null) {
                        Repository.getPlayerByName(args[1]) ?: return@define
                    } else {
                        reject(player, "ERROR: Username not found. Usage: ::quest <username>")
                        return@define
                    }
                sendQuestsDebug(player, lookupP)
            } else {
                reject(player, "Usage: ::quest || ::quest <username>")
            }
        }

        /*
         * Command for setting a specific quest stage.
         */

        define(name = "setqueststage", Privilege.ADMIN) { player, args ->
            if (args.size < 3) {
                reject(player, "You must specify the index# of a quest, and a stage number!")
            }
            val quest = args[1].toIntOrNull() ?: reject(player, "INVALID QUEST")
            val stage = args[2].toIntOrNull() ?: reject(player, "INVALID STAGE")
            val questObject = player.questRepository.forIndex(quest as Int)
            if (questObject == null) {
                reject(player, "$quest did not match anything in the quest repository!")
            } else {
                player.questRepository.setStageNonmonotonic(questObject, stage as Int)
                if (stage == 0) {
                    questObject.reset(player)
                }
                questObject.updateVarps(player)
                updateQuestTab(player)
                notify(player, "<col=209dff>Setting " + questObject.name + " to stage $stage</col>")
            }
        }

        /*
         * Command for displaying quest information by index.
         */

        define(name = "questinfo", privilege = Privilege.ADMIN, usage = "::questinfo <index>", description = "Shows quest information by index") { player, args ->
            if (args.size < 2) {
                reject(player, "Usage: ::questinfo <index>")
                return@define
            }

            val index = args[1].toIntOrNull()
            if (index == null) {
                reject(player, "Invalid quest index!")
                return@define
            }

            val quest = player.questRepository.forIndex(index)
            if (quest == null) {
                reject(player, "No quest found with index $index!")
                return@define
            }

            sendMessage(player, quest.toString())
        }

        /*
         * Command for dumping quest information for all indexes to a text file.
         */

        define(name = "dumpquests", privilege = Privilege.ADMIN, usage = "::dumpquests", description = "Dumps quest information to a text file") { player, _ ->
            val quests = QuestRepository.getQuests().values
            val maxIndex = quests.maxOfOrNull { it.index }

            if (maxIndex == null) {
                reject(player, "No quests found!")
                return@define
            }

            val dumpDirectory = File("dumps")

            try {
                if (!dumpDirectory.exists() && !dumpDirectory.mkdirs()) {
                    reject(player, "Failed to create the dumps directory!")
                    return@define
                }

                val dumpFile = File(dumpDirectory, "quests.txt")
                val content = buildString {
                    for (index in 0..maxIndex) {
                        val quest = player.questRepository.forIndex(index)

                        if (quest != null) {
                            appendLine(quest.toString())
                        } else {
                            appendLine("Quest [index=$index, not found]")
                        }
                    }
                }

                dumpFile.writeText(content, Charsets.UTF_8)

                player.debug("Quest dump saved to $dumpFile"
                )
            } catch (e: Exception) {
                reject(player, "Failed to dump quests: ${e.message}")
            }
        }

        /*
         * Command for opening the quest interface
         * and listing all quests.
         */

        define(name = "quests", privilege = Privilege.ADMIN) { player, _ ->
            sendQuests(player)
        }
    }

    private fun sendQuests(player: Player?) {
        player!!.interfaceManager.open(Component(Components.QUESTJOURNAL_SCROLL_275))
        for (i in 0..310) {
            sendString(player, "", Components.QUESTJOURNAL_SCROLL_275, i)
        }
        var lineId = 11
        sendString(player, "<col=ecf0f1>" + "Available Quests" + "</col>", Components.QUESTJOURNAL_SCROLL_275, 2)
        for (q in QuestRepository.getQuests().toSortedMap().values) {
            // Add a space to beginning and end of string for the strikethrough
            sendString(player, "<col=ecf0f1>" + (if (q.isCompleted(player)) "<str> " else "") + q.name + " ", Components.QUESTJOURNAL_SCROLL_275, lineId++)
        }
    }

    private fun sendQuestsDebug(
        admin: Player?,
        lookupUser: Player?,
    ) {
        admin!!.interfaceManager.open(Component(Components.QUESTJOURNAL_SCROLL_275))
        for (i in 0..310) {
            sendString(admin, "", Components.QUESTJOURNAL_SCROLL_275, i)
        }
        var lineId = 11
        sendString(admin, "<col=ecf0f1>${lookupUser!!.username}'s Quest Debug</col>", Components.QUESTJOURNAL_SCROLL_275, 2)
        for (q in QuestRepository.getQuests().values) {
            // Add a space to beginning and end of string for the strikethrough
            val stage = lookupUser.questRepository.getStage(q)
            val statusColor =
                when {
                    stage >= 100 -> "80ff00"
                    stage in 1..99 -> "ff8400"
                    else -> "ff0000"
                }
            sendString(admin, "<col=ecf0f1>${q.name}</col>", Components.QUESTJOURNAL_SCROLL_275, lineId++)
            sendString(
                admin,
                "<col=ecf0f1>Index: </col><col=ff1f1f><shad=2>${q.index}</shad></col> | <col=ecf0f1>Stage:</col> <col=$statusColor><shad=2>${
                    lookupUser.questRepository.getStage(q)
                }</shad></col>",
                Components.QUESTJOURNAL_SCROLL_275,
                lineId++,
            )
            sendString(admin, "<str>          ", Components.QUESTJOURNAL_SCROLL_275, lineId++)
        }
    }
}
