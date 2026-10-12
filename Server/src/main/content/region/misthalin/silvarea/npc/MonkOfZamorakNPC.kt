package content.region.misthalin.silvarea.npc

import core.api.getQuestStage
import core.game.node.entity.Entity
import core.game.node.entity.npc.NPC
import core.game.node.entity.npc.NPCBehavior
import core.game.node.entity.player.Player
import core.game.node.item.GroundItemManager
import core.game.node.item.Item
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Quests

class MonkOfZamorakNPC : NPCBehavior(*MONK_NPCS) {

    companion object {
        private val MONK_NPCS = intArrayOf(
            NPCs.MONK_OF_ZAMORAK_1044,
            NPCs.MONK_OF_ZAMORAK_1045,
            NPCs.MONK_OF_ZAMORAK_1046
        )
    }

    override fun onDropTableRolled(
        self: NPC,
        killer: Entity,
        drops: ArrayList<Item>,
    ) {
        super.onDropTableRolled(self, killer, drops)
        if (killer is Player) {
            if (getQuestStage(killer, Quests.PRIEST_IN_PERIL) > 0) {
                GroundItemManager.create(Item(Items.GOLDEN_KEY_2944, 1), self.location, killer)
            }
        }
    }
}
