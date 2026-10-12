package content.region.desert.al_kharid.quest.desertrescue.npc

import content.region.desert.al_kharid.quest.desertrescue.TouristTrap
import core.api.getQuestStage
import core.game.node.entity.npc.AbstractNPC
import core.game.node.entity.player.Player
import core.game.world.map.Location
import shared.consts.NPCs
import shared.consts.Quests

class AnaNPC : AbstractNPC {
    constructor() : super(0, null)

    constructor(id: Int, location: Location?) : super(id, location)

    override fun construct(
        id: Int,
        location: Location,
        vararg objects: Any,
    ): AbstractNPC = AnaNPC(id, location)

    override fun isHidden(player: Player): Boolean {
        return if (getQuestStage(player, Quests.THE_TOURIST_TRAP) > 61) {
            true
        } else {
            player.inventory.containsItem(TouristTrap.ANNA_BARREL) || player.bank.containsItem(
                TouristTrap.ANNA_BARREL,
            )
        }
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.ANA_822)
}