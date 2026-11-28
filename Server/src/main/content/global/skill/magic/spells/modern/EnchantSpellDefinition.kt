package content.global.skill.magic.spells.modern

import core.game.node.entity.combat.spell.Runes
import core.game.node.item.Item
import shared.consts.Items

/**
 * Represents configuration of jewelry enchantment spells.
 */
enum class EnchantSpellDefinition(val buttonId: Int, val level: Int, val experience: Double, val jewellery: Map<Int, Item>, val runes: Array<Item?>) {
    SAPPHIRE(5, 7, 17.5, jewellery = mapOf(Items.SAPPHIRE_RING_1637 to Item(Items.RING_OF_RECOIL_2550), Items.SAPPHIRE_NECKLACE_1656 to Item(Items.GAMES_NECKLACE_8_3853), Items.SAPPHIRE_AMULET_1694 to Item(Items.AMULET_OF_MAGIC_1727), Items.SAPPHIRE_BRACELET_11072 to Item(Items.BRACELET_OF_CLAY_11074)), runes = arrayOf(Item(Items.COSMIC_RUNE_564, 1), Item(Items.WATER_RUNE_555, 1))),
    EMERALD(16, 27, 37.0, jewellery = mapOf(Items.EMERALD_RING_1639 to Item(Items.RING_OF_DUELLING_8_2552), Items.EMERALD_NECKLACE_1658 to Item(Items.BINDING_NECKLACE_5521), Items.EMERALD_AMULET_1696 to Item(Items.AMULET_OF_DEFENCE_1729), Items.EMERALD_BRACELET_11076 to Item(Items.CASTLEWAR_BRACE_3_11079), Items.SILVER_SICKLE_EMERALD_B_13155 to Item(Items.ENCHANTED_SICKLE_EMERALD_B_13156)), runes = arrayOf(Item(Runes.COSMIC_RUNE.id, 1), Item(Runes.AIR_RUNE.id, 3))),
    RUBY(28, 49, 59.0, jewellery = mapOf(Items.RUBY_RING_1641 to Item(Items.RING_OF_FORGING_2568), Items.RUBY_NECKLACE_1660 to Item(Items.DIGSITE_PENDANT_5_11194), Items.RUBY_AMULET_1698 to Item(Items.AMULET_OF_STRENGTH_1725), Items.RUBY_BRACELET_11085 to Item(Items.INOCULATION_BRACE_11088)), runes = arrayOf(Item(Runes.COSMIC_RUNE.id, 1), Item(Runes.FIRE_RUNE.id, 5))),
    DIAMOND(36, 57, 67.0, jewellery = mapOf(Items.DIAMOND_RING_1643 to Item(Items.RING_OF_LIFE_2570), Items.DIAMOND_NECKLACE_1662 to Item(Items.PHOENIX_NECKLACE_11090), Items.DIAMOND_AMULET_1700 to Item(Items.AMULET_OF_POWER_1731), Items.DIAMOND_BRACELET_11092 to Item(Items.FORINTHRY_BRACE_5_11095)), runes = arrayOf(Item(Runes.COSMIC_RUNE.id, 1), Item(Runes.EARTH_RUNE.id, 10))),
    DRAGON(51, 68, 78.0, jewellery = mapOf(Items.DRAGONSTONE_RING_1645 to Item(Items.RING_OF_WEALTH_2572), Items.DRAGON_NECKLACE_1664 to Item(Items.SKILLS_NECKLACE_4_11105), Items.DRAGONSTONE_AMMY_1702 to Item(Items.AMULET_OF_GLORY_4_1712), Items.DRAGON_BRACELET_11115 to Item(Items.COMBAT_BRACELET_4_11118)), runes = arrayOf(Item(Runes.COSMIC_RUNE.id, 1), Item(Runes.WATER_RUNE.id, 15), Item(Runes.EARTH_RUNE.id, 15))),
    ONYX(61, 87, 97.0, jewellery = mapOf(Items.ONYX_RING_6575 to Item(Items.RING_OF_STONE_6583), Items.ONYX_NECKLACE_6577 to Item(Items.BERSERKER_NECKLACE_11128), Items.ONYX_AMULET_6581 to Item(Items.AMULET_OF_FURY_6585), Items.ONYX_BRACELET_11130 to Item(Items.REGEN_BRACELET_11133)), runes = arrayOf(Item(Runes.COSMIC_RUNE.id, 1), Item(Runes.FIRE_RUNE.id, 20), Item(Runes.EARTH_RUNE.id, 20)));

    companion object {
        val orbs = arrayOf(Items.CUBE_6899, Items.CYLINDER_6898, Items.ICOSAHEDRON_6900, Items.PENTAMID_6901, Items.DRAGONSTONE_6903).associateWith { Item(Items.ORB_6902) }
    }
}