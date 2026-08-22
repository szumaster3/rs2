package content.global.plugins.iface

import core.api.sendInputDialogue
import core.api.sendMessage
import core.game.interaction.InterfaceListener
import core.game.node.entity.player.link.request.trade.TradeModule.Companion.getExtension
import shared.consts.Components

/**
 * Represents the interface listener used to handle all trade related functions.
 * @author Vexia
 */
class TradeInterface : InterfaceListener {

    override fun defineInterfaceListeners() {

        on(Components.TRADECONFIRM_334) { player, _, _, buttonID, _, _ ->
            val module = getExtension(player) ?: return@on true

            when (buttonID) {
                20 -> module.setAccepted(true, true)
                21 -> module.decline()
            }

            return@on true
        }

        on(Components.TRADEMAIN_335) { player, _, opcode, buttonID, slot, _ ->
            val module = getExtension(player) ?: return@on true

            when (opcode) {
                155 -> {
                    when (buttonID) {
                        16 -> module.setAccepted(true, true)
                        18 -> module.decline()
                        30 -> module.container!!.withdraw(slot, 1)
                    }
                }

                196 -> module.container!!.withdraw(slot, 5)
                124 -> module.container!!.withdraw(slot, 10)
                199 -> {
                    module.container!!.withdraw(slot, module.container!!.getAmount(module.container!![slot]))
                }

                234 -> {
                    sendInputDialogue(player, false, "Enter the amount:") { value ->
                        var amount = value.toString()
                        amount = amount.replace("k", "000")
                        amount = amount.replace("K", "000")
                        amount = amount.replace("m", "000000")
                        amount = amount.replace("M", "000000")

                        module.container!!.withdraw(slot, amount.toInt())
                    }
                }

                9 -> {
                    val target = if (buttonID == 32) {
                        module.target
                    } else {
                        player
                    }
                    val targetModule = getExtension(target) ?: return@on true
                    sendMessage(player, targetModule.container!![slot].definition.examine)
                }
            }
            return@on true
        }

        on(Components.TRADESIDE_336) { player, _, opcode, _, slot, _ ->
            val module = getExtension(player) ?: return@on true

            when (opcode) {
                155 -> module.container!!.offer(slot, 1)
                196 -> module.container!!.offer(slot, 5)
                124 -> module.container!!.offer(slot, 10)
                199 -> {
                    module.container!!.offer(
                        slot,
                        player.inventory.getAmount(player.inventory[slot])
                    )
                }

                234 -> {
                    sendInputDialogue(player, false, "Enter the amount:") { value ->
                        var amount = value.toString()
                        amount = amount.replace("k", "000")
                        amount = amount.replace("K", "000")
                        amount = amount.replace("m", "000000")
                        amount = amount.replace("M", "000000")

                        module.container!!.offer(slot, amount.toInt())
                    }
                }

                9 -> {
                    sendMessage(player, player.inventory[slot].definition.examine)
                }
            }

            return@on true
        }
    }
}