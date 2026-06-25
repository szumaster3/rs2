package core.net.packet.context

import core.game.node.entity.player.info.Rights
import core.game.node.item.Item
import core.game.world.map.RegionChunk
import core.game.container.Container as GameContainer
import core.game.node.entity.player.Player as GamePlayer
import core.game.world.update.flag.context.Animation

sealed class Context(
    open val player: GamePlayer
) {


    data class AccessMask(
        override val player: GamePlayer,
        val interfaceId: Int,
        val childId: Int,
        val length: Int,
        val offset: Int,
        val id: Int
    ) : Context(player)


    data class AnimateInterface(
        override val player: GamePlayer,
        val animationId: Int,
        val interfaceId: Int,
        val childId: Int
    ) : Context(player)


    data class AnimateObject(
        override val player: GamePlayer,
        val objectId: Int,
        val animation: Animation
    ) : Context(player)

    data class AreaPosition(
        override val player: GamePlayer,
        val location: core.game.world.map.Location,
        val offsetX: Int,
        val offsetY: Int
    ) : Context(player)


    data class BuildItem(
        override val player: GamePlayer,
        val item: core.game.node.item.Item,
        val amount: Int
    ) : Context(player)


    data class BuildScenery(
        override val player: GamePlayer,
        val objectId: Int,
        val type: Int,
        val rotation: Int
    ) : Context(player)


    data class Camera(
        override val player: GamePlayer,
        val type: CameraType,
        val x: Int,
        val y: Int,
        val height: Int,
        val speed: Int,
        val zoomSpeed: Int
    ) : Context(player) {

        fun transform(player: GamePlayer, x: Int, y: Int) =
            copy(
                player = player,
                x = this.x + x,
                y = this.y + y
            )

        fun transform(heightOffset: Int) =
            copy(height = height + heightOffset)

        enum class CameraType(val opcode: Int) {
            POSITION(154),
            ROTATION(125),
            SET(187),
            SHAKE(27),
            RESET(24)
        }
    }


    data class ChildPosition(
        override val player: GamePlayer,
        val interfaceId: Int,
        val childId: Int,
        val x: Int,
        val y: Int
    ) : Context(player)


    data class Clan(
        override val player: GamePlayer,
        val name: String
    ) : Context(player)


    data class ClearChunk(
        override val player: GamePlayer,
        val chunk: RegionChunk
    ) : Context(player)


    data class Config(
        override val player: GamePlayer,
        val id: Int,
        val value: Int
    ) : Context(player)


    data class Contact(
        override val player: GamePlayer,
        var type: Int = UPDATE_STATE_TYPE,
        var name: String? = null,
        var worldId: Int = 0
    ) : Context(player) {

        constructor(player: GamePlayer, type: Int) : this(
            player,
            type,
            null,
            0
        )
        constructor(
            player: GamePlayer,
            name: String,
            worldId: Int
        ) : this(
            player = player,
            type = UPDATE_FRIEND_TYPE,
            name = name,
            worldId = worldId
        )

        val isOnline: Boolean
            get() = worldId > 0

        companion object {
            const val UPDATE_STATE_TYPE = 0
            const val UPDATE_FRIEND_TYPE = 1
            const val IGNORE_LIST_TYPE = 2
        }
    }


    data class ContainerContext(
        override val player: GamePlayer,
        val interfaceId: Int,
        val childId: Int,
        val containerId: Int,
        var items: Array<Item>?,
        val length: Int,
        val split: Boolean,
        val slots: IntArray? = null,
        var ids: IntArray? = null,
        private var clear: Boolean = false
    ) : Context(player) {

        constructor(
            player: GamePlayer,
            interfaceId: Int,
            childId: Int,
            clear: Boolean
        ) : this(
            player,
            interfaceId,
            childId,
            0,
            null,
            1,
            false,
            null,
            null,
            clear
        )

        constructor(
            player: GamePlayer,
            interfaceId: Int,
            childId: Int,
            containerId: Int,
            container: GameContainer,
            split: Boolean
        ) : this(
            player,
            interfaceId,
            childId,
            containerId,
            container.toArray(),
            container.toArray().size,
            split
        )

        constructor(
            player: GamePlayer,
            interfaceId: Int,
            childId: Int,
            containerId: Int,
            items: Array<Item>,
            split: Boolean
        ) : this(
            player,
            interfaceId,
            childId,
            containerId,
            items,
            items.size,
            split
        )

        constructor(
            player: GamePlayer,
            interfaceId: Int,
            childId: Int,
            containerId: Int,
            items: Array<Item>,
            length: Int,
            split: Boolean
        ) : this(
            player,
            interfaceId,
            childId,
            containerId,
            items,
            length,
            split,
            null,
            null,
            false
        )

        constructor(
            player: GamePlayer,
            interfaceId: Int,
            childId: Int,
            containerId: Int,
            ids: IntArray
        ) : this(
            player,
            interfaceId,
            childId,
            containerId,
            null,
            ids.size,
            false,
            null,
            ids,
            false
        )

        constructor(
            player: GamePlayer,
            interfaceId: Int,
            childId: Int,
            containerId: Int,
            items: Array<Item>,
            split: Boolean,
            vararg slots: Int
        ) : this(
            player,
            interfaceId,
            childId,
            containerId,
            items,
            items.size,
            split,
            slots,
            null,
            false
        )

        fun isClear(): Boolean = clear

        fun setClear(clear: Boolean) {
            this.clear = clear
        }
    }

    data class CSConfig(
        override val player: GamePlayer,
        val id: Int,
        val value: Int,
        val types: String,
        val parameters: Array<Any>
    ) : Context(player)


    data class Default(
        override val player: GamePlayer,
        val objects: Array<out Any>
    ) : Context(player) {

        constructor(player: GamePlayer, vararg objects: Any) : this(player, objects)
    }


    data class DisplayModel(
        override val player: GamePlayer,
        val modelId: Int
    ) : Context(player)


    data class DynamicScene(
        override val player: GamePlayer,
        val login: Boolean
    ) : Context(player)


    data class GameMessage(
        override val player: GamePlayer,
        val message: String
    ) : Context(player)


    data class GrandExchange(
        override val player: GamePlayer
    ) : Context(player)


    data class HintIcon(
        override val player: GamePlayer,
        val type: Int,
        val id: Int
    ) : Context(player)


    data class IntegerContext(
        override val player: GamePlayer,
        val value: Int
    ) : Context(player)


    data class InteractionOption(
        override val player: GamePlayer,
        val option: String,
        val slot: Int
    ) : Context(player)


    data class InterfaceAnimateRotate(
        override val player: GamePlayer,
        val interfaceId: Int,
        val childId: Int,
        val modelId: Int
    ) : Context(player)


    data class InterfaceConfig(
        override val player: GamePlayer,
        val interfaceId: Int,
        val childId: Int,
        val value: Int
    ) : Context(player)


    data class Interface(
        override val player: GamePlayer,
        val interfaceId: Int
    ) : Context(player)


    data class Location(
        override val player: GamePlayer,
        val x: Int,
        val y: Int,
        val level: Int
    ) : Context(player)


    data class Message(
        override val player: GamePlayer,
        val other: String,
        val chatIcon: Int,
        val opcode: Int,
        val message: String
    ) : Context(player) {

        constructor(
            player: GamePlayer,
            other: GamePlayer,
            opcode: Int,
            message: String
        ) : this(
            player = player,
            other = other.name,
            chatIcon = Rights.getChatIcon(other),
            opcode = opcode,
            message = message
        )

        companion object {
            const val SEND_MESSAGE = 71
            const val RECEIVE_MESSAGE = 0
            const val CLAN_MESSAGE = 54
        }
    }


    data class MinimapState(
        override val player: GamePlayer,
        val state: Int
    ) : Context(player)


    data class Music(
        override val player: GamePlayer,
        val id: Int
    ) : Context(player)


    data class PlayerContext(
        override val player: GamePlayer
    ) : Context(player)


    data class PositionedGraphic(
        override val player: GamePlayer,
        val graphicId: Int,
        val x: Int,
        val y: Int
    ) : Context(player)


    data class RunScript(
        override val player: GamePlayer,
        val scriptId: Int,
        val types: String,
        val parameters: Array<Any>
    ) : Context(player)


    data class SceneGraph(
        override val player: GamePlayer,
        val login: Boolean
    ) : Context(player)


    data class SceneryUpdate(
        override val player: GamePlayer,
        val objectId: Int
    ) : Context(player)


    data class Skill(
        override val player: GamePlayer,
        val skillId: Int,
        val level: Int
    ) : Context(player)


    data class StringContext(
        override val player: GamePlayer,
        val value: String
    ) : Context(player)


    data class SystemUpdate(
        override val player: GamePlayer,
        val time: Int
    ) : Context(player)


    data class Varbit(
        override val player: GamePlayer,
        val id: Int,
        val value: Int
    ) : Context(player)


    data class VarcUpdate(
        override val player: GamePlayer,
        val id: Int,
        val value: Int
    ) : Context(player)


    data class WalkOption(
        override val player: GamePlayer,
        val option: String
    ) : Context(player)


    data class WindowsPane(
        override val player: GamePlayer,
        val paneId: Int
    ) : Context(player)
}