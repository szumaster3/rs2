package content.region.morytania.port_phasmatys.quest.ahoy.plugin

import core.api.*
import core.game.component.Component
import core.game.event.EventHook
import core.game.event.TickEvent
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.InterfaceListener
import core.game.node.entity.Entity
import core.game.node.entity.player.Player
import core.game.world.map.zone.ZoneBorders
import core.plugin.Initializable
import core.tools.RandomFunction
import shared.consts.Components
import shared.consts.Scenery

@Initializable
class WindSpeedInterface : InterfaceListener, InteractionListener, EventHook<TickEvent>, MapArea {

    override fun defineAreaBorders(): Array<ZoneBorders> =
        arrayOf(ZoneBorders(3616, 3539, 3622, 3545, 2))

    override fun areaEnter(entity: Entity) {
        // The zone also fires for NPCs (pets, familiars).
        val player = entity as? Player ?: return
        openInterface(player, Components.AHOY_WINDSPEED_10)
        setAttribute(player, GhostsAhoyUtils.windSpeed, 0)
        // 0 = roll a new wind on the very next tick
        setAttribute(player, WIND_DELAY, 0)
        player.hook(Event.Tick, this)
    }

    override fun areaLeave(entity: Entity, logout: Boolean) {
        val player = entity as? Player ?: return
        player.unhook(this)
        player.interfaceManager.close(Component(Components.AHOY_WINDSPEED_10))
        removeAttribute(player, GhostsAhoyUtils.windSpeed)
        removeAttribute(player, WIND_DELAY)
    }

    override fun defineInterfaceListeners() {
        on(Components.AHOY_WINDSPEED_10) { _, _, _, _, _, _ -> true }
    }

    override fun defineListeners() {
        on(Scenery.MAST_5274, IntType.SCENERY, "search") { player, _ ->
            when (getAttribute(player, GhostsAhoyUtils.windSpeed, 0)) {
                0 -> sendDialogue(player, "You can see a tattered flag blowing in the wind. The wind is blowing too hard to make out any details.")
                1 -> sendDialogue(player, "You can see a tattered flag blowing in the wind. The top half of the flag is coloured red.")
                2 -> sendDialogue(player, "You can see a tattered flag blowing in the wind. The bottom half of the flag is coloured blue.")
                3 -> sendDialogue(player, "You can see a tattered flag blowing in the wind. The skull emblem is coloured blue.")
            }
            return@on true
        }
    }

    override fun process(entity: Entity, event: TickEvent) {
        val player = entity as? Player ?: return

        // Only change the wind every WIND_CHANGE_INTERVAL ticks instead of every tick.
        val delay = getAttribute(player, WIND_DELAY, 0)
        if (delay > 0) {
            setAttribute(player, WIND_DELAY, delay - 1)
            return
        }
        setAttribute(player, WIND_DELAY, WIND_CHANGE_INTERVAL - 1)

        val wind = RandomFunction.getRandom(3)
        setAttribute(player, GhostsAhoyUtils.windSpeed, wind)
        val windText = when (wind) {
            0 -> "High"
            1 -> "Low"
            2 -> "Medium"
            3 -> "Very High"
            else -> "High"
        }
        sendString(player, windText, Components.AHOY_WINDSPEED_10, 2)
    }

    companion object {
        /** Ticks between wind changes (1 tick = 0.6s, so 10 = about 6 seconds). */
        private const val WIND_CHANGE_INTERVAL = 10
        private const val WIND_DELAY = "ghostsahoy:wind-delay"
    }
}