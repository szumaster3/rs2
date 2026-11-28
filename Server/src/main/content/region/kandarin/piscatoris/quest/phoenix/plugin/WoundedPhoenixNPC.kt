package content.region.kandarin.piscatoris.quest.phoenix.plugin

import core.game.node.entity.npc.NPC
import core.game.node.entity.npc.NPCBehavior
import core.game.world.map.path.ClipMaskSupplier
import shared.consts.NPCs

class WoundedPhoenixNPC : NPCBehavior(NPCs.PHOENIX_QUESTPHOENIX_8547) {

    override fun getClippingSupplier(self: NPC): ClipMaskSupplier? {
        return ClipMaskSupplier { z, x, y -> 0 }
    }

    init {}
}