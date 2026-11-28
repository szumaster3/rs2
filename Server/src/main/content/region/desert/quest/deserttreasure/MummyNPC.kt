package content.region.desert.quest.deserttreasure

import core.api.poofClear
import core.game.node.entity.npc.NPC
import core.game.node.entity.npc.NPCBehavior
import shared.consts.NPCs

class MummyNPC : NPCBehavior(NPCs.DESERTTREASURE_MUMMY_1_1958) {
    var clearTime = 0

    override fun tick(self: NPC): Boolean {
        if (clearTime++ > 100) {
            clearTime = 0
            poofClear(self)
        }
        return true
    }
}