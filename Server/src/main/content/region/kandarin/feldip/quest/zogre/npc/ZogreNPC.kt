package content.region.kandarin.feldip.quest.zogre.npc

import core.api.getOrStartTimer
import core.api.inEquipment
import core.game.node.entity.Entity
import core.game.node.entity.combat.BattleState
import core.game.node.entity.combat.spell.SpellType
import core.game.node.entity.npc.NPC
import core.game.node.entity.npc.NPCBehavior
import core.game.node.entity.player.Player
import core.game.system.timer.impl.Disease
import shared.consts.Items
import shared.consts.NPCs

class ZogreNPC : NPCBehavior(*IDS) {

    override fun onCreation(self: NPC) {
        self.isWalks = true
        self.isNeverWalks = false
        self.isAggressive = true
        self.task.undead = true
    }

    override fun beforeDamageReceived(self: NPC, attacker: Entity, state: BattleState) {
        if (attacker is Player) {
            if (inEquipment(attacker, Items.COMP_OGRE_BOW_4827)) {
                return
            }
            if (state.spell != null && state.spell.type == SpellType.CRUMBLE_UNDEAD) {
                state.estimatedHit = (state.estimatedHit * 0.5).toInt()
                if (state.secondaryHit > 0) {
                    state.secondaryHit = (state.secondaryHit * 0.5).toInt()
                }
                return
            }
            state.estimatedHit = (state.estimatedHit * 0.25).toInt()
            if (state.secondaryHit > 0) {
                state.secondaryHit = (state.secondaryHit * 0.25).toInt()
            }
        }
    }

    override fun beforeAttackFinalized(self: NPC, victim: Entity, state: BattleState) {
        val disease = getOrStartTimer<Disease>(victim, 10)
        disease.hitsLeft = 10
    }


    companion object {
        @JvmField
        val IDS =
            intArrayOf(
                NPCs.ZOGRE_1_2044,
                NPCs.ZOGRE_2_2045,
                NPCs.ZOGRE_3_2046,
                NPCs.ZOGRE_4_2047,
                NPCs.ZOGRE_5_2048,
                NPCs.ZOGRE_6_2049,
                NPCs.ZOGRE_DANCE_1_2051,
                NPCs.ZOGRE_DANCE_2_2052,
                NPCs.ZOGRE_DANCE_3_2053,
                NPCs.ZOGRE_DANCE_4_2054,
                NPCs.ZOGRE_DRUMMER1_2055,

                NPCs.SKOGRE_2050,
                NPCs.SKOGRE_2056,
                NPCs.SKOGRE_2057,
            )
    }
}