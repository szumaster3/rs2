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

class BigFrogNPC : NPCBehavior(*bigFrogIds) {
    companion object {
        private val bigFrogIds = intArrayOf(NPCs.MEDIUM_FROG_1829)
    }

    override fun onDropTableRolled(
        self: NPC,
        killer: Entity,
        drops: ArrayList<Item>,
    ) {
        super.onDropTableRolled(self, killer, drops)

        if (killer is Player && isQuestInProgress(killer, Quests.RAG_AND_BONE_MAN.questName, 1, 99)) {
            if (RandomFunction.roll(4)) {
                drops.add(Item(Items.BIG_FROG_LEG_7908))
            }
        }
    }
}
