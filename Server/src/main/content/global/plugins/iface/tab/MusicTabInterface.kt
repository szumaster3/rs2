package content.global.plugins.iface.tab

import core.api.sendMessage
import core.game.interaction.InterfaceListener
import core.game.interaction.InterfaceOperation
import core.game.node.entity.player.link.music.MusicEntry
import shared.consts.Components

/**
 * Handles the interface tab buttons.
 * @author Emperor
 */
class MusicTabInterface : InterfaceListener {
    override fun defineInterfaceListeners() {
        onNative(Components.MUSIC_V3_187) { player, _, operation, buttonID, slot, _ ->
            if (operation == InterfaceOperation.OP1) {
                if (buttonID == 11) {
                    player.musicPlayer.toggleLooping()
                    return@onNative true
                }

                if (buttonID == 1 && slot in 0..767) {
                    if (player.musicPlayer.unlocked[slot] != null) {
                        player.musicPlayer.play(player.musicPlayer.unlocked[slot])
                        return@onNative true
                    }

                    if (player.isAdmin) {
                        for (entry in MusicEntry.getSongs().values) {
                            if (entry.index == slot) {
                                player.musicPlayer.unlock(entry.id)
                            }
                        }
                    } else {
                        sendMessage(player, "You have not unlocked this piece of music yet!")
                    }
                    return@onNative true
                }
            }
            return@onNative true
        }
    }
}
