package content.region.misthalin.varrock.npc

import core.api.visualize
import core.game.node.entity.npc.NPC
import core.game.node.entity.npc.NPCBehavior
import core.tools.RandomFunction
import shared.consts.Graphics
import shared.consts.NPCs

/**
 * Handles the ChurchNPC.
 */
class ChurchNPC : NPCBehavior(NPCs.MARTINA_SCORSBY_3326, NPCs.JEREMY_CLERKSIN_3327) {

    private var tickDelay = RandomFunction.random(30)

    override fun onCreation(self: NPC) {
        self.isWalks = false
    }

    override fun tick(self: NPC): Boolean {
        tickDelay++
        if (tickDelay < 100) {
            return true
        }
        tickDelay = 0

        if (RandomFunction.random(300) < 3) {
            visualize(self, -1, Graphics.DREAM_ZEDS_1056)
        }
        return true
    }
}
