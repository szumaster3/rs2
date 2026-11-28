package content.global.random.event.certers

import content.data.GameAttributes
import core.api.sendString
import core.api.setAttribute
import core.game.interaction.InterfaceListener
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.game.system.timer.impl.AntiMacro
import shared.consts.Components
import shared.consts.Items

/**
 * Represents the Certer interface.
 * @author Ceikry (March 18, 2021)
 */
class CerterInterface : InterfaceListener {
    val CERTER_INTERFACE = Components.MACRO_CERTER_IDENTIFY_AN_OBJECT_184
    val OPTION_A_CHILD = 1
    val OPTION_B_CHILD = 2
    val OPTION_C_CHILD = 3
    val ITEM_CHILD = 7
    val items =
        mapOf(
            Items.BOWL_1923 to "A bowl.",
            Items.OBJ_6189 to "A fish.",
            Items.OBJ_6190 to "A bass.",
            Items.OBJ_6191 to "A sword.",
            Items.OBJ_6192 to "A battleaxe.",
            Items.OBJ_6193 to "A helmet.",
            Items.OBJ_6194 to "A kiteshield.",
            Items.OBJ_6195 to "A pair of shears.",
            Items.OBJ_6196 to "A shovel.",
            Items.OBJ_6197 to "A ring.",
            Items.OBJ_6198 to "A necklace.",
        )
    val falseOptions =
        arrayOf(
            "An axe.",
            "An arrow.",
            "A pair of boots.",
            "A pair of gloves.",
            "A staff.",
            "A bow.",
            "A feather.",
            "The disenfrachaised youth of 1940's Columbia.",
        )

    override fun defineInterfaceListeners() {
        on(CERTER_INTERFACE) { player, _, _, buttonID, _, _ ->
            val answer = buttonID - 7
            val correctAnswer = player.getAttribute(GameAttributes.CERTER_INDEX, 0)
            setAttribute(player, GameAttributes.CERTER_CORRECT, correctAnswer == answer)
            player.interfaceManager.close()
            player.dialogueInterpreter.open(CerterBrothersDialogue(false), AntiMacro.getEventNpc(player))
            return@on true
        }

        onOpen(CERTER_INTERFACE) { player, _ ->
            generateOptions(player)
            return@onOpen true
        }
    }

    private fun generateOptions(player: Player) {
        val correct = items.keys.random()
        val indexes = arrayListOf(1, 2, 3)
        val correctIndex = indexes.random()
        val correctItem = Item(correct).definition
        val iFaceModelId = correctItem!!.interfaceModelId
        val iFaceZoom = correctItem.zoom2d
        indexes.remove(correctIndex)
        setAttribute(player, GameAttributes.CERTER_INDEX, correctIndex)

        player.packetDispatch.sendString(items[correct], CERTER_INTERFACE, optionFromIndex(correctIndex))

        val tempOptions = falseOptions.toMutableList()
        val false1 = tempOptions.random()
        tempOptions.remove(false1)
        val false2 = tempOptions.random()

        sendString(player, false1, CERTER_INTERFACE, optionFromIndex(indexes[0]))
        sendString(player, false2, CERTER_INTERFACE, optionFromIndex(indexes[1]))
        player.packetDispatch.sendModelOnInterface(iFaceModelId, CERTER_INTERFACE, ITEM_CHILD, iFaceZoom)
    }

    private fun optionFromIndex(index: Int): Int =
        when (index) {
            1 -> OPTION_A_CHILD
            2 -> OPTION_B_CHILD
            3 -> OPTION_C_CHILD
            else -> OPTION_A_CHILD
        }
}
