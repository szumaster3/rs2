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

class GiantRatNPC : NPCBehavior(*giantRatIds) {
    companion object {
        private val giantRatIds =
            intArrayOf(
                NPCs.SOS_FAM_GIANTRAT_4395,
                NPCs.GIANTRAT_GREY_446,
                NPCs.GIANTRAT2_4922,
                NPCs.GIANTRAT3_4923,
                NPCs.GIANTRAT_GREY2_4924,
                NPCs.GIANT_RAT_4925,
                NPCs.GIANTRAT1_2_4926,
                NPCs.GIANTRAT1_3_4927,
                NPCs.NEWBIEGIANTRAT2_4942,
                NPCs.NEWBIEGIANTRAT3_4943,
                NPCs.SOS_FAM_GIANTRAT2_4944,
                NPCs.SOS_FAM_GIANTRAT3_4945,
                NPCs.GIANTRAT_86,
                NPCs.GIANTRAT1_87,
                NPCs.NEWBIEGIANTRAT_950,
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
                drops.add(Item(Items.GIANT_RAT_BONE_7824))
            }
        }
    }
}
