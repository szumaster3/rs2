package content.global.plugins.iface.tab

import core.api.getStatLevel
import core.api.getQuestPoints
import core.cache.def.impl.QuestDefinition
import core.api.openInterface
import core.api.sendString
import core.game.component.Component
import core.game.interaction.InterfaceListener
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.diary.DiaryType
import core.game.node.entity.player.link.quest.*
import core.game.node.entity.skill.Skills
import core.tools.colorize
import shared.consts.Components
import kotlin.math.max
import kotlin.math.min

/**
 * Handles the quest tab reward buttons.
 * @author Emperor, Vexia
 */
class QuestTabInterface : InterfaceListener {

    override fun defineInterfaceListeners() {
        on(190) { player, _, _, buttonID, slot, _ ->
            if (buttonID != 18) return@on false
            val id = journalSlots[slot] ?: return@on false
            val quest = player.questRepository.forIndex(id)
            if (quest == null) {
                val definition = QuestDefinition.forId(id) ?: return@on false
                openInterface(player, Components.QUESTJOURNAL_SCROLL_275)
                QuestJournal.clear(player, definition.name ?: return@on false)
                var row = 0
                for (requirement in definition.skills) {
                    QuestJournal.writeRow(player, row++, "Level ${requirement.level} ${requirement.skill}")
                }
                for (required in definition.prerequisiteQuests) {
                    val name = QuestDefinition.forId(required)?.name ?: continue
                    QuestJournal.writeRow(player, row++, name)
                }
                if (definition.requiredQuestPoints > 0) QuestJournal.writeRow(player, row++, "${definition.requiredQuestPoints} Quest Points")
                QuestJournal.writeRow(player, row, "This quest is not implemented.")
                QuestJournal.finish(player)
                return@on true
            }
            openInterface(player, Components.QUESTJOURNAL_SCROLL_275)
            quest.drawJournal(player, quest.getStage(player))
            QuestJournal.finish(player)
            return@on true
        }

        on(Components.AREA_TASK_259) { player, _, _, buttonID, _, _ ->
            val type = DiaryType.forChild(buttonID) ?: return@on false
            val diary = player.achievementDiaryManager.getDiary(type) ?: return@on false
            val level = type.childIds.indexOf(buttonID) - 1
            if (type == DiaryType.ARDOUGNE && level == 3 && !diary.isStarted(3)) return@on false
            diary.open(player, level.takeIf { it >= 0 })
            return@on true
        }
    }

    companion object {
        // enum_2252 -> struct param 845 -> cache quest name.
        private val journalSlots = mapOf(
            1 to 6,
            2 to 51,
            3 to 99,
            4 to 66,
            5 to 15,
            6 to 137,
            7 to 72,
            8 to 152,
            9 to 60,
            10 to 64,
            11 to 27,
            13 to 55,
            14 to 83,
            15 to 63,
            16 to 132,
            17 to 11,
            18 to 43,
            19 to 57,
            20 to 102,
            21 to 128,
            22 to 0,
            23 to 44,
            24 to 158,
            25 to 104,
            26 to 92,
            28 to 13,
            29 to 140,
            30 to 135,
            31 to 123,
            32 to 69,
            33 to 111,
            34 to 34,
            35 to 75,
            36 to 149,
            37 to 8,
            38 to 94,
            39 to 97,
            40 to 107,
            41 to 143,
            42 to 73,
            43 to 154,
            44 to 116,
            45 to 136,
            46 to 106,
            47 to 76,
            49 to 50,
            50 to 93,
            52 to 138,
            53 to 82,
            54 to 134,
            55 to 3,
            56 to 65,
            57 to 150,
            58 to 120,
            59 to 125,
            60 to 38,
            61 to 112,
            62 to 105,
            63 to 141,
            64 to 21,
            65 to 19,
            66 to 59,
            67 to 119,
            68 to 42,
            69 to 37,
            70 to 24,
            71 to 124,
            72 to 108,
            73 to 130,
            74 to 147,
            75 to 151,
            76 to 156,
            77 to 129,
            78 to 28,
            79 to 22,
            80 to 133,
            81 to 54,
            82 to 146,
            83 to 58,
            84 to 9,
            85 to 68,
            87 to 30,
            88 to 62,
            89 to 100,
            90 to 110,
            91 to 127,
            92 to 101,
            93 to 2,
            94 to 121,
            96 to 10,
            97 to 71,
            98 to 39,
            99 to 70,
            100 to 36,
            101 to 46,
            102 to 48,
            103 to 89,
            104 to 45,
            105 to 81,
            106 to 126,
            107 to 157,
            108 to 113,
            109 to 7,
            110 to 78,
            111 to 40,
            112 to 90,
            113 to 85,
            114 to 31,
            115 to 103,
            116 to 16,
            117 to 145,
            118 to 148,
            119 to 1,
            121 to 144,
            122 to 153,
            123 to 155,
            124 to 79,
            125 to 32,
            126 to 61,
            127 to 5,
            128 to 49,
            129 to 91,
            130 to 25,
            131 to 52,
            133 to 23,
            134 to 96,
            135 to 47,
            136 to 109,
            137 to 139,
            138 to 80,
            139 to 4,
            140 to 14,
            141 to 142,
            142 to 29,
            143 to 95,
            144 to 56,
            145 to 86,
            146 to 87,
            147 to 131,
            148 to 74,
            149 to 115,
            151 to 122,
            153 to 20,
            154 to 33,
            155 to 77,
            157 to 168,
            159 to 53,
            160 to 35,
            161 to 167,
            163 to 169
        )
    }
}
