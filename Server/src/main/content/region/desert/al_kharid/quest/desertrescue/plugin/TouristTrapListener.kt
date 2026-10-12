package content.region.desert.al_kharid.quest.desertrescue.plugin

import content.region.desert.al_kharid.quest.desertrescue.TouristTrap
import core.api.getQuestStage
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import shared.consts.NPCs
import shared.consts.Quests

class TouristTrapListener : InteractionListener {

    override fun defineListeners() {
        onUseWith(IntType.NPC, TouristTrap.BARREL.id, NPCs.ANA_822) { player, _, with ->
            if (getQuestStage(player, Quests.THE_TOURIST_TRAP) == 61) {
                player.dialogueInterpreter.open(NPCs.ANA_822, with.asNpc(), true)
            } else {
                return@onUseWith false
            }

            return@onUseWith true
        }
    }
}