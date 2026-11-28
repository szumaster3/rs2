package content.global.plugins.inter.with_item

import core.api.addItemOrDrop
import core.api.removeItem
import core.api.replaceSlot
import core.api.sendMessage
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.item.Item
import shared.consts.Items
import kotlin.math.min

class PoisonWeaponPlugin : InteractionListener {

    private val poisonsId = intArrayOf(
        Items.WEAPON_POISON_187,
        Items.WEAPON_POISON_PLUS_5937,
        Items.WEAPON_POISON_PLUS_PLUS_5940,
    )

    private val regularWeapons = PoisonSets.itemMap.keys.toIntArray()
    private val poisonedWeapons = PoisonSets.poisonedToBase.keys.toIntArray()

    private val regularKarambwanWeapons = KarambwanPoisonSets.itemMap.keys.toIntArray()
    private val karambwanPoisonedWeapons = KarambwanPoisonSets.poisonedToBase.keys.toIntArray()

    override fun defineListeners() {
        /*
         * Handles apply weapon poison to up to five pieces of ammunition or weapons.
         */

        onUseWith(IntType.ITEM, poisonsId, *regularWeapons) { player, used, with ->
            val poisonIndex = poisonsId.indexOf(used.id)
            if (poisonIndex == -1) return@onUseWith false

            val weapon = with.asItem()
            val product = PoisonSets.itemMap[weapon.id]?.getOrNull(poisonIndex) ?: return@onUseWith false
            val amount = min(5, weapon.amount)

            if (amount == weapon.amount) {
                replaceSlot(player, weapon.slot, Item(product, amount))
            } else {
                if (!removeItem(player, Item(weapon.id, amount))) {
                    return@onUseWith false
                }
                addItemOrDrop(player, product, amount)
            }
            replaceSlot(player, used.asItem().slot, Item(Items.VIAL_229))
            sendMessage(player, "You poison the ${weapon.name.lowercase()}.")
            return@onUseWith true
        }

        /*
         * Handles apply poisonous Karambwan paste to a spear or hasta.
         */

        onUseWith(IntType.ITEM, Items.KARAMBWAN_PASTE_3153, *regularKarambwanWeapons) { player, used, with ->
            val weapon = with.asItem()
            val product = KarambwanPoisonSets.itemMap[weapon.id] ?: return@onUseWith false

            if (!removeItem(player, used.asItem())) {
                return@onUseWith false
            }

            replaceSlot(player, weapon.slot, Item(product.kp))
            val weaponType = if (product.name.endsWith("SPEAR")) "spear" else "hasta"
            sendMessage(player, "You smear the poisonous Karambwan paste over the $weaponType.")
            return@onUseWith true
        }

        /*
         * Handles remove the regular weapon poison with a cleaning cloth.
         */

        onUseWith(IntType.ITEM, Items.CLEANING_CLOTH_3188, *poisonedWeapons) { player, _, with ->
            val weapon = with.asItem()
            val base = PoisonSets.getBase(weapon.id) ?: return@onUseWith false
            val amount = min(5, weapon.amount)

            if (amount == weapon.amount) {
                replaceSlot(player, weapon.slot, Item(base, amount))
            } else {
                if (!removeItem(player, Item(weapon.id, amount))) {
                    return@onUseWith false
                }
                addItemOrDrop(player, base, amount)
            }
            return@onUseWith true
        }

        /*
         * Handles remove Karambwan poison with a cleaning cloth.
         */

        onUseWith(IntType.ITEM, Items.CLEANING_CLOTH_3188, *karambwanPoisonedWeapons) { player, _, with ->
            val weapon = with.asItem()
            val base = KarambwanPoisonSets.getBase(weapon.id) ?: return@onUseWith false
            val amount = min(5, weapon.amount)

            if (amount == weapon.amount) {
                replaceSlot(player, weapon.slot, Item(base, amount))
            } else {
                if (!removeItem(player, Item(weapon.id, amount))) {
                    return@onUseWith false
                }
                addItemOrDrop(player, base, amount)
            }
            return@onUseWith true
        }
    }

    internal enum class KarambwanPoisonSets(val base: Int, val kp: Int) {
        BRONZE_SPEAR(base = Items.BRONZE_SPEAR_1237, kp = Items.BRONZE_SPEAR_KP_3170),
        IRON_SPEAR(base = Items.IRON_SPEAR_1239, kp = Items.IRON_SPEAR_KP_3171),
        STEEL_SPEAR(base = Items.STEEL_SPEAR_1241, kp = Items.STEEL_SPEAR_KP_3172),
        BLACK_SPEAR(base = Items.BLACK_SPEAR_4580, kp = Items.BLACK_SPEAR_KP_4584),
        MITHRIL_SPEAR(base = Items.MITHRIL_SPEAR_1243, kp = Items.MITHRIL_SPEAR_KP_3173),
        ADAMANT_SPEAR(base = Items.ADAMANT_SPEAR_1245, kp = Items.ADAMANT_SPEAR_KP_3174),
        RUNE_SPEAR(base = Items.RUNE_SPEAR_1247, kp = Items.RUNE_SPEAR_KP_3175),
        DRAGON_SPEAR(base = Items.DRAGON_SPEAR_1249, kp = Items.DRAGON_SPEAR_KP_3176),
        BRONZE_HASTA(base = Items.BRONZE_HASTA_11367, kp = Items.BRONZE_HASTA_KP_11381),
        IRON_HASTA(base = Items.IRON_HASTA_11369, kp = Items.IRON_HASTA_KP_11388),
        STEEL_HASTA(base = Items.STEEL_HASTA_11371, kp = Items.STEEL_HASTA_KP_11395),
        MITHRIL_HASTA(base = Items.MITHRIL_HASTA_11373, kp = Items.MITHRIL_HASTA_KP_11402),
        ADAMANT_HASTA(base = Items.ADAMANT_HASTA_11375, kp = Items.ADAMANT_HASTA_KP_11409),
        RUNE_HASTA(base = Items.RUNE_HASTA_11377, kp = Items.RUNE_HASTA_KP_11416);

        companion object {
            val itemMap = values().associateBy { it.base }
            val poisonedToBase: Map<Int, Int> = values().associate { it.kp to it.base }

            fun getBase(poisoned: Int): Int? = poisonedToBase[poisoned]
        }
    }

    internal enum class PoisonSets(val base: Int, val p: Int, val pp: Int, val ppp: Int) {
        BRONZE_ARROW(
            base = Items.BRONZE_ARROW_882,
            p = Items.BRONZE_ARROW_P_883,
            pp = Items.BRONZE_ARROW_P_PLUS_5616,
            ppp = Items.BRONZE_ARROW_P_PLUS_PLUS_5622
        ),
        IRON_ARROW(
            base = Items.IRON_ARROW_884,
            p = Items.IRON_ARROW_P_885,
            pp = Items.IRON_ARROW_P_PLUS_5617,
            ppp = Items.IRON_ARROW_P_PLUS_PLUS_5623,
        ),
        STEEL_ARROW(
            base = Items.STEEL_ARROW_886,
            p = Items.STEEL_ARROW_P_887,
            pp = Items.STEEL_ARROW_P_PLUS_5618,
            ppp = Items.STEEL_ARROW_P_PLUS_PLUS_5624,
        ),
        MITHRIL_ARROW(
            base = Items.MITHRIL_ARROW_888,
            p = Items.MITHRIL_ARROW_P_889,
            pp = Items.MITHRIL_ARROW_P_PLUS_5619,
            ppp = Items.MITHRIL_ARROW_P_PLUS_PLUS_5625,
        ),
        ADAMANT_ARROW(
            base = Items.ADAMANT_ARROW_890,
            p = Items.ADAMANT_ARROW_P_891,
            pp = Items.ADAMANT_ARROW_P_PLUS_5620,
            ppp = Items.ADAMANT_ARROW_P_PLUS_PLUS_5626,
        ),
        RUNE_ARROW(
            base = Items.RUNE_ARROW_892,
            p = Items.RUNE_ARROW_P_893,
            pp = Items.RUNE_ARROW_P_PLUS_5621,
            ppp = Items.RUNE_ARROW_P_PLUS_PLUS_5627,
        ),
        DRAGON_ARROW(
            base = Items.DRAGON_ARROW_11212,
            p = Items.DRAGON_ARROW_P_11227,
            pp = Items.DRAGON_ARROW_P_PLUS_11228,
            ppp = Items.DRAGON_ARROW_P_PLUS_PLUS_11229,
        ),
        BRONZE_KNIFE(
            base = Items.BRONZE_KNIFE_864,
            p = Items.BRONZE_KNIFE_P_870,
            pp = Items.BRONZE_KNIFE_P_PLUS_5654,
            ppp = Items.BRONZE_KNIFE_P_PLUS_PLUS_5661,
        ),
        IRON_KNIFE(
            base = Items.IRON_KNIFE_863,
            p = Items.IRON_KNIFE_P_871,
            pp = Items.IRON_KNIFE_P_PLUS_5655,
            ppp = Items.IRON_KNIFE_P_PLUS_PLUS_5662,
        ),
        STEEL_KNIFE(
            base = Items.STEEL_KNIFE_865,
            p = Items.STEEL_KNIFE_P_872,
            pp = Items.STEEL_KNIFE_P_PLUS_5656,
            ppp = Items.STEEL_KNIFE_P_PLUS_PLUS_5663,
        ),
        BLACK_KNIFE(
            base = Items.BLACK_KNIFE_869,
            p = Items.BLACK_KNIFE_P_874,
            pp = Items.BLACK_KNIFE_P_PLUS_5658,
            ppp = Items.BLACK_KNIFE_P_PLUS_PLUS_5665,
        ),
        MITHRIL_KNIFE(
            base = Items.MITHRIL_KNIFE_866,
            p = Items.MITHRIL_KNIFE_P_873,
            pp = Items.MITHRIL_KNIFE_P_PLUS_5657,
            ppp = Items.MITHRIL_KNIFE_P_PLUS_PLUS_5664,
        ),
        ADAMANT_KNIFE(
            base = Items.ADAMANT_KNIFE_867,
            p = Items.ADAMANT_KNIFE_P_875,
            pp = Items.ADAMANT_KNIFE_P_PLUS_5659,
            ppp = Items.ADAMANT_KNIFE_P_PLUS_PLUS_5666,
        ),
        RUNE_KNIFE(
            base = Items.RUNE_KNIFE_868,
            p = Items.RUNE_KNIFE_P_876,
            pp = Items.RUNE_KNIFE_P_PLUS_5660,
            ppp = Items.RUNE_KNIFE_P_PLUS_PLUS_5667,
        ),
        BRONZE_DART(
            base = Items.BRONZE_DART_806,
            p = Items.BRONZE_DART_P_812,
            pp = Items.BRONZE_DART_P_PLUS_5628,
            ppp = Items.BRONZE_DART_P_PLUS_PLUS_5635,
        ),
        IRON_DART(
            base = Items.IRON_DART_807,
            p = Items.IRON_DART_P_813,
            pp = Items.IRON_DART_P_PLUS_5629,
            ppp = Items.IRON_DART_P_PLUS_PLUS_5636,
        ),
        STEEL_DART(
            base = Items.STEEL_DART_808,
            p = Items.STEEL_DART_P_814,
            pp = Items.STEEL_DART_P_PLUS_5630,
            ppp = Items.STEEL_DART_P_PLUS_PLUS_5637,
        ),
        BLACK_DART(
            base = Items.BLACK_DART_3093,
            p = Items.BLACK_DART_P_3094,
            pp = Items.BLACK_DART_P_PLUS_5631,
            ppp = Items.BLACK_DART_P_PLUS_PLUS_5638,
        ),
        MITHRIL_DART(
            base = Items.MITHRIL_DART_809,
            p = Items.MITHRIL_DART_P_815,
            pp = Items.MITHRIL_DART_P_PLUS_5632,
            ppp = Items.MITHRIL_DART_P_PLUS_PLUS_5639,
        ),
        ADAMANT_DART(
            base = Items.ADAMANT_DART_810,
            p = Items.ADAMANT_DART_P_816,
            pp = Items.ADAMANT_DART_P_PLUS_5633,
            ppp = Items.ADAMANT_DART_P_PLUS_PLUS_5640,
        ),
        RUNE_DART(
            base = Items.RUNE_DART_811,
            p = Items.RUNE_DART_P_817,
            pp = Items.RUNE_DART_P_PLUS_5634,
            ppp = Items.RUNE_DART_P_PLUS_PLUS_5641,
        ),
        DRAGON_DART(
            base = Items.DRAGON_DART_11230,
            p = Items.DRAGON_DART_P_11231,
            pp = Items.DRAGON_DART_P_PLUS_11233,
            ppp = Items.DRAGON_DART_P_PLUS_PLUS_11234
        ),
        BLURITE_BOLT(
            base = Items.BLURITE_BOLTS_9139,
            p = Items.BLURITE_BOLTS_P_9286,
            pp = Items.BLURITE_BOLTS_P_PLUS_9293,
            ppp = Items.BLURITE_BOLTS_P_PLUS_PLUS_9300,
        ),
        BRONZE_BOLT(
            base = Items.BRONZE_BOLTS_877,
            p = Items.BRONZE_BOLTS_P_878,
            pp = Items.BRONZE_BOLTS_P_PLUS_6061,
            ppp = Items.BRONZE_BOLTS_P_PLUS_PLUS_6062,
        ),
        IRON_BOLT(
            base = Items.IRON_BOLTS_9140,
            p = Items.IRON_BOLTS_P_9287,
            pp = Items.IRON_BOLTS_P_PLUS_9294,
            ppp = Items.IRON_BOLTS_P_PLUS_PLUS_9301,
        ),
        STEEL_BOLT(
            base = Items.STEEL_BOLTS_9141,
            p = Items.STEEL_BOLTS_P_9288,
            pp = Items.STEEL_BOLTS_P_PLUS_9295,
            ppp = Items.STEEL_BOLTS_P_PLUS_PLUS_9302,
        ),
        BLACK_BOLT(
            base = Items.BLACK_BOLTS_13083,
            p = Items.BLACK_BOLTS_P_13084,
            pp = Items.BLACK_BOLTS_P_PLUS_13085,
            ppp = Items.BLACK_BOLTS_P_PLUS_PLUS_13086,
        ),
        MITHRIL_BOLT(
            base = Items.MITHRIL_BOLTS_9142,
            p = Items.MITHRIL_BOLTS_P_9289,
            pp = Items.MITHRIL_BOLTS_P_PLUS_9296,
            ppp = Items.MITHRIL_BOLTS_P_PLUS_PLUS_9303,
        ),
        ADAMANT_BOLT(
            base = Items.ADAMANT_BOLTS_9143,
            p = Items.ADAMANT_BOLTS_P_9290,
            pp = Items.ADAMANT_BOLTS_P_PLUS_9297,
            ppp = Items.ADAMANT_BOLTS_P_PLUS_PLUS_9304,
        ),
        RUNE_BOLT(
            base = Items.RUNITE_BOLTS_9144,
            p = Items.RUNITE_BOLTS_P_9291,
            pp = Items.RUNITE_BOLTS_P_PLUS_9298,
            ppp = Items.RUNITE_BOLTS_P_PLUS_PLUS_9305,
        ),
        SILVER_BOLT(
            base = Items.SILVER_BOLTS_9145,
            p = Items.SILVER_BOLTS_P_9292,
            pp = Items.SILVER_BOLTS_P_PLUS_9299,
            ppp = Items.SILVER_BOLTS_P_PLUS_PLUS_9306,
        ),
        BRONZE_JAVELIN(
            base = Items.BRONZE_JAVELIN_825,
            p = Items.BRONZE_JAVELIN_P_831,
            pp = Items.BRONZE_JAVELIN_P_PLUS_5642,
            ppp = Items.BRONZE_JAVN_P_PLUS_PLUS_5648,
        ),
        IRON_JAVELIN(
            base = Items.IRON_JAVELIN_826,
            p = Items.IRON_JAVELIN_P_832,
            pp = Items.IRON_JAVELIN_P_PLUS_5643,
            ppp = Items.IRON_JAVELIN_P_PLUS_PLUS_5649,
        ),
        STEEL_JAVELIN(
            base = Items.STEEL_JAVELIN_827,
            p = Items.STEEL_JAVELIN_P_833,
            pp = Items.STEEL_JAVELIN_P_PLUS_5644,
            ppp = Items.STEEL_JAVELIN_P_PLUS_PLUS_5650,
        ),
        MITHRIL_JAVELIN(
            base = Items.MITHRIL_JAVELIN_828,
            p = Items.MITHRIL_JAVELIN_P_834,
            pp = Items.MITHRIL_JAVELIN_P_PLUS_5645,
            ppp = Items.MITHRIL_JAVELIN_P_PLUS_PLUS_5651,
        ),
        ADAMANT_JAVELIN(
            base = Items.ADAMANT_JAVELIN_829,
            p = Items.ADAMANT_JAVELIN_P_835,
            pp = Items.ADAMANT_JAVELIN_P_PLUS_5646,
            ppp = Items.ADAMANT_JAVELIN_P_PLUS_PLUS_5652,
        ),
        RUNE_JAVELIN(
            base = Items.RUNE_JAVELIN_830,
            p = Items.RUNE_JAVELIN_P_836,
            pp = Items.RUNE_JAVELIN_P_PLUS_5647,
            ppp = Items.RUNE_JAVELIN_P_PLUS_PLUS_5653,
        ),
        MORRIGAN_JAVELIN(
            base = Items.MORRIGANS_JAVELIN_13879,
            p = Items.MORRIGANS_JAVELIN_P_13880,
            pp = Items.MORRIGANS_JAVELIN_P_PLUS_13881,
            ppp = Items.MORRIGANS_JAVELIN_P_PLUS_PLUS_13882,
        ),
        IRON_DAGGER(
            base = Items.IRON_DAGGER_1203,
            p = Items.IRON_DAGGER_P_1219,
            pp = Items.IRON_DAGGER_P_PLUS_5668,
            ppp = Items.IRON_DAGGER_P_PLUS_PLUS_5686,
        ),
        BRONZE_DAGGER(
            base = Items.BRONZE_DAGGER_1205,
            p = Items.BRONZE_DAGGER_P_1221,
            pp = Items.BRONZE_DAGGER_P_PLUS_5670,
            ppp = Items.BRZE_DAGGER_P_PLUS_PLUS_5688,
        ),
        STEEL_DAGGER(
            base = Items.STEEL_DAGGER_1207,
            p = Items.STEEL_DAGGER_P_1223,
            pp = Items.STEEL_DAGGER_P_PLUS_5672,
            ppp = Items.STEEL_DAGGER_P_PLUS_PLUS_5690,
        ),
        WHITE_DAGGER(
            base = Items.WHITE_DAGGER_6591,
            p = Items.WHITE_DAGGER_P_6593,
            pp = Items.WHITE_DAGGER_P_PLUS_6595,
            ppp = Items.WHITE_DAGGER_P_PLUS_PLUS_6597,
        ),
        BLACK_DAGGER(
            base = Items.BLACK_DAGGER_1217,
            p = Items.BLACK_DAGGER_P_1233,
            pp = Items.BLACK_DAGGER_P_PLUS_5682,
            ppp = Items.BLACK_DAGGER_P_PLUS_PLUS_5700,
        ),
        BONE_DAGGER(
            base = Items.BONE_DAGGER_8872,
            p = Items.BONE_DAGGER_P_8874,
            pp = Items.BONE_DAGGER_P_PLUS_8876,
            ppp = Items.BONE_DAGGER_P_PLUS_PLUS_8878,
        ),
        MITHRIL_DAGGER(
            base = Items.MITHRIL_DAGGER_1209,
            p = Items.MITHRIL_DAGGER_P_1225,
            pp = Items.MITHRIL_DAGGER_P_PLUS_5674,
            ppp = Items.MITHRIL_DAGGER_P_PLUS_PLUS_5692,
        ),
        ADAMANT_DAGGER(
            base = Items.ADAMANT_DAGGER_1211,
            p = Items.ADAMANT_DAGGER_P_1227,
            pp = Items.ADAMANT_DAGGER_P_PLUS_5676,
            ppp = Items.ADAMANT_DAGGER_P_PLUS_PLUS_5694,
        ),
        RUNE_DAGGER(
            base = Items.RUNE_DAGGER_1213,
            p = Items.RUNE_DAGGER_P_1229,
            pp = Items.RUNE_DAGGER_P_PLUS_5678,
            ppp = Items.RUNE_DAGGER_P_PLUS_PLUS_5696,
        ),
        DRAGON_DAGGER(
            base = Items.DRAGON_DAGGER_1215,
            p = Items.DRAGON_DAGGER_P_1231,
            pp = Items.DRAGON_DAGGER_P_PLUS_5680,
            ppp = Items.DRAGON_DAGGER_P_PLUS_PLUS_5698,
        ),
        BRONZE_SPEAR(
            base = Items.BRONZE_SPEAR_1237,
            p = Items.BRONZE_SPEAR_P_1251,
            pp = Items.BRONZE_SPEAR_P_PLUS_5704,
            ppp = Items.BRONZE_SPEAR_P_PLUS_PLUS_5718,
        ),
        IRON_SPEAR(
            base = Items.IRON_SPEAR_1239,
            p = Items.IRON_SPEAR_P_1253,
            pp = Items.IRON_SPEAR_P_PLUS_5706,
            ppp = Items.IRON_SPEAR_P_PLUS_PLUS_5720,
        ),
        STEEL_SPEAR(
            base = Items.STEEL_SPEAR_1241,
            p = Items.STEEL_SPEAR_P_1255,
            pp = Items.STEEL_SPEAR_P_PLUS_5708,
            ppp = Items.STEEL_SPEAR_P_PLUS_PLUS_5722,
        ),
        BLACK_SPEAR(
            base = Items.BLACK_SPEAR_4580,
            p = Items.BLACK_SPEAR_P_4582,
            pp = Items.BLACK_SPEAR_P_PLUS_5734,
            ppp = Items.BLACK_SPEAR_P_PLUS_PLUS_5736,
        ),
        MITHRIL_SPEAR(
            base = Items.MITHRIL_SPEAR_1243,
            p = Items.MITHRIL_SPEAR_P_1257,
            pp = Items.MITHRIL_SPEAR_P_PLUS_5710,
            ppp = Items.MITHRIL_SPEAR_P_PLUS_PLUS_5724,
        ),
        ADAMANT_SPEAR(
            base = Items.ADAMANT_SPEAR_1245,
            p = Items.ADAMANT_SPEAR_P_1259,
            pp = Items.ADAMANT_SPEAR_P_PLUS_5712,
            ppp = Items.ADAMANT_SPEAR_P_PLUS_PLUS_5726,
        ),
        RUNE_SPEAR(
            base = Items.RUNE_SPEAR_1247,
            p = Items.RUNE_SPEAR_P_1261,
            pp = Items.RUNE_SPEAR_P_PLUS_5714,
            ppp = Items.RUNE_SPEAR_P_PLUS_PLUS_5728,
        ),
        DRAGON_SPEAR(
            base = Items.DRAGON_SPEAR_1249,
            p = Items.DRAGON_SPEAR_P_1263,
            pp = Items.DRAGON_SPEAR_P_PLUS_5716,
            ppp = Items.DRAGON_SPEAR_P_PLUS_PLUS_5730,
        ),
        BRONZE_HASTA(
            base = Items.BRONZE_HASTA_11367,
            p = Items.BRONZE_HASTA_P_11379,
            pp = Items.BRONZE_HASTA_P_PLUS_11382,
            ppp = Items.BRONZE_HASTA_P_PLUS_PLUS_11384,
        ),
        IRON_HASTA(
            base = Items.IRON_HASTA_11369,
            p = Items.IRON_HASTA_P_11386,
            pp = Items.IRON_HASTA_P_PLUS_11389,
            ppp = Items.IRON_HASTA_P_PLUS_PLUS_11391,
        ),
        STEEL_HASTA(
            base = Items.STEEL_HASTA_11371,
            p = Items.STEEL_HASTA_P_11393,
            pp = Items.STEEL_HASTA_P_PLUS_11396,
            ppp = Items.STEEL_HASTA_P_PLUS_PLUS_11398,
        ),
        MITHRIL_HASTA(
            base = Items.MITHRIL_HASTA_11373,
            p = Items.MITHRIL_HASTA_P_11400,
            pp = Items.MITHRIL_HASTA_P_PLUS_11403,
            ppp = Items.MITHRIL_HASTA_P_PLUS_PLUS_11405,
        ),
        ADAMANT_HASTA(
            base = Items.ADAMANT_HASTA_11375,
            p = Items.ADAMANT_HASTA_P_11407,
            pp = Items.ADAMANT_HASTA_P_PLUS_11410,
            ppp = Items.ADAMANT_HASTA_P_PLUS_PLUS_11412,
        ),
        RUNE_HASTA(
            base = Items.RUNE_HASTA_11377,
            p = Items.RUNE_HASTA_P_11414,
            pp = Items.RUNE_HASTA_P_PLUS_11417,
            ppp = Items.RUNE_HASTA_P_PLUS_PLUS_11419
        ),
        ;

        companion object {
            val itemMap: Map<Int, IntArray> = values().associate {
                it.base to intArrayOf(it.p, it.pp, it.ppp)
            }

            val poisonedToBase: Map<Int, Int> = buildMap {
                for (set in values()) {
                    put(set.p, set.base)
                    put(set.pp, set.base)
                    put(set.ppp, set.base)
                }
            }

            fun getBase(poisoned: Int): Int? = poisonedToBase[poisoned]
        }
    }
}
