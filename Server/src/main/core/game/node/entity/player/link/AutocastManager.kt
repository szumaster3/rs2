package core.game.node.entity.player.link

import core.api.*
import core.game.node.entity.combat.equipment.WeaponInterface
import core.game.node.entity.combat.spell.CombatSpell
import core.game.node.entity.player.Player

/**
 * Manages autocast spell selection, defensive casting,
 * combat style updates and autocast state restoration.
 *
 * Spell mappings are based on cache enum 729.
 */
object AutocastManager {
    private data class Entry(val id: Int, val book: Int, val button: Int)
    private val entries = listOf(
        Entry(1, 192, 25),
        Entry(2, 192, 28),
        Entry(3, 192, 30),
        Entry(4, 192, 32),
        Entry(5, 192, 34),
        Entry(6, 192, 39),
        Entry(7, 192, 42),
        Entry(8, 192, 45),
        Entry(9, 192, 49),
        Entry(10, 192, 52),
        Entry(11, 192, 58),
        Entry(12, 192, 63),
        Entry(13, 192, 70),
        Entry(14, 192, 73),
        Entry(15, 192, 77),
        Entry(16, 192, 80),
        Entry(17, 192, 47),
        Entry(18, 192, 56),
        Entry(19, 192, 67),
        Entry(20, 192, 66),
        Entry(21, 192, 68),
        Entry(22, 192, 54),
        Entry(23, 192, 84),
        Entry(24, 192, 87),
        Entry(25, 192, 89),
        Entry(26, 192, 91),
        Entry(31, 193, 28),
        Entry(32, 193, 32),
        Entry(33, 193, 24),
        Entry(34, 193, 20),
        Entry(35, 193, 30),
        Entry(36, 193, 34),
        Entry(37, 193, 26),
        Entry(38, 193, 22),
        Entry(39, 193, 29),
        Entry(40, 193, 33),
        Entry(41, 193, 25),
        Entry(42, 193, 21),
        Entry(43, 193, 31),
        Entry(44, 193, 35),
        Entry(45, 193, 27),
        Entry(46, 193, 23),
        Entry(47, 193, 36),
        Entry(48, 193, 38),
        Entry(49, 193, 37),
        Entry(50, 193, 39)
    )

    fun handle(player: Player, book: Int, button: Int, option: Int): Boolean {
        if (book != 192 && book != 193) return false
        if (player.spellBookManager.spellBook != book) return true
        if (button == (if (book == 192) 2 else 18) && option == 0) {
            setVarbit(player, 2668, if (getVarbit(player, 2668) == 0) 1 else 0)
            if (player.properties.autocastSpell != null) restore(player)
            return true
        }
        val entry = entries.firstOrNull { it.book == book && it.button == button } ?: return false
        if (option != (if (book == 192) 0 else 5)) return true
        val legacy = SpellButtonMapping.getSpellId(book, button) ?: return true
        if (player.properties.autocastSpell?.spellId == legacy) {
            player.getExtension<WeaponInterface>(WeaponInterface::class.java)?.selectAutoSpell(-1, true)
        } else selectLegacy(player, legacy)
        return true
    }

    fun selectLegacy(player: Player, legacy: Int) {
        val book = player.spellBookManager.spellBook
        val entry = entries.firstOrNull { it.book == book && SpellButtonMapping.getSpellId(book, it.button) == legacy } ?: return
        if (getAttribute(player, "spell:swap", 0) != 0) return
        val spell = SpellBookManager.SpellBook.forInterface(book)?.getSpell(legacy) as? CombatSpell ?: return
        if (player.skills.getLevel(6) < spell.levelRequirement()) return
        player.properties.autocastSpell = spell
        apply(player, entry)
    }

    fun restore(player: Player) {
        val spell = player.properties.autocastSpell ?: return
        val book = player.spellBookManager.spellBook
        val entry = entries.firstOrNull { it.book == book && SpellButtonMapping.getSpellId(book, it.button) == spell.spellId }
        if (entry == null) { clear(player); return }
        apply(player, entry)
    }

    private fun apply(player: Player, entry: Entry) {
        // Preserve the saved melee/ranged style index; magic XP uses the active style.
        player.properties.attackStyle = WeaponInterface.AttackStyle(
            if (getVarbit(player, 2668) == 1) WeaponInterface.STYLE_DEFENSIVE_CAST else WeaponInterface.STYLE_CAST,
            WeaponInterface.BONUS_MAGIC
        )
        setVarp(player, 108, (entry.id shl 1) or 1)
        player.properties.combatPulse.updateStyle()
    }

    fun clear(player: Player) {
        player.properties.autocastSpell = null
        setVarp(player, 108, 0)
    }
}
