package content.global.skill.herblore.potion

import shared.consts.Items

/**
 * Represents finished potions in the Herblore skill.
 */
enum class FinishedPotion(val base: UnfinishedPotion, val ingredient: Int, val product: Int, val level: Int, val xp: Double) {
    ATTACK_POTION(UnfinishedPotion.GUAM, Items.EYE_OF_NEWT_221, Items.ATTACK_POTION_3_121, 3, 25.0),
    ANTIDOTE_POTION(UnfinishedPotion.MARRENTILL, Items.UNICORN_HORN_DUST_235, Items.ANTIPOISON_3_175, 5, 37.5),
    RELIC_BALM(UnfinishedPotion.ROGUE_PURSE, Items.CLEAN_SNAKE_WEED_1526, Items.RELICYMS_BALM_3_4844, 8, 0.0),
    STRENGTH_POTION(UnfinishedPotion.TARROMIN, Items.LIMPWURT_ROOT_225, Items.STRENGTH_POTION_3_115, 12, 50.0),
    OGRE_POTION(UnfinishedPotion.GUAM, Items.JANGERBERRIES_247, Items.VIAL_2390, 14, 10.0),
    RESTORE_POTION(UnfinishedPotion.HARRALANDER, Items.RED_SPIDERS_EGGS_223, Items.RESTORE_POTION_3_127, 22, 62.5),
    ENERGY_POTION(UnfinishedPotion.HARRALANDER, Items.CHOCOLATE_DUST_1975, Items.ENERGY_POTION_3_3010, 26, 67.5),
    DEFENCE_POTION(UnfinishedPotion.RANARR, Items.WHITE_BERRIES_239, Items.DEFENCE_POTION_3_133, 30, 45.0),
    AGILITY_POTION(UnfinishedPotion.TOADFLAX, Items.TOADS_LEGS_2152, Items.AGILITY_POTION_3_3034, 34, 80.0),
    COMBAT_POTION(UnfinishedPotion.HARRALANDER, Items.GOAT_HORN_DUST_9736, Items.COMBAT_POTION_3_9741, 36, 84.0),
    PRAYER_POTION(UnfinishedPotion.RANARR, Items.SNAPE_GRASS_231, Items.PRAYER_POTION_3_139, 38, 87.5),
    SUMMONING_POTION(UnfinishedPotion.SPIRIT_WEED, Items.COCKATRICE_EGG_12109, Items.SUMMONING_POTION_3_12142, 40, 92.0),
    SUPER_ATTACK(UnfinishedPotion.IRIT, Items.EYE_OF_NEWT_221, Items.SUPER_ATTACK_3_145, 45, 100.0),
    SUPER_ANTIDOTE(UnfinishedPotion.IRIT, Items.UNICORN_HORN_DUST_235, Items.SUPER_ANTIPOISON_3_181, 48, 106.3),
    FISHING_POTION(UnfinishedPotion.AVANTOE, Items.SNAPE_GRASS_231, Items.FISHING_POTION_3_151, 50, 112.5),
    SUPER_ENERGY(UnfinishedPotion.AVANTOE, Items.MORT_MYRE_FUNGUS_2970, Items.SUPER_ENERGY_3_3018, 52, 117.5),
    HUNTER_POTION(UnfinishedPotion.AVANTOE, Items.KEBBIT_TEETH_DUST_10111, Items.HUNTER_POTION_3_10000, 53, 120.0),
    SUPER_STRENGTH(UnfinishedPotion.KWUARM, Items.LIMPWURT_ROOT_225, Items.SUPER_STRENGTH_3_157, 55, 125.0),
    WEAPON_POISON(UnfinishedPotion.KWUARM, Items.DRAGON_SCALE_DUST_241, Items.WEAPON_POISON_187, 60, 137.5),
    SUPER_RESTORE(UnfinishedPotion.SNAPDRAGON, Items.RED_SPIDERS_EGGS_223, Items.SUPER_RESTORE_3_3026, 63, 142.5),
    SUPER_DEFENCE(UnfinishedPotion.CADANTINE, Items.WHITE_BERRIES_239, Items.SUPER_DEFENCE_3_163, 66, 160.0),
    ANTIFIRE_POTION(UnfinishedPotion.LANTADYME, Items.DRAGON_SCALE_DUST_241, Items.ANTIFIRE_3_2454, 69, 157.5),
    SUPER_RANGING_POTION(UnfinishedPotion.DWARF_WEED, Items.WINE_OF_ZAMORAK_245, Items.RANGING_POTION_3_169, 72, 162.5),
    SUPER_MAGIC(UnfinishedPotion.LANTADYME, Items.POTATO_CACTUS_3138, Items.MAGIC_POTION_3_3042, 76, 172.5),
    ZAMORAK_BREW(UnfinishedPotion.TORSTOL, Items.JANGERBERRIES_247, Items.ZAMORAK_BREW_3_189, 78, 175.0),
    SARADOMIN_BREW(UnfinishedPotion.TOADFLAX, Items.CRUSHED_NEST_6693, Items.SARADOMIN_BREW_3_6687, 81, 180.0),
    STRONG_WEAPON_POISON(UnfinishedPotion.STRONG_WEAPON_POISON, Items.RED_SPIDERS_EGGS_223, Items.WEAPON_POISON_PLUS_5937, 73, 165.0),
    SUPER_STRONG_WEAPON_POISON(UnfinishedPotion.SUPER_STRONG_WEAPON_POISON, Items.POISON_IVY_BERRIES_6018, Items.WEAPON_POISON_PLUS_PLUS_5940, 82, 190.0),
    STRONG_ANTIDOTE(UnfinishedPotion.STRONG_ANTIPOISON, Items.YEW_ROOTS_6049, Items.ANTIPOISON_PLUS_3_5945, 68, 155.0),
    SUPER_STRONG_ANTIDOTE(UnfinishedPotion.SUPER_STRONG_ANTIPOISON, Items.MAGIC_ROOTS_6051, Items.ANTIPOISON_PLUS_PLUS_3_5954, 79, 177.5),
    BLAMISH_OIL(UnfinishedPotion.HARRALANDER, Items.BLAMISH_SNAIL_SLIME_1581, Items.BLAMISH_OIL_1582, 25, 80.0);

    companion object {
        /**
         * Returns the finished potion for the given unfinished potion
         * and ingredient items, or null if none found.
         */
        fun getPotion(unfinished: Int, ingredient: Int): FinishedPotion? {
            return values().firstOrNull {
                it.base.product == unfinished && it.ingredient == ingredient
            }
        }
    }
}