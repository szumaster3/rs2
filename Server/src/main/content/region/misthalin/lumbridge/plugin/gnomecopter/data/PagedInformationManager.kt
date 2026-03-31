package content.region.misthalin.lumbridge.plugin.gnomecopter.data

import core.api.sendString
import core.game.node.entity.player.Player
import shared.consts.Components

object PagedInformationManager {

    fun sendPage(player: Player, data: PagedInformation, page: Int) {

        val max = data.pages.size
        val pageNumber = page.coerceIn(0, max - 1)
        val lines = data.pages[pageNumber]

        // clear.
        for (i in 4..13) {
            sendString(player, "", Components.CARPET_MAIN_728, i)
        }

        // fill.
        lines.forEachIndexed { index, line ->
            if (index < 10) {
                sendString(player, line, Components.CARPET_MAIN_728, 4 + index)
            }
        }

        // page info.
        sendString(
            player,
            "Page ${pageNumber + 1} / $max",
            Components.CARPET_MAIN_728,
            14
        )
        player.setAttribute("gc:page", pageNumber)
    }
}