
package content.global.skill.herblore.item

import shared.consts.Items

enum class GuthixRest(val ingredients: List<Int>, val product: Int) {
    HERB_TEA_MIX_1(listOf(Items.CLEAN_HARRALANDER_255), Items.HERB_TEA_MIX_4464),
    HERB_TEA_MIX_2(listOf(Items.CLEAN_GUAM_249), Items.HERB_TEA_MIX_4466),
    HERB_TEA_MIX_3(listOf(Items.CLEAN_MARRENTILL_251), Items.HERB_TEA_MIX_4468),
    HERB_TEA_MIX_4(listOf(Items.CLEAN_HARRALANDER_255, Items.CLEAN_MARRENTILL_251), Items.HERB_TEA_MIX_4470),
    HERB_TEA_MIX_5(listOf(Items.CLEAN_HARRALANDER_255, Items.CLEAN_GUAM_249), Items.HERB_TEA_MIX_4472),
    HERB_TEA_MIX_6(listOf(Items.CLEAN_GUAM_249, Items.CLEAN_GUAM_249), Items.HERB_TEA_MIX_4474),
    HERB_TEA_MIX_7(listOf(Items.CLEAN_GUAM_249, Items.CLEAN_MARRENTILL_251), Items.HERB_TEA_MIX_4476),
    HERB_TEA_MIX_8(listOf(Items.CLEAN_HARRALANDER_255, Items.CLEAN_MARRENTILL_251, Items.CLEAN_GUAM_249), Items.HERB_TEA_MIX_4478),
    HERB_TEA_MIX_9(listOf(Items.CLEAN_GUAM_249, Items.CLEAN_GUAM_249, Items.CLEAN_MARRENTILL_251), Items.HERB_TEA_MIX_4480),
    HERB_TEA_MIX_10(listOf(Items.CLEAN_GUAM_249, Items.CLEAN_GUAM_249, Items.CLEAN_HARRALANDER_255), Items.HERB_TEA_MIX_4482),
    COMPLETE_MIX(listOf(Items.CLEAN_GUAM_249, Items.CLEAN_GUAM_249, Items.CLEAN_MARRENTILL_251, Items.CLEAN_HARRALANDER_255), Items.GUTHIX_REST3_4419);

    companion object {
        private val all = values()

        val byIngredients: Map<List<Int>, GuthixRest> =
            all.associateBy { it.ingredients.sorted() }

        val byTeaId: Map<Int, GuthixRest> =
            all.associateBy { it.product }
    }
}