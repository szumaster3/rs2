package content.region.desert.al_kharid.quest.desertrescue.npc

import content.region.desert.al_kharid.quest.desertrescue.TouristTrap
import core.api.sendMessage
import core.game.node.entity.npc.AbstractNPC
import core.game.system.task.Pulse
import core.game.world.GameWorld.Pulser
import core.game.world.GameWorld.ticks
import core.game.world.map.Location
import core.game.world.map.RegionManager.getLocalPlayers
import core.tools.RandomFunction
import shared.consts.NPCs

class DesertGuardNPC : AbstractNPC {
    private var lastCheck = 0

    constructor() : super(0, null)

    constructor(id: Int, location: Location?) : super(id, location)

    override fun construct(
        id: Int,
        location: Location,
        vararg objects: Any,
    ): AbstractNPC = DesertGuardNPC(id, location)

    override fun tick() {
        if (lastCheck < ticks) {
            lastCheck = ticks + RandomFunction.random(50, 150)
            if (!inCombat()) {
                warn()
            }
        }
        super.tick()
    }

    private fun warn() {
        val players = getLocalPlayers(this)
        for (player in players) {
            if (player.getAttribute(
                    "guard-warning",
                    0,
                ) > ticks || !player.zoneMonitor.isInZone("mining camp") || player.inCombat() || !player.location.withinDistance(
                    getLocation(),
                    8,
                )
            ) {
                continue
            }
            if (TouristTrap.inJail(player)) {
                continue
            }
            if (!TouristTrap.hasSlaveClothes(player)) {
                player.setAttribute("guard-warning", ticks + 300)
                player.lock()
                Pulser.submit(
                    object : Pulse(1, this, player) {
                        var count = 0

                        override fun pulse(): Boolean {
                            when (++count) {
                                1 -> {
                                    face(player)
                                    if (!TouristTrap.hasSlaveClothes(player)) {
                                        sendChat("Hey, they're interesting clothes!")
                                        sendMessage(player, "Guard: You're no slave.")
                                    } else {
                                        sendChat("Hey - you with the armour!")
                                        sendMessage(player, "Guard: Hey - You with the armour!")
                                    }
                                }

                                5 -> if (!TouristTrap.hasSlaveClothes(player)) {
                                    sendChat("You're no slave.")
                                    sendMessage(player, "Guard: What are you doing in here?")
                                } else {
                                    sendChat("You're not allowed in here!")
                                    sendMessage(player, "Guard: You're not allowed in here!")
                                }

                                8 -> {
                                    sendChat("Intel the cell you go! I hope this teaches you a lesson.")
                                    properties.combatPulse.attack(player)
                                }

                                10 -> if (!TouristTrap.inJail(player)) {
                                    TouristTrap.jail(player)
                                }
                            }
                            return false
                        }
                    },
                )
                break
            }
        }
    }

    override fun getIds(): IntArray = intArrayOf(
        NPCs.GUARD_4993,
        NPCs.GUARD_4994,
        NPCs.GUARD_4995,
        NPCs.GUARD_4996,
        NPCs.GUARD_4997,
        NPCs.GUARD_4998,
        NPCs.GUARD_4999,
        NPCs.GUARD_5000,
        NPCs.GUARD_5001,
        NPCs.GUARD_5002,
    )
}