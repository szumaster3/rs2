package content.global.plugins.npc

import content.data.Quests

import core.api.getQuest
import core.game.node.entity.Entity
import core.game.node.entity.npc.AbstractNPC
import core.game.node.entity.player.Player
import core.game.node.item.GroundItemManager
import core.game.node.item.Item
import core.game.world.map.Location
import core.plugin.Initializable
import shared.consts.Items
import shared.consts.NPCs

@Initializable
class RatNPC(id: Int = 0, location: Location? = null) : AbstractNPC(id, location) {

    override fun construct(id: Int, location: Location, vararg objects: Any): AbstractNPC = RatNPC(id, location)

    override fun finalizeDeath(killer: Entity) {
        super.finalizeDeath(killer)
        if (killer is Player) {
            if (getQuest(killer, Quests.WITCHS_POTION.questName).isStarted(killer.asPlayer())) {
                GroundItemManager.create(RAT_TAIL, getLocation(), killer.asPlayer())
            }
        }
    }

    override fun getIds(): IntArray = ID

    companion object {
        private val ID = intArrayOf(
            NPCs.RAT_47,
            NPCs.RAT_INDOORS_2682,
            NPCs.VC_PARTY_RAT_2980,
            NPCs.VC_RAT_2981,
            NPCs.PITRAT_ARDOUGNE_DEF_3007,
            NPCs.PITRAT_ARDOUGNE_AGR_3008,
            NPCs.PITRAT_ARDOUGNE_NEU_3009,
            NPCs.PITRAT_KELDAGRIM_DEF_3010,
            NPCs.PITRAT_KELDAGRIM_AGR_3011,
            NPCs.PITRAT_KELDAGRIM_NEU_3012,
            NPCs.PITRAT_SARIM_DEF_3013,
            NPCs.PITRAT_SARIM_AGR_3014,
            NPCs.PITRAT_SARIM_NEU_3015,
            NPCs.PITRAT_VARROCK_DEF_3016,
            NPCs.PITRAT_VARROCK_AGR_3017,
            NPCs.PITRAT_VARROCK_NEU_3018,
            NPCs.SOS_FAM_RAT_4396,
            NPCs.SOS_WAR_RAT_4415,
            NPCs.EASTER08_RAT_7202,
            NPCs.EASTER08_RAT3_7204,
            NPCs.EASTER09_BIRD_ASLEEP_7417,
            NPCs.EASTER08_RAT2_7461,
        )
        private val RAT_TAIL = Item(Items.RATS_TAIL_300, 1)
    }
}
