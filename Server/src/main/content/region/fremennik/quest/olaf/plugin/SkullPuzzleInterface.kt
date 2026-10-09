package content.region.fremennik.quest.olaf.plugin

import core.api.closeInterface
import core.api.sendMessage
import core.game.global.action.DoorActionHandler
import core.game.interaction.InterfaceListener
import core.game.world.map.Location
import core.game.world.map.RegionManager.getObject
import shared.consts.Components

class SkullPuzzleInterface : InterfaceListener {
    override fun defineInterfaceListeners() {
        on(Components.OLAF2_SKULL_PUZZLE_253) { player, _, _, buttonID, _, _ ->
            when(buttonID) {
                11 -> {
                    // TODO: Logic.
                    closeInterface(player)
                    getObject(Location(2707,10147))?.let { DoorActionHandler.handleDoor(player, it) }
                    sendMessage(player, "Something in the wall goes 'clunk'.")
                }
            }
            return@on true
        }
    }
}