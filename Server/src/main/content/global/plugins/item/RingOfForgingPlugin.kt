package content.global.plugins.item

import content.data.GameAttributes
import core.api.sendMessage
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import shared.consts.Items

class RingOfForgingPlugin: InteractionListener
{
    override fun defineListeners()
    {
        on(Items.RING_OF_FORGING_2568, IntType.ITEM, "operate") { player, _ ->
            val charges = player.getAttribute(GameAttributes.ROF_CHARGES,140)
            sendMessage(player, "You can smelt $charges more iron ore before the ring disintegrates.")
            return@on true
        }
    }
}