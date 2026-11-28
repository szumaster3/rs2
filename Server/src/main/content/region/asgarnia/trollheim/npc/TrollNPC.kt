package content.region.asgarnia.trollheim.npc

import core.game.node.entity.npc.AbstractNPC
import core.game.world.map.Location
import core.plugin.Initializable
import shared.consts.NPCs

@Initializable
class TrollNPC : AbstractNPC {
    constructor() : super(0, null)

    constructor(id: Int, location: Location?) : super(id, location, true) {
        this.isAggressive = true
    }

    override fun construct(id: Int, location: Location, vararg objects: Any): AbstractNPC = TrollNPC(id, location)

    override fun getIds(): IntArray = ID

    companion object {
        val ID = intArrayOf(
            NPCs.DEATH_TROLL_MELEE1_1106,
            NPCs.DEATH_TROLL_MELEE2_1107,
            NPCs.DEATH_TROLL_MELEE3_1108,
            NPCs.DEATH_TROLL_MELEE4_1109,
            NPCs.DEATH_TROLL_MELEE5_1110,
            NPCs.DEATH_TROLL_MELEE6_1111,
            NPCs.DEATH_TROLL_MELEE7_1112,
            NPCs.TROLL_MELEE1_1138,
        )
    }
}
