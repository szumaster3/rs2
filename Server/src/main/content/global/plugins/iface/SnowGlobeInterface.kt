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
                    animate(player, Animations.XMAS07_SNOWGLOBE_PUT_AWAY_7538)
                    queueScript(player, 1, QueueStrength.WEAK) {
                        visualize(player, Animations.XMAS_07_PLAYER_STOMPING_SNOW_7528, Graphics.XMAS_07_PLAYER_SNOWFALL_1284)
                        player.inventory.add(Item(Items.SNOWBALL_11951, freeSlots(player)))
                        return@queueScript stopExecuting(player)
                    }
                }

                else -> {
                    animate(player, Animations.MAH4_PILLAR_CIRCLE_FIRE_7537)
                }
            }
            return@on true
        }
    }
}