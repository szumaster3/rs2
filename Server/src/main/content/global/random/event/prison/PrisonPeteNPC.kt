package content.global.random.event.prison

import content.global.random.RandomEventNPC
import core.api.lock
import core.api.lockInteractions
import core.api.sendMessage
import core.api.utils.WeightBasedTable
import core.game.node.entity.npc.NPC
import shared.consts.NPCs
/**
 * Represents the Prison Pete random event NPC.
 * @author szu
 */
class PrisonPeteNPC(
    override var loot: WeightBasedTable? = null,
) : RandomEventNPC(NPCs.MACRO_EVIL_BOB_OUTSIDE_2478) {
    override fun init() {
        super.init()
        lock(player, 6)
        lockInteractions(player, 6)
        PrisonPeteUtils.teleport(player)
        PrisonTimer.start(player)
        sendMessage(player, "Welcome to ScapeRune.")
    }

    override fun talkTo(npc: NPC) {
        // If a player talks to Prison Pete after he escorts someone else back to the hood,
        // he will say, "Don't mind me, I was just leaving."
    }
}
