package content.region.misthalin.varrock.quest.dragon.npc

import core.api.getQuestStage
import core.api.produceGroundItem
import core.game.node.entity.Entity
import core.game.node.entity.npc.AbstractNPC
import core.game.node.entity.player.Player
import core.game.world.map.Location
import core.tools.RandomFunction
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Quests

class ZombieRatNPC : AbstractNPC {
    constructor() : super(0, null)

    private constructor(id: Int, location: Location) : super(id, location)

    override fun construct(id: Int, location: Location, vararg objects: Any): AbstractNPC = ZombieRatNPC(id, location)

    override fun finalizeDeath(killer: Entity) {
        super.finalizeDeath(killer)

        if (killer !is Player) return

        if (getQuestStage(killer, Quests.DRAGON_SLAYER) in 1..99) {
            if (RandomFunction.random(5) == 2) {
                produceGroundItem(killer, Items.KEY_1543, 1, getLocation())
            }
        }
        if (getQuestStage(killer, Quests.WITCHS_POTION) in 1..99) {
            produceGroundItem(killer, Items.RATS_TAIL_300, 1, getLocation())
        }
        produceGroundItem(killer, Items.BONES_526, 1, getLocation())
    }

    override fun getIds(): IntArray = ID

    companion object {
        private val ID = intArrayOf(NPCs.ZOMBIE_RAT_6088, NPCs.ZOMBIE_RAT_6089, NPCs.ZOMBIE_RAT_6090)
    }
}
