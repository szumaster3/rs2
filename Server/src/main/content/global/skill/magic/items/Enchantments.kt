package content.global.skill.magic.items

import shared.consts.Items

object Enchantments {

    val LVL_1 = mapOf(
        Items.SAPPHIRE_RING_1637      to Items.RING_OF_RECOIL_2550,
        Items.SAPPHIRE_NECKLACE_1656  to Items.GAMES_NECKLACE_8_3853,
        Items.SAPPHIRE_AMULET_1694    to Items.AMULET_OF_MAGIC_1727,
        Items.SAPPHIRE_BRACELET_11072 to Items.BRACELET_OF_CLAY_11074
    )

    val LVL_2 = mapOf(
        Items.EMERALD_RING_1639            to Items.RING_OF_DUELLING_8_2552,
        Items.EMERALD_NECKLACE_1658        to Items.BINDING_NECKLACE_5521,
        Items.EMERALD_AMULET_1696          to Items.AMULET_OF_DEFENCE_1729,
        Items.EMERALD_BRACELET_11076       to Items.CASTLEWAR_BRACE_3_11079,
        Items.SILVER_SICKLE_EMERALD_B_13155 to Items.ENCHANTED_SICKLE_EMERALD_B_13156
    )

    val LVL_3 = mapOf(
        Items.RUBY_RING_1641      to Items.RING_OF_FORGING_2568,
        Items.RUBY_NECKLACE_1660  to Items.DIGSITE_PENDANT_5_11194,
        Items.RUBY_AMULET_1698    to Items.AMULET_OF_STRENGTH_1725,
        Items.RUBY_BRACELET_11085 to Items.INOCULATION_BRACE_11088
    )

    val LVL_4 = mapOf(
        Items.DIAMOND_RING_1643      to Items.RING_OF_LIFE_2570,
        Items.DIAMOND_NECKLACE_1662  to Items.PHOENIX_NECKLACE_11090,
        Items.DIAMOND_AMULET_1700    to Items.AMULET_OF_POWER_1731,
        Items.DIAMOND_BRACELET_11092 to Items.FORINTHRY_BRACE_5_11095
    )

    val LVL_5 = mapOf(
        Items.DRAGONSTONE_RING_1645 to Items.RING_OF_WEALTH_2572,
        Items.DRAGON_NECKLACE_1664  to Items.SKILLS_NECKLACE_4_11105,
        Items.DRAGONSTONE_AMMY_1702 to Items.AMULET_OF_GLORY_4_1712,
        Items.DRAGON_BRACELET_11115 to Items.COMBAT_BRACELET_4_11118
    )

    val LVL_6 = mapOf(
        Items.ONYX_RING_6575      to Items.RING_OF_STONE_6583,
        Items.ONYX_NECKLACE_6577  to Items.BERSERKER_NECKLACE_11128,
        Items.ONYX_AMULET_6581    to Items.AMULET_OF_FURY_6585,
        Items.ONYX_BRACELET_11130 to Items.REGEN_BRACELET_11133
    )

    val BY_TABLET = mapOf(
        Items.ENCHANT_SAPPHIRE_8016  to LVL_1,
        Items.ENCHANT_EMERALD_8017   to LVL_2,
        Items.ENCHANT_RUBY_8018      to LVL_3,
        Items.ENCHANT_DIAMOND_8019   to LVL_4,
        Items.ENCHANT_DRAGONSTN_8020 to LVL_5,
        Items.ENCHANT_ONYX_8021      to LVL_6
    )
}
