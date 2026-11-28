package content.region.asgarnia.falador.plugin.temple_knights

import content.data.Quests

import core.api.hasRequirement
import core.game.node.entity.Entity
import core.game.node.entity.npc.AbstractNPC
import core.game.node.entity.player.Player
import core.game.world.map.Location
import core.plugin.Initializable
import shared.consts.NPCs

/**
 * Represents the Black knight NPCs.
 */
@Initializable
class BlackKnightNPC(id: Int = 0, location: Location? = null) : AbstractNPC(id, location) {

    override fun construct(id: Int, location: Location, vararg objects: Any): AbstractNPC = BlackKnightNPC(id, location)

    override fun finalizeDeath(killer: Entity) {
        super.finalizeDeath(killer)
        if (killer is Player) {
            if(!hasRequirement(killer,Quests.WANTED.questName, false)) return
            val killsToAdd = if (id > 8000) 10 else 1
            WhiteKnightRankManager.addToKillCount(killer, killsToAdd)
        }
    }

    override fun getIds(): IntArray = ID

    companion object {
        private val ID = intArrayOf(
            NPCs.BLACK_KNIGHT_178,
            NPCs.AGGRESSIVE_BLACK_KNIGHT_179,
            NPCs.JAIL_BLACK_KNIGHT_2698,
            NPCs.WANTED_SUMMONED_BLACK_KNIGHT_2777,
            NPCs.KR_AGGRESSIVE_BLACK_KNIGHT_6189,
            NPCs.LUC2_ELITE_BLACK_KNIGHT_PRAY_NONE_8324,
            NPCs.LUC2_ELITE_BLACK_KNIGHT_PRAY_MELEE_8325,
            NPCs.LUC2_ELITE_BLACK_KNIGHT_PRAY_RANGE_8326,
            NPCs.LUC2_ELITE_BLACK_KNIGHT_PRAY_MAGIC_8327,
            NPCs.LUC2_ELITE_BLACK_KNIGHT_NOATTACK_STOP_8330,
        )
    }
}