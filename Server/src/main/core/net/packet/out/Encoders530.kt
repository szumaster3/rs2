package core.net.packet.out

import core.game.container.ContainerEvent
import core.game.node.entity.player.Player
import core.game.node.entity.player.info.Rights
import core.game.node.entity.player.link.audio.Audio
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import core.game.node.scenery.Scenery
import core.game.system.config.XteaParser.Companion.getRegionXTEA
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
import core.net.packet.out.PacketContext.LocationContext
import core.net.packet.out.PacketContext.MessageContext
import core.net.packet.out.PacketContext.MinimapStateContext
import core.net.packet.out.PacketContext.MusicContext
import core.net.packet.out.PacketContext.PlayerContext
import core.net.packet.out.PacketContext.PositionedGraphicContext
import core.net.packet.out.PacketContext.ResetInterfaceContext
import core.net.packet.out.PacketContext.RunScriptContext
import core.net.packet.out.PacketContext.SceneGraphContext
import core.net.packet.out.PacketContext.SceneryUpdateContext
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
import shared.consts.Network

private inline fun <reified T : PacketContext> encode(crossinline writer: (T) -> Unit): (PacketContext) -> Unit = { context ->
    require(context is T) { "Expected ${T::class.java.simpleName}, got ${context.javaClass.simpleName}." }
    writer(context)
}

@Suppress("UNINITIALIZED_ENUM_COMPANION", "UNINITIALIZED_ENUM_COMPANION_WARNING")
enum class Encoders530(private val writer: (PacketContext) -> Unit, internal val immediate: Boolean = false) {
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
        val buffer = IoBuffer(164)
        buffer.imp4(Inet4Address.getByName(context.player.details.ipAddress).hashCode())
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        context.player.session.write(buffer)
    }),
    LOGOUT(encode<PlayerContext> { context ->
        val buffer = IoBuffer(Network.LOGOUT_FULL)
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        context.player.details.session.write(buffer)
    }, immediate = true),
    PING_PACKET(encode<PlayerContext> {
        // TODO: Identify the ping opcode for revision 530.
    }),
    CLEAR_MINIMAP_FLAG(encode<PlayerContext> { context ->
        val buffer = IoBuffer(153)
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        context.player.details.session.write(buffer)
    }),
    MINIMAP_STATE(encode<MinimapStateContext> { context ->
        val buffer = IoBuffer(192).put(context.state)
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        context.player.details.session.write(buffer)
    }),
    SYSTEM_UPDATE_PACKET(encode<SystemUpdateContext> { context ->
        val buffer = IoBuffer(85).putShort(context.time)
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        context.player.details.session.write(buffer)
    }),
    RUN_ENERGY(encode<PlayerContext> { context ->
        val buffer = IoBuffer(Network.UPDATE_RUN_ENERGY)
        buffer.put(context.player.settings.runEnergy.toInt().toByte().toInt())
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        context.player.details.session.write(buffer)
    }),
    WEIGHT_UPDATE(encode<PlayerContext> { context ->
        val buffer = IoBuffer(174)
        buffer.putShort(context.player.settings.weight.toInt())
        // TODO context.getPlayer().getSession().write(buffer);
    }),
    SKILL_LEVEL(encode<SkillContext> { context ->
        val buffer = IoBuffer(Network.SEND_SKILL_LEVEL)
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        val skills = context.player.getSkills()
        if (context.skillId == Skills.PRAYER) {
            buffer.putA(ceil(skills.prayerPoints).toInt())
        } else if (context.skillId == Skills.HITPOINTS) {
            buffer.putA(skills.lifepoints)
        } else {
            buffer.putA(skills.getLevel(context.skillId, true))
        }
        buffer.putIntA(skills.getExperience(context.skillId).toInt()).put(context.skillId)
        context.player.details.session.write(buffer)
    }),
    UPDATE_RANDOM_FILE(encode<PlayerContext> {
        val buffer = IoBuffer(211)
        buffer.putInt(1) // Let's assume this is UID.
        buffer.put(0)
        buffer.put(0)
        buffer.put(0)
        buffer.put(0)
        for (i in 0..3) {
            buffer.putInt(i + 100)
        }
        // TODO context.getPlayer().getSession().write(buffer);
    }),

    // Interfaces
    WINDOWS_PANE(encode<WindowsPaneContext> { context ->
        val buffer = IoBuffer(Network.WINDOW_PANE)
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        buffer.putLEShortA(context.windowId)
        buffer.putS(context.type)
        buffer.putLEShortA(context.player.interfaceManager.getPacketCount(1))
        context.player.details.session.write(buffer)
    }),
    INTERFACE(encode<InterfaceContext> { context ->
        val buffer = IoBuffer(Network.SEND_INTERFACE)
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        buffer.put(if (context.isWalkable) 1 else 0)
        buffer.putIntB(context.windowId shl 16 or context.componentId)
            .putShortA(context.player.interfaceManager.getPacketCount(1)).putShort(context.interfaceId)
        context.player.details.session.write(buffer)
    }),
    CLOSE_INTERFACE(encode<InterfaceContext> { context ->
        val buffer = IoBuffer(Network.CLOSE_INTERFACE)
        buffer.putShort(context.player.interfaceManager.getPacketCount(1))
        buffer.putShort(context.windowId)
        buffer.putShort(context.componentId)
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        context.player.session.write(buffer)
    }),
    RESET_INTERFACE(encode<ResetInterfaceContext> { context ->
        val buffer = IoBuffer(Network.CLOSE_INTERFACE)
        buffer.putShort(context.player.interfaceManager.getPacketCount(1))
        buffer.putInt(context.interfaceId)
        context.player.session.write(buffer)
    }),
    ACCESS_MASK(encode<AccessMaskContext> { context ->
        val buffer = IoBuffer(Network.ACCESS_MASK)
        buffer.putLEShort(context.player.interfaceManager.getPacketCount(1))
        buffer.putLEShort(context.length)
        buffer.putInt(context.interfaceId shl 16 or context.childId)
        buffer.putShortA(context.offset)
        buffer.putIntA(context.id)
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        context.player.session.write(buffer)
    }),
    INTERFACE_CONFIG(encode<InterfaceConfigContext> { context ->
        val buffer = IoBuffer(Network.INTERFACE_SCRIPT)
        buffer.putC(if (context.isHidden) 1 else 0)
        buffer.putShort(context.player.interfaceManager.getPacketCount(1))
        buffer.putLEInt(context.interfaceId shl 16 or context.childId)
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        context.player.session.write(buffer)
    }),
    ANIMATE_INTERFACE(encode<AnimateInterfaceContext> { context ->
        val buffer = IoBuffer(Network.ANIMATE_INTERFACE)
        buffer.putIntB((context.interfaceId shl 16) + context.childId)
        buffer.putLEShort(context.animationId)
        buffer.putShortA(context.player.interfaceManager.getPacketCount(1))
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        context.player.details.session.write(buffer)
    }),
    DISPLAY_MODEL(encode<DisplayModelContext> { context ->
        val buffer: IoBuffer
        when (context.type) {
            DisplayModelContext.ModelType.PLAYER -> {
                buffer = IoBuffer(Network.DISPLAY_MODEL_PLAYER)
                buffer.putLEShortA(context.player.interfaceManager.getPacketCount(1))
                buffer.putIntA(context.interfaceId shl 16 or context.childId)
            }

            DisplayModelContext.ModelType.NPC -> {
                buffer = IoBuffer(Network.DISPLAY_MODEL_NPC)
                buffer.putShortA(context.nodeId)
                buffer.putLEInt((context.interfaceId shl 16) or context.childId)
                buffer.putLEShort(context.player.interfaceManager.getPacketCount(1))
            }

            DisplayModelContext.ModelType.ITEM -> {
                val value = if (context.amount > 0) context.amount else context.zoom
                buffer = IoBuffer(Network.DISPLAY_MODEL_ITEM)
                buffer.putInt(value)
                buffer.putIntB((context.interfaceId shl 16) or context.childId)
                buffer.putLEShortA(context.nodeId)
                buffer.putLEShort(context.player.interfaceManager.getPacketCount(1))
            }

            DisplayModelContext.ModelType.MODEL -> {
                buffer = IoBuffer(Network.DISPLAY_MODEL)
                buffer.putLEInt(context.interfaceId shl 16 or context.childId)
                buffer.putLEShortA(context.player.interfaceManager.getPacketCount(1))
                buffer.putShortA(context.nodeId)
            }

        }
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        context.player.session.write(buffer)
    }),
    STRING_PACKET(encode<StringContext> { context ->
        val buffer = IoBuffer(Network.SET_TEXT, PacketHeader.SHORT)
        buffer.putIntB((context.interfaceId shl 16) or context.lineId)
        buffer.putString(context.string)
        buffer.putShortA(context.player.interfaceManager.getPacketCount(1))
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        context.player.details.session.write(buffer)
    }),
    REPOSITION_CHILD(encode<ChildPositionContext> { context ->
        val buffer = IoBuffer(119)
            .putShortA(context.player.interfaceManager.getPacketCount(1))
            .putLEInt(context.interfaceId shl 16 or context.childId)
            .putShort(context.position.x)
            .putShortA(context.position.y)
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        context.player.session.write(buffer)
    }),
    INTERFACE_SET_ANGLE(encode<InterfaceSetAngleContext> { context ->
        val player = context.player
        val buffer = IoBuffer(132)
        buffer.putShort(context.pitch)
        buffer.putShortA(player.interfaceManager.getPacketCount(1))
        buffer.putLEShortA(context.scale)
        buffer.putLEShortA(context.yaw)
        buffer.putInt(context.interfaceId shl 16 or context.childId)
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        context.player.session.write(buffer)
    }),
    INTERFACE_SET_MODEL_ROTATION(encode<InterfaceSetModelRotationContext> { context ->
        val player = context.player
        val buffer = IoBuffer(207)
        val componentHash = (context.id shl 16) or context.componentId
        val track = player.interfaceManager.getPacketCount(1)
        val pitch = context.pitch
        val yaw = context.yaw
        buffer.putIntB(componentHash)
        buffer.putShortA(track)
        buffer.putShort(pitch)
        buffer.putShortA(yaw)
        buffer.cypherOpcode(player.session.isaacPair.output)
        player.details.session.write(buffer)
    }),
    RUN_SCRIPT_PACKET(encode<RunScriptContext> { context ->
        val types = context.string
        val objects = context.objects
        val buffer = IoBuffer(Network.RUN_SCRIPT, PacketHeader.SHORT)
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
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        context.player.details.session.write(buffer)
    }),

    // Variables and containers
    CONFIG(encode<ConfigContext> { context ->
        val buffer: IoBuffer
        if (context.value < Byte.MIN_VALUE || context.value > Byte.MAX_VALUE) {
            buffer = IoBuffer(226)
            buffer.putInt(context.value)
            buffer.putShortA(context.id)
        } else {
            buffer = IoBuffer(60)
            buffer.putShortA(context.id)
            buffer.putC(context.value)
        }
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        if (!context.player.isArtificial) {
            context.player.details.session.write(buffer)
        }
    }),
    CS_CONFIG_PACKET(encode<CSConfigContext> { context ->
        val buffer = IoBuffer(65)
        buffer.putLEShort(context.player.interfaceManager.getPacketCount(1))
        buffer.putC(context.value.toByte().toInt())
        buffer.putLEShortA(context.id)
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        context.player.details.session.write(buffer)
    }),
    VARBIT(encode<VarbitContext> { context ->
        val buffer: IoBuffer
        if (context.value > 255) {
            buffer = IoBuffer(84)
            buffer.putLEInt((128 or context.value) and 255)
        } else {
            buffer = IoBuffer(37)
            buffer.put(128.toByte().toInt() or context.value)
        }
        buffer.putLEShort(context.varbitId)
        context.player.session.write(buffer)
    }),
    VARC_UPDATE(encode<VarcUpdateContext> { context ->
        val player = context.player
        if (context.value <= 255) {
            val buffer = IoBuffer(65)
            buffer.putLEShort(player.interfaceManager.getPacketCount(1))
            buffer.putC(context.value.toByte().toInt())
            buffer.putLEShortA(context.varcId)
            player.session.write(buffer)
        } else {
            val buffer = IoBuffer(69)
            buffer.putLEShortA(player.interfaceManager.getPacketCount(1))
            buffer.putInt(context.value)
            buffer.putShortA(context.varcId)
            player.session.write(buffer)
        }
    }),
    CONTAINER_PACKET(encode<ContainerContext> { context ->
        val buffer: IoBuffer
        if (context.isClear) {
            buffer = IoBuffer(Network.UPDATE_CONTAINER)
            buffer.putIntB(context.interfaceId shl 16 or context.childId)
        } else {
            val slots = context.slots
            buffer = IoBuffer(if (slots != null) 22 else 105, PacketHeader.SHORT)
            buffer.putShort(context.interfaceId)
            buffer.putShort(context.childId)
            buffer.putShort(context.containerId)
            if (slots != null) {
                for (slot in slots) {
                    buffer.putSmart(slot)
                    val item = context.items!![slot]
                    if (item != null && item != ContainerEvent.NULL_ITEM) {
                        buffer.putShort(item.id + 1)
                        val amount = item.amount
                        if (amount < 0 || amount > 254) {
                            buffer.put(255).putInt(amount)
                        } else {
                            buffer.put(amount)
                        }
                    } else {
                        buffer.putShort(0)
                    }
                }
            } else {
                if (context.ids != null) {
                    buffer.p2(context.length)
                    for (i in 0 until context.length) {
                        buffer.putS(1)
                        buffer.p2(context.ids[i] + 1)
                    }
                } else {
                    buffer.putShort(context.items!!.size)
                    for (item in context.items) if (item != null) {
                        val amount = item.amount
                        if (amount < 0 || amount > 254) {
                            buffer.putS(255).putInt(amount)
                        } else {
                            buffer.putS(amount)
                        }
                        buffer.putShort(item.id + 1)
                    } else {
                        buffer.putS(0).putShort(0)
                    }
                }
            }
        }
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        context.player.session.write(buffer)
    }),
    GRAND_EXCHANGE_PACKET(encode<GrandExchangeContext> { context ->
        val buffer = IoBuffer(116, PacketHeader.NORMAL)
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
            buffer.cypherOpcode(context.player.session.isaacPair.output)
            context.player.session.write(buffer)
        } catch (e: Exception) {
        }
    }),

    // Scene and world updates
    UPDATE_SCENE_GRAPH(encode<SceneGraphContext> { context ->
        val buffer = IoBuffer(Network.UPDATE_SCENE_GRAPH, PacketHeader.SHORT)
        val player = context.player
        buffer.cypherOpcode(player.session.isaacPair.output)
        player.playerFlags.lastSceneGraph = player.location
        buffer.putShortA(player.location.sceneX)
        for (regionX in (player.location.regionX - 6) / 8..((player.location.regionX + 6) / 8)) {
            for (regionY in (player.location.regionY - 6) / 8..((player.location.regionY + 6) / 8)) {
                val keys = getRegionXTEA(regionX shl 8 or regionY)
                for (i in 0..3) {
                    if (keys != null) buffer.putIntB(keys[i])
                    else buffer.putIntB(0)
                }
            }
        }

        buffer.putS(player.location.z)
        buffer.putShort(player.location.regionX)
        buffer.putShortA(player.location.regionY)
        buffer.putShortA(player.location.sceneY)
        player.details.session.write(buffer)
    }, immediate = true),
    BUILD_DYNAMIC_SCENE(encode<DynamicSceneContext> { context ->
        val buffer = IoBuffer(214, PacketHeader.SHORT)
        val regionIds: MutableList<Int> = ArrayList(20)
        val player = context.player
        buffer.putLEShortA(player.location.getSceneX())
        buffer.putLEShortA(player.location.regionX)
        buffer.putS(player.location.z)
        buffer.putLEShortA(player.location.getSceneY())
        buffer.setBitAccess()
        val chunks = Array(4) { Array(13) { arrayOfNulls<RegionChunk>(13) } }
        val baseX = player.location.regionX - 6
        val baseY = player.location.regionY - 6
        for (z in 0..3) {
            for (x in baseX..player.location.regionX + 6) {
                for (y in baseY..player.location.regionY + 6) {
                    val dr = forId((x shr 3) shl 8 or (y shr 3))
                    if (dr is DynamicRegion) {
                        chunks[z][x - baseX][y - baseY] = dr.chunks[z][x - (dr.x shl 3)][y - (dr.y shl 3)]
                    }
                }
            }
        }
        for (plane in 0..3) {
            for (offsetX in 0..12) {
                for (offsetY in 0..12) {
                    val c = chunks[plane][offsetX][offsetY]
                    if (c == null || c.base.x < 0 || c.base.y < 0) {
                        buffer.putBits(1, 0)
                        continue
                    }
                    val realRegionX = c.base.regionX
                    val realRegionY = c.base.regionY
                    val realPlane = c.base.z
                    val rotation = c.rotation
                    val id = (realRegionX shr 3) shl 8 or (realRegionY shr 3)
                    if (!regionIds.contains(id)) {
                        regionIds.add(id)
                    }
                    buffer.putBits(1, 1)
                    buffer.putBits(
                        26,
                        (rotation shl 1) or (realPlane shl 24) or (realRegionX shl 14) or (realRegionY shl 3)
                    )
                }
            }
        }
        buffer.setByteAccess()
        for (id in regionIds) {
            val keys = getRegionXTEA(id)
            buffer.putIntB(keys[0]).putIntB(keys[1]).putIntB(keys[2]).putIntB(keys[3])
        }
        buffer.putShort(player.location.regionY)
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        context.player.session.write(buffer)
        player.playerFlags.lastSceneGraph = player.location
    }, immediate = true),
    INSTANCED_LOCATION_UPDATE(encode<LocationContext> { context ->
        val buffer = IoBuffer(110)
        val l = context.location
        val player = context.player
        var flag = l.z shl 1
        if (context.isTeleport) {
            flag = flag or 0x1
        }
        buffer.putS(flag)
        buffer.put(l.getSceneX(player.playerFlags.lastSceneGraph))
        buffer.putA(l.getSceneY(player.playerFlags.lastSceneGraph))
        // TODO player.getSession().write(buffer);
    }, immediate = true),
    UPDATE_AREA_POSITION(encode<AreaPositionContext> { context ->
        val buffer = areaPositionBuffer(context.player, context.location)
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        context.player.session.write(buffer)
    }),
    CLEAR_REGION_CHUNK(encode<ClearChunkContext> { context ->
        val l = context.player.playerFlags.lastSceneGraph
        val x = context.chunk.currentBase.getSceneX(l)
        val y = context.chunk.currentBase.getSceneY(l)
        if (x >= 0 && y >= 0 && x < 96 && y < 96) {
            val buffer = IoBuffer(112).put(x).putC(y)
            buffer.cypherOpcode(context.player.session.isaacPair.output)
            context.player.session.write(buffer)
        }
    }, immediate = true),
    CONSTRUCT_SCENERY(encode<BuildSceneryContext> { context ->
        val player = context.player
        val o = context.scenery
        val buffer = writeSceneryAddition(areaPositionBuffer(player, o.location.chunkBase), o)
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        player.session.write(buffer)
    }),
    CLEAR_SCENERY(encode<BuildSceneryContext> { context ->
        val player = context.player
        val scenery = context.scenery
        val buffer = writeSceneryRemoval(areaPositionBuffer(player, scenery.location.chunkBase), scenery)
        buffer.cypherOpcode(player.session.isaacPair.output)
        player.session.write(buffer)
    }),
    CONSTRUCT_GROUND_ITEM(encode<BuildItemContext> { context ->
        val player = context.player
        val item = context.item
        val buffer = writeGroundItemAddition(areaPositionBuffer(player, item.location.chunkBase), item)
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        player.details.session.write(buffer)
    }),
    CLEAR_GROUND_ITEM(encode<BuildItemContext> { context ->
        val player = context.player
        val item = context.item
        val buffer = writeGroundItemRemoval(areaPositionBuffer(player, item.location.chunkBase), item)
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        player.session.write(buffer)
    }),
    UPDATE_GROUND_ITEM_AMOUNT(encode<BuildItemContext> { context ->
        val player = context.player
        val item = context.item
        val buffer = writeGroundItemAmount(areaPositionBuffer(player, item.location.chunkBase), item, context.oldAmount)
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        player.details.session.write(buffer)
    }),
    ANIMATE_OBJECT_PACKET(encode<AnimateObjectContext> { context ->
        val player = context.player
        val `object` = context.animation.getObject()
        val buffer = writeObjectAnimation(areaPositionBuffer(player, `object`.location.chunkBase), context.animation)
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        player.session.write(buffer)
    }),
    LOC_ADD_CHANGE_PACKET(encode<SceneryUpdateContext> { context ->
        val player = context.player
        val buffer = IoBuffer(202, PacketHeader.NORMAL)

        buffer.cypherOpcode(player.session.isaacPair.output)
        val packed = (context.type shl 2) or (context.rotation and 0x3)
        buffer.p1(packed)
        val base = player.playerFlags.lastSceneGraph
        val localX = context.x - base.x
        val localY = context.y - base.y
        val tile = ((localX and 7) shl 4) or (localY and 7)
        buffer.p1(tile)

        buffer.p1add(context.int0)
        buffer.p1add(context.int1)
        buffer.p1sub(context.int2)

        buffer.p2add(context.objectId)

        buffer.ip2(context.int4)
        buffer.p1(context.int3)
        buffer.p2(context.int5)
        buffer.ip2add(context.int6)

        player.session.write(buffer)
    }),
    POSITIONED_GRAPHIC(encode<PositionedGraphicContext> { context ->
        val g = context.graphic
        val offsetHash = (context.offsetX shl 4) or context.offsetY
        val buffer = IoBuffer()
            .put(26)                 // update current scene x and scene y client-side
            .putC(context.sceneX)        // this has to be done for each graphics being sent
            .put(context.sceneY)         // opcode 26 is the lastSceneX/lastSceneY update packet
            .put(17).put(offsetHash) // send the graphics
            .putShort(g.id)
            .put(g.height)
            .putShort(g.delay)
        context.player.session.write(buffer)
    }),
    HINT_ICON(encode<HintIconContext> { context ->
        val buffer = IoBuffer(217)
        buffer.put(context.slot shl 6 or context.targetType).put(context.arrowId)
        if (context.arrowId > 0) {
            if (context.targetType == 1 || context.targetType == 10) {
                buffer.putShort(context.index).putShort(0) // Skip 3 bytes
                    .put(0)
            } else if (context.location != null) {
                buffer.putShort(context.location.x).putShort(context.location.y).put(context.height)
            } else {
                buffer.putShort(0).putShort(0).put(0) // Skip all bytes.
            }
            buffer.putShort(context.modelId)
        }
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        context.player.session.write(buffer)
    }),
    INTERACTION_OPTION(encode<InteractionOptionContext> { context ->
        val buffer = IoBuffer(44, PacketHeader.BYTE)
        buffer.putLEShortA(-1)
        if (context.isRemove) {
            buffer.put(0)
        } else {
            buffer.put(if (context.index == 0) 1 else 0)
        }
        buffer.put(context.index + 1)
        buffer.putString(context.name)
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        context.player.session.write(buffer)
    }),
    SET_WALK_OPTION(encode<WalkOptionContext> {
        // TODO: Verify the walk-to option packet for revision 530.
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
                buffer.putShort(context.player.interfaceManager.getPacketCount(1))
                val x = l.getSceneX(p.playerFlags.lastSceneGraph)
                val y = l.getSceneY(p.playerFlags.lastSceneGraph)
                buffer.put(x).put(y).putShort(context.height).put(context.speed).put(context.zoomSpeed)
            }

            CameraType.SET -> buffer.putLEShort(context.x)
                .putShort(context.player.interfaceManager.getPacketCount(1)).putShort(context.y)

            CameraType.SHAKE -> {
                buffer.putShort(context.player.interfaceManager.getPacketCount(1))
                buffer.put(l.x).put(l.y).put(context.speed).put(context.zoomSpeed).putShort(context.height)
            }

            CameraType.RESET -> buffer.putShort(context.player.interfaceManager.getPacketCount(1))
        }
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        p.session.write(buffer)
    }),
    MUSIC_PACKET(encode<MusicContext> { context ->
        val buffer = if (context.isSecondary) {
            IoBuffer(208).putTri(255).putLEShort(context.musicId)
        } else {
            IoBuffer(4).putLEShortA(context.musicId)
        }
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        context.player.details.session.write(buffer)
    }),
    AUDIO_PACKET(encode<AudioContext> { context ->
        val audio = context.audio
        val loc = context.location
        val buffer = if (loc == null) {
            IoBuffer()
        } else {
            areaPositionBuffer(context.player, loc.chunkBase)
        }
        writeAudio(buffer, audio, loc)
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        context.player.session.write(buffer)
    }),

    // Messages and contacts
    GAME_MESSAGE(encode<GameMessageContext> { context ->
        val buffer = IoBuffer(Network.SEND_GAME_MESSAGE, PacketHeader.BYTE)
        buffer.putString(context.message)
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        context.player.session.write(buffer)
    }),
    COMMUNICATION_MESSAGE(encode<MessageContext> { context ->
        val buffer = IoBuffer(context.opcode, PacketHeader.BYTE)
        val message = context.message
        val player = context.player
        val other = context.other
        when (context.opcode) {
            MessageContext.SEND_MESSAGE -> {
                val bytes = ByteArray(256)
                val length = encryptPlayerChat(bytes, 0, 0, message.length, message.toByteArray())
                buffer.putLong(stringToLong(other))
                buffer.put(message.length.toByte().toInt())
                buffer.putBytes(bytes, 0, length)
            }

            MessageContext.RECEIVE_MESSAGE -> {
                val bytes = ByteArray(256)
                bytes[0] = message.length.toByte()
                val length = 1 + encryptPlayerChat(bytes, 0, 1, message.length, message.toByteArray())
                player.setAttribute("replyTo", other)
                buffer.putLong(stringToLong(other)).putShort(Random().nextInt(0xFFFF)).putTri(getMessageIndex(player))
                    .put(context.chatIcon.toByte().toInt()).putBytes(bytes, 0, length)
            }

            MessageContext.CLAN_MESSAGE -> {
                val clan = player.communication.clan ?: return@encode
                val bytes = ByteArray(256)
                bytes[0] = context.message.length.toByte()
                val length = 1 + encryptPlayerChat(bytes, 0, 1, message.length, message.toByteArray())
                buffer.putLong(stringToLong(other))
                buffer.put(0.toByte().toInt()) // it's just read does nothing
                buffer.putLong(stringToLong(clan.name))
                buffer.putShort(Random().nextInt(0xFFFF))
                buffer.putTri(getMessageIndex(player))
                buffer.put(context.chatIcon.toByte().toInt()) // rights
                buffer.putBytes(bytes, 0, length)
            }
        }
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        player.session.write(buffer)
    }),
    CONTACT_PACKETS(encode<ContactContext> { context ->
        val player = context.player
        val buffer = when (context.type) {
            ContactContext.UPDATE_STATE_TYPE -> IoBuffer(197).put(2) // Always send the AVAILABLE state.
            ContactContext.IGNORE_LIST_TYPE -> {
                val payload = IoBuffer(126, PacketHeader.SHORT)
                for (string in player.communication.blocked) {
                    if (string.length == 0) {
                        continue
                    }
                    payload.putLong(stringToLong(string))
                }
                payload
            }

            ContactContext.UPDATE_FRIEND_TYPE -> {
                val payload = IoBuffer(62, PacketHeader.BYTE)
                context.name?.let { payload.putLong(stringToLong(it)) }
                payload.putShort(context.worldId)
                val c = player.communication.contacts[context.name]
                if (c != null) {
                    payload.put(c.rank.value.toByte().toInt())
                } else {
                    payload.put(0)
                }
                if (context.isOnline) {
                    payload.putString("World " + context.worldId)
                }
                payload
            }
            else -> return@encode
        }
        buffer.cypherOpcode(player.session.isaacPair.output)
        player.session.write(buffer)
    }),
    UPDATE_CLAN_CHAT(encode<ClanContext> { context ->
        val buffer = IoBuffer(55, PacketHeader.SHORT)
        val clan = context.clan
        if (!context.isLeave) {
            buffer.putLong(stringToLong(clan.owner))
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
                buffer.putLong(stringToLong(name)).putShort(entry.worldId)
                buffer.put(clan.getRank(entry).value)
                buffer.putString("World " + entry.worldId)
            }
        } else {
            buffer.putLong(0)
        }
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        context.player.session.write(buffer)
    });

    internal fun write(context: PacketContext) = writer(context)

    companion object {
        private const val GE_REMOVED = 6
        private const val GE_ABORTED = 2

        fun writeObjectAnimation(buffer: IoBuffer, animation: Animation): IoBuffer {
            val `object` = animation.getObject()
            val l = `object`.location
            buffer.put(20)
            buffer.putS((l.chunkOffsetX shl 4) or (l.chunkOffsetY and 0x7))
            buffer.putS((`object`.type shl 2) + (`object`.rotation and 0x3))
            buffer.putLEShort(animation.id)
            return buffer
        }

        private fun writeAudio(buffer: IoBuffer, audio: Audio, loc: Location?): IoBuffer {
            if (loc == null) {
                buffer.put(172)
                buffer.putShort(audio.id)
                buffer.put(audio.loops)
                buffer.putShort(audio.delay)
            } else {
                buffer.put(97)
                buffer.put((loc.chunkOffsetX shl 4) or loc.chunkOffsetY)
                buffer.putShort(audio.id)
                buffer.put((audio.radius shl 4) or (audio.loops and 7))
                buffer.put(audio.delay)
            }
            return buffer
        }

        @JvmStatic
        fun writeGroundItemRemoval(buffer: IoBuffer, item: Item): IoBuffer {
            val l = item.location
            buffer.put(240)
            buffer.putS((l.chunkOffsetX shl 4) or (l.chunkOffsetY and 0x7)).putShort(item.id)
            return buffer
        }

        @JvmStatic
        fun writeSceneryRemoval(buffer: IoBuffer, scenery: Scenery): IoBuffer {
            val location = scenery.location
            return buffer.put(195) // Opcode
                .putC((scenery.type shl 2) + (scenery.rotation and 3))
                .put((location.chunkOffsetX shl 4) or (location.chunkOffsetY and 0x7))
        }

        private fun getMessageIndex(p: Player): Int {
            val count = p.getAttribute("pm_index", 0) + 1
            p.setAttribute("pm_index", count)
            return count
        }

        @JvmStatic
        fun writeGroundItemAddition(buffer: IoBuffer, item: Item): IoBuffer {
            val l = item.location
            buffer.put(33)
            buffer.putLEShort(item.id).put((l.chunkOffsetX shl 4) or (l.chunkOffsetY and 0x7)).putShortA(item.amount)
            return buffer
        }

        @JvmStatic
        fun writeSceneryAddition(buffer: IoBuffer, scenery: Scenery): IoBuffer {
            val l = scenery.location
            buffer.put(179).putA((scenery.type shl 2) or (scenery.rotation and 0x3))
                .put((l.chunkOffsetX shl 4) or (l.chunkOffsetY and 0x7)).putShortA(scenery.id)
            return buffer
        }

        @JvmStatic
        fun chunkUpdateBuffer(player: Player, base: Location): IoBuffer {
            val scene = player.playerFlags.lastSceneGraph
            val x = base.getSceneX(scene)
            val y = base.getSceneY(scene)
            return IoBuffer(230, PacketHeader.SHORT).putA(y).putS(x)
        }

        private fun areaPositionBuffer(player: Player, base: Location): IoBuffer {
            val scene = player.playerFlags.lastSceneGraph
            val x = base.getSceneX(scene)
            val y = base.getSceneY(scene)
            return IoBuffer(26).putC(x).put(y)
        }

        @JvmStatic
        fun writeGroundItemAmount(buffer: IoBuffer, item: Item, oldAmount: Int): IoBuffer {
            val l = item.location
            buffer.put(14).put((l.chunkOffsetX shl 4) or (l.chunkOffsetY and 0x7)).putShort(item.id).putShort(oldAmount).putShort(item.amount)
            return buffer
        }
    }
}
