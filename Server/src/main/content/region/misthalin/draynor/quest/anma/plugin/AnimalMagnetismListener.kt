package content.region.misthalin.draynor.quest.anma.plugin

import content.data.Quests

import core.api.isQuestComplete
import core.api.sendMessage
import core.game.interaction.InteractionListener
import shared.consts.Items

class AnimalMagnetismListener : InteractionListener {

    override fun defineListeners() {

        /*
         * Handles equip the crone made amulet (Ghosts ahoy quest).
         */

        onEquip(Items.CRONE_MADE_AMULET_10500) { player, _ ->
            if (!isQuestComplete(player, Quests.ANIMAL_MAGNETISM.questName)) {
                sendMessage(player, "Your ghostliness isn't ethereal enough to wear this.")
                return@onEquip false
            } else {
                return@onEquip true
            }
        }
    }
}
