package content.region.misthalin.varrock.quest.dragon.npc

import content.data.Quests

import core.api.getQuestStage
import core.game.node.entity.npc.AbstractNPC
import core.game.node.entity.player.Player
import core.game.world.map.Location
import shared.consts.NPCs

class NedNPC : AbstractNPC {
    constructor() : super(0, null)

    private constructor(id: Int, location: Location) : super(id, location)

    override fun construct(id: Int, location: Location, vararg objects: Any): AbstractNPC = NedNPC(id, location)

    override fun isHidden(player: Player): Boolean =
        getQuestStage(player, Quests.DRAGON_SLAYER.questName) != 30 && getQuestStage(player, Quests.DRAGON_SLAYER.questName) != 40

    override fun getIds(): IntArray = intArrayOf(NPCs.NED_918)
}
