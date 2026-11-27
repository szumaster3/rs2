package content.global.plugins.iface

import core.api.sendInputDialogue
import core.api.sendMessage
import core.game.interaction.InterfaceListener
import core.game.interaction.InterfaceOperation
import core.game.node.entity.player.link.request.trade.TradeModule.Companion.getExtension
import shared.consts.Components

/**
 * Represents the interface listener used to handle all trade related functions.
 * @author Vexia
 */
class TradeInterface : InterfaceListener {

    override fun defineInterfaceListeners() {

        onNative(Components.TRADECONFIRM_334) { player, _, _, buttonID, _, _ ->
            val module = getExtension(player) ?: return@onNative true

            when (buttonID) {
                20 -> module.setAccepted(true, true)
                21 -> module.decline()
            }

            return@onNative true
        }

        onNative(Components.TRADEMAIN_335) { player, _, opcode, buttonID, slot, _ ->
            val module = getExtension(player) ?: return@onNative true

            when (opcode) {
                InterfaceOperation.OP1 -> {
                    when (buttonID) {
                        16 -> module.setAccepted(true, true)
                        18 -> module.decline()
                        30 -> module.container!!.withdraw(slot, 1)
                    }
                }

                InterfaceOperation.OP2 -> module.container!!.withdraw(slot, 5)
                InterfaceOperation.OP3 -> module.container!!.withdraw(slot, 10)
                InterfaceOperation.OP4 -> {
                    module.container!!.withdraw(slot, module.container!!.getAmount(module.container!![slot]))
                }

                InterfaceOperation.OP5 -> {
                    sendInputDialogue(player, false, "Enter the amount:") { value ->
                        var amount = value.toString()
                        amount = amount.replace("k", "000")
                        amount = amount.replace("K", "000")
                        amount = amount.replace("m", "000000")
                        amount = amount.replace("M", "000000")

                        module.container!!.withdraw(slot, amount.toInt())
                    }
                }

                InterfaceOperation.OP10 -> {
                    val target = if (buttonID == 32) {
                        module.target
                    } else {
                        player
                    }
                    val targetModule = getExtension(target) ?: return@onNative true
                    sendMessage(player, targetModule.container!![slot].definition.examine)
                }
            }
            return@onNative true
        }

        onNative(Components.TRADESIDE_336) { player, _, opcode, _, slot, _ ->
            val module = getExtension(player) ?: return@onNative true

            when (opcode) {
                InterfaceOperation.OP1 -> module.container!!.offer(slot, 1)
                InterfaceOperation.OP2 -> module.container!!.offer(slot, 5)
                InterfaceOperation.OP3 -> module.container!!.offer(slot, 10)
                InterfaceOperation.OP4 -> {
                    module.container!!.offer(
                        slot,
                        player.inventory.getAmount(player.inventory[slot])
                    )
                }

                InterfaceOperation.OP5 -> {
                    sendInputDialogue(player, false, "Enter the amount:") { value ->
                        var amount = value.toString()
                        amount = amount.replace("k", "000")
                        amount = amount.replace("K", "000")
                        amount = amount.replace("m", "000000")
                        amount = amount.replace("M", "000000")

                        module.container!!.offer(slot, amount.toInt())
                    }
                }

                InterfaceOperation.OP10 -> {
                    sendMessage(player, player.inventory[slot].definition.examine)
                }
            }

            return@onNative true
        }
    }
}