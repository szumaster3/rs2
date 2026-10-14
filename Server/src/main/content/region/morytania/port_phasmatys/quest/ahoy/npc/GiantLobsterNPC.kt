package content.region.morytania.port_phasmatys.quest.ahoy.npc

import content.region.morytania.port_phasmatys.quest.ahoy.plugin.GhostsAhoyUtils
import core.api.*
import core.game.node.entity.Entity
import core.game.node.entity.npc.AbstractNPC
import core.game.node.entity.player.Player
import core.game.system.task.Pulse
import core.game.world.GameWorld
import core.game.world.map.Location
import shared.consts.NPCs

class GiantLobsterNPC(id: Int = 0, location: Location? = null) : AbstractNPC(id, location) {

    private var owner: Player? = null

    override fun construct(id: Int, location: Location, vararg objects: Any): AbstractNPC = GiantLobsterNPC(id, location)

    override fun getIds(): IntArray = intArrayOf(NPCs.GIANT_LOBSTER_1693)

    companion object {
        @JvmStatic
        fun spawnGiantLobster(player: Player) {
            val giantLobster = GiantLobsterNPC(NPCs.GIANT_LOBSTER_1693)
            giantLobster.owner = player
            setAttribute(player, GhostsAhoyUtils.crabSpawned, true)
            giantLobster.location = location(3617, 3543, 0)
            giantLobster.isWalks = true
            giantLobster.isAggressive = true
            giantLobster.isActive = false

            if (giantLobster.asNpc() != null && giantLobster.isActive) {
                giantLobster.properties.teleportLocation = giantLobster.properties.spawnLocation
            }
            giantLobster.isActive = true
            GameWorld.Pulser.submit(
                object : Pulse(1, giantLobster) {
                    override fun pulse(): Boolean {
                        giantLobster.init()
                        sendMessage(player, "You are attacked by a giant lobster!!")
                        registerHintIcon(player, giantLobster)
                        giantLobster.attack(player)
                        return true
                    }
                },
            )
        }
    }

    override fun handleTickActions() {
        super.handleTickActions()
        val player = owner ?: return
        // The owner died, logged out or walked away: remove the lobster so the chest can't stay blocked.
        if (!player.isActive || player.location.getDistance(location) > 25.0) {
            release(player)
            clear()
        }
    }

    private fun release(player: Player) {
        removeAttribute(player, GhostsAhoyUtils.crabSpawned)
        clearHintIcon(player)
        owner = null
    }

    override fun finalizeDeath(killer: Entity?) {
        val player = owner
        if (player != null) {
            if (killer == player) {
                setAttribute(player, GhostsAhoyUtils.lastMapScrap, true)
            }
            release(player)
        }
        clear()
        super.finalizeDeath(killer)
    }
}
