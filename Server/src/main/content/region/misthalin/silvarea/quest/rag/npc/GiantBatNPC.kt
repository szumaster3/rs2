package content.region.misthalin.silvarea.quest.rag.npc

import content.data.Quests

import core.api.isQuestInProgress
import core.game.node.entity.Entity
import core.game.node.entity.npc.NPC
import core.game.node.entity.npc.NPCBehavior
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.tools.RandomFunction
import shared.consts.Items
import shared.consts.NPCs

class GiantBatNPC : NPCBehavior(*giantBatIds) {
    companion object {
        private val giantBatIds =
            intArrayOf(
                NPCs.BAT_78,
                NPCs.NON_COMBAT_BAT_1005,
                NPCs.AGILITYARENA_NON_COMBAT_BAT_2482,
                NPCs.TIADECHE_2483,
                NPCs.TIADECHE_2484,
                NPCs.TINSAY_2485,
                NPCs.TINSAY_2486,
                NPCs.TAMAYU_2487,
                NPCs.TAMAYU_2488,
                NPCs.OLAF2_GIANT_BAT_3711,
            )
    }

    override fun onDropTableRolled(
        self: NPC,
        killer: Entity,
        drops: ArrayList<Item>,
    ) {
        super.onDropTableRolled(self, killer, drops)

        if (killer is Player && isQuestInProgress(killer, Quests.RAG_AND_BONE_MAN.questName, 1, 99)) {
            if (RandomFunction.roll(4)) {
                drops.add(Item(Items.GIANT_BAT_WING_7827))
            }
        }
    }
}
