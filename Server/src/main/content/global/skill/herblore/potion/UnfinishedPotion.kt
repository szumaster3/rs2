package content.global.skill.herblore.potion

import content.global.skill.herblore.herb.HerbItem
import shared.consts.Items

/**
 * Enum representing unfinished potions used in Herblore.
 */
enum class UnfinishedPotion(val base: Int, val ingredient: Int, val product: Int, val level: Int) {
    GUAM(PotionMakingPulse.VIAL_OF_WATER, HerbItem.GUAM.product.id, Items.GUAM_POTION_UNF_91, 3),
    ROGUE_PURSE(PotionMakingPulse.VIAL_OF_WATER, HerbItem.ROGUES_PUSE.product.id, Items.ROGUES_PURSE_POTION_UNF_4840, 3),
    MARRENTILL(PotionMakingPulse.VIAL_OF_WATER, HerbItem.MARRENTILL.product.id, Items.MARRENTILL_POTION_UNF_93, 5),
    TARROMIN(PotionMakingPulse.VIAL_OF_WATER, HerbItem.TARROMIN.product.id, Items.TARROMIN_POTION_UNF_95, 12),
    OGRE(PotionMakingPulse.VIAL_OF_WATER, Items.JANGERBERRIES_247, Items.VIAL_2389, 14),
    HARRALANDER(PotionMakingPulse.VIAL_OF_WATER, HerbItem.HARRALANDER.product.id, Items.HARRALANDER_POTION_UNF_97, 22),
    RANARR(PotionMakingPulse.VIAL_OF_WATER, HerbItem.RANARR.product.id, Items.RANARR_POTION_UNF_99, 30),
    TOADFLAX(PotionMakingPulse.VIAL_OF_WATER, HerbItem.TOADFLAX.product.id, Items.TOADFLAX_POTION_UNF_3002, 34),
    SPIRIT_WEED(PotionMakingPulse.VIAL_OF_WATER, HerbItem.SPIRIT_WEED.product.id, Items.SPIRIT_WEED_POTION_UNF_12181, 40),
    IRIT(PotionMakingPulse.VIAL_OF_WATER, HerbItem.IRIT.product.id, Items.IRIT_POTION_UNF_101, 45),
    AVANTOE(PotionMakingPulse.VIAL_OF_WATER, HerbItem.AVANTOE.product.id, Items.AVANTOE_POTION_UNF_103, 50),
    KWUARM(PotionMakingPulse.VIAL_OF_WATER, HerbItem.KWUARM.product.id, Items.KWUARM_POTION_UNF_105, 55),
    SNAPDRAGON(PotionMakingPulse.VIAL_OF_WATER, HerbItem.SNAPDRAGON.product.id, Items.SNAPDRAGON_POTION_UNF_3004, 63),
    CADANTINE(PotionMakingPulse.VIAL_OF_WATER, HerbItem.CADANTINE.product.id, Items.CADANTINE_POTION_UNF_107, 66),
    LANTADYME(PotionMakingPulse.VIAL_OF_WATER, HerbItem.LANTADYME.product.id, Items.LANTADYME_POTION_UNF_2483, 69),
    DWARF_WEED(PotionMakingPulse.VIAL_OF_WATER, HerbItem.DWARF_WEED.product.id, Items.DWARF_WEED_POTION_UNF_109, 72),
    TORSTOL(PotionMakingPulse.VIAL_OF_WATER, HerbItem.TORSTOL.product.id, Items.TORSTOL_POTION_UNF_111, 75),
    STRONG_WEAPON_POISON(PotionMakingPulse.COCONUT_MILK, Items.CACTUS_SPINE_6016, Items.WEAPON_POISON_PLUS_UNF_5936, 73),
    SUPER_STRONG_WEAPON_POISON(PotionMakingPulse.COCONUT_MILK, Items.CAVE_NIGHTSHADE_2398, Items.WEAPON_POISON_PLUS_PLUS_UNF_5939, 82),
    STRONG_ANTIPOISON(PotionMakingPulse.COCONUT_MILK, HerbItem.TOADFLAX.product.id, Items.ANTIPOISON_PLUS_UNF_5942, 68),
    SUPER_STRONG_ANTIPOISON(PotionMakingPulse.COCONUT_MILK, HerbItem.IRIT.product.id, Items.ANTIPOISON_PLUS_PLUS_UNF_5951, 79);

    companion object {
        /**
         * Finds the unfinished potion matching the given item and base.
         */
        fun forID(item: Int, base: Int): UnfinishedPotion? = values().firstOrNull { potion ->
            (potion.ingredient == item || potion.ingredient == base) && (item == potion.base || base == potion.base)
        }
    }
}
