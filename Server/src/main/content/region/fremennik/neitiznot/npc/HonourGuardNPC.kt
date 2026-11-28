package content.region.fremennik.neitiznot.npc

import core.game.node.entity.npc.AbstractNPC
import core.game.world.map.Location
import core.game.world.map.RegionManager
import core.plugin.Initializable
import core.tools.RandomFunction
import shared.consts.NPCs

@Initializable
class HonourGuardNPC : AbstractNPC {

    private var tickDelay = RandomFunction.random(30)

    constructor() : super(NPCs.HONOUR_GUARD_5514, null, true)
    private constructor(id: Int, location: Location) : super(id, location)

    override fun construct(id: Int, location: Location, vararg objects: Any?): AbstractNPC =
        HonourGuardNPC(id, location)

    private val targetTrolls = setOf(
        NPCs.FRIS_TROLLF_5523,
        NPCs.FRIS_TROLLM_5522,
        NPCs.FRIS_BABY_TROLL_5521,
        NPCs.ICE_TROLL_GRUNT_5524,
        NPCs.FRIS_BABY_TROLL_LOWXP_5473,
        NPCs.FRIS_TROLLF_LOWXP_5475,
        NPCs.FRIS_TROLL_BODYGUARD_LOWXP_5476,
    )

    override fun tick() {
        tickDelay++
        if (tickDelay < 5) {
            super.tick()
            return
        }
        tickDelay = 0

        if (isActive && !inCombat() && RandomFunction.roll(10)) {
            val localNPCs = RegionManager.getLocalNpcs(this, 6)
            val localTrolls = localNPCs.filter { it.id in targetTrolls }
            for (troll in localTrolls) {
                attack(troll)
                super.tick()
                return
            }
        }

        if (!isActive) {
            properties.combatPulse.stop()
        }

        super.tick()
    }

    override fun getIds(): IntArray = intArrayOf(
        NPCs.HONOUR_GUARD_5514,
        NPCs.HONOUR_GUARD_5515,
        NPCs.HONOUR_GUARD_5516,
        NPCs.HONOUR_GUARD_5517,
    )
}
