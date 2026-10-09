package content.global.random.event.candlelight

import content.data.GameAttributes
import content.data.RandomEvent
import core.api.*
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.TeleportManager
import core.game.world.map.Location

/**
 * Utils for the candlelight random event.
 */
object CandlelightUtils {
    private val EVENT_LOCATION: Location = Location.create(1970,5002,0)

    fun init(player: Player) {
        lockTeleport(player)
        setAttribute(player, RandomEvent.save(), player.location)
        registerLogoutListener(player, RandomEvent.logout()) { p ->
            p.location = getAttribute(p, RandomEvent.save(), player.location)
        }
        teleport(player, EVENT_LOCATION, TeleportManager.TeleportType.NORMAL)
    }

    fun cleanup(player: Player) {
        player.unlock()
        player.locks.unlockTeleport()
        player.locks.unlockInteraction()
        player.properties.teleportLocation = getAttribute(player, RandomEvent.save(), null)
        clearLogoutListener(player, RandomEvent.logout())
        removeAttributes(
            player,
            GameAttributes.RE_CANDLELIGHT_CANDLES,
            GameAttributes.RE_CANDLELIGHT_CAMERA,
            RandomEvent.save()
        )
        player.interfaceManager.restoreTabs()
        player.animator.reset()
    }
}