package content.region.kandarin.gnome_stronghold.npc

import core.api.inBorders
import core.api.sendChat
import core.game.node.entity.npc.NPC
import core.game.node.entity.npc.NPCBehavior
import core.game.world.map.Location
import core.tools.RandomFunction
import shared.consts.NPCs

/**
 * Handles the Gnome Coach NPC.
 */
class GnomeCoachNPC : NPCBehavior(NPCs.GNOME_BALL_COACH_2802) {

    override fun onCreation(self: NPC) {
        if (inBorders(self, 2386, 3496, 2410, 3499)) {
            val movementPath =
                arrayOf(
                    Location.create(2392, 3498, 0),
                    Location.create(2398, 3498, 0),
                    Location.create(2403, 3498, 0),
                    Location.create(2397, 3498, 0),
                    Location.create(2391, 3498, 0),
                )
            self.configureMovementPath(*movementPath)
            self.isWalks = true
        }
    }
}
