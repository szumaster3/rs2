package core.game.dialogue

import core.api.sendRepositionOnInterface
import core.game.interaction.InterfaceListener
import shared.consts.Components

class DialogueInterface : InterfaceListener
{
    override fun defineInterfaceListeners()
    {
        onOpen(Components.DOUBLEOBJBOX_131)
        { player, _ ->
            sendRepositionOnInterface(player, Components.DOUBLEOBJBOX_131, 1, 96, 25)
            sendRepositionOnInterface(player, Components.DOUBLEOBJBOX_131, 3, 96, 98)
            return@onOpen true
        }
        onOpen(Components.SELECT_AN_OPTION_140) { player, _ ->
            val indices = intArrayOf(0, 2, 3, 4, 5, 6)
            val xs = intArrayOf(23, 31, 234, 24, 123, 334)
            val ys = intArrayOf(5, 32, 32, 3, 36, 36)

            for (i in indices.indices)
            {
                sendRepositionOnInterface(player, Components.SELECT_AN_OPTION_140, indices[i], xs[i], ys[i])
            }
            return@onOpen true
        }
        onOpen(Components.TUTORIAL_TEXT_372) { player, _ ->
            val indices = intArrayOf(1, 2, 3, 4)
            val xs = intArrayOf(10, 10, 10, 10)
            val ys = intArrayOf(35, 50, 65, 80)

            for (i in indices.indices)
            {
                sendRepositionOnInterface(player, Components.TUTORIAL_TEXT_372, indices[i], xs[i], ys[i])
            }
            return@onOpen true
        }
        onOpen(Components.TUTORIAL_TEXT2_421) { player, _ ->
            sendRepositionOnInterface(player, Components.TUTORIAL_TEXT2_421, 0, 19, 21)
            return@onOpen true
        }
    }
}