package content.region.fremennik.lunar.plugin

import core.api.location
import core.api.resetCamera
import core.api.sendMessage
import core.api.teleport
import core.api.utils.PlayerCamera
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.player.link.TeleportManager.TeleportType
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Scenery

class LunarIslePlugin : InteractionListener {

    companion object {
        private const val INSIDE_BABA_YAGA_SHOP = Scenery.DOOR_16774
        private const val BABA_YAGA_SHOP = NPCs.LUNAR_BABA_YAGA_HOUSE_4512
        private val CYRISUS = intArrayOf(NPCs.DREAM_CYRISUS_SITTING_5893, NPCs.DREAM_CYRISUS_5894, NPCs.DREAM_CYRISUS_RANGER_5895, NPCs.DREAM_CYRISUS_MELEE_5896, NPCs.DREAM_CYRISUS_CASTER_5897)
    }

    override fun defineListeners() {
        on(INSIDE_BABA_YAGA_SHOP, IntType.SCENERY, "open") { player, _ ->
            teleport(player, location(2101, 3926, 0), TeleportType.INSTANT)
            resetCamera(player)
            return@on true
        }

        on(BABA_YAGA_SHOP, IntType.NPC, "go-inside") { player, _ ->
            teleport(player, location(2451, 4645, 0), TeleportType.INSTANT)
            PlayerCamera(player).shake(1, 0, 0, 16, 20)
            return@on true
        }

        onUseWith(IntType.NPC, Items.CAVE_NIGHTSHADE_2398, *CYRISUS) { player, _, _ ->
            sendMessage(player, "How evil! Are you trying to kill him?")
            return@onUseWith true
        }
    }
}
