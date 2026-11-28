package content.region.misthalin.barbarian_village.npc

import core.api.sendChat
import core.game.node.entity.Entity
import core.game.node.entity.combat.BattleState
import core.game.node.entity.npc.NPC
import core.game.node.entity.npc.NPCBehavior
import core.tools.RandomFunction
import shared.consts.NPCs

class BarbarianNPC : NPCBehavior(*ID) {
    override fun afterDamageReceived(
        self: NPC,
        attacker: Entity,
        state: BattleState,
    ) {
        if (RandomFunction.random(8) == 1) {
            sendChat(self, "YEEEEEEEEAARRRGGGGHHHHHHHH")
        }
    }

    companion object {
        val ID =
            intArrayOf(
                NPCs.BARBARIAN_12,
                NPCs.FAI_BARBARIAN_1_3246,
                NPCs.FAI_BARBARIAN_2_3247,
                NPCs.FAI_BARBARIAN_3_3248,
                NPCs.FAI_BARBARIAN_4_3249,
                NPCs.FAI_BARBARIAN_5_3250,
                NPCs.FAI_BARBARIAN_6_3251,
                NPCs.FAI_BARBARIAN_7_3252,
                NPCs.FAI_BARBARIAN_8_3253,
                NPCs.FAI_BARBARIAN_10_3255,
                NPCs.FAI_BARBARIAN_11_3256,
                NPCs.FAI_BARBARIAN_12_3257,
                NPCs.FAI_BARBARIAN_13_3258,
                NPCs.FAI_BARBARIAN_14_3259,
                NPCs.FAI_BARBARIAN_FEMALE_1_3260,
                NPCs.FAI_BARBARIAN_FEMALE_2_3261,
                NPCs.FAI_BARBARIAN_FEMALE_3_3262,
                NPCs.FAI_BARBARIAN_FEMALE_4_3263,
                NPCs.BARBARIANWOMAN_VARROCK_5909,
            )
    }
}