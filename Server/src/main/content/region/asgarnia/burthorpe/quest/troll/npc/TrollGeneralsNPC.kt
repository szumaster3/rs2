package content.region.asgarnia.burthorpe.quest.troll.npc

import content.data.Quests

import core.api.inInventory
import core.api.isQuestInProgress
import core.api.produceGroundItem
import core.game.node.entity.Entity
import core.game.node.entity.npc.AbstractNPC
import core.game.node.entity.player.Player
import core.game.world.map.Location
import core.plugin.Initializable
import shared.consts.Items
import shared.consts.NPCs

@Initializable
class TrollGeneralsNPC(id: Int = 0, location: Location? = null) : AbstractNPC(id, location) {

    override fun construct(id: Int, location: Location?, vararg objects: Any?): AbstractNPC = TrollGeneralsNPC(id, location)

    override fun getIds(): IntArray = intArrayOf(NPCs.TROLL_GENERAL_1115, NPCs.TROLL_GENERAL2_1116, NPCs.TROLL_GENERAL3_1117)

    override fun finalizeDeath(killer: Entity?) {
        if (isQuestInProgress(killer as Player, Quests.TROLL_STRONGHOLD.questName, 1, 7) && !inInventory(killer, Items.PRISON_KEY_3135)) {
            produceGroundItem(killer, Items.PRISON_KEY_3135, 1, this.location)
        }
        super.finalizeDeath(killer)
    }
}
