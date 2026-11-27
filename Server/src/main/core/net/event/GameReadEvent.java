package core.net.event;

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
    private static final int[] IF_WIRE = {12, 4, 2, 43, 63, 57, 10, 17, 54, 14};
    // Internal content listener IDs, not network opcodes; do not replace without migrating listeners.
    private static final int[] IF_LEGACY = {155, 196, 124, 199, 234, 168, 166, 64, 53, 9};
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
            case 78:
            case 59: {
                boolean run = readModifier(payload, 1);
                int x = Short.toUnsignedInt(payload.getShort());
                int z = Short.toUnsignedInt(Short.reverseBytes(payload.getShort()));
                if (opcode == 78) return new Packet.WorldspaceWalk(player, x, z, run);
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
            case 12: case 4: case 2: case 43: case 63:
            case 57: case 10: case 17: case 54: case 14: {
                int option = 0;
                while (IF_WIRE[option] != opcode) option++;
                int component = payload.getInt();
                int item = (Byte.toUnsignedInt(payload.get()) << 8)
                        | ((Byte.toUnsignedInt(payload.get()) - 128) & 255);
                int slot = payload.getShort();
                return new Packet.IfAction(player, IF_LEGACY[option], option, component >>> 16,
                        component & 65535, slot, item == 65535 ? -1 : item);
            }
            // NPC options: Static241 menu order and Protocol.java wire writes.
            case 28: { boolean run = readModifier(payload, 2); return withRun(new Packet.NpcAction(player, 0, le(payload)), run); }
            case 50: { int index = leA(payload); boolean run = readModifier(payload, 3); return withRun(new Packet.NpcAction(player, 1, index), run); }
            case 70: { int index = leA(payload); boolean run = readModifier(payload, 0); return withRun(new Packet.NpcAction(player, 2, index), run); }
            case 31: { int index = be(payload); boolean run = readModifier(payload, 2); return withRun(new Packet.NpcAction(player, 3, index), run); }
            case 68: { int index = be(payload); boolean run = readModifier(payload, 1); return withRun(new Packet.NpcAction(player, 4, index), run); }
            case 46: return new Packet.NpcExamine(player, be(payload));
            // Location options: Static327 menu order and Protocol.java wire writes.
            case 73: { int id = le(payload), x = le(payload), z = le(payload); boolean run = readModifier(payload, 0); return withRun(new Packet.SceneryAction(player, 0, id, x, z), run); }
            case 3: { int z = leA(payload), id = beA(payload), x = be(payload); boolean run = readModifier(payload, 2); return withRun(new Packet.SceneryAction(player, 1, id, x, z), run); }
            case 40: { int id = leA(payload), z = leA(payload), x = be(payload); boolean run = readModifier(payload, 0); return withRun(new Packet.SceneryAction(player, 2, id, x, z), run); }
            case 72: { int id = leA(payload); boolean run = readModifier(payload, 0); int x = be(payload), z = le(payload); return withRun(new Packet.SceneryAction(player, 3, id, x, z), run); }
            case 47: { int z = beA(payload), id = le(payload), x = beA(payload); boolean run = readModifier(payload, 2); return withRun(new Packet.SceneryAction(player, 4, id, x, z), run); }
            case 66: return new Packet.SceneryExamine(player, be(payload));
            case 0: return new Packet.ItemExamine(player, be(payload));
            case 21: {
                int component = payload.getInt();
                int slot = (short) leA(payload);
                return new Packet.ContinueOption(player, component >>> 16, component & 65535, slot, 132);
            }
            case 51:
                return new Packet.CloseIface(player);
            case 23: // EVENT_KEYBOARD: Protocol.mainLogic, key byte + BE elapsed u16.
                if (payload.remaining() % 3 != 0 || payload.remaining() > 225)
                    throw new IllegalArgumentException("Invalid keyboard telemetry length");
                while (payload.hasRemaining()) {
                    payload.get();
                    payload.getShort();
                }
                return null; // Local singleplayer does not retain keyboard telemetry.
            case 42: // Preferences.encode: version 16, 39 bytes including version.
                if (payload.remaining() != 39 || Byte.toUnsignedInt(payload.get()) != 16)
                    throw new IllegalArgumentException("Invalid revision 578 preferences payload");
                payload.position(payload.limit());
                return null; // Client graphics/audio preferences are not player settings.
            case 61: // EVENT_MOUSE_MOVE: compressed records in Protocol.mainLogic.
                while (payload.hasRemaining()) {
                    int prefix = Byte.toUnsignedInt(payload.get());
                    if (prefix < 128) payload.get();
                    else if (prefix < 192) payload.getShort();
                    else if (prefix < 224) payload.getInt();
                    else { payload.get(); payload.getInt(); }
                }
                return null; // Validate record boundaries; do not retain mouse movement.
            case 38: // Stellar Dawn-only Face here action; not a RuneScape action.
                payload.position(payload.limit());
                return null;
            case 33: // EVENT_FRAME_MAP_LOADED: desktop frame telemetry, LoginManager.java (1057001181).
                payload.getInt();
                return null;
            case 55: // Client finished building the map. No reply is required.
                return null;
            case 8: { // Protocol.method2002: window mode, canvas size, antialiasing mode.
                int mode = Byte.toUnsignedInt(payload.get());
                int width = be(payload), height = be(payload);
                int antiAliasing = Byte.toUnsignedInt(payload.get());
                if (mode < 1 || mode > 3 || antiAliasing > 2) return null;
                return new Packet.TrackingDisplayUpdate(player, mode, width, height, antiAliasing);
            }
            case 37: {
                int timing = Short.toUnsignedInt(payload.getShort());
                int coords = Integer.reverseBytes(payload.getInt());
                return new Packet.TrackingMouseClick(player, coords >>> 16, coords & 65535,
                        (timing & 32768) != 0, timing & 32767);
            }
            case 7:
                return new Packet.TrackingCameraPos(player, Short.toUnsignedInt(payload.getShort()),
                        Short.toUnsignedInt(Short.reverseBytes(payload.getShort())));
            case 44:
                return new Packet.TrackingFocus(player, readModifier(payload, 0));
            case 81:
                payload.getShort(); // Host ping result, Protocol.java:3412-3420.
                return null;
            case 19: {
                if (payload.remaining() < 3) return null;
                int channel = Byte.toUnsignedInt(payload.get());
                if (channel > 1) return null;
                int phrase = Short.toUnsignedInt(payload.getShort());
                byte[] parameters = new byte[payload.remaining()];
                payload.get(parameters);
                return new Packet.NativeQuickChat(player, channel, null, phrase, parameters);
            }
            case 30: {
                String recipient = ByteBufferUtils.getString(payload);
                if (recipient.isEmpty() || payload.remaining() < 2) return null;
                int phrase = Short.toUnsignedInt(payload.getShort());
                byte[] parameters = new byte[payload.remaining()];
                payload.get(parameters);
                return new Packet.NativeQuickChat(player, 2, recipient, phrase, parameters);
            }
            case 60: { // Chat.sendPublicMessage: channel, colour, effect, WordPack.
                if (payload.remaining() < 4) return null;
                int channel = Byte.toUnsignedInt(payload.get());
                int colour = Byte.toUnsignedInt(payload.get());
                int effect = Byte.toUnsignedInt(payload.get());
                if (channel > 1 || colour > 11 || effect > 5) return null;
                String message = readChat(payload);
                if (message.isEmpty()) return null;
                return new Packet.ChatMessage(player, colour << 8 | effect, message, channel == 1);
            }
            case 1: { // ScriptRunner 5009: recipient string, WordPack.
                String recipient = ByteBufferUtils.getString(payload);
                String message = readChat(payload);
                return recipient.isEmpty() || message.isEmpty() ? null : new Packet.PrivateMessage(player, recipient, message);
            }
            case 77: { // ScriptRunner 5002: name, rule-1, mute, comment.
                String target = ByteBufferUtils.getString(payload);
                int rule = Byte.toUnsignedInt(payload.get());
                boolean mute = payload.get() == 1;
                String comment = readText(payload);
                return new Packet.ReportAbuse(player, target, rule, mute, comment);
            }
            case 62: {
                int pub = Byte.toUnsignedInt(payload.get());
                int priv = Byte.toUnsignedInt(payload.get());
                int trade = Byte.toUnsignedInt(payload.get());
                if (priv > 2) return null;
                return new Packet.ChatSetting(player, pub, priv, trade);
            }
            case 67: // ScriptRunner.java:1326-1327
                return new Packet.InputPromptResponse(player, payload.getInt());
            case 29: // ScriptRunner.java:1333-1335
            case 74: // ScriptRunner.java:1341-1343
                return new Packet.InputPromptResponse(player, readText(payload));
            case 76: // Cheat.java:428-431
                readModifier(payload, 0); // console flag
                String command = readText(payload);
                return command.isEmpty() ? null : new Packet.Command(player, command.toLowerCase(Locale.ROOT));
            // Revision 578 social packets: FriendsList, IgnoreList and ClanChat.
            case 18: return new Packet.AddFriend(player, readText(payload));
            case 53: return new Packet.RemoveFriend(player, readText(payload));
            case 24: return new Packet.RemoveIgnore(player, readText(payload));
            case 16: {
                String name = ByteBufferUtils.getString(payload);
                if (payload.remaining() != 1) return null;
                boolean temporary = readModifier(payload, 0);
                return new Packet.AddIgnore(player, name, temporary);
            }
            case 15: return new Packet.KickFromClan(player, readText(payload));
            case 64: return new Packet.JoinClan(player, payload.hasRemaining() ? readText(payload) : "");
            case 25: // Protocol.method2432: acknowledgement of the last transmit verify ID.
                // Never let a delayed or forged acknowledgement rewind the outgoing counter.
                player.getSession().recordTransmitVerifyId(be(payload));
                return null;
            case 71: return new Packet.Ping(player);
            case 20: return new Packet.TrackFinished(player, payload.getInt());
            // Ground item options, Protocol.java and Static327 menu ordering.
            case 22: { int id = leA(payload), z = beA(payload), x = leA(payload); boolean run = readModifier(payload, 3); return withRun(new Packet.GroundItemAction(player, 0, id, x, z), run); }
            case 5: { int z = le(payload), id = be(payload); boolean run = readModifier(payload, 3); int x = beA(payload); return withRun(new Packet.GroundItemAction(player, 1, id, x, z), run); }
            case 80: { int id = leA(payload); boolean run = readModifier(payload, 1); int z = be(payload), x = le(payload); return withRun(new Packet.GroundItemAction(player, 2, id, x, z), run); }
            case 79: { int id = be(payload); boolean run = readModifier(payload, 1); int x = beA(payload), z = be(payload); return withRun(new Packet.GroundItemAction(player, 3, id, x, z), run); }
            case 26: { int z = leA(payload), x = be(payload); boolean run = readModifier(payload, 3); int id = le(payload); return withRun(new Packet.GroundItemAction(player, 4, id, x, z), run); }
            case 39: { boolean run = readModifier(payload, 0); return withRun(new Packet.PlayerAction(player, 0, leA(payload)), run); }
            case 32: { int id = leA(payload); boolean run = readModifier(payload, 2); return withRun(new Packet.PlayerAction(player, 1, id), run); }
            case 41: { boolean run = readModifier(payload, 0); return withRun(new Packet.PlayerAction(player, 2, le(payload)), run); }
            case 69: { boolean run = readModifier(payload, 2); return withRun(new Packet.PlayerAction(player, 3, le(payload)), run); }
            case 36: { int id = le(payload); boolean run = readModifier(payload, 3); return withRun(new Packet.PlayerAction(player, 4, id), run); }
            case 45: { int id = le(payload); boolean run = readModifier(payload, 1); return withRun(new Packet.PlayerAction(player, 5, id), run); }
            case 27: { boolean run = readModifier(payload, 3); return withRun(new Packet.PlayerAction(player, 6, leA(payload)), run); }
            case 35: { int id = leA(payload); boolean run = readModifier(payload, 1); return withRun(new Packet.PlayerAction(player, 7, id), run); }
            // Protocol.method4427 and Static185.method3400: selected component target.
            case 75: {
                int source = payload.getInt(); boolean run = readModifier(payload, 3);
                int item = leA(payload), slot = le(payload), npc = be(payload);
                if (!active(player, source) || npc <= 0 || npc >= core.ServerConstants.MAX_NPCS) return null;
                return withSelection(item == 65535 ? new Packet.ComponentNpcAction(player, source >>> 16, source & 65535, npc)
                        : new Packet.UseWithNpc(player, item, npc, source >>> 16, slot), source, null, slot, run);
            }
            case 48: {
                int source = Integer.reverseBytes(payload.getInt()); boolean run = readModifier(payload, 3);
                int slot = le(payload), item = leA(payload), target = le(payload);
                if (!active(player, source) || target <= 0 || target >= core.ServerConstants.MAX_PLAYERS) return null;
                return withSelection(item == 65535 ? new Packet.ComponentPlayerAction(player, target, source >>> 16, source & 65535)
                        : new Packet.UseWithPlayer(player, item, target, source >>> 16, slot), source, null, slot, run);
            }
            case 6: {
                int item = be(payload), target = leA(payload), x = le(payload); boolean run = readModifier(payload, 2);
                int slot = leA(payload), source = intAlt3(payload), z = leA(payload);
                if (!active(player, source) || x > 16383 || z > 16383) return null;
                return withSelection(item == 65535 ? new Packet.ComponentGroundItemAction(player, source >>> 16, source & 65535, slot, target, x, z)
                        : new Packet.UseWithGroundItem(player, item, target, source >>> 16, source & 65535, slot, x, z), source, null, slot, run);
            }
            case 9: {
                int target = le(payload), source = intAlt2(payload), item = be(payload); boolean run = readModifier(payload, 0);
                int slot = leA(payload), x = be(payload), z = be(payload);
                if (!active(player, source) || x > 16383 || z > 16383) return null;
                if (item == 65535) return withSelection(new Packet.ComponentSceneryAction(player, source >>> 16, source & 65535, target, x, z), source, null, slot, run);
                return withSelection(new Packet.UseWithScenery(player, item, slot, target, x, z), source, null, slot, run);
            }
            case 11: { // Protocol.method6129: component target.
                int target = payload.getInt(), targetSlot = beA(payload), slot = beA(payload);
                int item = be(payload), source = intAlt3(payload), targetItem = be(payload);
                if (!active(player, source) || !active(player, target) || targetItem == 65535) return null;
                if (item == 65535) {
                    return withSelection(new Packet.ComponentItemAction(player, source >>> 16, source & 65535, targetItem, targetSlot), source, target, slot, null);
                }
                return withSelection(new Packet.UseWithItem(player, item, targetItem, slot, targetSlot,
                        source >>> 16, target >>> 16, source & 65535, target & 65535), source, target, slot, null);
            }
            case 13: { // Protocol.method5096 / Cs1ScriptRunner: source then destination.
                int source = payload.getInt(), item = leA(payload), slot = beA(payload);
                int target = intAlt3(payload), targetItem = leA(payload), targetSlot = le(payload);
                if (!active(player, source) || !active(player, target)) return null;
                // Keep both wire item IDs and widget hashes until the game-thread validation.
                return new Packet.NativeDrag(player, source, item, slot, target, targetItem, targetSlot);
            }
            case 49: { // FriendsList.setRank: signed rank + 128, CP1252 name.
                int rank = (byte) (payload.get() - 128);
                String name = readText(payload);
                if (name.isEmpty() || rank < 0 || rank > 6) return null;
                return new Packet.SetClanRank(player, name, rank);
            }
            case 34: { // ReflectionCheck.mainLogic: challenge ID, typed results, CRC32.
                // No challenge producer is implemented; never accept an unsolicited result as verified.
                validateReflectionEnvelope(payload);
                return null;
            }
            case 52: { // Protocol.method4427, menu action 51: selected component on tile.
                int slot = le(payload), component = payload.getInt(), z = leA(payload);
                int item = le(payload), x = leA(payload);
                if (!active(player, component) || x > 16383 || z > 16383) return null;
                // UNRESOLVED: no confirmed tile-target listener or spell mapping.
                return null;
            }
            case 56: { // ScriptRunner opcode 5400: two CP1252 strings and a mode byte.
                ByteBufferUtils.getString(payload);
                ByteBufferUtils.getString(payload);
                payload.get();
                if (payload.hasRemaining()) throw new IllegalArgumentException("Trailing URL request bytes");
                // Local-only policy: never relay arbitrary client URLs or launch a browser.
                return null;
            }
            case 65: { // Protocol.mainLogic / Static45: packed source coordinate, little-endian.
                int coordinate = Integer.reverseBytes(payload.getInt());
                if ((coordinate >>> 28) > 3) return null;
                // UNRESOLVED: world-map click server behavior. This is not a movement command.
                return null;
            }
            case 58: return new Packet.GESetOfferItem(player, be(payload)); // Generic wire reply; current game-thread caller is GE search only.
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
