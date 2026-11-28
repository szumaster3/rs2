package content.region.fremennik.plugin

import com.google.gson.JsonObject
import content.data.GameAttributes
import core.ServerStore.Companion.getArchive
import core.api.*
import core.game.node.entity.impl.Animator
import core.game.node.entity.player.Player
import core.game.system.task.Pulse
import core.game.world.GameWorld
import core.game.world.map.Location
import core.game.world.update.flag.context.Animation
import core.game.world.update.flag.context.Graphics
import shared.consts.Animations
import shared.consts.Items

/**
 * Handles teleport using lyre.
 */
class LyreTeleport(val player: Player) : Pulse() {
    companion object {
        const val LYRE_TELEPORT_ALT = "/save:lyre-waterbirth-tp"
        val waterBirthIsland = Location(2544, 3759, 0)
        val rellekaProvince = Location(2663, 3646, 0)

        @JvmStatic
        fun getStoreFile(): JsonObject = getArchive("daily-lyre-teleport")

        fun teleport(player: Player) {
            if (hasTimerActive(player, GameAttributes.TELEBLOCK_TIMER)) {
                sendMessage(player, "A magical force has stopped you from teleporting.")
            } else {
                GameWorld.Pulser.submit(LyreTeleport(player))
                getStoreFile().addProperty(player.username.lowercase(), true)
            }
        }
    }

    var counter = 0

    override fun pulse(): Boolean {
        when (counter++) {
            0 -> {
                lock(player, 1000)
                player.animator.animate(
                    Animation(Animations.MAGIC_LYRE_TELEPORT_REWORK_9600, Animator.Priority.VERY_HIGH),
                    Graphics(shared.consts.Graphics.MAGIC_LYRE_TELEPORT_REWORK_SPOTANIM_1682),
                )
            }

            6 -> {
                if (inEquipment(player, Items.FREMENNIK_SEA_BOOTS_3_14573) &&
                    getAttribute(player, LYRE_TELEPORT_ALT, false))
                {
                    player.properties.teleportLocation = waterBirthIsland
                }
                else
                {
                    player.properties.teleportLocation = rellekaProvince
                }
            }

            7 -> {
                unlock(player)
                return true
            }
        }
        return false
    }
}
