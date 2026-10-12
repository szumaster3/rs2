package content.region.desert.al_kharid.quest.desertrescue.npc

import content.region.desert.al_kharid.quest.desertrescue.TouristTrap
import core.api.getQuestStage
import core.api.setQuestStage
import core.game.node.entity.Entity
import core.game.node.entity.npc.AbstractNPC
import core.game.node.entity.player.Player
import core.game.world.map.Location
import shared.consts.NPCs
import shared.consts.Quests

class MercenaryCaptainNPC : AbstractNPC {
    constructor() : super(0, null)

    constructor(id: Int, location: Location?) : super(id, location)

    override fun construct(
        id: Int,
        location: Location,
        vararg objects: Any,
    ): AbstractNPC = MercenaryCaptainNPC(id, location)

    override fun finalizeDeath(killer: Entity) {
        super.finalizeDeath(killer)
        if (killer is Player) {
            val player = killer
            when (getQuestStage(player, Quests.THE_TOURIST_TRAP)) {
                0, 10 -> {}
                else -> {
                    if (!player.inventory.containsItem(TouristTrap.METAL_KEY) && !player.bank.containsItem(TouristTrap.METAL_KEY)) {
                        player.inventory.add(TouristTrap.METAL_KEY, player)
                        player.dialogueInterpreter.sendItemMessage(TouristTrap.METAL_KEY, "The mercenary captain drops a metal key on the floor.", "You quickly grab the key and add it to your inventory.")
                    }
                    setQuestStage(player, Quests.THE_TOURIST_TRAP, 20)
                }
            }
        }
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.MERCENARY_CAPTAIN_830)
}