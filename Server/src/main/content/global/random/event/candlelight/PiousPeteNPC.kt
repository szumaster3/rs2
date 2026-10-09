package content.global.random.event.candlelight

import content.global.random.RandomEventNPC
import core.api.delayScript
import core.api.openDialogue
import core.api.queueScript
import core.api.stopExecuting
import core.api.utils.WeightBasedTable
import core.game.interaction.QueueStrength
import core.game.node.entity.npc.NPC
import core.game.system.timer.impl.AntiMacro
import shared.consts.NPCs

/**
 * Represents the Pious Pete random event npc.
 */
class PiousPeteNPC(override var loot: WeightBasedTable? = null): RandomEventNPC(NPCs.PRIEST_3206) {

    override fun init() {
        super.init()
        sendChat("Brother ${player.username}, please follow me.")
        queueScript(player, 3, QueueStrength.SOFT) { stage: Int ->
            when (stage) {
                0 -> {
                    CandlelightUtils.init(player)
                    AntiMacro.terminateEventNpc(player)
                    return@queueScript delayScript(player, 3)
                }
                1 -> {
                    openDialogue(player, PiousPeteDialogue(), NPCs.PIOUS_PETE_3207)
                    return@queueScript stopExecuting(player)
                }
                else -> return@queueScript stopExecuting(player)
            }
        }
    }

    override fun talkTo(npc: NPC) {
        openDialogue(player, PiousPeteDialogue(), npc)
    }
}