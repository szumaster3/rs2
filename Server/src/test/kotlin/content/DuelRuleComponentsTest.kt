package content

import content.minigame.duel_arena.plugin.DuelRule
import content.minigame.duel_arena.plugin.DuelRuleComponents
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DuelRuleComponentsTest {
    @Test fun normalRulesAndSummoningUseNativeWidgetPairs() {
        for (button in 29..30) assertEquals(DuelRule.NO_RANGE, DuelRuleComponents.rule(631, button))
        for (button in 49..50) assertEquals(DuelRule.NO_SPECIAL_ATTACKS, DuelRuleComponents.rule(631, button))
        for (button in 51..52) assertEquals(DuelRule.ENABLE_SUMMONING, DuelRuleComponents.rule(631, button))
        for (button in 38..39) assertEquals(DuelRule.ENABLE_SUMMONING, DuelRuleComponents.rule(637, button))
        for (button in 40..41) assertEquals(DuelRule.NO_SPECIAL_ATTACKS, DuelRuleComponents.rule(637, button))
    }

    @Test fun wornSlotWidgetsMatchScript205And560() {
        val expected = listOf(DuelRule.NO_HATS, DuelRule.NO_CAPES, DuelRule.NO_AMULET,
            DuelRule.NO_ARROWS, DuelRule.NO_WEAPON, DuelRule.NO_BODY, DuelRule.NO_SHIELD,
            DuelRule.NO_LEGS, DuelRule.NO_RINGS, DuelRule.NO_BOOTS, DuelRule.NO_GLOVES)
        for (i in expected.indices) {
            assertEquals(expected[i], DuelRuleComponents.rule(631, 56 + i))
            assertEquals(expected[i], DuelRuleComponents.rule(637, 45 + i))
        }
        assertNull(DuelRuleComponents.rule(631, 101))
        assertNull(DuelRuleComponents.rule(637, 82))
        assertNull(DuelRuleComponents.rule(626, 53))
    }
}
