package content.global.plugins.`object`

import core.api.dumpContainer
import core.api.getUsedOption
import core.api.sendString
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.Node
import core.game.node.entity.player.Player
import core.game.world.GameWorld
import shared.consts.Components
import shared.consts.Scenery

private val BANK_DEPOSIT_BOXES = intArrayOf(Scenery.BANK_DEPOSIT_BOX_9398, Scenery.BANK_DEPOSIT_BOX_20228, Scenery.BANK_DEPOSIT_BOX_25937, Scenery.BANK_DEPOSIT_BOX_26969, Scenery.BANK_DEPOSIT_BOX_34755, Scenery.BANK_DEPOSIT_BOX_36788, Scenery.BANK_DEPOSIT_BOX_39830, Scenery.DEPOSIT_TABLE_39533, Scenery.DEPOSIT_BOX_15985, Scenery.ICE_MOUND_30044, Scenery.ICE_MOUND_30045)

class DepositBoxOptionPlugin : InteractionListener {

    companion object {
        private fun openDepositBox(player: Player, node: Node, state: Int): Boolean {
            val option = getUsedOption(player)
            if (option == "Deposit-all") {
                dumpContainer(player, player.inventory)
                return true
            }


            player.bank.openDepositBox()
            sendString(player, "The Bank of " + GameWorld.settings!!.name + " - Deposit Box", Components.BANK_DEPOSIT_BOX_11, 13)

            return true
        }
    }

    override fun defineListeners() {
        defineInteraction(IntType.SCENERY, BANK_DEPOSIT_BOXES, "deposit", "deposit-all", handler = ::openDepositBox)
    }
}
