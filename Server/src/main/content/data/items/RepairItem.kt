package content.data.items

import core.game.node.item.Item
import shared.consts.Items

/**
 * Represents a repairable item, its repaired product, and the repair cost.
 * @author szu
 */
enum class RepairItem(val item: Item, val product: Item, val cost: Int) {
    // Bronze items.
    BRONZE_HATCHET(Item(Items.BROKEN_HATCHET_494, 1), Item(Items.BRONZE_HATCHET_1351, 1), 0),
    BRONZE_PICKAXE(Item(Items.BROKEN_PICKAXE_468, 1), Item(Items.BRONZE_PICKAXE_1265, 1), 0),

    // Iron items.
    IRON_HATCHET(Item(Items.BROKEN_HATCHET_496, 1), Item(Items.IRON_HATCHET_1349, 1), 0),
    IRON_PICKAXE(Item(Items.BROKEN_PICKAXE_470, 1), Item(Items.IRON_PICKAXE_1267, 1), 0),

    // Steel items.
    STEEL_HATCHET(Item(Items.BROKEN_HATCHET_498, 1), Item(Items.STEEL_HATCHET_1353, 1), 0),
    STEEL_PICKAXE(Item(Items.BROKEN_PICKAXE_472, 1), Item(Items.STEEL_PICKAXE_1269, 1), 14),

    // Black items.
    BLACK_HATCHET(Item(Items.BROKEN_HATCHET_500, 1), Item(Items.BLACK_HATCHET_1361, 1), 10),

    // Mithril items.
    MITHRIL_HATCHET(Item(Items.BROKEN_HATCHET_502, 1), Item(Items.MITHRIL_HATCHET_1355, 1), 18),
    MITHRIL_PICKAXE(Item(Items.BROKEN_PICKAXE_474, 1), Item(Items.MITHRIL_PICKAXE_1273, 1), 43),

    // Adamant items.
    ADAMANT_HATCHET(Item(Items.BROKEN_HATCHET_504, 1), Item(Items.ADAMANT_HATCHET_1357, 1), 43),
    ADAMANT_PICKAXE(Item(Items.BROKEN_PICKAXE_476, 1), Item(Items.ADAMANT_PICKAXE_1271, 1), 107),

    // Rune items.
    RUNE_HATCHET(Item(Items.BROKEN_HATCHET_506, 1), Item(Items.RUNE_HATCHET_1359, 1), 427),
    RUNE_PICKAXE(Item(Items.BROKEN_PICKAXE_478, 1), Item(Items.RUNE_PICKAXE_1275, 1), 1100),

    // Dragon items.
    DRAGON_HATCHET(Item(Items.BROKEN_HATCHET_6741, 1), Item(Items.DRAGON_HATCHET_6739, 1), 1800),
    ;

    companion object {
        val repairableItemIds: List<Int> = values().map { it.item.id }

        /**
         * Gets the repair item by the broken items ID.
         */
        fun forId(id: Int): RepairItem? = values().firstOrNull { it.item.id == id }
    }
}
