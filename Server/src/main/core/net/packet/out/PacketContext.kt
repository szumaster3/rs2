package core.net.packet.out

import core.net.packet.ServerProt

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

    class AbsoluteLocAnimation(override val player: Player, val location: Location, val shape: Int, val rotation: Int, val animation: Int) : PacketContext()
    class LocPrefetch(override val player: Player, val id: Int, val shape: Int) : PacketContext()
    class CoordText(override val player: Player, val location: Location, val duration: Int, val height: Int, val colour: Int, val text: String) : PacketContext()
    // Half-tile offsets are relative to the source tile's chunk; heights are wire units.
    class HalfProjectile(override val player: Player, val location: Location, val halfX: Int, val halfY: Int, val dx: Int, val dy: Int, val target: Int, val graphic: Int, val sourceHeight: Int, val targetHeight: Int, val start: Int, val end: Int, val peak: Int, val arc: Int, val sourceEntity: Int? = null) : PacketContext()
    class LocMerge(override val player: Player, val location: Location, val id: Int, val shape: Int, val rotation: Int, val entity: Int, val start: Int, val end: Int, val x1: Int, val x2: Int, val y1: Int, val y2: Int) : PacketContext()

    class InterfacePlayerModelContext(override val player: Player, val interfaceId: Int, val childId: Int, val playerIndex: Int, val nameHash: Int) : PacketContext()
    class InterfaceKitModelContext(override val player: Player, val interfaceId: Int, val childId: Int, val kit1: Int, val kit2: Int, val kit3: Int) : PacketContext()
    class DirectNpcAnimationContext(override val player: Player, val npcIndex: Int, val sequenceId: Int, val delay: Int) : PacketContext()
    class ClanMemberUpdateContext(override val player: Player, val name: String, val alternateName: String?, val world: Int, val rank: Int, val worldName: String?) : PacketContext()

    class DeleteClientContainerContext(override val player: Player, val containerId: Int, val split: Boolean) : PacketContext()

    class Component24BitValuesContext(override val player: Player, val interfaceId: Int, val firstChild: Int, val values: IntArray) : PacketContext()
    class EntityRenderPriorityContext(override val player: Player, val mode: Int) : PacketContext()

    // Keep mutable input private to the queued packet, independent of caller changes.
    class ClientRandomDataContext(override val player: Player, data: ByteArray) : PacketContext() {
        val data = data.copyOf()
    }
    class ClientSettingsCookieContext(override val player: Player, val settings: String) : PacketContext()
    class OpenUrlContext(override val player: Player, val url: String) : PacketContext()

    class ComponentValueContext(override val player: Player, val interfaceId: Int, val childId: Int, val value: Int) : PacketContext()

    class GlobalSpotAnimationContext(override val player: Player, val target: Int, val id: Int, val delay: Int, val height: Int) : PacketContext()

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
            POSITION(ServerProt.CAM_MOVE_TO), ROTATION(ServerProt.CAM_LOOK_AT), SET(ServerProt.CAM_FORCE_ANGLE), SHAKE(ServerProt.CAM_SHAKE), RESET(ServerProt.CAM_RESET), SMOOTH_RESET(ServerProt.CAM_SMOOTH_RESET)
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
        val targetType: Int,
        private val target: Node,
        val modelId: Int,
        val height: Int = 0,
    ) : PacketContext() {
        constructor(player: Player, slot: Int, arrowId: Int, target: Node, modelId: Int) :
            this(player, slot, arrowId, targetType(target), target, modelId)

        val index = (target as? Entity)?.index ?: -1
        val location: Location? = if (target is Entity) null else target.location

        fun removal() = HintIconContext(player, slot, arrowId, 0, target, modelId, height)

        fun isValid(): Boolean {
            if (slot !in 0..7 || targetType !in listOf(0, 1, 2, 3, 4, 5, 6, 10)) return false
            if (targetType == 0) return true
            if (arrowId !in 0..255 || modelId !in -1..65534) return false
            return when (targetType) {
                1 -> target is Entity && target !is Player && index in 0..32766
                10 -> target is Player && index in 1..2047
                else -> location?.let { it.z in 0..3 && it.x in 0..16383 && it.y in 0..16383 && height in 0..255 } ?: false
            }
        }

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

    class MoveInterfaceContext(override val player: Player, val source: Int, val destination: Int) : PacketContext()

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
        val kind: Kind,
        val message: String,
    ) : PacketContext() {
        constructor(player: Player, other: Player, kind: Kind, message: String) :
            this(player, other.name, Rights.getChatIcon(other), kind, message)

        enum class Kind { PRIVATE_ECHO, PRIVATE_RECEIVED, CLAN }
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

    class VarcStringUpdateContext(override val player: Player, val varcId: Int, val value: String) : PacketContext()

    class WalkOptionContext(override val player: Player, val option: String) : PacketContext()

    class WindowsPaneContext(override val player: Player, val windowId: Int, val type: Int) : PacketContext()
}
