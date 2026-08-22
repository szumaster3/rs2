package content.global.plugins.iface

import core.api.*
import core.game.interaction.InterfaceListener
import core.game.interaction.QueueStrength
import core.game.node.item.Item
import shared.consts.Animations
import shared.consts.Components
import shared.consts.Graphics
import shared.consts.Items

class SnowGlobeInterface : InterfaceListener {

    override fun defineInterfaceListeners() {
        on(Components.SNOWGLOBE_INTERFACE_659) { player, _, _, buttonID, _, _ ->
            when (buttonID) {
                2 -> {
                    closeInterface(player)
                    animate(player, Animations.SNOWGLOBE_SNOW_FALL_SLOW_7538)
                    queueScript(player, 1, QueueStrength.WEAK) {
                        visualize(player, Animations.SNOWGLOBE_STOMP_7528, Graphics.SNOW_FALLING_FROM_SNOW_GLOBE_1284)
                        player.inventory.add(Item(Items.SNOWBALL_11951, freeSlots(player)))
                        return@queueScript stopExecuting(player)
                    }
                }

                else -> {
                    animate(player, Animations.SNOWGLOBE_SNOW_FALL_FAST_7537)
                }
            }
            return@on true
        }
    }
}