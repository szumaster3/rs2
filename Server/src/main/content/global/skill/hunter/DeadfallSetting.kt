package content.global.skill.hunter

import content.global.skill.firemaking.LogItem
import core.api.*
import core.game.node.Node
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.game.node.scenery.Scenery
import core.game.world.map.Direction
import core.game.world.map.Location
import core.game.world.update.flag.context.Animation
import shared.consts.Animations

class DeadfallSetting :
    TrapSetting(
        /* nodeIds = */             intArrayOf(28935, 19205),
        /* items = */               arrayOf(Item(946)),
        /* objectIds = */           intArrayOf(28937, 19206),
        /* baitIds = */             intArrayOf(10138, 6006, 12574, 341, 2132),
        /* option = */              "set-trap",
        /* level = */               23,
        /* failId = */              -1,
        /* setupAnimation = */      Animation(Animations.HUMAN_LAYTRAP_5208),
        /* dismantleAnimation = */  Animation(9726),
        /* objectTrap = */          true
    )
{
    override fun hasItems(player: Player): Boolean {
        if (!super.hasItems(player)) {
            sendMessage(player, "You need a knife in order to set a deadfall trap.")
            return false
        }
        for (log in LogItem.values()) {
            if (inInventory(player, log.logId, 1)) {
                return true
            }
        }
        return false
    }

    override fun createHook(wrapper: TrapWrapper): TrapHook =
        TrapHook(wrapper, getLocations(wrapper.`object`).toTypedArray())

    override fun reward(player: Player, node: Node, wrapper: TrapWrapper) {
        removeItem(player, getLog(player)!!.logId)
    }

    override fun clear(wrapper: TrapWrapper, type: Int): Boolean {
        if (super.clear(wrapper, type)) {
            addScenery(
                wrapper.`object`.transform(
                    getNodeForObjectId(if (wrapper.isCaught) wrapper.originalId else wrapper.`object`.id),
                ),
            )
            return true
        }
        return false
    }

    override fun canCatch(wrapper: TrapWrapper, npc: NPC): Boolean {
        val x = wrapper.`object`.location.x
        val y = wrapper.`object`.location.y
        val direction = wrapper.`object`.direction
        var dir = 0
        if (direction == Direction.NORTH) {
            dir = if (npc.location.y < y) 1 else 0
        } else if (direction == Direction.SOUTH) {
            dir = if (npc.location.y < y) 0 else 1
        } else if (direction == Direction.WEST) {
            dir = if (npc.location.x > x) 1 else 0
        } else {
            dir = if (npc.location.x > x) 0 else 1
        }
        faceLocation(npc, wrapper.`object`.location)
        wrapper.`object`.attributes.setAttribute("kebbit-dir", dir)
        return true
    }

    override fun isSuccess(player: Player, node: TrapNode): Boolean = true

    override fun getTransformId(wrapper: TrapWrapper, node: TrapNode): Int {
        val dir = wrapper.`object`.attributes.getAttribute("kebbit-dir", 0)
        val id = if (dir == 0) node.objectIds[0] else node.objectIds[1]
        return id
    }

    override fun getFinalId(wrapper: TrapWrapper, node: TrapNode): Int = node.objectIds[2]

    override fun buildObject(player: Player, node: Node): Scenery = node.asScenery().transform(getObjectForNode(node))

    override fun getLimitMessage(player: Player): String = "You can only have one deadfall trap at a time."

    override fun exceedsLimit(player: Player): Boolean = HunterManager.getInstance(player).trapAmount > 0

    private fun getLocations(scenery: Scenery): List<Location> {
        val locations = ArrayList<Location>(20)
        when (scenery.direction) {
            Direction.NORTH -> {
                locations.add(scenery.location.transform(1, -1, 0))
                locations.add(scenery.location.transform(1, 1, 0))
            }

            Direction.SOUTH -> {
                locations.add(scenery.location.transform(0, 1, 0))
                locations.add(scenery.location.transform(0, -1, 0))
            }

            Direction.WEST -> {
                locations.add(scenery.location.transform(1, 1, 0))
                locations.add(scenery.location.transform(-1, 1, 0))
            }

            else -> {
                locations.add(scenery.location.transform(-1, 0, 0))
                locations.add(scenery.location.transform(1, 0, 0))
            }
        }
        return locations
    }

    private fun getLog(player: Player): LogItem? {
        for (log in LogItem.values()) {
            if (player.inventory.contains(log.logId, 1)) {
                return log
            }
        }
        return null
    }
}
