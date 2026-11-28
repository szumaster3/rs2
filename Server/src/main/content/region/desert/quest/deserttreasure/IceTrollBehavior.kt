package content.region.desert.quest.deserttreasure

import core.api.*
import core.game.node.entity.Entity
import core.game.node.entity.npc.NPC
import core.game.node.entity.npc.NPCBehavior
import core.game.node.entity.player.Player
import shared.consts.NPCs

class IceTrollBehavior : NPCBehavior(*iceTrollIds) {
    companion object {
        private val iceTrollIds = intArrayOf(
            NPCs.TROLLRESCUE_ICETROLL_MELEE1_1936,
            NPCs.TROLLRESCUE_ICETROLL_MELEE2_1937,
            NPCs.TROLLRESCUE_ICETROLL_MELEE3_1938,
            NPCs.TROLLRESCUE_ICETROLL_MELEE4_1939,
            NPCs.TROLLRESCUE_ICETROLL_MELEE5_1940,
            NPCs.TROLLRESCUE_ICETROLL_MELEE6_1941,
            NPCs.TROLLRESCUE_ICETROLL_MELEE7_1942,
        )
    }

    override fun onDeathFinished(self: NPC, killer: Entity) {
        if (killer is Player) {
            if (getQuestStage(killer, DesertTreasure.questName) >= 9) {
                val currentIceTrollKill = getAttribute(killer, DesertTreasure.attributeTrollKillCount, 0)
                if (currentIceTrollKill < 5) {
                    setAttribute(killer, DesertTreasure.attributeTrollKillCount, currentIceTrollKill + 1)
                    setVarbit(
                        killer,
                        DesertTreasure.varbitCaveEntrance,
                        getAttribute(killer, DesertTreasure.attributeTrollKillCount, 0)
                    )
                    sendMessage(killer, "A chunk of ice falls away from the cave entrance...")
                }
            }
        }
    }
}