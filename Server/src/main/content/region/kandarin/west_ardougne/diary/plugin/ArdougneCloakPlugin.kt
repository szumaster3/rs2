package content.region.kandarin.west_ardougne.diary.plugin

import content.data.GameAttributes
import core.ServerStore.Companion.getArchive
import core.ServerStore.Companion.getBoolean
import core.api.*
import core.game.dialogue.DialogueFile
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.diary.Diary
import core.game.node.entity.player.link.diary.DiaryType
import core.game.node.entity.player.link.TeleportManager
import core.game.world.map.Location
import java.util.Locale

class ArdougneCloakPlugin : InteractionListener {
    override fun defineListeners() {
        on(CLOAKS, IntType.ITEM, "teleports", "operate") { player, node ->
            if (node.id == CLOAKS[0]) travel(player, false)
            else openDialogue(player, CloakTeleport(node.id))
            true
        }
    }

    private class CloakTeleport(private val cloak: Int) : DialogueFile() {
        override fun handle(componentID: Int, buttonID: Int) {
            val p = player ?: return
            when (stage) {
                0 -> {
                    sendOptions(p, "Where would you like to teleport to?", "Kandarin Monastery.", "Ardougne Farm.")
                    stage++
                }
                1 -> {
                    end()
                    if (!inEquipmentOrInventory(p, cloak)) return
                    when (buttonID) {
                        1 -> travel(p, false)
                        2 -> travel(p, true)
                    }
                }
            }
        }
    }

    companion object {
        private val CLOAKS = intArrayOf(15345, 15347, 15349)
        private fun travel(player: Player, farm: Boolean) {
            if (hasTimerActive(player, GameAttributes.TELEBLOCK_TIMER)) {
                sendMessage(player, "A magical force has stopped you from teleporting.")
                return
            }
            val store = getArchive("daily-ardougne-farm-teleport")
            val key = player.username.lowercase(Locale.ROOT)
            val unlimited = Diary.hasClaimedLevelRewards(player, DiaryType.ARDOUGNE, 3)
            if (farm && !unlimited && store.getBoolean(key)) {
                sendMessage(player, "This can only be done once per day.")
                return
            }
            val destination = if (farm) Location.create(2677, 3375, 0) else Location.create(2606, 3217, 0)
            if (teleport(player, Location.getRandomLocation(destination, 1, true), TeleportManager.TeleportType.RANDOM_EVENT_OLD) && farm && !unlimited) {
                store.addProperty(key, true)
            }
        }
    }
}
