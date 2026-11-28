package core.net.packet.out

import core.net.packet.ServerProt

import core.cache.Cache
import core.cache.def.impl.VarpType
import core.cache.def.impl.VarcType
import core.cache.def.impl.VarbitType
import core.game.node.entity.player.Player
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
import core.net.packet.QuickChatDelivery
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
import core.net.packet.out.PacketContext.VarcStringUpdateContext
import core.net.packet.out.PacketContext.WalkOptionContext
import core.net.packet.out.PacketContext.WindowsPaneContext
import core.tools.StringUtils.encryptPlayerChat
import core.tools.StringUtils.stringToLong
import java.net.Inet4Address
import kotlin.math.ceil

private inline fun <reified T : PacketContext> encode(crossinline writer: (T) -> Unit): (PacketContext) -> Unit = { context ->
    require(context is T) { "Expected ${T::class.java.simpleName}, got ${context.javaClass.simpleName}." }
    writer(context)
}

@Suppress("UNINITIALIZED_ENUM_COMPANION", "UNINITIALIZED_ENUM_COMPANION_WARNING")
enum class Encoders578(private val writer: (PacketContext) -> Unit, internal val immediate: Boolean = false) {
    LOC_ANIM_ABSOLUTE(encode<PacketContext.AbsoluteLocAnimation> { c ->
        val l = c.location
        if (l.x !in 0..16383 || l.y !in 0..16383 || l.z !in 0..3 || c.shape !in 0..22 || c.rotation !in 0..3 || c.animation !in -1..65534) return@encode
        c.player.session.write(IoBuffer(ServerProt.LOC_ANIM_ABSOLUTE).putA((c.shape shl 2) or c.rotation)
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
        context.player.session.write(resetEntityAnimations())
    }),
    RESET_CLIENT_VARPS(encode<PlayerContext> { context ->
        context.player.session.write(resetClientVarps())
    }),
    RUN_DIALOG_ABORT_HOOKS(encode<PlayerContext> { context ->
        context.player.session.write(runDialogAbortHooks())
    }),
    // Session and status
    @Deprecated("Historical login response; use LoginWorldEncoder during the login phase.")
    LOGIN_PACKET(encode<PlayerContext> {
        // Never enqueue the old unframed login response into an ISAAC gameplay stream.
        System.err.println("[Protocol] Rejected legacy LOGIN_PACKET")
    }),
    LAST_LOGIN_INFO(encode<PlayerContext> { context ->
        val buffer = IoBuffer(ServerProt.LAST_LOGIN_INFO)
        val ip = context.player.details.accountInfo.lastUsedIp.ifEmpty { context.player.details.ipAddress }
        val octets = ip.split('.').map { it.toIntOrNull() }
        buffer.putInt(if (octets.size == 4 && octets.all { it != null && it in 0..255 })
            octets.fold(0) { acc, value -> (acc shl 8) or value!! } else 0)
        context.player.session.write(buffer)
    }),
    LOGOUT(encode<PlayerContext> { context ->
        val buffer = logout()
        context.player.details.session.write(buffer)
    }, immediate = true),
    @Deprecated("ClientProt.NO_TIMEOUT is client-to-server; there is no gameplay reply.")
    PING_PACKET(encode<PlayerContext> {
        // No revision 578 server keep-alive encoder; client sends NO_TIMEOUT. Unused.
    }),
    CLEAR_MINIMAP_FLAG(encode<PlayerContext> { context ->
        val buffer = clearMinimapFlag()
        context.player.details.session.write(buffer)
    }),
    MINIMAP_STATE(encode<MinimapStateContext> { context ->
        if (context.state !in 0..255) return@encode
        val buffer = minimapState(context.state)
        context.player.details.session.write(buffer)
    }),
    UPDATE_REBOOT_TIMER(encode<SystemUpdateContext> { context ->
        if (context.time !in 0..65535) return@encode
        context.player.details.session.write(IoBuffer(ServerProt.UPDATE_REBOOT_TIMER).putShort(context.time))
    }),
    UPDATE_RUNENERGY(encode<PlayerContext> { context ->
        val energy = context.player.settings.runEnergy
        if (!energy.isFinite() || energy < 0.0 || energy > 100.0) return@encode
        context.player.details.session.write(IoBuffer(ServerProt.UPDATE_RUNENERGY).put(energy.toInt()))
    }),
    UPDATE_RUNWEIGHT(encode<PlayerContext> { context ->
        val weight = context.player.settings.weight
        if (!weight.isFinite() || weight.toInt() !in -32768..32767) return@encode
        context.player.details.session.write(IoBuffer(ServerProt.UPDATE_RUNWEIGHT).putShort(weight.toInt()))
    }),
    UPDATE_STAT(encode<SkillContext> { context ->
        if (context.skillId !in Skills.SKILL_NAME.indices) return@encode
        val skills = context.player.getSkills()
        val xp = skills.getExperience(context.skillId)
        val level = when (context.skillId) {
            Skills.PRAYER -> ceil(skills.prayerPoints).toInt()
            Skills.HITPOINTS -> skills.lifepoints
            else -> skills.getLevel(context.skillId, true)
        }
        if (!xp.isFinite() || xp < 0.0 || xp > Int.MAX_VALUE.toDouble() || level !in 0..255) return@encode
        context.player.details.session.write(IoBuffer(ServerProt.UPDATE_STAT).putInt(xp.toInt()).putC(context.skillId).putS(level))
    }),
    GLOBAL_SPOT_ANIMATION(encode<PacketContext.GlobalSpotAnimationContext> { c ->
        c.player.details.session.write(globalSpotAnimation(c.target, c.id, c.delay, c.height))
    }),

    // Interfaces
    IF_OPENTOP(encode<WindowsPaneContext> { context ->
        val buffer = openTop(context.type, context.windowId, context.player.interfaceManager.getPacketCount(1))
        context.player.details.session.write(buffer)
    }),
    IF_OPENSUB(encode<InterfaceContext> { context ->
        if (!validComponent578(context.windowId, context.componentId)) return@encode
        val buffer = openSub(context.windowId shl 16 or context.componentId,
            if (context.isWalkable) 3 else 0, context.interfaceId, context.player.interfaceManager.getPacketCount(1))
        context.player.details.session.write(buffer)
    }),
    IF_MOVESUB(encode<PacketContext.MoveInterfaceContext> { context ->
        val buffer = IoBuffer(ServerProt.IF_MOVESUB).putIntB(context.source).putIntB(context.destination)
            .putLEShort(context.player.interfaceManager.getPacketCount(1))
        context.player.session.write(buffer)
    }),
    IF_CLOSESUB(encode<InterfaceContext> { context ->
        val buffer = IoBuffer(ServerProt.IF_CLOSESUB)
        buffer.putShort(context.player.interfaceManager.getPacketCount(1))
        buffer.putShort(context.windowId)
        buffer.putShort(context.componentId)
        context.player.session.write(buffer)
    }),
    @Deprecated("No verified equivalent in ServerProt; migrate callers individually.")
    RESET_INTERFACE(encode<ResetInterfaceContext> { context ->
        System.err.println("[Protocol578] Unsupported RESET_INTERFACE for interface ${context.interfaceId}")
    }),
    SET_INTERFACE_SETTINGS(encode<AccessMaskContext> { context ->
        if (!validComponent578(context.interfaceId, context.childId)) return@encode
        if (context.offset !in -1..65534 || context.length !in context.offset..65534) return@encode
        val buffer = settings(context.interfaceId shl 16 or context.childId,
            context.offset, context.length, context.id, context.player.interfaceManager.getPacketCount(1))
        context.player.session.write(buffer)
    }),
    WIDGETSTRUCT_SETTING(encode<AccessMaskContext> { context ->
        if (!validComponent578(context.interfaceId, context.childId)) return@encode
        if (context.offset !in -1..65534 || context.length !in context.offset..65534 || context.id !in 0..65535) return@encode
        context.player.session.write(
            targetParam(context.interfaceId shl 16 or context.childId,
            context.offset, context.length, context.id, context.player.interfaceManager.getPacketCount(1)))
    }),
    IF_SETHIDE(encode<InterfaceConfigContext> { context ->
        if (context.interfaceId !in 0..65535 || context.childId !in 0..65535 ||
            Cache.getIndexes()[3]?.getFileData(context.interfaceId, context.childId) == null) {
            System.err.println("[Protocol578] Rejected IF_SETHIDE for missing component ${context.interfaceId}:${context.childId}")
            return@encode
        }
        val buffer = IoBuffer(ServerProt.IF_SETHIDE)
        buffer.putLEShortA(context.player.interfaceManager.getPacketCount(1))
        buffer.putLEInt(context.interfaceId shl 16 or context.childId)
        buffer.putS(if (context.isHidden) 1 else 0)
        context.player.session.write(buffer)
    }),
    IF_SETANIM(encode<AnimateInterfaceContext> { context ->
        if (!validComponent578(context.interfaceId, context.childId)) return@encode
        val buffer = IoBuffer(ServerProt.IF_SETANIM).putInt(context.interfaceId shl 16 or context.childId)
            .putLEShort(context.player.interfaceManager.getPacketCount(1)).putLEShortA(context.animationId)
        context.player.session.write(buffer)
    }),
    DISPLAY_MODEL(encode<DisplayModelContext> { context ->
        if (!validComponent578(context.interfaceId, context.childId)) return@encode
        val hash = context.interfaceId shl 16 or context.childId
        val verify = context.player.interfaceManager.getPacketCount(1)
        val buffer = when (context.type) {
            DisplayModelContext.ModelType.PLAYER -> IoBuffer(ServerProt.IF_SETPLAYER_MODEL_TYPE3).putLEInt(hash).putLEShortA(verify)
            DisplayModelContext.ModelType.NPC -> IoBuffer(ServerProt.IF_SETNPC_MODEL_TYPE2).putIntA(hash).putShortA(context.nodeId).putLEShortA(verify)
            DisplayModelContext.ModelType.ITEM -> IoBuffer(ServerProt.IF_SETOBJ).putLEShortA(verify).putLEShort(context.nodeId)
                .putIntB(if (context.amount > 0) context.amount else context.zoom).putIntA(hash)
            DisplayModelContext.ModelType.MODEL -> IoBuffer(ServerProt.IF_SETMODEL).putIntA(hash).putLEShortA(verify).putShort(context.nodeId)
        }
        context.player.session.write(buffer)
    }),
    IF_SETTEXT(encode<StringContext> { context ->
        if (context.interfaceId !in 0..65535 || context.lineId !in 0..65535 ||
            Cache.getIndexes()[3]?.getFileData(context.interfaceId, context.lineId) == null) {
            System.err.println("[Protocol578] Rejected IF_SETTEXT for missing component ${context.interfaceId}:${context.lineId}")
            return@encode
        }
        if ('\u0000' in context.string || context.string.length > 65528) return@encode
        val buffer = IoBuffer(ServerProt.IF_SETTEXT, PacketHeader.SHORT)
        buffer.putString(context.string)
        buffer.putLEShort(context.player.interfaceManager.getPacketCount(1))
        buffer.putIntA((context.interfaceId shl 16) or context.lineId)
        context.player.details.session.write(buffer)
    }),
    IF_SETPOSITION(encode<ChildPositionContext> { context ->
        if (!validComponent578(context.interfaceId, context.childId)) return@encode
        val buffer = IoBuffer(ServerProt.IF_SETPOSITION).putLEInt(context.interfaceId shl 16 or context.childId)
            .putLEShort(context.player.interfaceManager.getPacketCount(1))
            .putLEShort(context.position.y).putShortA(context.position.x)
        context.player.session.write(buffer)
    }),
    IF_SETANGLE(encode<InterfaceSetAngleContext> { context ->
        if (!validComponent578(context.interfaceId, context.childId)) return@encode
        val buffer = IoBuffer(ServerProt.IF_SETANGLE).putIntB(context.interfaceId shl 16 or context.childId)
            .putLEShortA(context.player.interfaceManager.getPacketCount(1))
            .putShortA(context.scale).putShortA(context.pitch).putShortA(context.yaw)
        context.player.session.write(buffer)
    }),
    INTERFACE_SET_MODEL_ROTATION(encode<InterfaceSetModelRotationContext> { context ->
        // The legacy opcode 207 is outside the revision 578 protocol.
        // No equivalent for independent continuous model rotation has been verified.
        System.err.println("[Protocol578] Unsupported INTERFACE_SET_MODEL_ROTATION for ${context.id}:${context.componentId}")
    }),
    IF_SETCOLOUR(encode<PacketContext.ComponentValueContext> { context ->
        if (!validComponent578(context.interfaceId, context.childId) || context.value !in 0..32767) return@encode
        val hash = context.interfaceId shl 16 or context.childId
        val verify = context.player.interfaceManager.getPacketCount(1)
        context.player.session.write(IoBuffer(ServerProt.IF_SETCOLOUR).putInt(hash).putLEShortA(context.value).putShort(verify))
    }),
    IF_SETSPRITE(encode<PacketContext.ComponentValueContext> { context ->
        if (!validComponent578(context.interfaceId, context.childId) || context.value !in 0..65535) return@encode
        val hash = context.interfaceId shl 16 or context.childId
        val verify = context.player.interfaceManager.getPacketCount(1)
        context.player.session.write(IoBuffer(ServerProt.IF_SETSPRITE).putLEShortA(context.value).putIntA(hash).putShortA(verify))
    }),
    IF_SET_SELF_BODY_MODEL(encode<PacketContext.ComponentValueContext> { context ->
        if (!validComponent578(context.interfaceId, context.childId)) return@encode
        val hash = context.interfaceId shl 16 or context.childId
        val verify = context.player.interfaceManager.getPacketCount(1)
        context.player.session.write(IoBuffer(ServerProt.IF_SET_SELF_BODY_MODEL).putShortA(verify).putIntA(hash))
    }),
    IF_SET_PLAYER_BODY_MODEL(encode<PacketContext.InterfacePlayerModelContext> { context ->
        if (!validComponent578(context.interfaceId, context.childId) || context.playerIndex !in 0..2047) return@encode
        val hash = context.interfaceId shl 16 or context.childId
        val verify = context.player.interfaceManager.getPacketCount(1)
        context.player.session.write(IoBuffer(ServerProt.IF_SET_PLAYER_BODY_MODEL).putInt(hash).putIntA(context.nameHash).putShortA(verify).putLEShort(context.playerIndex))
    }),
    IF_SET_IDENTITY_KIT_MODEL(encode<PacketContext.InterfaceKitModelContext> { context ->
        if (!validComponent578(context.interfaceId, context.childId) ||
            context.kit1 !in 0..65535 || context.kit2 !in 0..65535 || context.kit3 !in 0..65535) return@encode
        val hash = context.interfaceId shl 16 or context.childId
        val verify = context.player.interfaceManager.getPacketCount(1)
        context.player.session.write(IoBuffer(ServerProt.IF_SET_IDENTITY_KIT_MODEL).putShort(context.kit1).putShort(verify)
            .putShort(context.kit2).putLEShortA(context.kit3).putLEInt(hash))
    }),
    IF_COMPONENT_24BIT_VALUES(encode<PacketContext.Component24BitValuesContext> { c ->
        // Protocol PACKET_10: BE verify/interface/start/end, then one BE u24 per child.
        // ScriptRunner 6705 exposes the field; its content semantics remain unknown.
        if (c.interfaceId !in 0..65535 || c.firstChild !in 0..65535 || c.values.size !in 1..82) return@encode
        val end = c.firstChild + c.values.size
        if (end > 65535 || c.values.any { it !in 0..0xffffff }) return@encode
        if ((c.firstChild until end).any { !validComponent578(c.interfaceId, it) }) return@encode
        val verify = c.player.interfaceManager.getPacketCount(1)
        val buffer = IoBuffer(ServerProt.IF_COMPONENT_24BIT_VALUES, PacketHeader.BYTE).putShort(verify).putShort(c.interfaceId)
            .putShort(c.firstChild).putShort(end)
        c.values.forEach { buffer.putTri(it) }
        c.player.session.write(buffer)
    }),
    ENTITY_RENDER_PRIORITY(encode<PacketContext.EntityRenderPriorityContext> { c ->
        // Static45.method1398 explicitly distinguishes modes 0 and 1.
        if (c.mode !in 0..1) return@encode
        val verify = c.player.interfaceManager.getPacketCount(1)
        c.player.session.write(IoBuffer(ServerProt.ENTITY_RENDER_PRIORITY).putShortA(verify).putC(c.mode))
    }),
    CLIENT_RANDOM_DATA(encode<PacketContext.ClientRandomDataContext> { c ->
        // Protocol.PACKET_26 / Packet.checkcrc / Static294.method5065.
        if (c.data.size != 24) return@encode
        val crc = java.util.zip.CRC32().apply { update(c.data) }.value.toInt()
        c.player.session.write(IoBuffer(ServerProt.CLIENT_RANDOM_DATA).put(c.data).putInt(crc))
    }),
    CLIENT_SETTINGS_COOKIE(encode<PacketContext.ClientSettingsCookieContext> { c ->
        // Static383.method6379 inserts the string into an applet JavaScript cookie.
        // Accept only opaque cookie-safe ASCII tokens (or empty to clear); no arbitrary JS.
        if (c.settings.length > 254 || c.settings.any { it !in 'a'..'z' && it !in 'A'..'Z' &&
                    it !in '0'..'9' && it !in "-_.~" }) return@encode
        c.player.session.write(IoBuffer(ServerProt.CLIENT_SETTINGS_COOKIE, PacketHeader.BYTE).putString(c.settings))
    }),
    OPEN_URL(encode<PacketContext.OpenUrlContext> { c ->
        // Protocol.PACKET_56 uses PacketBit.gisaac for each payload byte, not gjstr.
        // Restrict this explicit server-only API to simple HTTP(S) URLs; do not relay client input.
        // Exclude shell expansion/metacharacters because the original Windows client uses cmd /c start.
        val url = c.url
        if (url.length !in 1..255 || url.any { it !in 'a'..'z' && it !in 'A'..'Z' &&
                    it !in '0'..'9' && it !in "?=,.+-_#:/" }) return@encode
        val uri = try { java.net.URI(url) } catch (_: java.net.URISyntaxException) { return@encode }
        if (uri.scheme !in listOf("http", "https") || uri.host.isNullOrEmpty() || uri.rawUserInfo != null) return@encode
        // IoSession encrypts opcode and payload in the same queue commit, after validation.
        c.player.session.write(IoBuffer(ServerProt.OPEN_URL, PacketHeader.BYTE).put(url.toByteArray(Charsets.US_ASCII)))
    }),
    IF_SET_FONT(encode<PacketContext.ComponentValueContext> { context ->
        if (!validComponent578(context.interfaceId, context.childId) || context.value !in -1..65534) return@encode
        val hash = context.interfaceId shl 16 or context.childId
        val verify = context.player.interfaceManager.getPacketCount(1)
        context.player.session.write(IoBuffer(ServerProt.IF_SET_FONT).putLEShort(verify).putIntB(hash).putLEShort(context.value))
    }),
    IF_SET_SCROLL_Y(encode<PacketContext.ComponentValueContext> { context ->
        if (!validComponent578(context.interfaceId, context.childId) || context.value !in 0..65535) return@encode
        val hash = context.interfaceId shl 16 or context.childId
        val verify = context.player.interfaceManager.getPacketCount(1)
        context.player.session.write(IoBuffer(ServerProt.IF_SET_SCROLL_Y).putInt(hash).putLEShort(verify).putShortA(context.value))
    }),
    DELETE_CLIENT_CONTAINER(encode<PacketContext.DeleteClientContainerContext> { context ->
        if (context.containerId !in 0..65535) return@encode
        context.player.session.write(IoBuffer(ServerProt.DELETE_CLIENT_CONTAINER).putC(if (context.split) 1 else 0).putLEShortA(context.containerId))
    }),
    RUNCLIENTSCRIPT(encode<RunScriptContext> { context ->
        val types = context.string
        val objects = context.objects
        // Client reads the signature backwards; keep the existing wire-order API.
        if (types.length != objects.size || '\u0000' in types) {
            System.err.println("[Protocol578] Rejected invalid CS2 signature for ${context.id}")
            return@encode
        }
        var payloadLength = 7L + types.length
        for ((j, type) in types.reversed().withIndex()) {
            val value = objects[j]
            if (type == 's') {
                if (value !is String || '\u0000' in value) return@encode
                payloadLength += value.length.toLong() + 1
            } else {
                if (value !is Int) return@encode
                payloadLength += 4
            }
        }
        if (payloadLength > 65535) return@encode
        val buffer = IoBuffer(ServerProt.RUNCLIENTSCRIPT, PacketHeader.SHORT)
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
        if (context.id !in 0..65535 || !VarpType.exists(context.id)) {
            System.err.println("[Protocol578] Rejected unknown varp ${context.id}, value=${context.value}")
            return@encode
        }
        val buffer: IoBuffer
        if (context.value < Byte.MIN_VALUE || context.value > Byte.MAX_VALUE) {
            buffer = IoBuffer(ServerProt.VARP_LARGE)
            buffer.putLEShortA(context.id)
            buffer.putLEInt(context.value)
        } else {
            buffer = IoBuffer(ServerProt.VARP_SMALL)
            buffer.putC(context.value)
            buffer.putShort(context.id)
        }
        if (!context.player.isArtificial) {
            context.player.details.session.write(buffer)
        }
    }),
    CS_CONFIG_PACKET(encode<CSConfigContext> { context ->
        if (!VarcType.exists(context.id)) {
            System.err.println("[Protocol578] Rejected unknown varc ${context.id}")
            return@encode
        }
        val buffer = varc(context.id, context.value, context.player.interfaceManager.getPacketCount(1))
        context.player.details.session.write(buffer)
    }),
    VARBIT(encode<VarbitContext> { context ->
        if (context.varbitId !in 0..65535 || !VarbitType.exists(context.varbitId)) {
            System.err.println("[Protocol578] Rejected unknown varbit ${context.varbitId}, value=${context.value}")
            return@encode
        }
        val buffer: IoBuffer
        if (context.value < 0 || context.value > 255) {
            buffer = IoBuffer(ServerProt.VARBIT_LARGE)
            buffer.putLEInt(context.value)
        } else {
            buffer = IoBuffer(ServerProt.VARBIT_SMALL)
            buffer.put(context.value)
        }
        buffer.putLEShort(context.varbitId)
        context.player.session.write(buffer)
    }),
    VARC_UPDATE(encode<VarcUpdateContext> { context ->
        val player = context.player
        if (!VarcType.exists(context.varcId)) {
            System.err.println("[Protocol578] Rejected unknown varc ${context.varcId}")
            return@encode
        }
        player.session.write(varc(context.varcId, context.value, player.interfaceManager.getPacketCount(1)))
    }),
    VARC_STRING_UPDATE(encode<VarcStringUpdateContext> { context ->
        if (!VarcType.stringExists(context.varcId) || '\u0000' in context.value || context.value.length > 65530) {
            System.err.println("[Protocol578] Rejected varc string ${context.varcId}")
            return@encode
        }
        context.player.session.write(varcString(context.varcId, context.value,
            context.player.interfaceManager.getPacketCount(1)))
    }),
    CONTAINER_PACKET(encode<ContainerContext> { context ->
        // Containers in 578 are addressed by container key, not widget hash.
        if (context.isClear) {
            System.err.println("[578] Unmapped widget-only container clear: ${context.interfaceId}:${context.childId}")
            return@encode
        }
        val buffer = ContainerUpdate.encode(context.containerId, context.isSplit,
            context.entries, context.length, context.slots, context.ids) ?: return@encode
        context.player.session.write(buffer)
    }),
    GRAND_EXCHANGE_OFFER(encode<GrandExchangeContext> { context ->
        // Revision 578 has exactly six client offer slots.
        if (context.idx.toInt() !in 0..5 || context.state.toInt() !in 0..GE_REMOVED) return@encode
        val buffer = IoBuffer(ServerProt.GRAND_EXCHANGE_OFFER, PacketHeader.NORMAL)
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
        val buffer = IoBuffer(ServerProt.REBUILD_NORMAL, PacketHeader.SHORT)
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
    UPDATE_ZONE_PARTIAL_FOLLOWS(encode<AreaPositionContext> { context ->
        val scene = context.player.playerFlags.lastSceneGraph
        val l = context.location
        val x = l.getSceneX(scene)
        val y = l.getSceneY(scene)
        if (x !in 0..96 || y !in 0..96 || l.z !in 0..3) return@encode
        val buffer = IoBuffer(ServerProt.UPDATE_ZONE_PARTIAL_FOLLOWS).putA(l.z).putC(y).putS(x)
        context.player.session.write(buffer)
    }),
    UPDATE_ZONE_FULL_FOLLOWS(encode<ClearChunkContext> { context ->
        val l = context.player.playerFlags.lastSceneGraph
        val x = context.chunk.currentBase.getSceneX(l)
        val y = context.chunk.currentBase.getSceneY(l)
        if (x in 0..96 && y in 0..96 && context.chunk.currentBase.z in 0..3) {
            val buffer = IoBuffer(ServerProt.UPDATE_ZONE_FULL_FOLLOWS).put(x).putC(y).putA(context.chunk.currentBase.z)
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
        val `object` = context.animation.getObject() ?: return@encode
        if (!validSceneryShape(`object`) || context.animation.id !in -1..65535) return@encode
        val location = `object`.location ?: return@encode
        val buffer = writeObjectAnimation(checkedZone(player, location) ?: return@encode, context.animation)
        player.session.write(buffer)
    }),
    POSITIONED_GRAPHIC(encode<PositionedGraphicContext> { context ->
        val g = context.graphic
        if (g.id !in 0..65534 || g.height !in 0..255 || g.delay !in 0..65535) return@encode
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
        if (!context.isValid()) return@encode
        val buffer = IoBuffer(ServerProt.HINT_ICON)
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
    SET_INTERACTION(encode<InteractionOptionContext> { context ->
        if (context.index !in 0..7) return@encode
        val buffer = interaction(if (context.isRemove) "null" else context.name,
            context.index + 1, !context.isRemove && context.index == 0)
        context.player.session.write(buffer)
    }),
    SET_WALK_OPTION(encode<WalkOptionContext> { context ->
        if (context.option.length > 254 || context.option.contains('\u0000')) return@encode
        context.player.session.write(walkOption(context.option))
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
        if (context.musicId !in -1..65535) return@encode
        val buffer = if (context.isSecondary) {
            IoBuffer(ServerProt.MIDI_JINGLE).putA(255).putTri(0).putShort(context.musicId)
        } else {
            IoBuffer(ServerProt.MIDI_SONG).putS(2).putLEShort(context.musicId).putS(255)
        }
        context.player.details.session.write(buffer)
    }),
    AUDIO_PACKET(encode<AudioContext> { context ->
        val audio = context.audio
        val loc = context.location
        if (audio.id !in -1..65535) return@encode
        if (loc == null) {
            if (audio.loops !in 0..255 || audio.delay !in 0..65535) return@encode
        } else {
            if (audio.radius !in 0..15 || audio.loops !in 0..7 || audio.delay !in 0..255) return@encode
        }
        val buffer = if (loc == null) {
            IoBuffer(ServerProt.SOUND_SYNTH)
        } else {
            checkedZone(context.player, loc) ?: return@encode
        }
        writeAudio(buffer, audio, loc)
        context.player.session.write(buffer)
    }),

    // Messages and contacts
    MESSAGE_GAME(encode<GameMessageContext> { context ->
        val buffer = message(context.message, (System.currentTimeMillis() / 1000).toInt())
        context.player.session.write(buffer)
    }),
    COMMUNICATION_MESSAGE(encode<MessageContext> { context ->
        val player = context.player
        val other = context.other
        if (other.contains('\u0000') || context.message.contains('\u0000') || other.length > 254 || context.message.length > 32767) return@encode
        val plain = context.message.toByteArray(charset("windows-1252"))
        val bytes = ByteArray(plain.size * 4 + 8)
        val length = encryptPlayerChat(bytes, 0, 0, plain.size, plain)
        if (length < 0 || length > bytes.size || (plain.isNotEmpty() && length == 0)) return@encode
        val fixed = when (context.kind) {
            MessageContext.Kind.PRIVATE_ECHO -> 0
            MessageContext.Kind.PRIVATE_RECEIVED -> 7
            MessageContext.Kind.CLAN -> 15
        }
        if (fixed != 0 && context.chatIcon !in 0..255) return@encode
        val smartLength = if (plain.size < 128) 1 else 2
        if (fixed + other.length + 1 + smartLength + length > 255) return@encode
        val buffer = when (context.kind) {
            MessageContext.Kind.PRIVATE_ECHO -> IoBuffer(ServerProt.MESSAGE_PRIVATE_ECHO, PacketHeader.BYTE).putString(other)
            MessageContext.Kind.PRIVATE_RECEIVED -> {
                player.setAttribute("replyTo", other)
                QuickChatDelivery.putMessageId(IoBuffer(ServerProt.MESSAGE_PRIVATE, PacketHeader.BYTE).put(0).putString(other))
                    .put(context.chatIcon)
            }
            MessageContext.Kind.CLAN -> {
                val clan = player.communication.clan ?: return@encode
                QuickChatDelivery.putMessageId(IoBuffer(ServerProt.MESSAGE_CLAN, PacketHeader.BYTE).put(0).putString(other).putLong(stringToLong(clan.name)))
                    .put(context.chatIcon)
            }
        }
        buffer.putSmart(plain.size).putBytes(bytes, 0, length)
        player.session.write(buffer)
    }),
    CONTACT_PACKETS(encode<ContactContext> { context ->
        val player = context.player
        val buffer = when (context.type) {
            ContactContext.UPDATE_STATE_TYPE -> IoBuffer(ServerProt.FRIENDLIST_LOADING)
            ContactContext.IGNORE_LIST_TYPE -> {
                val payload = IoBuffer(ServerProt.UPDATE_IGNORELIST, PacketHeader.SHORT)
                val blocked = player.communication.blocked.filter { it.isNotEmpty() }.take(100)
                if (blocked.any { it.contains('\u0000') } || 1L + blocked.sumOf { 2L * it.length + 4 } > 65535) return@encode
                payload.put(blocked.size)
                for (name in blocked) {
                    payload.putString(name)
                    payload.putString("")
                    payload.putString(name)
                    payload.putString("")
                }
                val temporary = player.communication.temporaryBlocked.filter { it.isNotEmpty() }.take(100 - blocked.size)
                if (temporary.any { it.contains('\u0000') || it.length * 2 + 5 > 255 }) return@encode
                player.session.write(payload)
                for (name in temporary) {
                    // Protocol.PACKET_50: bit 0 rename, bit 1 temporary; four CP1252 strings.
                    player.session.write(IoBuffer(ServerProt.IGNORE_LIST_INCREMENTAL, PacketHeader.BYTE).put(2).putString(name).putString("").putString(name).putString(""))
                }
                return@encode
            }
            ContactContext.UPDATE_FRIEND_TYPE -> {
                val name = context.name ?: return@encode
                val rank = player.communication.contacts[name]?.rank?.value ?: 0
                if (name.contains('\u0000') || context.worldId !in 0..65535 || rank !in -1..255) return@encode
                val worldLength = if (context.worldId > 0) ("World " + context.worldId).length + 2 else 0
                if (2L * name.length + 6 + worldLength > 65535) return@encode
                val payload = IoBuffer(ServerProt.UPDATE_FRIENDLIST, PacketHeader.SHORT)
                payload.put(0)
                payload.putString(name)
                payload.putString(name)
                payload.putShort(context.worldId)
                payload.put(rank)
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
    NPC_ANIMATION_DIRECT(encode<PacketContext.DirectNpcAnimationContext> { c ->
        if (c.npcIndex !in 0..32767 || c.sequenceId !in 0..65534 || c.delay !in 0..255) return@encode
        c.player.session.write(IoBuffer(ServerProt.NPC_ANIMATION_DIRECT).putLEShort(c.npcIndex).put(c.delay).putShortA(c.sequenceId))
    }),
    CLAN_MEMBER_INCREMENTAL(encode<PacketContext.ClanMemberUpdateContext> { context ->
        val alternate = context.alternateName
        val worldName = context.worldName
        if (context.name.isEmpty() || context.world !in 0..65535 || context.rank !in -128..127 ||
            (alternate != null && alternate.isEmpty()) ||
            (context.rank == -128 && worldName != null) || (context.rank != -128 && worldName == null)) return@encode
        if (listOfNotNull(context.name, alternate, worldName).any { '\u0000' in it }) return@encode
        val length = 5L + context.name.length + (alternate?.let { it.length.toLong() + 1 } ?: 0L) +
            (worldName?.let { it.length.toLong() + 1 } ?: 0L)
        if (length > 255) return@encode
        val buffer = IoBuffer(ServerProt.CLAN_MEMBER_INCREMENTAL, PacketHeader.BYTE).putString(context.name).put(if (alternate != null) 1 else 0)
        if (alternate != null) buffer.putString(alternate)
        buffer.putShort(context.world).put(context.rank)
        if (worldName != null) buffer.putString(worldName)
        context.player.session.write(buffer)
    }),
    UPDATE_CLAN_CHAT(encode<ClanContext> { context ->
        val buffer = IoBuffer(ServerProt.UPDATE_CLAN_CHAT, PacketHeader.SHORT)
        val clan = context.clan
        if (!context.isLeave) {
            if (clan.owner.contains('\u0000')) return@encode
            val members = clan.players.take(100)
            if (members.any { it.name.contains('\u0000') || it.worldId !in 0..65535 }) return@encode
            if (12L + clan.owner.length + members.sumOf { it.name.length.toLong() + 6 + ("World " + it.worldId).length } > 65535) return@encode
            buffer.putString(clan.owner).put(0)
            buffer.putLong(stringToLong(clan.name))
            buffer.put(clan.kickRequirement.value)
            buffer.put(members.size)
            for (entry in members) {
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
            val `object` = animation.getObject() ?: return buffer
            if (!validSceneryShape(`object`) || animation.id !in -1..65535) return buffer
            val l = `object`.location ?: return buffer
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
            if (!validSceneryShape(scenery)) return buffer
            val location = scenery.location ?: return buffer
            return ZoneProt.LOC_DEL.start(buffer).putA((location.chunkOffsetX shl 4) or (location.chunkOffsetY and 0x7))
                .putC((scenery.type shl 2) + (scenery.rotation and 3))
        }

        private fun validSceneryShape(scenery: Scenery): Boolean {
            val valid = scenery.type in 0..22 && scenery.rotation in 0..3
            if (!valid) System.err.println("[Protocol578] Rejected scenery shape ${scenery.type}, rotation ${scenery.rotation}")
            return valid
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
            if (!validSceneryShape(scenery) || scenery.id !in 0..65535) return buffer
            val l = scenery.location ?: return buffer
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

// Native revision 578 frame builders. Kept private to the encoder implementation.
private fun resetEntityAnimations(): IoBuffer = IoBuffer(ServerProt.RESET_ENTITY_ANIMATIONS)

private fun resetClientVarps(): IoBuffer = IoBuffer(ServerProt.RESET_CLIENT_VARPS)

private fun runDialogAbortHooks(): IoBuffer = IoBuffer(ServerProt.RUN_DIALOG_ABORT_HOOKS)

/** Empty payload restores the client's localized default walk option. */
private fun walkOption(option: String): IoBuffer {
    require(option.length <= 254 && !option.contains('\u0000')) { "Invalid walk option" }
    val buffer = IoBuffer(ServerProt.SET_WALK_OPTION, PacketHeader.BYTE)
    if (option.isNotEmpty()) buffer.putString(option)
    return buffer
}

/** Packed target: bit 30 for tiles, bit 29 for NPCs, bit 28 for players. */
private fun globalSpotAnimation(target: Int, id: Int, delay: Int, height: Int): IoBuffer {
    val tile = (target and 0x40000000) != 0
    val npc = (target and 0xffff8000.toInt()) == 0x20000000
    val player = (target and 0xfffff800.toInt()) == 0x10000000
    require(target >= 0 && (tile || npc || player) && id in -1..65534 &&
        delay in 0..65535 && height in 0..65535 && (!tile || (id != -1 && delay == 0))) {
        "Invalid global spot animation"
    }
    return IoBuffer(ServerProt.GLOBAL_SPOT_ANIMATION).putShortA(id).putShortA(delay).putInt(target).putLEShort(height)
}

private fun logout(): IoBuffer = IoBuffer(ServerProt.LOGOUT)

private fun clearMinimapFlag(): IoBuffer = IoBuffer(ServerProt.MINIMAP_FLAG).putC(255).putS(255)

private fun minimapState(state: Int): IoBuffer {
    require(state in 0..255) { "Invalid minimap state" }
    return IoBuffer(ServerProt.MINIMAP_STATE).put(state)
}

private fun varc(id: Int, value: Int, verify: Int): IoBuffer =
    if (value in Byte.MIN_VALUE.toInt()..Byte.MAX_VALUE.toInt()) {
        IoBuffer(ServerProt.VARC_SMALL).putA(value).putShortA(verify).putLEShortA(id)
    } else {
        IoBuffer(ServerProt.VARC_LARGE).putShort(verify).putInt(value).putLEShort(id)
    }

/** Short and long string varc frames share the same delayed client state. */
private fun varcString(id: Int, value: String?, verify: Int): IoBuffer {
    require(id in 0..65535 && value != null && !value.contains('\u0000') && value.length <= 65530) {
        "Invalid varc string"
    }
    return if (value.length <= 250) {
        IoBuffer(ServerProt.VARCSTR_SMALL, PacketHeader.BYTE).putShort(id).putString(value).putLEShort(verify)
    } else {
        IoBuffer(ServerProt.VARCSTR_LARGE, PacketHeader.SHORT).putString(value).putLEShortA(id).putLEShort(verify)
    }
}

private fun openTop(type: Int, id: Int, verify: Int): IoBuffer =
    IoBuffer(ServerProt.IF_OPENTOP).put(type).putLEShort(id).putLEShort(verify)

private fun openSub(component: Int, type: Int, id: Int, verify: Int): IoBuffer =
    IoBuffer(ServerProt.IF_OPENSUB).putShortA(verify).putS(type).putLEInt(component).putShort(id)

private fun settings(component: Int, first: Int, last: Int, events: Int, verify: Int): IoBuffer =
    IoBuffer(ServerProt.SET_INTERFACE_SETTINGS).putIntA(component).putLEShort(first).putIntB(events)
        .putShortA(last).putLEShort(verify)

private fun targetParam(component: Int, first: Int, last: Int, param: Int, verify: Int): IoBuffer =
    IoBuffer(ServerProt.WIDGETSTRUCT_SETTING).putShort(first).putLEShort(param).putShortA(last)
        .putShort(verify).putIntA(component)

private fun interaction(name: String, slot: Int, primary: Boolean): IoBuffer {
    require(slot in 1..8) { "Invalid interaction slot" }
    return IoBuffer(ServerProt.SET_INTERACTION, PacketHeader.BYTE).putString(wireText(name, 250))
        .putShortA(-1).putS(slot).putS(if (primary) 1 else 0)
}

private fun message(message: String, timestamp: Int): IoBuffer =
    IoBuffer(ServerProt.MESSAGE_GAME, PacketHeader.BYTE).put(0).putInt(timestamp).put(0)
        .putString(wireText(message, 248))

private fun wireText(value: String, maxLength: Int): String = value.replace('\u0000', '?').take(maxLength)
