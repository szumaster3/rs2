package content.global.skill.fletching

import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.game.world.update.flag.context.Animation
import shared.consts.Animations
import shared.consts.Items

object FletchingDefinition {
    /**
     * Represents types of darts.
     */
    enum class Dart(val dartTipId: Int, val dartId: Int, val level: Int, val xp: Double) {
        BRONZE_DART(Items.BRONZE_DART_TIP_819, Items.BRONZE_DART_806, 1, 1.8),
        IRON_DART(Items.IRON_DART_TIP_820, Items.IRON_DART_807, 22, 3.8),
        STEEL_DART(Items.STEEL_DART_TIP_821, Items.STEEL_DART_808, 37, 7.5),
        MITHRIL_DART(Items.MITHRIL_DART_TIP_822, Items.MITHRIL_DART_809, 52, 11.2),
        ADAMANT_DART(Items.ADAMANT_DART_TIP_823, Items.ADAMANT_DART_810, 67, 15.0),
        RUNE_DART(Items.RUNE_DART_TIP_824, Items.RUNE_DART_811, 81, 18.8),
        DRAGON_DART(Items.DRAGON_DART_TIP_11232, Items.DRAGON_DART_11230, 95, 25.0),
        ;

        companion object {
            val values = enumValues<Dart>()
            val product = values.associateBy { it.dartTipId }
        }
    }

    /**
     * Represents a types of limbs for crossbows.
     */
    enum class Limb(val stock: Int, val limb: Int, val cbowId: Int, val level: Int, val xp: Double, val animation: Int) {
        WOODEN_STOCK(Items.WOODEN_STOCK_9440, Items.BRONZE_LIMBS_9420, Items.BRONZE_CBOW_U_9454, 9, 12.0, Animations.XBOWS_FLETCHING_WOOD_BRONZE_4436),
        OAK_STOCK(Items.OAK_STOCK_9442, Items.BLURITE_LIMBS_9422, Items.BLURITE_CBOW_U_9456, 24, 32.0, Animations.XBOWS_FLETCHING_OAK_BLURITE_4437),
        WILLOW_STOCK(Items.WILLOW_STOCK_9444, Items.IRON_LIMBS_9423, Items.IRON_CBOW_U_9457, 39, 44.0, Animations.XBOWS_FLETCHING_WILLOW_IRON_4438),
        TEAK_STOCK(Items.TEAK_STOCK_9446, Items.STEEL_LIMBS_9425, Items.STEEL_CBOW_U_9459, 46, 54.0, Animations.XBOWS_FLETCHING_TEAK_STEEL_4439),
        MAPLE_STOCK(Items.MAPLE_STOCK_9448, Items.MITHRIL_LIMBS_9427, Items.MITHRIL_CBOW_U_9461, 54, 64.0, Animations.XBOWS_FLETCHING_MAPLE_MITHRIL_4440),
        MAHOGANY_STOCK(Items.MAHOGANY_STOCK_9450, Items.ADAMANTITE_LIMBS_9429, Items.ADAMANT_CBOW_U_9463, 61, 82.0, Animations.XBOWS_FLETCHING_MAHOGANY_ADAMANTITE_4441),
        YEW_STOCK(Items.YEW_STOCK_9452, Items.RUNITE_LIMBS_9431, Items.RUNITE_CBOW_U_9465, 69, 100.0, Animations.XBOWS_FLETCHING_YEW_RUNITE_4442),
        ;

        companion object {
            val values = enumValues<Limb>()
            val product = values.associateBy { it.stock }
        }
    }

    /**
     * Represents the types of bow strings.
     */
    enum class Strings(private val indicator: Byte, val unfinished: Int, val bowId: Int, val level: Int, val xp: Double, val animation: Int) {
        // Bows
        SHORT_BOW(1, Items.SHORTBOW_U_50, Items.SHORTBOW_841, 5, 5.0, Animations.STAR_USE_TELESCOPE_6678),
        LONG_BOW(1, Items.LONGBOW_U_48, Items.LONGBOW_839, 10, 10.0, Animations.NATURAL_ROCKS_METEORITE_STRIKE_6684),
        OAK_SHORTBOW(1, Items.OAK_SHORTBOW_U_54, Items.OAK_SHORTBOW_843, 20, 16.5, Animations.STAR_SPRITE_PET_SPAWN_6679),
        OAK_LONGBOW(1, Items.OAK_LONGBOW_U_56, Items.OAK_LONGBOW_845, 25, 25.0, Animations.NATURAL_ROCKS_METEORITE_HIT_6685),
        COMP_OGRE_BOW(1, Items.UNSTRUNG_COMP_BOW_4825, Items.COMP_OGRE_BOW_4827, 30, 40.0, Animations.NATURAL_ROCKS_METEORITE_HIT_6685),
        WILLOW_SHORTBOW(1, Items.WILLOW_SHORTBOW_U_60, Items.WILLOW_SHORTBOW_849, 35, 33.3, Animations.STAR_SPRITE_PET_DESPAWN_6680),
        WILLOW_LONGBOW(1, Items.WILLOW_LONGBOW_U_58, Items.WILLOW_LONGBOW_847, 40, 41.5, Animations.STARSPRITE_IDLE_6686),
        MAPLE_SHORTBOW(1, Items.MAPLE_SHORTBOW_U_64, Items.MAPLE_SHORTBOW_853, 50, 50.0, Animations.SHOOTING_STAR_FALLING_6681),
        MAPLE_LONGBOW(1, Items.MAPLE_LONGBOW_U_62, Items.MAPLE_LONGBOW_851, 55, 58.3, Animations.STARSPRITE_IDLE_EXTRA_6687),
        YEW_SHORTBOW(1, Items.YEW_SHORTBOW_U_68, Items.YEW_SHORTBOW_857, 65, 67.5, Animations.SHOOTING_STAR_FALLING_IMPACT_6682),
        YEW_LONGBOW(1, Items.YEW_LONGBOW_U_66, Items.YEW_LONGBOW_855, 70, 75.0, Animations.STARSPRITE_CRAWL_6688),
        MAGIC_SHORTBOW(1, Items.MAGIC_SHORTBOW_U_72, Items.MAGIC_SHORTBOW_861, 80, 83.3, Animations.SHOOTING_STAR_FALLEN_POSITION_STILL_6683),
        MAGIC_LONGBOW(1, Items.MAGIC_LONGBOW_U_70, Items.MAGIC_LONGBOW_859, 85, 91.5, Animations.STARSPRITE_WALK_6689),
        // Crossbows
        BRONZE_CBOW(2, Items.BRONZE_CBOW_U_9454, Items.BRONZE_CROSSBOW_9174, 9, 6.0, Animations.XBOWS_STRINGING_CROSSBOW_BRONZE_6671),
        BLURITE_CBOW(2, Items.BLURITE_CBOW_U_9456, Items.BLURITE_CROSSBOW_9176, 24, 16.0, Animations.XBOWS_STRINGING_CROSSBOW_BLURITE_6672),
        IRON_CBOW(2, Items.IRON_CBOW_U_9457, Items.IRON_CROSSBOW_9177, 39, 22.0, Animations.XBOWS_STRINGING_CROSSBOW_IRON_6673),
        STEEL_CBOW(2, Items.STEEL_CBOW_U_9459, Items.STEEL_CROSSBOW_9179, 46, 27.0, Animations.XBOWS_STRINGING_CROSSBOW_STEEL_6674),
        MITHIRIL_CBOW(2, Items.MITHRIL_CBOW_U_9461, Items.MITH_CROSSBOW_9181, 54, 32.0, Animations.XBOWS_STRINGING_CROSSBOW_MITHRIL_6675),
        ADAMANT_CBOW(2, Items.ADAMANT_CBOW_U_9463, Items.ADAMANT_CROSSBOW_9183, 61, 41.0, Animations.XBOWS_STRINGING_CROSSBOW_ADAMANTITE_6676),
        RUNITE_CBOW(2, Items.RUNITE_CBOW_U_9465, Items.RUNE_CROSSBOW_9185, 69, 50.0, Animations.XBOWS_STRINGING_CROSSBOW_RUNITE_6677),
        ;

        val string: Int =
            when (indicator.toInt() and 0xFF) {
                1 -> Items.BOW_STRING_1777
                2 -> Items.CROSSBOW_STRING_9438
                else -> 0
            }

        companion object {
            val values = enumValues<Strings>()
            val product = values.associateBy { it.unfinished }
        }
    }

    /**
     * Represents the types of arrows.
     */
    enum class ArrowHead(val arrowTipsId: Int, val arrowId: Int, val level: Int, val xp: Double) {
        BRONZE_ARROW(Items.BRONZE_ARROWTIPS_39, Items.BRONZE_ARROW_882, 1, 1.3),
        IRON_ARROW(Items.IRON_ARROWTIPS_40, Items.IRON_ARROW_884, 15, 2.5),
        STEEL_ARROW(Items.STEEL_ARROWTIPS_41, Items.STEEL_ARROW_886, 30, 5.0),
        MITHRIL_ARROW(Items.MITHRIL_ARROWTIPS_42, Items.MITHRIL_ARROW_888, 45, 7.5),
        BROAD_ARROW(Items.BROAD_ARROW_HEADS_13278, Items.BROAD_ARROW_4160, 52, 15.0),
        ADAMANT_ARROW(Items.ADAMANT_ARROWTIPS_43, Items.ADAMANT_ARROW_890, 60, 10.0),
        RUNE_ARROW(Items.RUNE_ARROWTIPS_44, Items.RUNE_ARROW_892, 75, 12.5),
        DRAGON_ARROW(Items.DRAGON_ARROWTIPS_11237, Items.DRAGON_ARROW_11212, 90, 15.0),
        ;

        companion object {
            private val PRODUCT_MAP: MutableMap<Int, ArrowHead> = HashMap()

            init {
                for (arrowHead in values()) {
                    PRODUCT_MAP[arrowHead.arrowTipsId] = arrowHead
                }
            }

            fun getByUnfinishedId(id: Int): ArrowHead? = PRODUCT_MAP[id]
        }
    }

    /**
     * Represents the types of brutal arrows.
     */
    enum class BrutalArrow(val nailId: Int, val product: Int, val level: Int, val xp: Double) {
        BRONZE_BRUTAL(Items.BRONZE_NAILS_4819, Items.BRONZE_BRUTAL_4773, 7, 1.4),
        IRON_BRUTAL(Items.IRON_NAILS_4820, Items.IRON_BRUTAL_4778, 18, 2.6),
        STEEL_BRUTAL(Items.STEEL_NAILS_1539, Items.STEEL_BRUTAL_4783, 33, 5.1),
        BLACK_BRUTAL(Items.BLACK_NAILS_4821, Items.BLACK_BRUTAL_4788, 38, 6.4),
        MITHRIL_BRUTAL(Items.MITHRIL_NAILS_4822, Items.MITHRIL_BRUTAL_4793, 49, 7.5),
        ADAMANT_BRUTAL(Items.ADAMANTITE_NAILS_4823, Items.ADAMANT_BRUTAL_4798, 62, 10.1),
        RUNE_BRUTAL(Items.RUNE_NAILS_4824, Items.RUNE_BRUTAL_4803, 77, 12.5),
        ;

        companion object {
            val values = enumValues<BrutalArrow>()
            val product = values.associateBy { it.nailId }
        }
    }

    /**
     * Represents types of bolts.
     */
    enum class Bolt(val unfinished: Int, val boltId: Int, val level: Int, val xp: Double) {
        BRONZE_BOLT(Items.BRONZE_BOLTS_UNF_9375, Items.BRONZE_BOLTS_877, 9, 0.5),
        BLURITE_BOLT(Items.BLURITE_BOLTS_UNF_9376, Items.BLURITE_BOLTS_9139, 24, 1.0),
        IRON_BOLT(Items.IRON_BOLTS_UNF_9377, Items.IRON_BOLTS_9140, 39, 1.5),
        SILVER_BOLT(Items.SILVER_BOLTS_UNF_9382, Items.SILVER_BOLTS_9145, 43, 2.5),
        STEEL_BOLT(Items.STEEL_BOLTS_UNF_9378, Items.STEEL_BOLTS_9141, 46, 3.5),
        MITHRIL_BOLT(Items.MITHRIL_BOLTS_UNF_9379, Items.MITHRIL_BOLTS_9142, 54, 5.0),
        BROAD_BOLT(Items.UNFINISHED_BROAD_BOLTS_13279, Items.BROAD_TIPPED_BOLTS_13280, 55, 3.0),
        ADAMANT_BOLT(Items.ADAMANT_BOLTS_UNF_9380, Items.ADAMANT_BOLTS_9143, 61, 7.0),
        RUNITE_BOLT(Items.RUNITE_BOLTS_UNF_9381, Items.RUNITE_BOLTS_9144, 69, 10.0),
        ;

        companion object {
            val product = enumValues<Bolt>().associateBy { it.unfinished }
        }
    }

    /**
     * Represents the gem bolt data.
     */
    enum class GemBolt(val base: Int, val gem: Int, val tip: Int, val boltTipId: Int, val level: Int, val xp: Double, val animation: Animation) {
        OPAL(Items.BRONZE_BOLTS_877, Items.OPAL_1609, Items.OPAL_BOLT_TIPS_45, Items.OPAL_BOLTS_879, 11, 1.6, Animation(Animations.HUMAN_OPALCUTTING_890)),
        JADE(Items.BLURITE_BOLTS_9139, Items.JADE_1611, Items.JADE_BOLT_TIPS_9187, Items.JADE_BOLTS_9335, 26, 2.4, Animation(Animations.HUMAN_JADECUTTING_891)),
        PEARL(Items.IRON_BOLTS_9140, Items.OYSTER_PEARL_411, Items.PEARL_BOLT_TIPS_46, Items.PEARL_BOLTS_880, 41, 3.2, Animation(Animations.XBOWS_FLETCHING_PEARL_4470)),
        PEARLS(Items.IRON_BOLTS_9140, Items.OYSTER_PEARLS_413, Items.PEARL_BOLT_TIPS_46, Items.PEARL_BOLTS_880, 41, 3.2, Animation(Animations.XBOWS_FLETCHING_PEARL_4470)),
        RED_TOPAZ(Items.STEEL_BOLTS_9141, Items.RED_TOPAZ_1613, Items.TOPAZ_BOLT_TIPS_9188, Items.TOPAZ_BOLTS_9336, 48, 3.9, Animation(Animations.HUMAN_REDTOPAZCUTTING_892)),
        SAPPHIRE(Items.MITHRIL_BOLTS_9142, Items.SAPPHIRE_1607, Items.SAPPHIRE_BOLT_TIPS_9189, Items.SAPPHIRE_BOLTS_9337, 56, 4.7, Animation(Animations.HUMAN_SAPPHIRECUTTING_888)),
        EMERALD(Items.MITHRIL_BOLTS_9142, Items.EMERALD_1605, Items.EMERALD_BOLT_TIPS_9190, Items.EMERALD_BOLTS_9338, 58, 5.5, Animation(Animations.HUMAN_EMERALDCUTTING_889)),
        RUBY(Items.ADAMANT_BOLTS_9143, Items.RUBY_1603, Items.RUBY_BOLT_TIPS_9191, Items.RUBY_BOLTS_9339, 63, 6.3, Animation(Animations.HUMAN_RUBYCUTTING_887)),
        DIAMOND(Items.ADAMANT_BOLTS_9143, Items.DIAMOND_1601, Items.DIAMOND_BOLT_TIPS_9192, Items.DIAMOND_BOLTS_9340, 65, 7.0, Animation(Animations.HUMAN_DIAMONDCUTTING_886)),
        DRAGONSTONE(Items.RUNITE_BOLTS_9144, Items.DRAGONSTONE_1615, Items.DRAGON_BOLT_TIPS_9193, Items.DRAGON_BOLTS_9341, 71, 8.2, Animation(Animations.HUMAN_DRAGONSTONECUTTING_885)),
        ONYX(Items.RUNITE_BOLTS_9144, Items.ONYX_6573, Items.ONYX_BOLT_TIPS_9194, Items.ONYX_BOLTS_9342, 73, 9.4, Animation(Animations.HUMAN_ONYXCUTTING_2717));

        companion object {
            val values = enumValues<GemBolt>()
            val product = values.associateBy { it.base }
            val gemToBolt = values.associateBy { it.gem }

            @JvmStatic
            fun forId(id: Int): GemBolt? = values().find { it.base == id || it.tip == id }
        }
    }

    /**
     * Represents the types of Kebbit bolts.
     */
    enum class KebbitBolt(val base: Int, val product: Int, val level: Int, val xp: Double) {
        KEBBIT_BOLT(Items.KEBBIT_SPIKE_10105, Items.KEBBIT_BOLTS_10158, 32, 5.80),
        LONG_KEBBIT_BOLT(Items.LONG_KEBBIT_SPIKE_10107, Items.LONG_KEBBIT_BOLTS_10159, 83, 7.90),
        ;

        companion object {
            fun forId(item: Item): KebbitBolt? = values().firstOrNull { it.base == item.id }
        }
    }

    data class FletchData(val id: Int, val xp: Double, val level: Int, val amount: Int, val logId: Int)

    /**
     * Represents table of all log-based fletching products.
     */
    private val fletchTable = listOf(
        // Standard logs.
        FletchData(Items.ARROW_SHAFT_52,    5.0,  1,  15, Items.LOGS_1511),
        FletchData(Items.SHORTBOW_U_50,     5.0,  5,  1, Items.LOGS_1511),
        FletchData(Items.LONGBOW_U_48,      10.0, 10, 1, Items.LOGS_1511),
        FletchData(Items.WOODEN_STOCK_9440, 6.0,  9,  1, Items.LOGS_1511),

        // Achey logs.
        FletchData(Items.OGRE_ARROW_SHAFT_2864,  6.4,  5,  4, Items.ACHEY_TREE_LOGS_2862),
        FletchData(Items.UNSTRUNG_COMP_BOW_4825, 40.0, 30, 1, Items.ACHEY_TREE_LOGS_2862),

        // Oak.
        FletchData(Items.OAK_SHORTBOW_U_54, 16.5, 20, 1, Items.OAK_LOGS_1521),
        FletchData(Items.OAK_LONGBOW_U_56,  25.0, 25, 1, Items.OAK_LOGS_1521),
        FletchData(Items.OAK_STOCK_9442,    16.0, 24, 1, Items.OAK_LOGS_1521),

        // Willow.
        FletchData(Items.WILLOW_SHORTBOW_U_60, 33.3, 35, 1, Items.WILLOW_LOGS_1519),
        FletchData(Items.WILLOW_LONGBOW_U_58,  41.5, 40, 1, Items.WILLOW_LOGS_1519),
        FletchData(Items.WILLOW_STOCK_9444,    22.0, 39, 1, Items.WILLOW_LOGS_1519),

        // Maple.
        FletchData(Items.MAPLE_SHORTBOW_U_64, 50.0, 50, 1, Items.MAPLE_LOGS_1517),
        FletchData(Items.MAPLE_LONGBOW_U_62,  58.3, 55, 1, Items.MAPLE_LOGS_1517),
        FletchData(Items.MAPLE_STOCK_9448,    32.0, 54, 1, Items.MAPLE_LOGS_1517),

        // Yew.
        FletchData(Items.YEW_SHORTBOW_U_68, 67.5, 65, 1, Items.YEW_LOGS_1515),
        FletchData(Items.YEW_LONGBOW_U_66,  75.0, 70, 1, Items.YEW_LOGS_1515),
        FletchData(Items.YEW_STOCK_9452,    50.0, 69, 1, Items.YEW_LOGS_1515),

        // Magic.
        FletchData(Items.MAGIC_SHORTBOW_U_72, 83.3, 80, 1, Items.MAGIC_LOGS_1513),
        FletchData(Items.MAGIC_LONGBOW_U_70,  91.5, 85, 1, Items.MAGIC_LOGS_1513),

        // Teak & Mahogany stocks.
        FletchData(Items.TEAK_STOCK_9446,     27.0, 46, 1, Items.TEAK_LOGS_6333),
        FletchData(Items.MAHOGANY_STOCK_9450, 41.0, 61, 1, Items.MAHOGANY_LOGS_6332)
    )

    /**
     * Maps log id with list of fletchable products.
     */
    private val LOGS_MAP: Map<Int, List<FletchData>> = fletchTable.groupBy { it.logId }

    // Core product constants.
    const val ARROW_SHAFT       = Items.ARROW_SHAFT_52
    const val HEADLESS_ARROW    = Items.HEADLESS_ARROW_53

    // Ogre item ids.
    const val OGRE_ARROW_SHAFT      = Items.OGRE_ARROW_SHAFT_2864
    const val WOLFBONE_ARROWTIP     = Items.WOLFBONE_ARROWTIPS_2861
    const val FLIGHTED_OGRE_ARROW   = Items.FLIGHTED_OGRE_ARROW_2865
    const val OGRE_ARROW            = Items.OGRE_ARROW_2866

    val FLETCH_LOGS       = intArrayOf(
        Items.LOGS_1511, Items.OAK_LOGS_1521, Items.WILLOW_LOGS_1519,
        Items.MAPLE_LOGS_1517, Items.YEW_LOGS_1515, Items.MAGIC_LOGS_1513,
        Items.ACHEY_TREE_LOGS_2862, Items.MAHOGANY_LOGS_6332, Items.TEAK_LOGS_6333
    )

    /**
     * All usable feather variations.
     */
    val FEATHER_IDS       = intArrayOf(
        Items.FEATHER_314, Items.STRIPY_FEATHER_10087, Items.RED_FEATHER_10088,
        Items.BLUE_FEATHER_10089, Items.YELLOW_FEATHER_10090, Items.ORANGE_FEATHER_10091
    )

    /**
     * Bowstrings and crossbow strings.
     */
    val STRING_IDS        = intArrayOf(Items.BOW_STRING_1777, Items.CROSSBOW_STRING_9438)

    /**
     * Kebbit spike ids used for long/regular darts.
     */
    val KEBBIT_SPIKE_IDS  = intArrayOf(Items.KEBBIT_SPIKE_10105, Items.LONG_KEBBIT_SPIKE_10107)

    /**
     * Gems that can be cut and attached to bolts.
     */
    val BOLT_GEM_IDS      = intArrayOf(
        Items.OYSTER_PEARL_411, Items.OYSTER_PEARLS_413, Items.OPAL_1609, Items.JADE_1611,
        Items.RED_TOPAZ_1613, Items.SAPPHIRE_1607, Items.EMERALD_1605, Items.RUBY_1603,
        Items.DIAMOND_1601, Items.DRAGONSTONE_1615, Items.ONYX_6573
    )

    /**
     * Crossbow limbs.
     */
    val LIMB_IDS          = Limb.values().map(Limb::limb).toIntArray()

    /**
     * Crossbow stocks.
     */
    val STOCK_IDS         = Limb.values().map(Limb::stock).toIntArray()

    /**
     * Nails used for brutal arrows.
     */
    val NAIL_IDS          = BrutalArrow.values().map(BrutalArrow::nailId).toIntArray()

    /**
     * Unfinished arrow shafts.
     */
    val UNF_ARROW_IDS     = ArrowHead.values().map(ArrowHead::arrowTipsId).toIntArray()

    /**
     * Unstrung bows.
     */
    val UNF_BOW_IDS       = Strings.values().map(Strings::unfinished).toIntArray()

    /**
     * Bolt bases that can be gem-tipped.
     */
    val GEM_BOLT_IDS      = GemBolt.values().map(GemBolt::base).toIntArray()

    /**
     * Gem bolt tips.
     */
    val GEM_BOLT_TIPS_IDS = GemBolt.values().map(GemBolt::tip).toIntArray()

    /**
     * Unfinished darts.
     */
    val UNF_DARTS         = Dart.values().map(Dart::dartTipId).toIntArray()

    /**
     * Unfinished bolts.
     */
    val UNF_BOLTS         = Bolt.values().map(Bolt::unfinished).toIntArray()

    /**
     * Counts all feathers in the player's inventory across all feather types.
     */
    fun getFeatherAmount(player: Player): Int =
        FEATHER_IDS.sumOf { id -> player.inventory.getAmount(Item(id)) }

    /**
     * Determines feather priority used when converting shafts to headless arrows.
     */
    fun getFeatherPriorityOrder(): List<Int> {
        val normal = Items.FEATHER_314
        val others = FEATHER_IDS.filter { it != normal }
        return listOf(normal) + others
    }

    /**
     * Returns all possible fletching products for the given log id.
     */
    fun getEntries(id: Int) = LOGS_MAP[id]

    /**
     * Returns an array of item instances representing fletchable products.
     */
    fun getItems(id: Int) =
        getEntries(id)?.map { Item(it.id) }?.toTypedArray()
}