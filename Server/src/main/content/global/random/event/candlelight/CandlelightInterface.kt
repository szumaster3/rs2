package content.global.random.event.candlelight

import content.data.GameAttributes
import core.api.*
import core.api.utils.PlayerCamera
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.InterfaceListener
import core.game.node.entity.Entity
import core.game.node.entity.player.Player
import core.game.world.map.Direction
import core.game.world.map.Location
import core.game.world.map.zone.ZoneBorders
import core.game.world.map.zone.ZoneRestriction
import shared.consts.Components
import shared.consts.Scenery

/**
 * Represents the candlelight interface.
 */
class CandlelightInterface : InterfaceListener, InteractionListener, MapArea {

    companion object {
        const val CANDLELIGHT_INTERFACE = Components.LIGHT2_178
        private const val FIRST_CANDLE_VARBIT = 1771
        private val DEFAULT_CAMERA = Location(1968,5002)
        private val EMPTY_CANDLES get() = IntArray(11)

        private val CANDLE_LOC_ARRAY = arrayOf(
            Location(1967, 4997), Location(1968, 4998), Location(1967, 4999),
            Location(1968, 5000), Location(1967, 5001), Location(1968, 5002),
            Location(1967, 5003), Location(1968, 5004), Location(1967, 5005),
            Location(1968, 5006), Location(1967, 5007),
        )

        fun initCandlelight(player: Player) {
            val candleArray = intArrayOf(0,0,0,0,0,0,2,2,2,2,2)
            candleArray.shuffle()
            setAttribute(player, GameAttributes.RE_CANDLELIGHT_CANDLES, candleArray)
            candleArray.forEachIndexed { index, state ->
                setVarbit(player, FIRST_CANDLE_VARBIT + index, state)
            }
        }

        fun areCandlesLit(player: Player): Boolean {
            val candleArray = getAttribute(player, GameAttributes.RE_CANDLELIGHT_CANDLES, EMPTY_CANDLES)
            return candleArray.none { it == 0 }
        }

        fun lightCandle(player: Player) {
            val camLoc = getAttribute(player, GameAttributes.RE_CANDLELIGHT_CAMERA, DEFAULT_CAMERA)
            val candleIndex = CANDLE_LOC_ARRAY.indexOf(camLoc)
            if (candleIndex == -1) return

            val candleArray = getAttribute(player, GameAttributes.RE_CANDLELIGHT_CANDLES, EMPTY_CANDLES)
            if (candleArray[candleIndex] == 0) {
                candleArray[candleIndex] = 1
                setAttribute(player, GameAttributes.RE_CANDLELIGHT_CANDLES, candleArray)
                setVarbit(player, FIRST_CANDLE_VARBIT + candleIndex, 1)
            }
        }

        fun moveCamera(player: Player, direction: Direction, firstTime: Boolean = false) {
            var camLoc = getAttribute(player, GameAttributes.RE_CANDLELIGHT_CAMERA, DEFAULT_CAMERA)
            when (direction) {
                Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST ->
                    camLoc = camLoc.transform(direction)
                else -> {}
            }
            camLoc.x = camLoc.x.coerceIn(1967, 1968)
            camLoc.y = camLoc.y.coerceIn(4997, 5007)
            val camera = PlayerCamera(player)
            setAttribute(player, GameAttributes.RE_CANDLELIGHT_CAMERA, camLoc)
            camera.rotateTo(camLoc.x - 30, camLoc.y, 0, 200)
            camera.panTo(camLoc.x + 2, camLoc.y, 350, if (firstTime) 400 else 10)
        }
    }

    override fun defineInterfaceListeners() {
        on(CANDLELIGHT_INTERFACE) { player, _, _, buttonID, _, _ ->
            when (buttonID)
            {
                1 -> moveCamera(player, Direction.WEST)
                2 -> moveCamera(player, Direction.EAST)
                3 -> lightCandle(player)
                4 -> moveCamera(player, Direction.SOUTH)
                5 -> moveCamera(player, Direction.NORTH)
                9 -> closeInterface(player)
            }
            return@on true
        }

        onClose(CANDLELIGHT_INTERFACE) { player, _ ->
            resetCamera(player)
            return@onClose true
        }
    }

    override fun defineListeners() {
        on((Scenery.DECORATIVE_PILLAR_11364..Scenery.DECORATIVE_PILLAR_11394).toIntArray(), IntType.SCENERY, "light") { player, node ->
            setAttribute(player, GameAttributes.RE_CANDLELIGHT_CAMERA, Location(node.location.x, node.location.y))
            moveCamera(player, Direction.NORTH_WEST, true)
            openInterface(player, CANDLELIGHT_INTERFACE)
            return@on true
        }
    }

    override fun defineDestinationOverrides() {
        setDest(IntType.SCENERY, (Scenery.DECORATIVE_PILLAR_11364..Scenery.DECORATIVE_PILLAR_11394).toIntArray(), "light") { _, node ->
            return@setDest Location(1970, node.location.y)
        }
    }

    override fun defineAreaBorders(): Array<ZoneBorders> = arrayOf(ZoneBorders.forRegion(7758))

    override fun getRestrictions(): Array<ZoneRestriction> = arrayOf(
        ZoneRestriction.RANDOM_EVENTS, ZoneRestriction.CANNON, ZoneRestriction.FOLLOWERS, ZoneRestriction.TELEPORT
    )

    override fun areaEnter(entity: Entity) {
        if (entity is Player) {
            initCandlelight(entity)
            removeTabs(entity,0,1,2,3,4,5,6,12)
        }
    }

    override fun areaLeave(entity: Entity, logout: Boolean) {
        if (entity is Player) {
            restoreTabs(entity)
        }
    }
}