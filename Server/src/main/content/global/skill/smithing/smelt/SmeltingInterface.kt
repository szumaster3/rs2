package content.global.skill.smithing.smelt

import content.data.Quests

import content.global.skill.smithing.bar.BarItem
import core.api.*
import core.game.interaction.InterfaceListener
import core.game.node.entity.impl.PulseType
import shared.consts.Components

class SmeltingInterface : InterfaceListener {
    override fun defineInterfaceListeners() {

        /*
         * Handles drawing the components.
         */

        onOpen(Components.SMELTING_311) { player, c ->
            val barItems = listOf(BarItem.BRONZE, BarItem.BLURITE, BarItem.IRON, BarItem.SILVER, BarItem.STEEL, BarItem.GOLD, BarItem.MITHRIL, BarItem.ADAMANT, BarItem.RUNITE)

            sendItemZoomOnInterface(player, Components.SMELTING_311, 4, BarItem.BRONZE.product.id, 150)

            if (isQuestComplete(player, Quests.THE_KNIGHTS_SWORD.questName)) {
                sendString(player, "<br><br><br><br><col=000000>Blurite", Components.SMELTING_311, 20)
            }

            barItems.forEachIndexed { index, bar ->
                val componentIndex = 4 + index
                sendItemZoomOnInterface(player, c.id, componentIndex, bar.product.id, 160)
            }
            return@onOpen true
        }

        /*
         * Handles options of smelting interface.
         */

        on(Components.SMELTING_311) { player, _, _, buttonID, _, _ ->
            val barType = BarButton.forId(buttonID) ?: return@on true
            if (barType.amount == -1) {
                player.interfaceManager.closeChatbox()
                sendInputDialogue(player, true, "Enter the amount:") { value: Any ->
                    if (value is String) {
                        submitIndividualPulse(player, SmeltingPulse(player, null, barType.barItem, value.toInt()), type = PulseType.STANDARD)
                    } else {
                        submitIndividualPulse(player, SmeltingPulse(player, null, barType.barItem, value as Int), type = PulseType.STANDARD)
                    }
                }
            } else {
                player.pulseManager.run(SmeltingPulse(player, null, barType.barItem, barType.amount))
            }
            return@on true
        }
    }

    enum class BarButton(val button: Int, val barItem: BarItem, val amount: Int) {
        BRONZE_1(16, BarItem.BRONZE, 1),
        BRONZE_5(15, BarItem.BRONZE, 5),
        BRONZE_10(14, BarItem.BRONZE, 10),
        BRONZE_X(13, BarItem.BRONZE, -1),

        BLURITE_1(20, BarItem.BLURITE, 1),
        BLURITE_5(19, BarItem.BLURITE, 5),
        BLURITE_10(18, BarItem.BLURITE, 10),
        BLURITE_X(17, BarItem.BLURITE, -1),

        IRON_1(24, BarItem.IRON, 1),
        IRON_5(23, BarItem.IRON, 5),
        IRON_10(22, BarItem.IRON, 10),
        IRON_X(21, BarItem.IRON, -1),

        SILVER_1(28, BarItem.SILVER, 1),
        SILVER_5(27, BarItem.SILVER, 5),
        SILVER_10(26, BarItem.SILVER, 10),
        SILVER_X(25, BarItem.SILVER, -1),

        STEEL_1(32, BarItem.STEEL, 1),
        STEEL_5(31, BarItem.STEEL, 5),
        STEEL_10(30, BarItem.STEEL, 10),
        STEEL_X(29, BarItem.STEEL, -1),

        GOLD_1(36, BarItem.GOLD, 1),
        GOLD_5(35, BarItem.GOLD, 5),
        GOLD_10(34, BarItem.GOLD, 10),
        GOLD_X(33, BarItem.GOLD, -1),

        MITHRIL_1(40, BarItem.MITHRIL, 1),
        MITHRIL_5(39, BarItem.MITHRIL, 5),
        MITHRIL_10(38, BarItem.MITHRIL, 10),
        MITHRIL_X(37, BarItem.MITHRIL, -1),

        ADAMANT_1(44, BarItem.ADAMANT, 1),
        ADAMANT_5(43, BarItem.ADAMANT, 5),
        ADAMANT_10(42, BarItem.ADAMANT, 10),
        ADAMANT_X(41, BarItem.ADAMANT, -1),

        RUNE_1(48, BarItem.RUNITE, 1),
        RUNE_5(47, BarItem.RUNITE, 5),
        RUNE_10(46, BarItem.RUNITE, 10),
        RUNE_X(45, BarItem.RUNITE, -1),
        ;

        companion object {
            @JvmStatic
            fun forId(id: Int): BarButton? {
                for (button in BarButton.values()) {
                    if (button.button == id) {
                        return button
                    }
                }
                return null
            }
        }
    }
}
