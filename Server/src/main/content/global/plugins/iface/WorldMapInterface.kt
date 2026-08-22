package content.global.plugins.iface

import core.api.getVarbit
import core.api.setVarbit
import core.game.component.Component
import core.game.interaction.InterfaceListener
import shared.consts.Components
import shared.consts.Vars

/**
 * Handles the world map interface.
 * @author Emperor
 */
class WorldMapInterface : InterfaceListener {

    companion object {
        private const val KEY_SORT_VARBIT = Vars.VARBIT_INTERFACE_WORLD_MAP_KEY_SORT_5367
        private const val MAX_KEY_SORT_VALUE = 3
    }

    override fun defineInterfaceListeners() {
        on(Components.WORLDMAP_755) { player, _, _, buttonID, _, _ ->
            when (buttonID) {
                3 -> {
                    player.interfaceManager.openWindowsPane(Component(if (player.interfaceManager.isResizable) 746 else 548), 2)
                    player.packetDispatch.sendRunScript(1187, "ii", 0, 0)
                    player.updateSceneGraph(true)
                }

                29 -> {
                    var keySort = getVarbit(player, KEY_SORT_VARBIT)
                    keySort = (keySort + 1) % MAX_KEY_SORT_VALUE
                    setVarbit(player, KEY_SORT_VARBIT, keySort)
                }
            }
            return@on true
        }
    }
}