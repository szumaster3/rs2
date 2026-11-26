package content.region.misthalin.varrock.quest.phoenixgang.plugin

import content.data.Quests

import content.region.misthalin.varrock.quest.phoenixgang.ShieldofArrav
import core.api.getQuestStage
import core.api.inBank
import core.api.inInventory
import core.game.node.entity.Entity
import core.game.node.entity.npc.AbstractNPC
import core.game.node.entity.player.Player
import core.game.node.item.GroundItemManager
import core.game.world.map.Location
import shared.consts.Items
import shared.consts.NPCs

class JohnnyBeardNPC : AbstractNPC {
    constructor() : super(0, null)

    private constructor(id: Int, location: Location) : super(id, location)

    override fun construct(
        id: Int,
        location: Location,
        vararg objects: Any,
    ): AbstractNPC = JohnnyBeardNPC(id, location)

    override fun finalizeDeath(killer: Entity) {
        super.finalizeDeath(killer)
        if (killer is Player) {
            val player = killer.asPlayer()
            val quest = getQuestStage(player, Quests.SHIELD_OF_ARRAV.questName)
            if (quest == 60 &&
                ShieldofArrav.isPhoenixMission(player) &&
                (!inInventory(player, Items.INTEL_REPORT_761) && !inBank(player, Items.INTEL_REPORT_761))
            ) {
                GroundItemManager.create(ShieldofArrav.INTEL_REPORT, getLocation(), player)
            }
        }
    }

    override fun getIds(): IntArray = ID

    companion object {
        private val ID = intArrayOf(NPCs.JONNY_THE_BEARD_645)
    }
}
