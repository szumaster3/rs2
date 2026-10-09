package content.region.fremennik.quest.olaf.npc

import content.data.GameAttributes
import core.api.*
import core.game.node.entity.Entity
import core.game.node.entity.npc.AbstractNPC
import core.game.node.entity.npc.NPC
import core.game.node.entity.npc.NPCBehavior
import core.game.node.entity.player.Player
import core.game.system.task.Pulse
import core.game.world.GameWorld
import core.game.world.map.Location
import shared.consts.NPCs
import shared.consts.Quests

class UlfricNPC(id: Int = 0, location: Location? = null) : AbstractNPC(id, location) {

    override fun construct(id: Int, location: Location, vararg objects: Any): AbstractNPC = UlfricNPC(id, location)

    override fun getIds(): IntArray = intArrayOf(NPCs.ULFRIC_3706, NPCs.ULFRIC_3710)

    companion object {
        /**
         * Spawns Ulfric NPC for the player.
         * @return true if Ulfric was spawned, false if one is already active for this player.
         */
        @JvmStatic
        fun spawnUlfric(player: Player): Boolean {
            val existing = getAttribute<UlfricNPC?>(player, GameAttributes.OLAF_ULFRIC_SPAWN, null)
            if (existing != null && existing.isActive) {
                return false
            }

            val ulfric = UlfricNPC(NPCs.ULFRIC_3710)
            ulfric.location = Location.create(2744, 10161, 0)
            ulfric.isWalks = true
            ulfric.isAggressive = true
            ulfric.isActive = true
            setAttribute(player, GameAttributes.OLAF_ULFRIC_SPAWN, ulfric)
            GameWorld.Pulser.submit(
                object : Pulse(1, ulfric) {
                    var counter = 0

                    override fun pulse(): Boolean {
                        when (counter++) {
                            1 -> {
                                lock(player, 3)
                                // TODO: Adjust the camera movement to the correct position.
                                // val camera = PlayerCamera(player)
                                // camera.setPosition(player.location.x, player.location.y, 300)
                                // camera.rotateTo(x, y, 300, 1000)
                                // camera.panTo(x, y, 200, 1000)
                                ulfric.init()
                                return false
                            }
                            2 -> {
                                resetCamera(player)
                                ulfric.asNpc().transform(NPCs.ULFRIC_3706)
                                sendChat(ulfric, "Slay... Outlander... Intruder!")
                                registerHintIcon(player, ulfric)
                                ulfric.attack(player)
                                return true
                            }
                        }
                        return false
                    }
                },
            )
            return true
        }

        @JvmStatic
        fun clearUlfric(player: Player) {
            removeAttribute(player, GameAttributes.OLAF_ULFRIC_SPAWN)
        }
    }

    override fun finalizeDeath(killer: Entity?) {
        if (killer is Player) {
            clearHintIcon(killer)
            clearUlfric(killer)
            setQuestStage(killer, Quests.OLAFS_QUEST, 99)
        }
        clear()
        super.finalizeDeath(killer)
    }
}

class UlfricXPLock: NPCBehavior(NPCs.ULFRIC_3706) {
    override fun getXpMultiplier(self: NPC, attacker: Entity): Double = 0.0
}