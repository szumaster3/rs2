package content.region.misthalin.lumbridge.plugin.gnomecopter.sign

import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import shared.consts.Scenery

class InformationSignListener : InteractionListener {

    override fun defineListeners() {
        on(Scenery.SIGNPOST_30039, IntType.SCENERY, "read") { player, node ->
            return@on InformationSignManager.handle(player, node.location)
        }
    }
}