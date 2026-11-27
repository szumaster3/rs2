package core.net.event;

import core.net.packet.ClientProt;

import core.cache.misc.buffer.ByteBufferUtils;
import core.game.node.entity.player.Player;
import core.net.IoReadEvent;
import core.net.IoSession;
import core.net.packet.PacketProcessor;
import core.net.packet.IoBuffer;
import core.net.packet.PacketHeader;
import core.tools.StringUtils;
import core.net.packet.in.Packet;
import java.nio.ByteBuffer;
import java.util.Locale;

public final class GameReadEvent extends IoReadEvent {
    private static final int[] IF_WIRE = {ClientProt.IF_BUTTON1, ClientProt.IF_BUTTON2, ClientProt.IF_BUTTON3, ClientProt.IF_BUTTON4, ClientProt.IF_BUTTON5, ClientProt.IF_BUTTON6, ClientProt.IF_BUTTON7, ClientProt.IF_BUTTON8, ClientProt.IF_BUTTON9, ClientProt.IF_BUTTON10};
    public GameReadEvent(IoSession session, ByteBuffer buffer) {
        super(session, buffer);
    }

    @Override
    public void read(IoSession session, ByteBuffer buffer) {
        Player player = session.getPlayer();
        if (player == null) {
            session.disconnect();
            return;
        }
        session.gameInput.read(buffer, session.getIsaacPair().getInput(), (opcode, payload) -> {
            session.setLastPing(System.currentTimeMillis());
            if (player.opCounts[opcode] >= 10) return;
            player.opCounts[opcode]++;
            Packet packet;
            try {
                packet = decode(player, opcode, payload);
            } catch (java.nio.BufferUnderflowException | IllegalArgumentException malformed) {
                return;
            }
            if (player.<Boolean>getAttribute("logging_in", false) && !(packet instanceof Packet.CloseIface)
                    && !(packet instanceof Packet.TrackingDisplayUpdate)) return;
            if (packet != null) PacketProcessor.enqueue(packet);
        });
    }

    static Packet decode(Player player, int opcode, ByteBuffer payload) {
        switch (opcode) {
            case ClientProt.MOVE_GAMECLICK:
            case ClientProt.MOVE_MINIMAPCLICK: {
                boolean run = readModifier(payload, 1);
                int x = Short.toUnsignedInt(payload.getShort());
                int z = Short.toUnsignedInt(Short.reverseBytes(payload.getShort()));
                if (opcode == ClientProt.MOVE_GAMECLICK) return new Packet.WorldspaceWalk(player, x, z, run);
                int clickX = payload.get();
                int clickY = payload.get();
                int yaw = Short.toUnsignedInt(payload.getShort());
                // Protocol.method4427: fixed check bytes around the camera and fine coordinates.
                if (Byte.toUnsignedInt(payload.get()) != 57) return null;
                payload.get(); // client camera telemetry
                payload.get(); // client camera telemetry
                if (Byte.toUnsignedInt(payload.get()) != 89) return null;
                payload.getShort(); // local player fine X
                payload.getShort(); // local player fine Z
                if (Byte.toUnsignedInt(payload.get()) != 63) return null;
                return new Packet.MinimapWalk(player, x, z, clickX, clickY, yaw, run);
            }
            case ClientProt.IF_BUTTON1: case ClientProt.IF_BUTTON2: case ClientProt.IF_BUTTON3: case ClientProt.IF_BUTTON4: case ClientProt.IF_BUTTON5:
            case ClientProt.IF_BUTTON6: case ClientProt.IF_BUTTON7: case ClientProt.IF_BUTTON8: case ClientProt.IF_BUTTON9: case ClientProt.IF_BUTTON10: {
                int option = 0;
                while (IF_WIRE[option] != opcode) option++;
                int component = payload.getInt();
                int item = (Byte.toUnsignedInt(payload.get()) << 8)
                        | ((Byte.toUnsignedInt(payload.get()) - 128) & 255);
                int slot = payload.getShort();
                return new Packet.IfAction(player, opcode, option, component >>> 16,
                        component & 65535, slot, item == 65535 ? -1 : item);
            }
            // NPC options: Static241 menu order and Protocol.java wire writes.
            case ClientProt.OPNPC1: { boolean run = readModifier(payload, 2); return withRun(new Packet.NpcAction(player, 0, le(payload)), run); }
            case ClientProt.OPNPC2: { int index = leA(payload); boolean run = readModifier(payload, 3); return withRun(new Packet.NpcAction(player, 1, index), run); }
            case ClientProt.OPNPC3: { int index = leA(payload); boolean run = readModifier(payload, 0); return withRun(new Packet.NpcAction(player, 2, index), run); }
            case ClientProt.OPNPC4: { int index = be(payload); boolean run = readModifier(payload, 2); return withRun(new Packet.NpcAction(player, 3, index), run); }
            case ClientProt.OPNPC5: { int index = be(payload); boolean run = readModifier(payload, 1); return withRun(new Packet.NpcAction(player, 4, index), run); }
            case ClientProt.OPNPC_EXAMINE: return new Packet.NpcExamine(player, be(payload));
            // Location options: Static327 menu order and Protocol.java wire writes.
            case ClientProt.OPLOC1: { int id = le(payload), x = le(payload), z = le(payload); boolean run = readModifier(payload, 0); return withRun(new Packet.SceneryAction(player, 0, id, x, z), run); }
            case ClientProt.OPLOC2: { int z = leA(payload), id = beA(payload), x = be(payload); boolean run = readModifier(payload, 2); return withRun(new Packet.SceneryAction(player, 1, id, x, z), run); }
            case ClientProt.OPLOC3: { int id = leA(payload), z = leA(payload), x = be(payload); boolean run = readModifier(payload, 0); return withRun(new Packet.SceneryAction(player, 2, id, x, z), run); }
            case ClientProt.OPLOC4: { int id = leA(payload); boolean run = readModifier(payload, 0); int x = be(payload), z = le(payload); return withRun(new Packet.SceneryAction(player, 3, id, x, z), run); }
            case ClientProt.OPLOC5: { int z = beA(payload), id = le(payload), x = beA(payload); boolean run = readModifier(payload, 2); return withRun(new Packet.SceneryAction(player, 4, id, x, z), run); }
            case ClientProt.OPLOC_EXAMINE: return new Packet.SceneryExamine(player, be(payload));
            case ClientProt.OPOBJ_EXAMINE: return new Packet.ItemExamine(player, be(payload));
            case ClientProt.IF_BUTTON_CONTINUE: {
                int component = payload.getInt();
                int slot = (short) leA(payload);
                return new Packet.ContinueOption(player, component >>> 16, component & 65535, slot, opcode);
            }
            case ClientProt.CLOSE_MODAL:
                return new Packet.CloseIface(player);
            case ClientProt.EVENT_KEYBOARD: // EVENT_KEYBOARD: Protocol.mainLogic, key byte + BE elapsed u16.
                if (payload.remaining() % 3 != 0 || payload.remaining() > 225)
                    throw new IllegalArgumentException("Invalid keyboard telemetry length");
                while (payload.hasRemaining()) {
                    payload.get();
                    payload.getShort();
                }
                return null; // Local singleplayer does not retain keyboard telemetry.
            case ClientProt.CLIENT_PREFERENCES: // Preferences.encode: version 16, 39 bytes including version.
                if (payload.remaining() != 39 || Byte.toUnsignedInt(payload.get()) != 16)
                    throw new IllegalArgumentException("Invalid revision 578 preferences payload");
                payload.position(payload.limit());
                return null; // Client graphics/audio preferences are not player settings.
            case ClientProt.EVENT_MOUSE_MOVE: // EVENT_MOUSE_MOVE: compressed records in Protocol.mainLogic.
                while (payload.hasRemaining()) {
                    int prefix = Byte.toUnsignedInt(payload.get());
                    if (prefix < 128) payload.get();
                    else if (prefix < 192) payload.getShort();
                    else if (prefix < 224) payload.getInt();
                    else { payload.get(); payload.getInt(); }
                }
                return null; // Validate record boundaries; do not retain mouse movement.
            case ClientProt.FACE_HERE: // Stellar Dawn-only Face here action; not a RuneScape action.
                payload.position(payload.limit());
                return null;
            case ClientProt.EVENT_FRAME_MAP_LOADED: // EVENT_FRAME_MAP_LOADED: desktop frame telemetry, LoginManager.java (1057001181).
                payload.getInt();
                return null;
            case ClientProt.MAP_BUILD_COMPLETE: // Client finished building the map. No reply is required.
                return null;
            case ClientProt.WINDOW_STATUS: { // Protocol.method2002: window mode, canvas size, antialiasing mode.
                int mode = Byte.toUnsignedInt(payload.get());
                int width = be(payload), height = be(payload);
                int antiAliasing = Byte.toUnsignedInt(payload.get());
                if (mode < 1 || mode > 3 || antiAliasing > 2) return null;
                return new Packet.TrackingDisplayUpdate(player, mode, width, height, antiAliasing);
            }
            case ClientProt.EVENT_MOUSE_CLICK: {
                int timing = Short.toUnsignedInt(payload.getShort());
                int coords = Integer.reverseBytes(payload.getInt());
                return new Packet.TrackingMouseClick(player, coords >>> 16, coords & 65535,
                        (timing & 32768) != 0, timing & 32767);
            }
            case ClientProt.EVENT_CAMERA_POSITION:
                return new Packet.TrackingCameraPos(player, Short.toUnsignedInt(payload.getShort()),
                        Short.toUnsignedInt(Short.reverseBytes(payload.getShort())));
            case ClientProt.EVENT_APPLET_FOCUS:
                return new Packet.TrackingFocus(player, readModifier(payload, 0));
            case ClientProt.EVENT_HOST_PING:
                payload.getShort(); // Host ping result, Protocol.java:3412-3420.
                return null;
            case ClientProt.MESSAGE_QUICKCHAT_PUBLIC: {
                if (payload.remaining() < 3) return null;
                int channel = Byte.toUnsignedInt(payload.get());
                if (channel > 1) return null;
                int phrase = Short.toUnsignedInt(payload.getShort());
                byte[] parameters = new byte[payload.remaining()];
                payload.get(parameters);
                return new Packet.NativeQuickChat(player, channel, null, phrase, parameters);
            }
            case ClientProt.MESSAGE_QUICKCHAT_PRIVATE: {
                String recipient = ByteBufferUtils.getString(payload);
                if (recipient.isEmpty() || payload.remaining() < 2) return null;
                int phrase = Short.toUnsignedInt(payload.getShort());
                byte[] parameters = new byte[payload.remaining()];
                payload.get(parameters);
                return new Packet.NativeQuickChat(player, 2, recipient, phrase, parameters);
            }
            case ClientProt.MESSAGE_PUBLIC: { // Chat.sendPublicMessage: channel, colour, effect, WordPack.
                if (payload.remaining() < 4) return null;
                int channel = Byte.toUnsignedInt(payload.get());
                int colour = Byte.toUnsignedInt(payload.get());
                int effect = Byte.toUnsignedInt(payload.get());
                if (channel > 1 || colour > 11 || effect > 5) return null;
                String message = readChat(payload);
                if (message.isEmpty()) return null;
                return new Packet.ChatMessage(player, colour << 8 | effect, message, channel == 1);
            }
            case ClientProt.MESSAGE_PRIVATE: { // ScriptRunner 5009: recipient string, WordPack.
                String recipient = ByteBufferUtils.getString(payload);
                String message = readChat(payload);
                return recipient.isEmpty() || message.isEmpty() ? null : new Packet.PrivateMessage(player, recipient, message);
            }
            case ClientProt.REPORT_ABUSE: { // ScriptRunner 5002: name, rule-1, mute, comment.
                String target = ByteBufferUtils.getString(payload);
                int rule = Byte.toUnsignedInt(payload.get());
                boolean mute = readModifier(payload, 0);
                String comment = readText(payload);
                return new Packet.ReportAbuse(player, target, rule, mute, comment);
            }
            case ClientProt.SET_CHATFILTERSETTINGS: {
                int pub = Byte.toUnsignedInt(payload.get());
                int priv = Byte.toUnsignedInt(payload.get());
                int trade = Byte.toUnsignedInt(payload.get());
                if (priv > 2) return null;
                return new Packet.ChatSetting(player, pub, priv, trade);
            }
            case ClientProt.RESUME_P_COUNTDIALOG: // ScriptRunner.java:1326-1327
                return new Packet.InputPromptResponse(player, payload.getInt());
            case ClientProt.RESUME_P_NAMEDIALOG: // ScriptRunner.java:1333-1335
            case ClientProt.RESUME_P_STRINGDIALOG: // ScriptRunner.java:1341-1343
                return new Packet.InputPromptResponse(player, readText(payload));
            case ClientProt.CLIENT_CHEAT: // Cheat.java:428-431
                readModifier(payload, 0); // console flag
                String command = readText(payload);
                return command.isEmpty() ? null : new Packet.Command(player, command.toLowerCase(Locale.ROOT));
            // Revision 578 social packets: FriendsList, IgnoreList and ClanChat.
            case ClientProt.FRIEND_ADD: return new Packet.AddFriend(player, readText(payload));
            case ClientProt.FRIEND_REMOVE: return new Packet.RemoveFriend(player, readText(payload));
            case ClientProt.IGNORE_REMOVE: return new Packet.RemoveIgnore(player, readText(payload));
            case ClientProt.IGNORE_ADD: {
                String name = ByteBufferUtils.getString(payload);
                if (payload.remaining() != 1) return null;
                boolean temporary = readModifier(payload, 0);
                return new Packet.AddIgnore(player, name, temporary);
            }
            case ClientProt.CLAN_KICK: return new Packet.KickFromClan(player, readText(payload));
            case ClientProt.CLAN_JOIN_LEAVE: return new Packet.JoinClan(player, payload.hasRemaining() ? readText(payload) : "");
            case ClientProt.TRANSMITVAR_VERIFYID: // Protocol.method2432: acknowledgement of the last transmit verify ID.
                // Never let a delayed or forged acknowledgement rewind the outgoing counter.
                player.getSession().recordTransmitVerifyId(be(payload));
                return null;
            case ClientProt.NO_TIMEOUT: return new Packet.Ping(player);
            case ClientProt.SOUND_SONGEND: return new Packet.TrackFinished(player, payload.getInt());
            // Ground item options, Protocol.java and Static327 menu ordering.
            case ClientProt.OPOBJ1: { int id = leA(payload), z = beA(payload), x = leA(payload); boolean run = readModifier(payload, 3); return withRun(new Packet.GroundItemAction(player, 0, id, x, z), run); }
            case ClientProt.OPOBJ2: { int z = le(payload), id = be(payload); boolean run = readModifier(payload, 3); int x = beA(payload); return withRun(new Packet.GroundItemAction(player, 1, id, x, z), run); }
            case ClientProt.OPOBJ3: { int id = leA(payload); boolean run = readModifier(payload, 1); int z = be(payload), x = le(payload); return withRun(new Packet.GroundItemAction(player, 2, id, x, z), run); }
            case ClientProt.OPOBJ4: { int id = be(payload); boolean run = readModifier(payload, 1); int x = beA(payload), z = be(payload); return withRun(new Packet.GroundItemAction(player, 3, id, x, z), run); }
            case ClientProt.OPOBJ5: { int z = leA(payload), x = be(payload); boolean run = readModifier(payload, 3); int id = le(payload); return withRun(new Packet.GroundItemAction(player, 4, id, x, z), run); }
            case ClientProt.OPPLAYER1: { boolean run = readModifier(payload, 0); return withRun(new Packet.PlayerAction(player, 0, leA(payload)), run); }
            case ClientProt.OPPLAYER2: { int id = leA(payload); boolean run = readModifier(payload, 2); return withRun(new Packet.PlayerAction(player, 1, id), run); }
            case ClientProt.OPPLAYER3: { boolean run = readModifier(payload, 0); return withRun(new Packet.PlayerAction(player, 2, le(payload)), run); }
            case ClientProt.OPPLAYER4: { boolean run = readModifier(payload, 2); return withRun(new Packet.PlayerAction(player, 3, le(payload)), run); }
            case ClientProt.OPPLAYER5: { int id = le(payload); boolean run = readModifier(payload, 3); return withRun(new Packet.PlayerAction(player, 4, id), run); }
            case ClientProt.OPPLAYER6: { int id = le(payload); boolean run = readModifier(payload, 1); return withRun(new Packet.PlayerAction(player, 5, id), run); }
            case ClientProt.OPPLAYER7: { boolean run = readModifier(payload, 3); return withRun(new Packet.PlayerAction(player, 6, leA(payload)), run); }
            case ClientProt.OPPLAYER8: { int id = leA(payload); boolean run = readModifier(payload, 1); return withRun(new Packet.PlayerAction(player, 7, id), run); }
            // Protocol.method4427 and Static185.method3400: selected component target.
            case ClientProt.IF_ON_NPC: {
                int source = payload.getInt(); boolean run = readModifier(payload, 3);
                int item = leA(payload), slot = le(payload), npc = be(payload);
                if (!active(player, source) || npc <= 0 || npc >= core.ServerConstants.MAX_NPCS) return null;
                return withSelection(item == 65535 ? new Packet.ComponentNpcAction(player, source >>> 16, source & 65535, npc)
                        : new Packet.UseWithNpc(player, item, npc, source >>> 16, slot), source, null, slot, run);
            }
            case ClientProt.IF_ON_PLAYER: {
                int source = Integer.reverseBytes(payload.getInt()); boolean run = readModifier(payload, 3);
                int slot = le(payload), item = leA(payload), target = le(payload);
                if (!active(player, source) || target <= 0 || target >= core.ServerConstants.MAX_PLAYERS) return null;
                return withSelection(item == 65535 ? new Packet.ComponentPlayerAction(player, target, source >>> 16, source & 65535)
                        : new Packet.UseWithPlayer(player, item, target, source >>> 16, slot), source, null, slot, run);
            }
            case ClientProt.IF_ON_GROUNDITEM: {
                int item = be(payload), target = leA(payload), x = le(payload); boolean run = readModifier(payload, 2);
                int slot = leA(payload), source = intAlt3(payload), z = leA(payload);
                if (!active(player, source) || x > 16383 || z > 16383) return null;
                return withSelection(item == 65535 ? new Packet.ComponentGroundItemAction(player, source >>> 16, source & 65535, slot, target, x, z)
                        : new Packet.UseWithGroundItem(player, item, target, source >>> 16, source & 65535, slot, x, z), source, null, slot, run);
            }
            case ClientProt.IF_ON_LOC: {
                int target = le(payload), source = intAlt2(payload), item = be(payload); boolean run = readModifier(payload, 0);
                int slot = leA(payload), x = be(payload), z = be(payload);
                if (!active(player, source) || x > 16383 || z > 16383) return null;
                if (item == 65535) return withSelection(new Packet.ComponentSceneryAction(player, source >>> 16, source & 65535, target, x, z), source, null, slot, run);
                return withSelection(new Packet.UseWithScenery(player, item, slot, target, x, z), source, null, slot, run);
            }
            case ClientProt.IF_ON_IF: { // Protocol.method6129: component target.
                int target = payload.getInt(), targetSlot = beA(payload), slot = beA(payload);
                int item = be(payload), source = intAlt3(payload), targetItem = be(payload);
                if (!active(player, source) || !active(player, target) || targetItem == 65535) return null;
                if (item == 65535) {
                    return withSelection(new Packet.ComponentItemAction(player, source >>> 16, source & 65535, targetItem, targetSlot), source, target, slot, null);
                }
                return withSelection(new Packet.UseWithItem(player, item, targetItem, slot, targetSlot,
                        source >>> 16, target >>> 16, source & 65535, target & 65535), source, target, slot, null);
            }
            case ClientProt.IF_DRAG: { // Protocol.method5096 / Cs1ScriptRunner: source then destination.
                int source = payload.getInt(), item = leA(payload), slot = beA(payload);
                int target = intAlt3(payload), targetItem = leA(payload), targetSlot = le(payload);
                if (!active(player, source) || !active(player, target)) return null;
                // Keep both wire item IDs and widget hashes until the game-thread validation.
                return new Packet.NativeDrag(player, source, item, slot, target, targetItem, targetSlot);
            }
            case ClientProt.FRIEND_SETRANK: { // FriendsList.setRank: signed rank + 128, CP1252 name.
                int rank = (byte) (payload.get() - 128);
                String name = readText(payload);
                if (name.isEmpty() || rank < 0 || rank > 6) return null;
                return new Packet.SetClanRank(player, name, rank);
            }
            case ClientProt.REFLECTION_CHECK: { // ReflectionCheck.mainLogic: challenge ID, typed results, CRC32.
                // No challenge producer is implemented; never accept an unsolicited result as verified.
                validateReflectionEnvelope(payload);
                return null;
            }
            case ClientProt.IF_ON_TILE: { // Protocol.method4427, menu action 51: selected component on tile.
                int slot = le(payload), component = payload.getInt(), z = leA(payload);
                int item = le(payload), x = leA(payload);
                if (!active(player, component) || x > 16383 || z > 16383) return null;
                // UNRESOLVED: no confirmed tile-target listener or spell mapping.
                return null;
            }
            case ClientProt.URL_REQUEST: { // ScriptRunner opcode 5400: two CP1252 strings and a mode byte.
                ByteBufferUtils.getString(payload);
                ByteBufferUtils.getString(payload);
                payload.get();
                if (payload.hasRemaining()) throw new IllegalArgumentException("Trailing URL request bytes");
                // Local-only policy: never relay arbitrary client URLs or launch a browser.
                return null;
            }
            case ClientProt.WORLD_MAP_CLICK: { // Protocol.mainLogic / Static45: packed source coordinate, little-endian.
                int coordinate = Integer.reverseBytes(payload.getInt());
                if ((coordinate >>> 28) > 3) return null;
                // UNRESOLVED: world-map click server behavior. This is not a movement command.
                return null;
            }
            case ClientProt.RESUME_P_OBJDIALOG: return new Packet.GESetOfferItem(player, be(payload)); // Generic wire reply; current game-thread caller is GE search only.
            default:
                // Keep-alives and as-yet unmapped frames end here.
                return null;
        }
    }

    private static void validateReflectionEnvelope(ByteBuffer payload) {
        if (payload.remaining() < 8) throw new IllegalArgumentException("Truncated reflection response");
        int end = payload.limit() - Integer.BYTES;
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        ByteBuffer data = payload.duplicate();
        data.limit(end);
        crc.update(data);
        if ((int) crc.getValue() != payload.getInt(end))
            throw new IllegalArgumentException("Invalid reflection response CRC");
        payload.position(payload.limit());
    }

    private static String readChat(ByteBuffer payload) {
        if (!payload.hasRemaining()) return "";
        IoBuffer input = new IoBuffer(-1, PacketHeader.NORMAL, payload);
        int count = input.getSmart();
        if (count < 1 || count > 255) return "";
        String raw = StringUtils.decryptPlayerChat(input, count);
        if (raw.length() != count || payload.hasRemaining()) return "";
        StringBuilder text = new StringBuilder(count);
        for (int i = 0; i < raw.length(); i++) {
            byte value = (byte) raw.charAt(i);
            if (value == 0) return "";
            text.append(StringUtils.getFromByte(value));
        }
        return text.toString();
    }

    private static boolean active(Player player, int component) {
        core.game.component.Component widget = player.getInterfaceManager().getComponent(component >>> 16);
        return widget != null && !widget.isHidden();
    }

    private static Packet withRun(Packet packet, boolean run) {
        packet.setInteractionRun(run);
        return packet;
    }

    private static Packet withSelection(Packet packet, int source, Integer target, int sourceSlot, Boolean run) {
        packet.setNativeSelection(new Packet.NativeSelection(source, target, sourceSlot));
        packet.setInteractionRun(run);
        return packet;
    }

    // Inverse of client Packet.p4_alt2 and p4_alt3 respectively.
    private static int intAlt2(ByteBuffer b) {
        int a = Byte.toUnsignedInt(b.get()), c = Byte.toUnsignedInt(b.get());
        int d = Byte.toUnsignedInt(b.get()), e = Byte.toUnsignedInt(b.get());
        return d << 24 | e << 16 | a << 8 | c;
    }
    private static int intAlt3(ByteBuffer b) {
        int a = Byte.toUnsignedInt(b.get()), c = Byte.toUnsignedInt(b.get());
        int d = Byte.toUnsignedInt(b.get()), e = Byte.toUnsignedInt(b.get());
        return c << 24 | a << 16 | e << 8 | d;
    }

    // Client Packet.p1 / p1_alt1 / p1_alt2 / p1_alt3 write boolean modifiers.
    private static boolean readModifier(ByteBuffer b, int transform) {
        int raw = Byte.toUnsignedInt(b.get());
        int value;
        switch (transform) {
            case 0: value = raw; break;
            case 1: value = (raw - 128) & 255; break;
            case 2: value = (-raw) & 255; break;
            case 3: value = (128 - raw) & 255; break;
            default: throw new IllegalArgumentException("Unknown modifier transform");
        }
        if (value > 1) throw new IllegalArgumentException("Invalid interaction modifier");
        return value == 1;
    }

    private static int be(ByteBuffer b) { return Short.toUnsignedInt(b.getShort()); }
    private static int le(ByteBuffer b) { return Short.toUnsignedInt(Short.reverseBytes(b.getShort())); }
    private static int beA(ByteBuffer b) { return (Byte.toUnsignedInt(b.get()) << 8) | ((b.get() - 128) & 255); }
    private static int leA(ByteBuffer b) { int low = (b.get() - 128) & 255; return low | (Byte.toUnsignedInt(b.get()) << 8); }

    private static String readText(ByteBuffer payload) {
        String value = ByteBufferUtils.getString(payload);
        if (payload.hasRemaining()) throw new IllegalArgumentException("Trailing bytes after client string");
        return value;
    }
}
