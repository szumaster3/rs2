package content.region.misthalin.varrock.quest.romeo.plugin

import core.api.getQuestStage
import core.game.node.entity.npc.AbstractNPC
import core.game.node.entity.player.Player
import core.game.world.map.Location
import core.plugin.Initializable
import shared.consts.NPCs
import shared.consts.Quests

@Initializable
class JulietNPC : AbstractNPC {
    constructor() : super(0, null)

    private constructor(id: Int, location: Location) : super(id, location)

    override fun construct(id: Int, location: Location, vararg objects: Any): AbstractNPC = JulietNPC(id, location)

    override fun isHidden(player: Player): Boolean = getQuestStage(player, Quests.ROMEO_JULIET) in 61..99

    override fun getIds(): IntArray = ID

    companion object {
        private val ID = intArrayOf(NPCs.JULIET_637)
    }
}
