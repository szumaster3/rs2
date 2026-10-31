package content.global.skill.herblore.herb

import core.game.node.item.Item
import shared.consts.Items

/**
 * Represents types of herbs.
 */
enum class HerbItem(val herb: Item, val product: Item, val level: Int, val experience: Double) {
    GUAM(Item(Items.GRIMY_GUAM_199), Item(Items.CLEAN_GUAM_249), 3, 2.5),
    MARRENTILL(Item(Items.GRIMY_MARRENTILL_201), Item(Items.CLEAN_MARRENTILL_251), 5, 3.8),
    TARROMIN(Item(Items.GRIMY_TARROMIN_203), Item(Items.CLEAN_TARROMIN_253), 11, 5.0),
    HARRALANDER(Item(Items.GRIMY_HARRALANDER_205), Item(Items.CLEAN_HARRALANDER_255), 20, 6.3),
    RANARR(Item(Items.GRIMY_RANARR_207), Item(Items.CLEAN_RANARR_257), 25, 7.5),
    TOADFLAX(Item(Items.GRIMY_TOADFLAX_3049), Item(Items.CLEAN_TOADFLAX_2998), 30, 8.0),
    SPIRIT_WEED(Item(Items.GRIMY_SPIRIT_WEED_12174), Item(Items.CLEAN_SPIRIT_WEED_12172), 35, 7.8),
    IRIT(Item(Items.GRIMY_IRIT_209), Item(Items.CLEAN_IRIT_259), 40, 8.8),
    AVANTOE(Item(Items.GRIMY_AVANTOE_211), Item(Items.CLEAN_AVANTOE_261), 48, 10.0),
    KWUARM(Item(Items.GRIMY_KWUARM_213), Item(Items.CLEAN_KWUARM_263), 54, 11.3),
    SNAPDRAGON(Item(Items.GRIMY_SNAPDRAGON_3051), Item(Items.CLEAN_SNAPDRAGON_3000), 59, 11.8),
    CADANTINE(Item(Items.GRIMY_CADANTINE_215), Item(Items.CLEAN_CADANTINE_265), 65, 12.5),
    LANTADYME(Item(Items.GRIMY_LANTADYME_2485), Item(Items.CLEAN_LANTADYME_2481), 67, 13.1),
    DWARF_WEED(Item(Items.GRIMY_DWARF_WEED_217), Item(Items.CLEAN_DWARF_WEED_267), 70, 13.8),
    TORSTOL(Item(Items.GRIMY_TORSTOL_219), Item(Items.CLEAN_TORSTOL_269), 75, 15.0),
    SNAKE_WEED(Item(Items.GRIMY_SNAKE_WEED_1525), Item(Items.CLEAN_SNAKE_WEED_1526), 3, 2.5),
    ARDRIGAL(Item(Items.GRIMY_ARDRIGAL_1527), Item(Items.CLEAN_ARDRIGAL_1528), 3, 2.5),
    SITO_FOIL(Item(Items.GRIMY_SITO_FOIL_1529), Item(Items.CLEAN_SITO_FOIL_1530), 3, 2.5),
    VOLENCIA_MOSS(Item(Items.GRIMY_VOLENCIA_MOSS_1531), Item(Items.CLEAN_VOLENCIA_MOSS_1532), 3, 2.5),
    ROGUES_PUSE(Item(Items.GRIMY_ROGUES_PURSE_1533), Item(Items.CLEAN_ROGUES_PURSE_1534), 3, 2.5);

    companion object {
        /**
         * A map of herb IDs to their respective [HerbItem] enum values.
         */
        private val herbMap =
            HashMap<Int, HerbItem>().apply {
                values().forEach { herbData -> put(herbData.herb.id, herbData) }
            }

        /**
         * Finds the [HerbItem] enum value for the given [Item].
         *
         * @param item The [Item] representing the herb.
         * @return The corresponding [HerbItem] enum value or null if no match is found.
         */
        fun forItem(item: Item): HerbItem? = herbMap[item.id]
    }
}
