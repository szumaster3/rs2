package content.global.skill.herblore.item.grind

import content.global.skill.herblore.herb.HerbItem
import shared.consts.Items

/**
 * Represents the herb tars.
 */
enum class TarItem(val ingredient: Int, val product: Int, val level: Int, val experience: Double) {
    GUAM_TAR(HerbItem.GUAM.product.id, Items.GUAM_TAR_10142, 19, 30.0),
    GROUND_GUAM_TAR(Items.GROUND_GUAM_6681, Items.GUAM_TAR_10142, 19, 30.0),
    MARRENTILL_TAR(HerbItem.MARRENTILL.product.id, Items.MARRENTILL_TAR_10143, 31, 42.5),
    TARROMIN_TAR(HerbItem.TARROMIN.product.id, Items.TARROMIN_TAR_10144, 39, 55.0),
    HARRALANDER_TAR(HerbItem.HARRALANDER.product.id, Items.HARRALANDER_TAR_10145, 44, 72.5);

    companion object {
        private val all = values()
        private val mapByIngredient = all.associateBy { it.ingredient }

        val ingredientIds: IntArray = all.map { it.ingredient }.toIntArray()

        /**
         * Finds a [TarItem] by its ingredient id.
         */
        fun forId(id: Int): TarItem? = mapByIngredient[id]
    }
}
