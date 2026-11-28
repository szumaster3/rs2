package content.global.skill.magic.spells.modern

import content.global.skill.magic.spells.ModernSpells
import content.global.skill.magic.spells.SpellProjectile
import core.game.node.entity.combat.spell.Runes
import core.game.node.entity.combat.spell.SpellType
import core.game.node.entity.impl.Projectile
import core.game.node.item.Item
import core.game.world.update.flag.context.Graphics
import shared.consts.Sounds
import shared.consts.Graphics as Gfx

/**
 * Configuration of modern elemental spells.
 */
enum class ElementalSpellDefinition(val element: Element, val type: SpellType, val button: Int, val level: Int, val xp: Double, val sound: Int, val start: Graphics, val projectile: Projectile, val end: Graphics, val runes: Array<Item>) {
    AIR_STRIKE   (Element.AIR,   SpellType.STRIKE, ModernSpells.AIR_STRIKE, 1, 5.5,   Sounds.WINDSTRIKE_CAST_AND_FIRE_220,     Graphics(Gfx.ANCIENT_SPELLBOOK_TELEPORT_REWORK_SPOTANIM_NPC_1_90,92),  SpellProjectile.create(Gfx.STRIKE_TRAVEL_1_91),  Graphics(Gfx.STRIKE_IMPACT_1_92,92),  arrayOf(Runes.MIND_RUNE.getItem(1),  Runes.AIR_RUNE.getItem(1))),
    AIR_BOLT     (Element.AIR,   SpellType.BOLT,   ModernSpells.AIR_BOLT,   17, 13.5, Sounds.WINDBOLT_CAST_AND_FIRE_218,       Graphics(Gfx.ANCIENT_SPELLBOOK_TELEPORT_REWORK_SPOTANIM_NPC_5_117,92),   SpellProjectile.create(Gfx.AREA_DRAYNOR_BANKER_READY_MALE_HIGHCOUNTER_1_118),   Graphics(Gfx.BOLT_IMPACT_1_119,92),   arrayOf(Runes.CHAOS_RUNE.getItem(1), Runes.AIR_RUNE.getItem(2))),
    AIR_BLAST    (Element.AIR,   SpellType.BLAST,  ModernSpells.AIR_BLAST,  41, 25.5, Sounds.WINDBLAST_CAST_AND_FIRE_216,      Graphics(Gfx.ANCIENT_SPELLBOOK_TELEPORT_REWORK_SPOTANIM_NPC_10_132,92),  SpellProjectile.create(Gfx.BLAST_TRAVEL_2_133),  Graphics(Gfx.NECKLACE_TELEPORT_REWORK_SPOTANIM_NPC_2_134,92),  arrayOf(Runes.DEATH_RUNE.getItem(1), Runes.AIR_RUNE.getItem(3))),
    AIR_WAVE     (Element.AIR,   SpellType.WAVE,   ModernSpells.AIR_WAVE,   62, 36.0, Sounds.WINDWAVE_CAST_AND_FIRE_222,       Graphics(Gfx.AREA_DRAYNOR_BANKER_READY_RANDOM_MALE_HIGHCOUNTER_2_158,92),   SpellProjectile.create(Gfx.AREA_DRAYNOR_BANKER_READY_FEMALE_HIGHCOUNTER_2_159),   Graphics(Gfx.WAVE_IMPACT_3_160,92),   arrayOf(Runes.BLOOD_RUNE.getItem(1), Runes.AIR_RUNE.getItem(5))),

    WATER_STRIKE (Element.WATER, SpellType.STRIKE, ModernSpells.WATER_STRIKE, 5, 7.5,  Sounds.WATERSTRIKE_CAST_AND_FIRE_211, Graphics(Gfx.ANCIENT_SPELLBOOK_TELEPORT_REWORK_SPOTANIM_NPC_2_93,92), SpellProjectile.create(Gfx.STRIKE_TRAVEL_2_94), Graphics(Gfx.STRIKE_IMPACT_2_95,92), arrayOf(Runes.MIND_RUNE.getItem(1),  Runes.WATER_RUNE.getItem(1), Runes.AIR_RUNE.getItem(1))),
    WATER_BOLT   (Element.WATER, SpellType.BOLT,   ModernSpells.WATER_BOLT,  23, 16.5, Sounds.WATERBOLT_CAST_AND_FIRE_209,   Graphics(Gfx.ANCIENT_SPELLBOOK_TELEPORT_REWORK_SPOTANIM_NPC_6_120,92),  SpellProjectile.create(Gfx.AREA_DRAYNOR_BANKER_READY_MALE_HIGHCOUNTER_2_121),  Graphics(Gfx.BOLT_IMPACT_2_122,92),  arrayOf(Runes.CHAOS_RUNE.getItem(1), Runes.WATER_RUNE.getItem(2), Runes.AIR_RUNE.getItem(2))),
    WATER_BLAST  (Element.WATER, SpellType.BLAST,  ModernSpells.WATER_BLAST, 47, 28.5, Sounds.WATERBLAST_CAST_AND_FIRE_207,  Graphics(Gfx.ANCIENT_SPELLBOOK_TELEPORT_REWORK_SPOTANIM_NPC_11_135,92), SpellProjectile.create(Gfx.BLAST_TRAVEL_3_136), Graphics(Gfx.NECKLACE_TELEPORT_REWORK_SPOTANIM_NPC_3_137,92), arrayOf(Runes.DEATH_RUNE.getItem(1), Runes.WATER_RUNE.getItem(3), Runes.AIR_RUNE.getItem(3))),
    WATER_WAVE   (Element.WATER, SpellType.WAVE,   ModernSpells.WATER_WAVE,  65, 37.5, Sounds.WATERWAVE_CAST_AND_FIRE_213,   Graphics(Gfx.AREA_DRAYNOR_BANKER_READY_RANDOM_MALE_HIGHCOUNTER_3_161,92),  SpellProjectile.create(Gfx.AREA_DRAYNOR_BANKER_READY_FEMALE_HIGHCOUNTER_3_162),  Graphics(Gfx.WAVE_IMPACT_4_163,92),  arrayOf(Runes.BLOOD_RUNE.getItem(1), Runes.WATER_RUNE.getItem(7), Runes.AIR_RUNE.getItem(5))),

    EARTH_STRIKE (Element.EARTH, SpellType.STRIKE, ModernSpells.EARTH_STRIKE,9, 9.5,   Sounds.EARTHSTRIKE_CAST_AND_FIRE_132, Graphics(Gfx.ANCIENT_SPELLBOOK_TELEPORT_REWORK_SPOTANIM_NPC_3_96,92), SpellProjectile.create(Gfx.STRIKE_TRAVEL_3_97), Graphics(Gfx.STRIKE_IMPACT_3_98,92), arrayOf(Runes.MIND_RUNE.getItem(1),  Runes.EARTH_RUNE.getItem(2), Runes.AIR_RUNE.getItem(1))),
    EARTH_BOLT   (Element.EARTH, SpellType.BOLT,   ModernSpells.EARTH_BOLT,  29, 19.5, Sounds.EARTHBOLT_CAST_AND_FIRE_130,   Graphics(Gfx.ANCIENT_SPELLBOOK_TELEPORT_REWORK_SPOTANIM_NPC_7_123,92),  SpellProjectile.create(Gfx.AREA_DRAYNOR_BANKER_READY_MALE_HIGHCOUNTER_3_124),  Graphics(Gfx.BOLT_IMPACT_3_125,92),  arrayOf(Runes.CHAOS_RUNE.getItem(1), Runes.EARTH_RUNE.getItem(3), Runes.AIR_RUNE.getItem(2))),
    EARTH_BLAST  (Element.EARTH, SpellType.BLAST,  ModernSpells.EARTH_BLAST, 53, 31.5, Sounds.EARTHBLAST_CAST_AND_FIRE_128,  Graphics(Gfx.ANCIENT_SPELLBOOK_TELEPORT_REWORK_SPOTANIM_NPC_12_138,92), SpellProjectile.create(Gfx.BLAST_TRAVEL_4_139), Graphics(Gfx.NECKLACE_TELEPORT_REWORK_SPOTANIM_NPC_4_140,92), arrayOf(Runes.DEATH_RUNE.getItem(1), Runes.EARTH_RUNE.getItem(4), Runes.AIR_RUNE.getItem(3))),
    EARTH_WAVE   (Element.EARTH, SpellType.WAVE,   ModernSpells.EARTH_WAVE,  70, 40.0, Sounds.EARTHWAVE_CAST_AND_FIRE_134,   Graphics(Gfx.AREA_DRAYNOR_BANKER_READY_RANDOM_MALE_HIGHCOUNTER_4_164,92),  SpellProjectile.create(Gfx.AREA_DRAYNOR_BANKER_READY_FEMALE_HIGHCOUNTER_4_165),  Graphics(Gfx.WAVE_IMPACT_5_166,92),  arrayOf(Runes.BLOOD_RUNE.getItem(1), Runes.EARTH_RUNE.getItem(7), Runes.AIR_RUNE.getItem(5))),

    FIRE_STRIKE  (Element.FIRE,  SpellType.STRIKE, ModernSpells.FIRE_STRIKE, 13, 11.5, Sounds.FIRESTRIKE_CAST_AND_FIRE_160,   Graphics(Gfx.ANCIENT_SPELLBOOK_TELEPORT_REWORK_SPOTANIM_NPC_4_99,92),  SpellProjectile.create(Gfx.STRIKE_TRAVEL_4_100), Graphics(Gfx.STRIKE_IMPACT_4_101,92), arrayOf(Runes.MIND_RUNE.getItem(1),  Runes.FIRE_RUNE.getItem(3), Runes.AIR_RUNE.getItem(2))),
    FIRE_BOLT    (Element.FIRE,  SpellType.BOLT,   ModernSpells.FIRE_BOLT,   35, 22.5, Sounds.FIREBOLT_CAST_AND_FIRE_157,     Graphics(Gfx.ANCIENT_SPELLBOOK_TELEPORT_REWORK_SPOTANIM_NPC_8_126,92),   SpellProjectile.create(Gfx.AREA_DRAYNOR_BANKER_READY_MALE_HIGHCOUNTER_4_127),   Graphics(Gfx.BOLT_IMPACT_4_128,92),   arrayOf(Runes.CHAOS_RUNE.getItem(1), Runes.FIRE_RUNE.getItem(4), Runes.AIR_RUNE.getItem(3))),
    FIRE_BLAST   (Element.FIRE,  SpellType.BLAST,  ModernSpells.FIRE_BLAST,  59, 34.5, Sounds.FIREBLAST_CAST_AND_FIRE_155,    Graphics(Gfx.ANCIENT_SPELLBOOK_TELEPORT_REWORK_SPOTANIM_NPC_9_129,92),  SpellProjectile.create(Gfx.BLAST_TRAVEL_1_130),  Graphics(Gfx.NECKLACE_TELEPORT_REWORK_SPOTANIM_NPC_1_131,92),  arrayOf(Runes.DEATH_RUNE.getItem(1), Runes.FIRE_RUNE.getItem(5), Runes.AIR_RUNE.getItem(4))),
    FIRE_WAVE    (Element.FIRE,  SpellType.WAVE,   ModernSpells.FIRE_WAVE,   75, 42.5, Sounds.FIREWAVE_CAST_AND_FIRE_162,     Graphics(Gfx.AREA_DRAYNOR_BANKER_READY_RANDOM_MALE_HIGHCOUNTER_1_155,92),   SpellProjectile.create(Gfx.AREA_DRAYNOR_BANKER_READY_FEMALE_HIGHCOUNTER_1_156),   Graphics(Gfx.WAVE_IMPACT_2_157,92),   arrayOf(Runes.BLOOD_RUNE.getItem(1), Runes.FIRE_RUNE.getItem(7), Runes.AIR_RUNE.getItem(5)));

    enum class Element { AIR, WATER, EARTH, FIRE }
}
