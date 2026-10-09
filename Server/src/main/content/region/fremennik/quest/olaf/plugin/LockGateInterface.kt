package content.region.fremennik.quest.olaf.plugin

import core.api.closeInterface
import core.api.removeItem
import core.api.sendMessage
import core.game.global.action.DoorActionHandler
import core.game.interaction.InterfaceListener
import core.game.world.map.Location
import core.game.world.map.RegionManager.getObject
import shared.consts.Components
import shared.consts.Items

class LockGateInterface : InterfaceListener {

    override fun defineInterfaceListeners() {
        on(Components.OLAF2_LOCK_GATE_252) { player, _, _, buttonID, _, _ ->
            when(buttonID) {
                13 -> {
                    closeInterface(player)
                    if(removeItem(player, Items.KEY_11043)) {
                        getObject(Location(2725, 10168))?.let { DoorActionHandler.handleAutowalkDoor(player, it) }
                    } else {
                        // TODO: Wrong key message.
                        sendMessage(player, "The door is locked.")
                    }
                }
            }
            return@on true
        }
    }
}