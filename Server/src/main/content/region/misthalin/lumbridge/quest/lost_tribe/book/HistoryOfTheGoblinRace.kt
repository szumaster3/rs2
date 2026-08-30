package content.region.misthalin.lumbridge.quest.lost_tribe.book

import core.api.*
import core.game.interaction.InterfaceListener
import core.game.node.entity.player.Player
import shared.consts.Components
import shared.consts.Quests

class HistoryOfTheGoblinRace : InterfaceListener {

    override fun defineInterfaceListeners() {

        onOpen(Components.GOBLIN_SYMBOL_BOOK_183) { player, component ->
            sendInterfaceConfig(player, Components.GOBLIN_SYMBOL_BOOK_183, 17, true)
            val qstage = getQuestStage(player, Quests.THE_LOST_TRIBE)
            component.setUncloseEvent { p, _ ->
                if (qstage == 42 || qstage == 41) {
                    sendPlayerDialogue(p, "Hey... The symbol of the 'Dorgeshuun' tribe looks just, like the symbol on the brooch I found.")
                    setQuestStage(p!!, Quests.THE_LOST_TRIBE, 43)
                }
                removeAttribute(p, "hgr-index")
                return@setUncloseEvent true
            }
            return@onOpen true
        }

        on(Components.GOBLIN_SYMBOL_BOOK_183) { player, _, _, button, _, _ ->
            when (button) {
                16 -> setIndex(player, getIndex(player) + 1)
                17 -> setIndex(player, getIndex(player) - 1)
            }
            update(player)
            return@on true
        }
    }

    fun update(player: Player) {
        val index = getIndex(player)
        sendInterfaceConfig(player, Components.GOBLIN_SYMBOL_BOOK_183, 32, index != 0)
        sendInterfaceConfig(player, Components.GOBLIN_SYMBOL_BOOK_183, 14, index != 1)
        sendInterfaceConfig(player, Components.GOBLIN_SYMBOL_BOOK_183, 15, index != 2)
        sendInterfaceConfig(player, Components.GOBLIN_SYMBOL_BOOK_183, 16, index == 2)
        sendInterfaceConfig(player, Components.GOBLIN_SYMBOL_BOOK_183, 17, index == 0)
    }

    private fun setIndex(player: Player, index: Int) {
        setAttribute(player, "hgr-index", index)
    }

    fun getIndex(player: Player): Int = player.getAttribute("hgr-index", 0)
}