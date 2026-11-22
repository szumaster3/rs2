package core.net.packet.out

import core.game.container.Container
import core.game.node.Node
import core.game.node.entity.Entity
import core.game.node.entity.player.Player
import core.game.node.entity.player.info.Rights
import core.game.node.entity.player.link.audio.Audio
import core.game.node.item.Item
import core.game.node.scenery.Scenery
import core.game.system.communication.ClanRepository
import core.game.world.map.Location
import core.game.world.map.RegionChunk
import core.game.world.update.flag.context.Animation
import core.game.world.update.flag.context.Graphics
import java.awt.Point

sealed class PacketContext {
    abstract val player: Player

    class PlayerContext(override val player: Player) : PacketContext()

    class AccessMaskContext(
        override val player: Player,
        val id: Int,
        val childId: Int,
        val interfaceId: Int,
        val offset: Int,
        val length: Int,
    ) : PacketContext() {
        fun transform(player: Player, id: Int) = AccessMaskContext(player, id, childId, interfaceId, offset, length)
    }

    class AnimateInterfaceContext(
        override val player: Player,
        val animationId: Int,
        val interfaceId: Int,
        val childId: Int,
    ) : PacketContext()

    class AnimateObjectContext(override val player: Player, val animation: Animation) : PacketContext()

    class AreaPositionContext(
        override val player: Player,
        val location: Location,
        val offsetX: Int,
        val offsetY: Int,
    ) : PacketContext()

    class AudioContext(override val player: Player, val audio: Audio, val location: Location?) : PacketContext()

    class BuildItemContext @JvmOverloads constructor(
        override val player: Player,
        val item: Item,
        val oldAmount: Int = 0,
    ) : PacketContext()

    class BuildSceneryContext(override val player: Player, val scenery: Scenery) : PacketContext()

    class CameraContext(
        override val player: Player,
        val type: CameraType,
        val x: Int,
        val y: Int,
        val height: Int,
        val speed: Int,
        val zoomSpeed: Int,
    ) : PacketContext() {
        fun transform(player: Player, x: Int, y: Int) =
            CameraContext(player, type, this.x + x, this.y + y, height, speed, zoomSpeed)

        fun transform(heightOffset: Int) = CameraContext(player, type, x, y, height + heightOffset, speed, zoomSpeed)

        enum class CameraType(val opcode: Int) {
            POSITION(154), ROTATION(125), SET(187), SHAKE(27), RESET(24)
        }
    }

    class ChildPositionContext(
        override val player: Player,
        val interfaceId: Int,
        val childId: Int,
        positionX: Int,
        positionY: Int,
    ) : PacketContext() {
        val position = Point(positionX, positionY)
    }

    class ClanContext(override val player: Player, val clan: ClanRepository, val isLeave: Boolean) : PacketContext()

    class ClearChunkContext(override val player: Player, val chunk: RegionChunk) : PacketContext()

    class ConfigContext @JvmOverloads constructor(
        override val player: Player,
        val id: Int,
        val value: Int,
        val isCs2: Boolean = false,
    ) : PacketContext()

    class ContactContext private constructor(
        override val player: Player,
        val type: Int,
        val name: String?,
        val worldId: Int,
    ) : PacketContext() {
        constructor(player: Player, type: Int) : this(player, type, null, 0)
        constructor(player: Player, name: String?, worldId: Int) : this(player, UPDATE_FRIEND_TYPE, name, worldId)

        val isOnline: Boolean get() = worldId > 0

        companion object {
            const val UPDATE_STATE_TYPE = 0
            const val UPDATE_FRIEND_TYPE = 1
            const val IGNORE_LIST_TYPE = 2
        }
    }

    class ContainerContext private constructor(
        override val player: Player,
        val interfaceId: Int,
        val childId: Int,
        val containerId: Int,
        val items: Array<out Item?>?,
        val length: Int,
        val isSplit: Boolean,
        val slots: IntArray? = null,
        val ids: IntArray? = null,
        val isClear: Boolean = false,
    ) : PacketContext() {
        constructor(player: Player, interfaceId: Int, childId: Int, clear: Boolean) :
            this(player, interfaceId, childId, 0, null, 1, false, isClear = clear)

        constructor(player: Player, interfaceId: Int, childId: Int, containerId: Int, container: Container, split: Boolean) :
            this(player, interfaceId, childId, containerId, container.toArray(), split)

        constructor(player: Player, interfaceId: Int, childId: Int, containerId: Int, items: Array<out Item?>, split: Boolean) :
            this(player, interfaceId, childId, containerId, items, items.size, split)

        constructor(player: Player, interfaceId: Int, childId: Int, containerId: Int, items: Array<out Item?>?, length: Int, split: Boolean) :
            this(player, interfaceId, childId, containerId, items, length, split, slots = null)

        constructor(player: Player, interfaceId: Int, childId: Int, containerId: Int, items: IntArray) :
            this(player, interfaceId, childId, containerId, null, items.size, false, ids = items)

        constructor(player: Player, interfaceId: Int, childId: Int, containerId: Int, items: Array<out Item?>, split: Boolean, vararg slots: Int) :
            this(player, interfaceId, childId, containerId, items, items.size, split, slots = slots)
    }

    class CSConfigContext(
        override val player: Player,
        val id: Int,
        val value: Int,
        val types: String,
        val parameters: Array<out Any?>,
    ) : PacketContext()

    class DisplayModelContext @JvmOverloads constructor(
        override val player: Player,
        val type: ModelType,
        val nodeId: Int,
        val amount: Int,
        val interfaceId: Int,
        val childId: Int,
        val zoom: Int = 0,
    ) : PacketContext() {
        constructor(player: Player, interfaceId: Int, childId: Int) :
            this(player, ModelType.PLAYER, -1, 0, interfaceId, childId)

        constructor(player: Player, nodeId: Int, interfaceId: Int, childId: Int) :
            this(player, ModelType.NPC, nodeId, 0, interfaceId, childId)

        enum class ModelType { PLAYER, NPC, ITEM, MODEL }
    }

    class DynamicSceneContext(override val player: Player, val isLogin: Boolean) : PacketContext()

    class GameMessageContext(override val player: Player, val message: String) : PacketContext()

    class GrandExchangeContext(
        override val player: Player,
        val idx: Byte,
        val state: Byte,
        val itemID: Short,
        val isSell: Boolean,
        val value: Int,
        val amt: Int,
        val completedAmt: Int,
        val totalCoinsExchanged: Int,
    ) : PacketContext()

    class HintIconContext @JvmOverloads constructor(
        override val player: Player,
        val slot: Int,
        val arrowId: Int,
        var targetType: Int,
        target: Node,
        val modelId: Int,
        val height: Int = 0,
    ) : PacketContext() {
        constructor(player: Player, slot: Int, arrowId: Int, target: Node, modelId: Int) :
            this(player, slot, arrowId, targetType(target), target, modelId)

        val index = (target as? Entity)?.index ?: -1
        val location: Location? = if (target is Entity) null else target.location

        companion object {
            private fun targetType(target: Node) = when (target) {
                is Player -> 10
                is Entity -> 1
                else -> 2
            }
        }
    }

    class InteractionOptionContext @JvmOverloads constructor(
        override val player: Player,
        val index: Int,
        val name: String,
        val isRemove: Boolean = false,
    ) : PacketContext()

    class InterfaceConfigContext(
        override val player: Player,
        val interfaceId: Int,
        val childId: Int,
        val isHidden: Boolean,
    ) : PacketContext()

    class InterfaceContext(
        override val player: Player,
        val windowId: Int,
        var componentId: Int,
        val interfaceId: Int,
        val isWalkable: Boolean,
    ) : PacketContext() {
        fun transform(player: Player, id: Int) = InterfaceContext(player, windowId, componentId, id, isWalkable)
    }

    class InterfaceSetAngleContext(
        override val player: Player,
        val pitch: Int,
        val scale: Int,
        val yaw: Int,
        val interfaceId: Int,
        val childId: Int,
    ) : PacketContext()

    class InterfaceSetModelRotationContext(
        override val player: Player,
        val id: Int,
        val componentId: Int,
        val pitch: Int,
        val yaw: Int,
    ) : PacketContext()

    class LocationContext(
        override val player: Player,
        val location: Location,
        val isTeleport: Boolean,
    ) : PacketContext()

    class MessageContext(
        override val player: Player,
        val other: String,
        val chatIcon: Int,
        val opcode: Int,
        val message: String,
    ) : PacketContext() {
        constructor(player: Player, other: Player, opcode: Int, message: String) :
            this(player, other.name, Rights.getChatIcon(other), opcode, message)

        companion object {
            const val SEND_MESSAGE = 71
            const val RECEIVE_MESSAGE = 0
            const val CLAN_MESSAGE = 54
        }
    }

    class MinimapStateContext(override val player: Player, val state: Int) : PacketContext()

    class MusicContext @JvmOverloads constructor(
        override val player: Player,
        val musicId: Int,
        val isSecondary: Boolean = false,
    ) : PacketContext()

    class PositionedGraphicContext(
        override val player: Player,
        val graphic: Graphics,
        val location: Location,
        val offsetX: Int,
        val offsetY: Int,
    ) : PacketContext() {
        val sceneX: Int = location.getSceneX(player.playerFlags.lastSceneGraph)
        val sceneY: Int = location.getSceneY(player.playerFlags.lastSceneGraph)
    }

    class ResetInterfaceContext(override val player: Player, val interfaceId: Int) : PacketContext()

    class RunScriptContext(
        override val player: Player,
        val id: Int,
        val string: String,
        vararg val objects: Any?,
    ) : PacketContext()

    class SceneGraphContext(override val player: Player, val isLogin: Boolean) : PacketContext()

    data class SceneryUpdateContext(
        override val player: Player,
        val objectId: Int,
        val type: Int,
        val rotation: Int,
        val x: Int,
        val y: Int,
        val int0: Int,
        val int1: Int,
        val int2: Int,
        val int3: Int,
        val int4: Int,
        val int5: Int,
        val int6: Int,
    ) : PacketContext()

    class SkillContext(override val player: Player, val skillId: Int) : PacketContext()

    class StringContext(
        override val player: Player,
        val string: String,
        val interfaceId: Int,
        val lineId: Int,
    ) : PacketContext()

    class SystemUpdateContext(override val player: Player, val time: Int) : PacketContext()

    class VarbitContext(override val player: Player, val varbitId: Int, val value: Int) : PacketContext()

    class VarcUpdateContext(override val player: Player, val varcId: Int, val value: Int) : PacketContext()

    class WalkOptionContext(override val player: Player, val option: String) : PacketContext()

    class WindowsPaneContext(override val player: Player, val windowId: Int, val type: Int) : PacketContext()
}
