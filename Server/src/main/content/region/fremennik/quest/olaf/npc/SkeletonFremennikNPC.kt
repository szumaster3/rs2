package content.region.fremennik.quest.olaf.npc

import core.api.getQuestStage
import core.api.produceGroundItem
import core.game.node.entity.Entity
import core.game.node.entity.npc.AbstractNPC
import core.game.node.entity.player.Player
import core.game.world.map.Location
import core.plugin.Initializable
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Quests

@Initializable
class SkeletonFremennikNPC(
    id: Int = 0,
    location: Location? = null,
) : AbstractNPC(id, location) {

    override fun construct(
        id: Int,
        location: Location,
        vararg objects: Any,
    ): AbstractNPC = SkeletonFremennikNPC(id, location)

    override fun getIds(): IntArray = intArrayOf(
        NPCs.SKELETON_FREMENNIK_3698,
        NPCs.SKELETON_FREMENNIK_3699,
        NPCs.SKELETON_FREMENNIK_3701,
        NPCs.SKELETON_FREMENNIK_3702,
        NPCs.SKELETON_FREMENNIK_3703,
        NPCs.SKELETON_FREMENNIK_3704,
    )

    override fun finalizeDeath(killer: Entity?) {
        if (killer is Player) {
            val questStage = getQuestStage(killer, Quests.OLAFS_QUEST)

            if (
                questStage >= 4 &&
                (id == NPCs.SKELETON_FREMENNIK_3698 ||
                        id == NPCs.SKELETON_FREMENNIK_3699)
            ) {
                produceGroundItem(killer, Items.KEY_11043, 1, this.location)
            } else {
                val key = listOf(
                    Items.KEY_11039,
                    Items.KEY_11040,
                    Items.KEY_11041,
                    Items.KEY_11042,
                ).random()

                produceGroundItem(killer, key, 1, this.location)
            }
        }

        super.finalizeDeath(killer)
    }
}