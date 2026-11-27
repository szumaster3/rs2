package core.net.packet.out

import core.cache.Cache
import core.cache.def.impl.VarpDefinition
import core.cache.def.impl.VarbitDefinition
import core.game.container.ContainerEvent
import core.game.node.entity.player.Player
import core.game.node.entity.player.info.Rights
import core.game.node.entity.player.link.audio.Audio
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import core.game.node.scenery.Scenery
import core.game.world.map.Location
import core.game.world.map.RegionChunk
import core.game.world.map.RegionManager.forId
import core.game.world.map.build.DynamicRegion
import core.game.world.update.flag.context.Animation
import core.net.packet.IoBuffer
import core.net.packet.PacketHeader
import core.net.packet.out.PacketContext.AccessMaskContext
import core.net.packet.out.PacketContext.AnimateInterfaceContext
import core.net.packet.out.PacketContext.AnimateObjectContext
import core.net.packet.out.PacketContext.AreaPositionContext
import core.net.packet.out.PacketContext.AudioContext
import core.net.packet.out.PacketContext.BuildItemContext
import core.net.packet.out.PacketContext.BuildSceneryContext
import core.net.packet.out.PacketContext.CSConfigContext
import core.net.packet.out.PacketContext.CameraContext
import core.net.packet.out.PacketContext.CameraContext.CameraType
import core.net.packet.out.PacketContext.ChildPositionContext
import core.net.packet.out.PacketContext.ClanContext
import core.net.packet.out.PacketContext.ClearChunkContext
import core.net.packet.out.PacketContext.ConfigContext
import core.net.packet.out.PacketContext.ContactContext
import core.net.packet.out.PacketContext.ContainerContext
import core.net.packet.out.PacketContext.DisplayModelContext
import core.net.packet.out.PacketContext.DynamicSceneContext
import core.net.packet.out.PacketContext.GameMessageContext
import core.net.packet.out.PacketContext.GrandExchangeContext
import core.net.packet.out.PacketContext.HintIconContext
import core.net.packet.out.PacketContext.InteractionOptionContext
import core.net.packet.out.PacketContext.InterfaceConfigContext
import core.net.packet.out.PacketContext.InterfaceContext
import core.net.packet.out.PacketContext.InterfaceSetAngleContext
import core.net.packet.out.PacketContext.InterfaceSetModelRotationContext
import core.net.packet.out.PacketContext.MessageContext
import core.net.packet.out.PacketContext.MinimapStateContext
import core.net.packet.out.PacketContext.MusicContext
import core.net.packet.out.PacketContext.PlayerContext
import core.net.packet.out.PacketContext.PositionedGraphicContext
import core.net.packet.out.PacketContext.ResetInterfaceContext
import core.net.packet.out.PacketContext.RunScriptContext
import core.net.packet.out.PacketContext.SceneGraphContext
import core.net.packet.out.PacketContext.SkillContext
import core.net.packet.out.PacketContext.StringContext
import core.net.packet.out.PacketContext.SystemUpdateContext
import core.net.packet.out.PacketContext.VarbitContext
import core.net.packet.out.PacketContext.VarcUpdateContext
import core.net.packet.out.PacketContext.WalkOptionContext
import core.net.packet.out.PacketContext.WindowsPaneContext
import core.tools.StringUtils.encryptPlayerChat
import core.tools.StringUtils.stringToLong
import java.net.Inet4Address
import java.nio.ByteBuffer
import java.util.Random
import kotlin.math.ceil

private inline fun <reified T : PacketContext> encode(crossinline writer: (T) -> Unit): (PacketContext) -> Unit = { context ->
    require(context is T) { "Expected ${T::class.java.simpleName}, got ${context.javaClass.simpleName}." }
    writer(context)
}

@Suppress("UNINITIALIZED_ENUM_COMPANION", "UNINITIALIZED_ENUM_COMPANION_WARNING")
enum class Encoders530(private val writer: (PacketContext) -> Unit, internal val immediate: Boolean = false) {
    LOC_ANIM_ABSOLUTE(encode<PacketContext.AbsoluteLocAnimation> { c ->
        val l = c.location
        if (l.x !in 0..16383 || l.y !in 0..16383 || l.z !in 0..3 || c.shape !in 0..22 || c.rotation !in 0..3 || c.animation !in -1..65534) return@encode
        c.player.session.write(IoBuffer(16).putA((c.shape shl 2) or c.rotation)
            .putIntB((l.z shl 28) or (l.x shl 14) or l.y).putLEShortA(c.animation))
    }),
    LOC_PREFETCH(encode<PacketContext.LocPrefetch> { c ->
        if (c.id !in 0..65534 || c.shape !in 0..22) return@encode
        c.player.session.write(ZoneProt.LOC_PREFETCH.start(null).putShort(c.id).put(c.shape))
    }),
    TEXT_COORD(encode<PacketContext.CoordText> { c ->
        if (c.duration !in 0..65535 || c.height !in 0..255 || c.colour !in 0..0xffffff || c.text.contains('\u0000')) return@encode
        val bytes = c.text.toByteArray(charset("windows-1252"))
        if (bytes.size > 246 || !charset("windows-1252").newEncoder().canEncode(c.text)) return@encode
        val b = checkedZone(c.player, c.location) ?: return@encode
        c.player.session.write(ZoneProt.TEXT_COORD.start(b).put(0).put(tileHash(c.location))
            .putShort(c.duration).put(c.height).putTri(c.colour).put(bytes).put(0))
    }),
    MAP_PROJANIM_HALF(encode<PacketContext.HalfProjectile> { c ->
        if (c.halfX !in 0..15 || c.halfY !in 0..15 || c.dx !in -128..127 || c.dy !in -128..127 || c.target !in -32768..32767 || c.graphic !in 0..65534 || c.targetHeight !in 0..255 || c.start !in 0..65535 || c.end !in c.start..65535 || c.peak !in -1..254 || c.arc !in 0..255) return@encode
        val source = c.sourceEntity
        if (source == null && c.sourceHeight !in 0..255 || source != null && (source !in -32767..32767 || c.sourceHeight !in -128..127)) return@encode
        val b = checkedZone(c.player, c.location) ?: return@encode
        val scene = c.player.playerFlags.lastSceneGraph
        val base = c.location.chunkBase
        val sx = base.getSceneX(scene) * 2 + c.halfX
        val sy = base.getSceneY(scene) * 2 + c.halfY
        if (sx + c.dx !in 0..207 || sy + c.dy !in 0..207) return@encode
        val zone = if (source == null) ZoneProt.MAP_PROJANIM_HALFSQ else ZoneProt.MAP_PROJANIM_SPECIFIC
        zone.start(b).put((c.halfX shl 4) or c.halfY).put(c.dx).put(c.dy)
        if (source != null) b.putShort(source)
        b.putShort(c.target).putShort(c.graphic).put(c.sourceHeight).put(c.targetHeight)
            .putShort(c.start).putShort(c.end).put(c.peak).put(c.arc)
        c.player.session.write(b)
    }),
    LOC_MERGE(encode<PacketContext.LocMerge> { c ->
        if (c.id !in 0..65534 || c.shape !in 0..22 || c.rotation !in 0..3 || c.entity == 0 || c.entity !in -32768..32767 || c.start !in 0..65535 || c.end !in c.start..65535 || listOf(c.x1,c.x2,c.y1,c.y2).any { it !in -128..127 }) return@encode
        val b = checkedZone(c.player, c.location) ?: return@encode
        c.player.session.write(ZoneProt.LOC_MERGE.start(b).putC(c.y2).put(tileHash(c.location))
            .putLEShort(c.entity).putS(c.x2).putLEShortA(c.end).putLEShortA(c.start)
            .putC(c.y1).putC(c.x1).putA((c.shape shl 2) or c.rotation).putShort(c.id))
    }),
    RESET_ENTITY_ANIMATIONS(encode<PlayerContext> { context ->
        context.player.session.write(Encoders578.resetEntityAnimations())
    }),
    RESET_CLIENT_VARPS(encode<PlayerContext> { context ->
        context.player.session.write(Encoders578.resetClientVarps())
    }),
    RUN_DIALOG_ABORT_HOOKS(encode<PlayerContext> { context ->
        context.player.session.write(Encoders578.runDialogAbortHooks())
    }),
    // Session and status
    LOGIN_PACKET(encode<PlayerContext> { context ->
        val p = context.player
        val buffer = ByteBuffer.allocate(9)
        val right =
            if (context.player.details.rights === Rights.PLAYER_MODERATOR) 1 else if (context.player.details.rights === Rights.ADMINISTRATOR) 2 else 0
        buffer.put((right).toByte())
        buffer.put(0.toByte()) // Something with client scripts, maybe login.
        buffer.put(0.toByte()) // No idea.
        buffer.put(0.toByte()) // No idea.
        buffer.put(1.toByte()) // Boolean, possibly members.
        buffer.putShort(p.index.toShort())
        buffer.put(1.toByte()) // No idea. (something with client scripts, again).
        p.details.session.write(buffer)
    }),
    LAST_LOGIN_INFO(encode<PlayerContext> { context ->
        val buffer = IoBuffer(68)
        val ip = context.player.details.accountInfo.lastUsedIp.ifEmpty { context.player.details.ipAddress }
        val octets = ip.split('.').map { it.toIntOrNull() }
        buffer.putInt(if (octets.size == 4 && octets.all { it != null && it in 0..255 })
            octets.fold(0) { acc, value -> (acc shl 8) or value!! } else 0)
        context.player.session.write(buffer)
    }),
    LOGOUT(encode<PlayerContext> { context ->
        val buffer = Encoders578.logout()
        context.player.details.session.write(buffer)
    }, immediate = true),
    PING_PACKET(encode<PlayerContext> {
        // TODO: Identify the ping opcode for revision 530.
    }),
    CLEAR_MINIMAP_FLAG(encode<PlayerContext> { context ->
        val buffer = Encoders578.clearMinimapFlag()
        context.player.details.session.write(buffer)
    }),
    MINIMAP_STATE(encode<MinimapStateContext> { context ->
        val buffer = Encoders578.minimapState(context.state)
        context.player.details.session.write(buffer)
    }),
    SYSTEM_UPDATE_PACKET(encode<SystemUpdateContext> { context ->
        val buffer = IoBuffer(8).putShort(context.time)
        context.player.details.session.write(buffer)
    }),
    RUN_ENERGY(encode<PlayerContext> { context ->
        val buffer = IoBuffer(4)
        buffer.put(context.player.settings.runEnergy.toInt().toByte().toInt())
        context.player.details.session.write(buffer)
    }),
    WEIGHT_UPDATE(encode<PlayerContext> { context ->
        val buffer = IoBuffer(7)
        buffer.putShort(context.player.settings.weight.toInt())
        context.player.details.session.write(buffer)
    }),
    SKILL_LEVEL(encode<SkillContext> { context ->
        val buffer = IoBuffer(20)
        val skills = context.player.getSkills()
        buffer.putInt(skills.getExperience(context.skillId).toInt())
        buffer.putC(context.skillId)
        if (context.skillId == Skills.PRAYER) {
            buffer.putS(ceil(skills.prayerPoints).toInt())
        } else if (context.skillId == Skills.HITPOINTS) {
            buffer.putS(skills.lifepoints)
        } else {
            buffer.putS(skills.getLevel(context.skillId, true))
        }
        context.player.details.session.write(buffer)
    }),

    // Interfaces
    WINDOWS_PANE(encode<WindowsPaneContext> { context ->
        val buffer = Encoders578.openTop(context.type, context.windowId, context.player.interfaceManager.getPacketCount(1))
        context.player.details.session.write(buffer)
    }),
    INTERFACE(encode<InterfaceContext> { context ->
        if (!validComponent578(context.windowId, context.componentId)) return@encode
        val buffer = Encoders578.openSub(context.windowId shl 16 or context.componentId,
            if (context.isWalkable) 3 else 0, context.interfaceId, context.player.interfaceManager.getPacketCount(1))
        context.player.details.session.write(buffer)
    }),
    MOVE_INTERFACE(encode<PacketContext.MoveInterfaceContext> { context ->
        val buffer = IoBuffer(66).putIntB(context.source).putIntB(context.destination)
            .putLEShort(context.player.interfaceManager.getPacketCount(1))
        context.player.session.write(buffer)
    }),
    CLOSE_INTERFACE(encode<InterfaceContext> { context ->
        val buffer = IoBuffer(28)
        buffer.putShort(context.player.interfaceManager.getPacketCount(1))
        buffer.putShort(context.windowId)
        buffer.putShort(context.componentId)
        context.player.session.write(buffer)
    }),
    RESET_INTERFACE(encode<ResetInterfaceContext> { context ->
        System.err.println("[Protocol578] Unsupported RESET_INTERFACE for interface ${context.interfaceId}")
    }),
    ACCESS_MASK(encode<AccessMaskContext> { context ->
        if (!validComponent578(context.interfaceId, context.childId)) return@encode
        val buffer = Encoders578.settings(context.interfaceId shl 16 or context.childId,
            context.offset, context.length, context.id, context.player.interfaceManager.getPacketCount(1))
        context.player.session.write(buffer)
    }),
    WIDGETSTRUCT_SETTING(encode<AccessMaskContext> { context ->
        if (!validComponent578(context.interfaceId, context.childId)) return@encode
        context.player.session.write(
            Encoders578.targetParam(context.interfaceId shl 16 or context.childId,
            context.offset, context.length, context.id, context.player.interfaceManager.getPacketCount(1)))
    }),
    INTERFACE_CONFIG(encode<InterfaceConfigContext> { context ->
        if (context.interfaceId !in 0..65535 || context.childId !in 0..65535 ||
            Cache.getIndexes()[3]?.getFileData(context.interfaceId, context.childId) == null) {
            System.err.println("[Protocol578] Rejected INTERFACE_CONFIG for missing component ${context.interfaceId}:${context.childId}")
            return@encode
        }
        val buffer = IoBuffer(97)
        buffer.putLEShortA(context.player.interfaceManager.getPacketCount(1))
        buffer.putLEInt(context.interfaceId shl 16 or context.childId)
        buffer.putS(if (context.isHidden) 1 else 0)
        context.player.session.write(buffer)
    }),
    ANIMATE_INTERFACE(encode<AnimateInterfaceContext> { context ->
        if (!validComponent578(context.interfaceId, context.childId)) return@encode
        val buffer = IoBuffer(9).putInt(context.interfaceId shl 16 or context.childId)
            .putLEShort(context.player.interfaceManager.getPacketCount(1)).putLEShortA(context.animationId)
        context.player.session.write(buffer)
    }),
    DISPLAY_MODEL(encode<DisplayModelContext> { context ->
        if (!validComponent578(context.interfaceId, context.childId)) return@encode
        val hash = context.interfaceId shl 16 or context.childId
        val verify = context.player.interfaceManager.getPacketCount(1)
        val buffer = when (context.type) {
            DisplayModelContext.ModelType.PLAYER -> IoBuffer(89).putLEInt(hash).putLEShortA(verify)
            DisplayModelContext.ModelType.NPC -> IoBuffer(95).putIntA(hash).putShortA(context.nodeId).putLEShortA(verify)
            DisplayModelContext.ModelType.ITEM -> IoBuffer(54).putLEShortA(verify).putLEShort(context.nodeId)
                .putIntB(if (context.amount > 0) context.amount else context.zoom).putIntA(hash)
            DisplayModelContext.ModelType.MODEL -> IoBuffer(81).putIntA(hash).putLEShortA(verify).putShort(context.nodeId)
        }
        context.player.session.write(buffer)
    }),
    STRING_PACKET(encode<StringContext> { context ->
        if (context.interfaceId !in 0..65535 || context.lineId !in 0..65535 ||
            Cache.getIndexes()[3]?.getFileData(context.interfaceId, context.lineId) == null) {
            System.err.println("[Protocol578] Rejected STRING_PACKET for missing component ${context.interfaceId}:${context.lineId}")
            return@encode
        }
        val buffer = IoBuffer(60, PacketHeader.SHORT)
        buffer.putString(context.string)
        buffer.putLEShort(context.player.interfaceManager.getPacketCount(1))
        buffer.putIntA((context.interfaceId shl 16) or context.lineId)
        context.player.details.session.write(buffer)
    }),
    REPOSITION_CHILD(encode<ChildPositionContext> { context ->
        if (!validComponent578(context.interfaceId, context.childId)) return@encode
        val buffer = IoBuffer(86).putLEInt(context.interfaceId shl 16 or context.childId)
            .putLEShort(context.player.interfaceManager.getPacketCount(1))
            .putLEShort(context.position.y).putShortA(context.position.x)
        context.player.session.write(buffer)
    }),
    INTERFACE_SET_ANGLE(encode<InterfaceSetAngleContext> { context ->
        if (!validComponent578(context.interfaceId, context.childId)) return@encode
        val buffer = IoBuffer(57).putIntB(context.interfaceId shl 16 or context.childId)
            .putLEShortA(context.player.interfaceManager.getPacketCount(1))
            .putShortA(context.scale).putShortA(context.pitch).putShortA(context.yaw)
        context.player.session.write(buffer)
    }),
    INTERFACE_SET_MODEL_ROTATION(encode<InterfaceSetModelRotationContext> { context ->
        // The legacy opcode 207 is outside the revision 578 protocol.
        // No equivalent for independent continuous model rotation has been verified.
        System.err.println("[Protocol578] Unsupported INTERFACE_SET_MODEL_ROTATION for ${context.id}:${context.componentId}")
    }),
    RUN_SCRIPT_PACKET(encode<RunScriptContext> { context ->
        val types = context.string
        val objects = context.objects
        val buffer = IoBuffer(87, PacketHeader.SHORT)
        buffer.putShort(context.player.interfaceManager.getPacketCount(1))

        buffer.putString(types)
        for ((j, i) in ((types.length - 1) downTo 0).withIndex()) {
            if (types[i] == 's') {
                buffer.putString(objects[j] as String)
            } else {
                buffer.putInt((objects[j] as Int))
            }
        }

        buffer.putInt(context.id)
        context.player.details.session.write(buffer)
    }),

    // Variables and containers
    CONFIG(encode<ConfigContext> { context ->
        if (!VarpDefinition.exists(context.id)) {
            System.err.println("[Protocol578] Rejected unknown varp ${context.id}, value=${context.value}")
            return@encode
        }
        val buffer: IoBuffer
        if (context.value < Byte.MIN_VALUE || context.value > Byte.MAX_VALUE) {
            buffer = IoBuffer(73)
            buffer.putLEShortA(context.id)
            buffer.putLEInt(context.value)
        } else {
            buffer = IoBuffer(29)
            buffer.putC(context.value)
            buffer.putShort(context.id)
        }
        if (!context.player.isArtificial) {
            context.player.details.session.write(buffer)
        }
    }),
    CS_CONFIG_PACKET(encode<CSConfigContext> { context ->
        val buffer = Encoders578.varc(context.id, context.value, context.player.interfaceManager.getPacketCount(1))
        context.player.details.session.write(buffer)
    }),
    VARBIT(encode<VarbitContext> { context ->
        if (!VarbitDefinition.exists(context.varbitId)) {
            System.err.println("[Protocol578] Rejected unknown varbit ${context.varbitId}, value=${context.value}")
            return@encode
        }
        val buffer: IoBuffer
        if (context.value < 0 || context.value > 255) {
            buffer = IoBuffer(47)
            buffer.putLEInt(context.value)
        } else {
            buffer = IoBuffer(64)
            buffer.put(context.value)
        }
        buffer.putLEShort(context.varbitId)
        context.player.session.write(buffer)
    }),
    VARC_UPDATE(encode<VarcUpdateContext> { context ->
        val player = context.player
        player.session.write(Encoders578.varc(context.varcId, context.value, player.interfaceManager.getPacketCount(1)))
    }),
    CONTAINER_PACKET(encode<ContainerContext> { context ->
        // Containers in 578 are addressed by container key, not widget hash.
        if (context.isClear) {
            System.err.println("[578] Unmapped widget-only container clear: ${context.interfaceId}:${context.childId}")
            return@encode
        }
        val slots = context.slots
        val buffer = IoBuffer(if (slots != null) 100 else 69, PacketHeader.SHORT)
        buffer.putShort(context.containerId).put(if (context.isSplit) 1 else 0)
        if (slots != null) {
            for (slot in slots) {
                val item = context.items!![slot]
                buffer.putSmart(slot)
                if (item == null || item == ContainerEvent.NULL_ITEM) {
                    buffer.putShort(0)
                } else {
                    buffer.putShort(item.id + 1)
                    if (item.amount > 254 || item.amount < 0) buffer.put(255).putInt(item.amount)
                    else buffer.put(item.amount)
                }
            }
        } else {
            val count = context.length
            buffer.putShort(count)
            for (slot in 0 until count) {
                val item = context.items?.getOrNull(slot)
                val id = context.ids?.getOrNull(slot) ?: item?.id ?: -1
                val amount = if (context.ids != null && id >= 0) 1 else item?.amount ?: 0
                buffer.putLEShort(id + 1)
                if (amount > 254 || amount < 0) buffer.put(255).putIntB(amount)
                else buffer.put(amount)
            }
        }
        context.player.session.write(buffer)
    }),
    GRAND_EXCHANGE_PACKET(encode<GrandExchangeContext> { context ->
        val buffer = IoBuffer(71, PacketHeader.NORMAL)
        buffer.put(context.idx.toInt())
        if (context.state.toInt() == GE_REMOVED) {
            buffer.put(0).putShort(0).putInt(0).putInt(0).putInt(0).putInt(0)
        } else {
            var state = (context.state + 1).toByte()
            if (context.isSell) {
                state = (state + 8).toByte()
            }
            if (context.state.toInt() == GE_ABORTED) {
                state = if (context.isSell) (-3.toByte()).toByte() else 5.toByte()
            }
            buffer.put(state.toInt()).putShort(context.itemID.toInt()).putInt(context.value).putInt(context.amt).putInt(context.completedAmt).putInt(context.totalCoinsExchanged)
        }
        try {
            context.player.session.write(buffer)
        } catch (e: Exception) {
        }
    }),

    // Scene and world updates
    UPDATE_SCENE_GRAPH(encode<SceneGraphContext> { context ->
        val player = context.player
        player.playerFlags.lastSceneGraph = player.location
        player.playerFlags.isDynamicScene = false
        val buffer = IoBuffer(98, PacketHeader.SHORT)
        LoginWorldEncoder.appendRegion(buffer.toByteBuffer(), player.location)
        player.details.session.write(buffer)
    }, immediate = true),
    BUILD_DYNAMIC_SCENE(encode<DynamicSceneContext> { context ->
        val player = context.player
        val chunks = Array(4) { Array(13) { arrayOfNulls<RegionChunk>(13) } }
        val baseX = player.location.regionX - 6
        val baseY = player.location.regionY - 6
        for (z in 0..3) {
            for (x in baseX..player.location.regionX + 6) {
                for (y in baseY..player.location.regionY + 6) {
                    if (x !in 0..2047 || y !in 0..2047) continue
                    val dr = forId((x shr 3) shl 8 or (y shr 3))
                    if (dr is DynamicRegion) {
                        chunks[z][x - baseX][y - baseY] = dr.chunks[z][x - (dr.x shl 3)][y - (dr.y shl 3)]
                    }
                }
            }
        }
        context.player.session.write(LoginWorldEncoder.dynamicRegion(player.location, chunks))
        player.playerFlags.lastSceneGraph = player.location
        player.playerFlags.isDynamicScene = true
    }, immediate = true),
    UPDATE_AREA_POSITION(encode<AreaPositionContext> { context ->
        val scene = context.player.playerFlags.lastSceneGraph
        val l = context.location
        val buffer = IoBuffer(41).putA(l.z).putC(l.getSceneY(scene)).putS(l.getSceneX(scene))
        context.player.session.write(buffer)
    }),
    CLEAR_REGION_CHUNK(encode<ClearChunkContext> { context ->
        val l = context.player.playerFlags.lastSceneGraph
        val x = context.chunk.currentBase.getSceneX(l)
        val y = context.chunk.currentBase.getSceneY(l)
        if (x >= 0 && y >= 0 && x < 104 && y < 104) {
            val buffer = IoBuffer(42).put(x).putC(y).putA(context.chunk.currentBase.z)
            context.player.session.write(buffer)
        }
    }, immediate = true),
    CONSTRUCT_SCENERY(encode<BuildSceneryContext> { context ->
        val player = context.player
        val o = context.scenery
        val buffer = writeSceneryAddition(checkedZone(player, o.location) ?: return@encode, o)
        player.session.write(buffer)
    }),
    CLEAR_SCENERY(encode<BuildSceneryContext> { context ->
        val player = context.player
        val scenery = context.scenery
        val buffer = writeSceneryRemoval(checkedZone(player, scenery.location) ?: return@encode, scenery)
        player.session.write(buffer)
    }),
    CONSTRUCT_GROUND_ITEM(encode<BuildItemContext> { context ->
        val player = context.player
        val item = context.item
        val buffer = writeGroundItemAddition(checkedZone(player, item.location) ?: return@encode, item)
        player.details.session.write(buffer)
    }),
    CLEAR_GROUND_ITEM(encode<BuildItemContext> { context ->
        val player = context.player
        val item = context.item
        val buffer = writeGroundItemRemoval(checkedZone(player, item.location) ?: return@encode, item)
        player.session.write(buffer)
    }),
    UPDATE_GROUND_ITEM_AMOUNT(encode<BuildItemContext> { context ->
        val player = context.player
        val item = context.item
        val buffer = writeGroundItemAmount(checkedZone(player, item.location) ?: return@encode, item, context.oldAmount)
        player.details.session.write(buffer)
    }),
    ANIMATE_OBJECT_PACKET(encode<AnimateObjectContext> { context ->
        val player = context.player
        val `object` = context.animation.getObject()
        val buffer = writeObjectAnimation(checkedZone(player, `object`.location) ?: return@encode, context.animation)
        player.session.write(buffer)
    }),
    POSITIONED_GRAPHIC(encode<PositionedGraphicContext> { context ->
        val g = context.graphic
        val target = context.location.transform(context.offsetX, context.offsetY, 0)
        val offsetHash = (target.chunkOffsetX shl 4) or target.chunkOffsetY
        val scene = context.player.playerFlags.lastSceneGraph
        val base = target.chunkBase
        val x = base.getSceneX(scene)
        val y = base.getSceneY(scene)
        if (x !in 0..96 || y !in 0..96 || target.z != context.player.location.z) return@encode
        val buffer = ZoneProt.MAP_ANIM.start(ZoneProt.enclosed(x, y, target.z))
            .put(offsetHash)
            .putShort(g.id)
            .put(g.height)
            .putShort(g.delay)
        context.player.session.write(buffer)
    }),
    HINT_ICON(encode<HintIconContext> { context ->
        if (context.slot !in 0..7 || context.targetType !in listOf(0, 1, 2, 3, 4, 5, 6, 10)) return@encode
        if (context.targetType != 0) {
            if (context.arrowId !in 0..255 || context.modelId !in -1..65534) return@encode
            when (context.targetType) {
                1 -> if (context.index !in 0..32766) return@encode
                10 -> if (context.index !in 1..2047) return@encode
                else -> {
                    val loc = context.location ?: return@encode
                    if (loc.z !in 0..3 || loc.x !in 0..16383 || loc.y !in 0..16383 || context.height !in 0..255) return@encode
                }
            }
        }
        val buffer = IoBuffer(102)
        buffer.put((context.slot shl 5) or context.targetType)
        if (context.targetType == 0) {
            repeat(11) { buffer.put(0) }
        } else {
            buffer.put(context.arrowId)
            if (context.targetType == 1 || context.targetType == 10) {
                buffer.putShort(context.index)
                repeat(6) { buffer.put(0) }
            } else {
                val loc = context.location ?: return@encode
                buffer.put(loc.z).putShort(loc.x).putShort(loc.y).put(context.height).putShort(600)
            }
            buffer.putShort(context.modelId)
        }
        context.player.session.write(buffer)
    }),
    INTERACTION_OPTION(encode<InteractionOptionContext> { context ->
        val buffer = Encoders578.interaction(if (context.isRemove) "null" else context.name,
            context.index + 1, !context.isRemove && context.index == 0)
        context.player.session.write(buffer)
    }),
    SET_WALK_OPTION(encode<WalkOptionContext> { context ->
        if (context.option.length > 254 || context.option.contains('\u0000')) return@encode
        context.player.session.write(Encoders578.walkOption(context.option))
    }),

    // Camera and audio
    CAMERA_VIEW_PACKET(encode<CameraContext> { context ->
        val type = context.type
        val buffer = IoBuffer(type.opcode)
        val l = Location.create(context.x, context.y, 0)
        val p = context.player
        when (type) {
            CameraType.ROTATION,
            CameraType.POSITION -> {
                val x = l.getSceneX(p.playerFlags.lastSceneGraph)
                val y = l.getSceneY(p.playerFlags.lastSceneGraph)
                if (x !in 0..255 || y !in 0..255 || context.height !in 0..65535 ||
                    context.speed !in 0..255 || context.zoomSpeed !in 0..255) return@encode
                buffer.putShort(context.player.interfaceManager.getPacketCount(1))
                buffer.put(x).put(y).putShort(context.height).put(context.speed).put(context.zoomSpeed)
            }

            CameraType.SET -> {
                // Opcode 14 is forceAngle, not a world-coordinate camera move.
                if (context.x !in 128..384 || context.y !in 0..2047) return@encode
                buffer.putShort(context.x)
                    .putShortA(context.player.interfaceManager.getPacketCount(1)).putShortA(context.y)
            }

            CameraType.SHAKE -> {
                if (context.x !in 0..4) return@encode
                buffer.putShort(context.player.interfaceManager.getPacketCount(1))
                buffer.put(context.x).put(context.y).put(context.height).put(context.speed).putShort(context.zoomSpeed)
            }

            CameraType.RESET, CameraType.SMOOTH_RESET -> buffer.putShort(context.player.interfaceManager.getPacketCount(1))
        }
        p.session.write(buffer)
    }),
    MUSIC_PACKET(encode<MusicContext> { context ->
        val buffer = if (context.isSecondary) {
            IoBuffer(62).putA(255).putTri(0).putShort(context.musicId)
        } else {
            IoBuffer(61).putS(2).putLEShort(context.musicId).putS(255)
        }
        context.player.details.session.write(buffer)
    }),
    AUDIO_PACKET(encode<AudioContext> { context ->
        val audio = context.audio
        val loc = context.location
        val buffer = if (loc == null) {
            IoBuffer(92)
        } else {
            checkedZone(context.player, loc) ?: return@encode
        }
        writeAudio(buffer, audio, loc)
        context.player.session.write(buffer)
    }),

    // Messages and contacts
    GAME_MESSAGE(encode<GameMessageContext> { context ->
        val buffer = Encoders578.message(context.message, (System.currentTimeMillis() / 1000).toInt())
        context.player.session.write(buffer)
    }),
    COMMUNICATION_MESSAGE(encode<MessageContext> { context ->
        val player = context.player
        val other = context.other
        val plain = context.message.toByteArray(charset("windows-1252"))
        val bytes = ByteArray(plain.size * 4 + 8)
        val length = encryptPlayerChat(bytes, 0, 0, plain.size, plain)
        if (length < 0) return@encode
        val buffer = when (context.opcode) {
            MessageContext.SEND_MESSAGE -> IoBuffer(76, PacketHeader.BYTE).putString(other)
            MessageContext.RECEIVE_MESSAGE -> {
                player.setAttribute("replyTo", other)
                IoBuffer(55, PacketHeader.BYTE).put(0).putString(other)
                    .putShort(Random().nextInt(0xFFFF)).putTri(getMessageIndex(player)).put(context.chatIcon)
            }
            MessageContext.CLAN_MESSAGE -> {
                val clan = player.communication.clan ?: return@encode
                IoBuffer(84, PacketHeader.BYTE).put(0).putString(other).putLong(stringToLong(clan.name))
                    .putShort(Random().nextInt(0xFFFF)).putTri(getMessageIndex(player)).put(context.chatIcon)
            }
            else -> return@encode
        }
        buffer.putSmart(plain.size).putBytes(bytes, 0, length)
        player.session.write(buffer)
    }),
    CONTACT_PACKETS(encode<ContactContext> { context ->
        val player = context.player
        val buffer = when (context.type) {
            ContactContext.UPDATE_STATE_TYPE -> IoBuffer(19)
            ContactContext.IGNORE_LIST_TYPE -> {
                val payload = IoBuffer(6, PacketHeader.SHORT)
                val blocked = player.communication.blocked.filter { it.isNotEmpty() }.take(100)
                payload.put(blocked.size)
                for (name in blocked) {
                    payload.putString(name)
                    payload.putString("")
                    payload.putString(name)
                    payload.putString("")
                }
                payload
            }
            ContactContext.UPDATE_FRIEND_TYPE -> {
                val name = context.name ?: return@encode
                val payload = IoBuffer(70, PacketHeader.SHORT)
                payload.put(0)
                payload.putString(name)
                payload.putString(name)
                payload.putShort(context.worldId)
                payload.put(player.communication.contacts[name]?.rank?.value ?: 0)
                if (context.worldId > 0) {
                    payload.putString("World " + context.worldId)
                    payload.put(1)
                }
                payload
            }
            else -> return@encode
        }
        player.session.write(buffer)
    }),
    UPDATE_CLAN_CHAT(encode<ClanContext> { context ->
        val buffer = IoBuffer(35, PacketHeader.SHORT)
        val clan = context.clan
        if (!context.isLeave) {
            buffer.putString(clan.owner).put(0)
            buffer.putLong(stringToLong(clan.name))
            buffer.put(clan.kickRequirement.value)
            var size = clan.players.size
            if (size > 100) {
                size = 100
            }
            buffer.put(size)
            for (i in 0 until size) {
                val entry = clan.players[i]
                val name = entry.name
                buffer.putString(name).put(0).putShort(entry.worldId)
                buffer.put(clan.getRank(entry).value)
                buffer.putString("World " + entry.worldId)
            }
        }
        context.player.session.write(buffer)
    });

    internal fun write(context: PacketContext) = writer(context)

    companion object {
        private fun tileHash(l: Location): Int = (l.chunkOffsetX shl 4) or l.chunkOffsetY

        private fun checkedZone(player: Player, location: Location): IoBuffer? {
            val base = location.chunkBase
            val scene = player.playerFlags.lastSceneGraph
            val x = base.getSceneX(scene)
            val y = base.getSceneY(scene)
            if (x !in 0..96 || y !in 0..96 || location.z != player.location.z) return null
            return ZoneProt.enclosed(x, y, location.z)
        }

        private const val GE_REMOVED = 6
        private const val GE_ABORTED = 2

        fun writeObjectAnimation(buffer: IoBuffer, animation: Animation): IoBuffer {
            val `object` = animation.getObject()
            val l = `object`.location
            ZoneProt.LOC_ANIM.start(buffer).putShortA(animation.id)
            buffer.putS((l.chunkOffsetX shl 4) or (l.chunkOffsetY and 0x7))
            buffer.putS((`object`.type shl 2) + (`object`.rotation and 0x3))
            return buffer
        }

        private fun writeAudio(buffer: IoBuffer, audio: Audio, loc: Location?): IoBuffer {
            if (loc == null) {
                buffer.putShort(audio.id)
                buffer.put(audio.loops)
                buffer.putShort(audio.delay)
                buffer.put(255)
            } else {
                ZoneProt.SOUND_AREA.start(buffer)
                buffer.put((loc.chunkOffsetX shl 4) or loc.chunkOffsetY)
                buffer.putShort(audio.id)
                buffer.put((audio.radius shl 4) or (audio.loops and 7))
                buffer.put(audio.delay)
                buffer.put(255)
            }
            return buffer
        }

        @JvmStatic
        fun writeGroundItemRemoval(buffer: IoBuffer, item: Item): IoBuffer {
            val l = item.location
            ZoneProt.OBJ_DEL.start(buffer).putC((l.chunkOffsetX shl 4) or (l.chunkOffsetY and 0x7)).putLEShortA(item.id)
            return buffer
        }

        @JvmStatic
        fun writeSceneryRemoval(buffer: IoBuffer, scenery: Scenery): IoBuffer {
            val location = scenery.location
            return ZoneProt.LOC_DEL.start(buffer).putA((location.chunkOffsetX shl 4) or (location.chunkOffsetY and 0x7))
                .putC((scenery.type shl 2) + (scenery.rotation and 3))
        }

        private fun getMessageIndex(p: Player): Int {
            val count = p.getAttribute("pm_index", 0) + 1
            p.setAttribute("pm_index", count)
            return count
        }

        @JvmStatic
        fun writeGroundItemAddition(buffer: IoBuffer, item: Item): IoBuffer {
            val l = item.location
            ZoneProt.OBJ_ADD.start(buffer).putShort(item.id).putShortA(item.amount)
                .put((l.chunkOffsetX shl 4) or (l.chunkOffsetY and 0x7))
            return buffer
        }

        @JvmStatic
        fun writeSceneryAddition(buffer: IoBuffer, scenery: Scenery): IoBuffer {
            val l = scenery.location
            ZoneProt.LOC_ADD_CHANGE.start(buffer).putC((scenery.type shl 2) or (scenery.rotation and 0x3))
                .putShortA(scenery.id).put((l.chunkOffsetX shl 4) or (l.chunkOffsetY and 0x7))
            return buffer
        }

        @JvmStatic
        fun chunkUpdateBuffer(player: Player, base: Location): IoBuffer {
            val scene = player.playerFlags.lastSceneGraph
            val x = base.getSceneX(scene)
            val y = base.getSceneY(scene)
            return ZoneProt.enclosed(x, y, base.z)
        }

    
        @JvmStatic
        fun writeGroundItemAmount(buffer: IoBuffer, item: Item, oldAmount: Int): IoBuffer {
            val l = item.location
            ZoneProt.OBJ_COUNT.start(buffer).put((l.chunkOffsetX shl 4) or (l.chunkOffsetY and 0x7)).putShort(item.id).putShort(oldAmount).putShort(item.amount)
            return buffer
        }
    }
}

private fun validComponent578(interfaceId: Int, childId: Int): Boolean {
    val valid = interfaceId in 0..65535 && childId in 0..65535 &&
        Cache.getIndexes()[3]?.getFileData(interfaceId, childId) != null
    if (!valid) System.err.println("[Protocol578] Rejected interface update for missing component $interfaceId:$childId")
    return valid
}
