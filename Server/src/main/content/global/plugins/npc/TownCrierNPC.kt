package content.global.plugins.npc

import core.api.animate
import core.api.sendChat
import core.game.node.entity.npc.NPC
import core.game.node.entity.npc.NPCBehavior
import core.tools.RandomFunction
import shared.consts.Animations
import shared.consts.NPCs

/**
 * Handles the TownCrierNPC.
 */
class TownCrierNPC : NPCBehavior(NPCs.PMOD_TOWN_CRIER_VARROCK_6135, NPCs.PMOD_TOWN_CRIER_DRAYNOR_6136, NPCs.PMOD_TOWN_CRIER_FALADOR_6137, NPCs.PMOD_TOWN_CRIER_EAST_ARDOYNE_6138, NPCs.PMOD_TOWN_CRIER_SEERS_6139) {
    private val forceChatAnimation = mapOf(
        "The Duke of Lumbridge needs a hand." to Animations.TOWN_CRIER_BELL_RING_6865,
        "The squirrels! The squirrels are coming! Noooo, get them out of my head!" to Animations.TOWN_CRIER_SCRATCH_HEAD_6863
    )
    private val forceChat = forceChatAnimation.entries.toList()

    private var tickDelay = RandomFunction.random(30)
    private val TICK_INTERVAL = 30

    override fun tick(self: NPC): Boolean {
        tickDelay++
        if (tickDelay < TICK_INTERVAL) return super.tick(self)
        tickDelay = 0

        if (RandomFunction.random(100) < 15) {
            self.walkingQueue.reset()
            val (chat, animation) = forceChat.random()
            animate(self, animation)
            sendChat(self, chat)
        }

        return super.tick(self)
    }
}
