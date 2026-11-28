package content.global.skill.herblore.potion

import shared.consts.Items

/**
 * Represents barbarian mixes.
 */
enum class BarbarianMixes(val base: Int, val product: Int, val both: Boolean, val level: Int, val xp: Double) {
    ATTACK_POTION(Items.ATTACK_POTION_2_123, Items.ATTACK_MIX_2_11429, true, 4, 8.0),
    ANTI_POISON_POTION(Items.ANTIPOISON_2_177, Items.ANTIPOISON_MIX_2_11433, true, 6, 12.0),
    RELIC(Items.RELICYMS_BALM_2_4846, Items.RELICYMS_MIX_2_11437, true, 9, 14.0),
    STRENGTH_POTION(Items.STRENGTH_POTION_2_117, Items.STRENGTH_MIX_2_11443, true, 14, 17.0),
    RESTORE_POTION(Items.RESTORE_POTION_2_129, Items.RESTORE_MIX_2_11449, true, 24, 21.0),
    ENERGY_POTION(Items.ENERGY_POTION_2_3012, Items.ENERGY_MIX_2_11453, false, 29, 23.0),
    DEFENCE_POTION(Items.DEFENCE_POTION_2_135, Items.DEFENCE_MIX_2_11457, false, 33, 25.0),
    AGILITY_POTION(Items.AGILITY_POTION_2_3036, Items.AGILITY_MIX_2_11461, false, 37, 27.0),
    COMBAT_POTION(Items.COMBAT_POTION_2_9743, Items.COMBAT_MIX_2_11445, false, 40, 28.0),
    PRAYER_POTION(Items.PRAYER_POTION_2_141, Items.PRAYER_MIX_2_11465, false, 42, 29.0),
    SUPER_ATTACK_POTION(Items.SUPER_ATTACK_2_147, Items.SUPERATTACK_MIX_2_11469, false, 47, 33.0),
    SUPER_ANTI_POISON_POTION(Items.SUPER_ANTIPOISON_2_183, Items.ANTI_P_SUPERMIX_2_11473, false, 51, 35.0),
    FISHING_POTION(Items.FISHING_POTION_2_153, Items.FISHING_MIX_2_11477, false, 53, 38.0),
    SUPER_ENERGY_POTION(Items.SUPER_ENERGY_2_3020, Items.SUP_ENERGY_MIX_2_11481, false, 56, 39.0),
    HUNTER_POTION(Items.HUNTER_POTION_2_10002, Items.HUNTING_MIX_2_11517, false, 58, 40.0),
    SUPER_STRENGTH_POTION(Items.SUPER_STRENGTH_2_159, Items.SUP_STR_MIX_2_11485, false, 59, 42.0),
    MAGIC_ESSENCE_POTION(Items.MAGIC_ESSENCE_2_9023, Items.MAGIC_ESS_MIX_2_11489, false, 61, 43.0),
    SUPER_RESTORE(Items.SUPER_RESTORE_2_3028, Items.SUP_RESTORE_MIX_2_11493, false, 67, 48.0),
    SUPER_DEFENCE_POTION(Items.SUPER_DEFENCE_2_165, Items.SUP_DEF_MIX_2_11497, false, 71, 50.0),
    ANTIDOTE_PLUS(Items.ANTIPOISON_PLUS_2_5947, Items.ANTIDOTE_PLUS_MIX_2_11501, false, 74, 52.0),
    ANTIFIRE_POTION(Items.ANTIFIRE_2_2456, Items.ANTIFIRE_MIX_2_11505, false, 75, 53.0),
    RANGING_POTION(Items.RANGING_POTION_2_171, Items.RANGING_MIX_2_11509, false, 80, 54.0),
    MAGIC_POTION(Items.MAGIC_POTION_2_3044, Items.MAGIC_MIX_2_11513, false, 83, 57.0),
    ZAMORAK_BREW(Items.ZAMORAK_BREW_2_191, Items.ZAMORAK_MIX_2_11521, false, 85, 58.0);

    companion object {
        /**
         * Get a [BarbarianMixes] based on the item id.
         */
        fun forId(id: Int): BarbarianMixes? = values().find { it.base == id }
    }
}
