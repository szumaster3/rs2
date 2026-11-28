package content.region.desert.uzer.quest.golem

import core.game.node.entity.npc.AbstractNPC
import core.game.world.map.Location
import core.plugin.Initializable
import shared.consts.NPCs

@Initializable
class ClayGolemNPC : AbstractNPC {
    constructor() : super(NPCs.GOLEM_BROKEN_GOLEM_1908, null, true)
    private constructor(id: Int, location: Location) : super(id, location)

    override fun construct(id: Int, location: Location, vararg objects: Any?): AbstractNPC = ClayGolemNPC(id, location)

    override fun getIds(): IntArray = intArrayOf(
        NPCs.CLAY_GOLEM_1907,
        NPCs.GOLEM_BROKEN_GOLEM_1908,
        NPCs.GOLEM_PARTIALLY_BROKEN_GOLEM_1909,
        NPCs.GOLEM_FIXED_GOLEM_1910,
    )
}
