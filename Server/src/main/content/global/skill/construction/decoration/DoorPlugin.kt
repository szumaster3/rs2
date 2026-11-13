package content.global.skill.construction.decoration

import content.global.skill.construction.BuildHotspot
import content.global.skill.construction.HouseManager
import content.global.skill.construction.HousingStyle
import core.api.getDynLevel
import core.api.rewardXP
import core.api.sendMessage
import core.cache.def.impl.SceneryDefinition
import core.game.global.action.DoorActionHandler
import core.game.interaction.OptionHandler
import core.game.node.Node
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.game.node.scenery.Constructed
import core.game.node.scenery.Scenery
import core.game.node.scenery.SceneryBuilder
import core.game.world.map.Direction
import core.game.world.map.RegionManager
import core.plugin.Initializable
import core.plugin.Plugin
import core.tools.RandomFunction

@Initializable
class DoorPlugin : OptionHandler() {

    override fun newInstance(arg: Any?): Plugin<Any> {
        for ((closed, open) in OPEN_DOORS) {
            SceneryDefinition.forId(closed).handlers["option:open"] = this
            SceneryDefinition.forId(open).handlers["option:close"] = this
        }
        return this
    }

    override fun handle(player: Player, node: Node, option: String): Boolean {
        val door = node as? Scenery ?: return false

        return when {
            option.equals("open", ignoreCase = true) -> {
                val openId = OPEN_DOORS[door.id] ?: return false
                openDoor(door, getOtherLeaf(door, door), openId)
            }
            option.equals("close", ignoreCase = true) -> {
                val closed = (door as? Constructed)?.replaced ?: return false
                if (OPEN_DOORS[closed.id] != door.id) return false
                val other = getOtherLeaf(door, closed)
                val otherClosed = (other as? Constructed)?.replaced
                if (!SceneryBuilder.replace(door, closed)) return false
                if (otherClosed != null) SceneryBuilder.replace(other, otherClosed)
                true
            }
            else -> false
        }
    }

    fun handleDungeonDoor(player: Player, door: Scenery, option: String): Boolean {
        val lock = DungeonLock.values().firstOrNull { door.id == it.leftId || door.id == it.rightId } ?: return false
        val house = player.getAttribute<HouseManager>("poh_entry", player.houseManager)
        val dungeon = house.dungeonRegion ?: return false
        if (player.location.regionId != dungeon.id || door.location.regionId != dungeon.id ||
            RegionManager.forId(dungeon.id) !== dungeon) return false

        if (option == "open") {
            sendMessage(player, "The door is locked.")
            return true
        }

        val skill = when (option) {
            "pick-lock" -> Skills.THIEVING
            "force" -> Skills.STRENGTH
            else -> return false
        }
        if (!door.isActive) return true

        val level = getDynLevel(player, skill).coerceIn(1, 99)
        val chance = lock.startChance + (100 - lock.startChance) * (level - 1) / 98
        if (RandomFunction.random(100) >= chance) {
            sendMessage(player, if (option == "pick-lock") "You fail to pick the lock." else "You fail to force the door open.")
            return true
        }
        if (openDoor(door, getOtherLeaf(door, door), OPEN_DOORS.getValue(door.id))) {
            rewardXP(player, skill, lock.experience)
            sendMessage(player, if (option == "pick-lock") "You pick the lock." else "You force the door open.")
        }
        return true
    }

    private enum class DungeonLock(val leftId: Int, val rightId: Int, val experience: Double, val startChance: Int) {
        OAK(13344, 13345, 10.0, 50),
        STEEL(13346, 13347, 15.0, 35),
        MARBLE(13348, 13349, 20.0, 20)
    }

    private fun openDoor(door: Scenery, second: Scenery?, replaceId: Int): Boolean {
        if (second == null) {
            val (offsetX, offsetY, newRot) = getOpenOffset(door)
            val newDoor = Scenery(replaceId, door.location.transform(offsetX, offsetY, 0), 0, newRot)
            return SceneryBuilder.replace(door, newDoor)
        }

        val p = DoorActionHandler.getRotationPoint(door.rotation) ?: return false

        var firstDir = (door.rotation + 1) % 4
        var secondDir = (second.rotation + 1) % 4

        val offsetDir = Direction.getDirection(second.location.x - door.location.x,
            second.location.y - door.location.y)

        firstDir = when {
            firstDir == 1 && offsetDir == Direction.NORTH -> 3
            firstDir == 2 && offsetDir == Direction.EAST  -> 0
            firstDir == 3 && offsetDir == Direction.SOUTH -> 1
            firstDir == 0 && offsetDir == Direction.WEST  -> 2
            else -> firstDir
        }

        if (firstDir == secondDir) secondDir = (secondDir + 2) % 4

        val firstLoc = door.location.transform(p.x, p.y, 0)
        val secondLoc = second.location.transform(p.x, p.y, 0)

        if (!SceneryBuilder.replace(door, door.transform(replaceId, firstDir, firstLoc))) return false
        return SceneryBuilder.replace(second, second.transform(OPEN_DOORS.getValue(second.id), secondDir, secondLoc))
    }

    private fun getOpenOffset(door: Scenery): Triple<Int, Int, Int> {
        return when {
            door.location.chunkOffsetY == 0 || door.location.chunkOffsetY == 7 -> {
                val offsetY = if (door.location.chunkOffsetY == 7) 1 else -1
                val rot = if (door.location.chunkOffsetX == 3) 0 else 2
                Triple(0, offsetY, rot)
            }
            door.location.chunkOffsetX == 0 || door.location.chunkOffsetX == 7 -> {
                val offsetX = if (door.location.chunkOffsetX == 7) 1 else -1
                val rot = if (door.location.chunkOffsetY == 3) 3 else 1
                Triple(offsetX, 0, rot)
            }
            else -> Triple(0, 0, (door.rotation + 1) and 3)
        }
    }

    private fun getOtherLeaf(door: Scenery, closed: Scenery): Scenery? {
        val otherId = DOOR_PAIRS[closed.id] ?: return null
        val rotation = closed.rotation
        val closing = door !== closed
        val id = if (closing) OPEN_DOORS.getValue(otherId) else otherId
        for (step in intArrayOf(-1, 1)) {
            val dx = if (rotation and 1 == 1) step else 0
            val dy = if (rotation and 1 == 0) step else 0
            val loc = door.location.transform(dx, dy, 0)
            val other = RegionManager.getObject(loc.z, loc.x, loc.y, id) ?: continue
            val otherClosed = if (closing) (other as? Constructed)?.replaced else other
            if (otherClosed?.id == otherId && otherClosed.rotation == rotation &&
                otherClosed.location == closed.location.transform(dx, dy, 0)) {
                return other
            }
        }
        return null
    }

    companion object {
        val dungeonDoorIds: IntArray
            get() = DungeonLock.values().flatMap { listOf(it.leftId, it.rightId) }.toIntArray()

        private val OPEN_DOORS = mapOf(
            13100 to 13102,
            13101 to 13103,
            13006 to 13008,
            13007 to 13009,
            13015 to 13017,
            13016 to 13018,
            13094 to 13095,
            13096 to 13097,
            13109 to 13110,
            13107 to 13108,
            13118 to 13120,
            13119 to 13121,
            13344 to 13350,
            13345 to 13351,
            13346 to 13352,
            13347 to 13353,
            13348 to 13354,
            13349 to 13355
        )
        private val DOOR_PAIRS = buildMap {
            for (style in HousingStyle.values()) {
                put(style.doorId, style.secondDoorId)
                put(style.secondDoorId, style.doorId)
            }
            for (index in BuildHotspot.DUNGEON_DOOR_LEFT.decorations.indices) {
                val left = BuildHotspot.DUNGEON_DOOR_LEFT.decorations[index].objectId
                val right = BuildHotspot.DUNGEON_DOOR_RIGHT.decorations[index].objectId
                put(left, right)
                put(right, left)
            }
        }
    }
}
