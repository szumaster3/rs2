package content.global.plugins.item

import core.api.*
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.player.link.diary.DiaryType
import core.game.world.map.zone.ZoneBorders
import shared.consts.Items

class EnchantedKeyPlugin : InteractionListener {

    private val ENCHANTED_KEY = Items.ENCHANTED_KEY_13591
    private val RUB_BORDERS = ZoneBorders(2589, 3362, 2596, 3370)


    override fun defineListeners() {

        /*
         * Handles use of enchanted key.
         * Basic implementation (need Meeting history quest).
         */

        on(ENCHANTED_KEY, IntType.ITEM, "Feel", "Rub") { player, _ ->

            if (!inBorders(player, RUB_BORDERS)) {
                sendMessage(player, "The key's not hot enough for this to be correct spot.")
                return@on false
            }

            if (!player.inventory.isEmpty || !player.equipment.isEmpty) {
                sendDialogueLines(
                    player,
                    "You will need your Worn Equipment And Inventory to be empty",
                    "before you can use travel option (with the exception of the",
                    "enchanted key)."
                )
                return@on true
            }

            sendChat(player, "Predem abducto!")
            return@on true
        }
    }

}