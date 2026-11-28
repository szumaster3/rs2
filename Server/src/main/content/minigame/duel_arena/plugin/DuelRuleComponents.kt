package content.minigame.duel_arena.plugin

/** IF3 631/637; text pairs, equipment positions from CS2 205 and 560. */
object DuelRuleComponents {
    private val equipment = arrayOf(DuelRule.NO_HATS, DuelRule.NO_CAPES, DuelRule.NO_AMULET,
        DuelRule.NO_ARROWS, DuelRule.NO_WEAPON, DuelRule.NO_BODY, DuelRule.NO_SHIELD,
        DuelRule.NO_LEGS, DuelRule.NO_RINGS, DuelRule.NO_BOOTS, DuelRule.NO_GLOVES)
    fun rule(interfaceId: Int, button: Int): DuelRule? {
        val firstRule = when (interfaceId) { 631 -> 29; 637 -> 18; else -> return null }
        val firstEquipment = if (interfaceId == 631) 56 else 45
        if (button in firstEquipment..firstEquipment + 10) return equipment[button - firstEquipment]
        if (button !in firstRule..firstRule + 23) return null
        val index = (button - firstRule) / 2
        return if (interfaceId == 631 && index >= 10)
            if (index == 10) DuelRule.NO_SPECIAL_ATTACKS else DuelRule.ENABLE_SUMMONING
        else DuelRule.values()[index]
    }
}
