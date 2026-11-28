package content.global.random.event.prison

import content.data.RandomEvent
import core.api.*
import core.game.interaction.QueueStrength
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.TeleportManager
import core.game.system.timer.impl.AntiMacro
import core.game.world.GameWorld
import core.game.world.map.Location
import core.game.world.map.zone.ZoneBorders
import shared.consts.Items
import shared.consts.NPCs

object PrisonPeteUtils {
    private val PRISON_LOCATION: Location = Location.create(2086, 4462, 0)

    const val GET_REWARD = "/save:prisonpete:reward"
    const val POP_KEY_FALSE = "prisonpete:pop-incorrect"
    const val EXPECTED_NPC = "prisonpete:expected-npc"

    val ANIMAL_ID = intArrayOf(3119, 3120, 3121, 3122)
    val PRISON_ZONE = ZoneBorders.forRegion(8261)
    val modelList = intArrayOf(10749,10750,10751,10752)

    val MODEL_TO_NPC = mapOf(
        modelList[0] to ANIMAL_ID[0],
        modelList[1] to ANIMAL_ID[1],
        modelList[2] to ANIMAL_ID[2],
        modelList[3] to ANIMAL_ID[3]
    )

    fun cleanup(player: Player) {
        player.properties.teleportLocation = getAttribute(player, RandomEvent.save(), null)
        clearLogoutListener(player, RandomEvent.logout())
        removeAttributes(player,GET_REWARD,RandomEvent.save(),RandomEvent.logout(),POP_KEY_FALSE,EXPECTED_NPC)
        sendMessage(player, "Welcome back to ${GameWorld.settings!!.name}.")
        if (anyInInventory(player, Items.PRISON_KEY_6966)) {
            removeAll(player, Items.PRISON_KEY_6966)
        }
    }

    fun getKey(player: Player) {
        queueScript(player, 1, QueueStrength.WEAK) { stage: Int ->
            when (stage) {
                0 -> {
                    forceWalk(player, Location.create(2091, 4466, 0), "smart")
                    return@queueScript keepRunning(player)
                }
                1 -> {
                    face(player, findNPC(NPCs.PRISONPETE_BALLOON_L_3118)!!.location)
                    openDialogue(player, PrisonPeteDialogue(dialOpt = 2))
                    return@queueScript delayScript(player,3)
                }
                else -> return@queueScript stopExecuting(player)
            }
        }
    }

    fun teleport(player: Player) {
        setAttribute(player, RandomEvent.save(), player.location)
        registerLogoutListener(player, RandomEvent.logout()) { p ->
            p.location = getAttribute(p, RandomEvent.save(), player.location)
        }
        teleport(player, PRISON_LOCATION, TeleportManager.TeleportType.RANDOM_EVENT_OLD)
        removeAttribute(player, EXPECTED_NPC)
        AntiMacro.terminateEventNpc(player)
    }
}