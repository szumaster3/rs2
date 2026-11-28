package content.global.skill.thieving.blackjack

import shared.consts.NPCs

enum class BlackjackNPC(val npcIds: IntArray, val thievingLevel: Int, val xp: Double, val damage: Int) {
    VILLAGERS(intArrayOf(NPCs.FEUD_VILLAGER_1_2_1889, NPCs.FEUD_VILLAGER_1_3_1890, NPCs.FEUD_VILLAGER_2_2_1893, NPCs.FEUD_VILLAGER_2_3_1894, NPCs.FEUD_VILLAGER_3_2_1897, NPCs.FEUD_VILLAGER_3_3_1898), 30, 65.0, 3),
    BANDIT(intArrayOf(NPCs.BANDIT_1881, NPCs.BANDIT_6388), 45, 65.0, 4),
    MENAPHITE_THUG(intArrayOf(NPCs.MENAPHITE_THUG_1905), 65, 137.5, 5);

    companion object {
        /**
         * Wrapper (multi-state) NPC ids. The real NPC is picked by a varbit per player,
         * so the listener has to be registered on them too.
         */
        val WRAPPER_IDS = intArrayOf(1879, 1887, 1891, 1895, 1896)

        fun forId(id: Int) =
            values().firstOrNull { id in it.npcIds }
    }
}