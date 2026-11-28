package content.global.random.event.evil_bob

import content.data.RandomEvent
import content.global.random.RandomEventNPC
import core.api.*
import core.api.utils.WeightBasedTable
import core.game.interaction.QueueStrength
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.link.TeleportManager
import core.game.system.timer.impl.AntiMacro
import core.game.world.map.Location
import shared.consts.Music
import shared.consts.NPCs

/**
 * Represents the Evil Bob random event NPC.
 * @author szu, Zerken
 */
class EvilBobNPC(override var loot: WeightBasedTable? = null) : RandomEventNPC(NPCs.MACRO_EVIL_BOB_OUTSIDE_2478) {

    override fun init() {
        super.init()
        sendChat("meow")
        lock(player, 6)
        queueScript(player, 1, QueueStrength.SOFT) { stage: Int ->
            when (stage) {
                0 -> {
                    sendChat(player, "No... what? Nooooooooooooo!")
                    setAttribute(player, RandomEvent.save(), player.location)
                    teleport(player, Location.create(3419, 4776, 0), TeleportManager.TeleportType.RANDOM_EVENT_OLD)
                    EvilBobUtils.giveEventFishingSpot(player)
                    return@queueScript delayScript(player, 3)
                }
                1 -> {
                    sendMessage(player, "Welcome to ScapeRune.")
                   playAudio(player, Music.EVIL_BOBS_ISLAND_411)
                    openDialogue(player, EvilBobDialogue(), NPCs.MACRO_EVIL_BOB_ISLAND_2479)
                    AntiMacro.terminateEventNpc(player)
                    return@queueScript stopExecuting(player)
                }

                else -> return@queueScript stopExecuting(player)
            }
        }
    }

    override fun talkTo(npc: NPC) {
        openDialogue(player, EvilBobDialogue(), this.asNpc())
    }
}
