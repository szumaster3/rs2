package content.minigame.duel_arena.plugin

import core.api.setAttribute
import core.api.setVarbit
import core.game.interaction.InterfaceListeners
import core.game.interaction.InterfaceOperation
import core.game.component.Component
import core.game.component.ComponentDefinition
import core.game.component.ComponentPlugin
import core.game.node.entity.player.Player
import core.plugin.Plugin
import shared.consts.Components

class DuelComponentPlugin : ComponentPlugin() {
    @Throws(Throwable::class)
    override fun newInstance(arg: Any?): Plugin<Any> {
        ComponentDefinition.forId(Components.DUEL2_SELECT_TYPE_640)?.plugin = this
        InterfaceListeners.addNative(Components.DUEL2_SELECT_TYPE_640) { player, component, operation, button, slot, itemId ->
            if (operation == InterfaceOperation.OP1)
                handle(player, component, InterfaceOperation.listenerId(operation), button, slot, itemId)
            else true
        }
        return this
    }

    override fun handle(
        player: Player,
        component: Component,
        opcode: Int,
        button: Int,
        slot: Int,
        itemId: Int,
    ): Boolean {
        if (opcode != InterfaceOperation.listenerId(InterfaceOperation.OP1)) return true
        when (component.id) {
            Components.DUEL2_SELECT_TYPE_640 -> {
                var staked = false
                when (button) {
                    20 -> {
                        val other = player.getAttribute<Player>("duel:partner")
                        if (other == null || other.getExtension<Any?>(DuelSession::class.java) != null) {
                            player.packetDispatch.sendMessage("Other player is busy at the moment.")
                            return true
                        }
                        if (!player.interfaceManager.close()) return true
                        if (!player.getAttribute("duel:staked", false)) {
                            player.requestManager.request(other, DuelArenaActivity.FRIEND_REQUEST)
                        } else {
                            player.requestManager.request(other, DuelArenaActivity.STAKE_REQUEST)
                        }
                        return true
                    }

                    18, 22 -> staked = false
                    19, 21 -> {
                        staked = true
                    }

                    else -> return false
                }
                setAttribute(player, "duel:staked", staked)
                // script561 reads varplayerbit_4157 (varp 283, bits 26..29).
                setVarbit(player, 4157, if (staked) 2 else 1)
            }
        }
        return true
    }
}
