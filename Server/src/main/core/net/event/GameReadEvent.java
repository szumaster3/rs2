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
                boolean run = ((payload.get() - 128) & 255) == 1;
                int x = Short.toUnsignedInt(payload.getShort());
                int z = Short.toUnsignedInt(Short.reverseBytes(payload.getShort()));
                if (opcode == 78) return new Packet.WorldspaceWalk(player, x, z, run);
                int clickX = payload.get();
                int clickY = payload.get();
                int yaw = Short.toUnsignedInt(payload.getShort());
                // Four camera/check bytes, two fine coordinates and a final check byte.
                payload.position(payload.position() + 9);
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
            case 28: payload.get(); return new Packet.NpcAction(player, 0, le(payload));
            case 50: { int index = leA(payload); payload.get(); return new Packet.NpcAction(player, 1, index); }
            case 70: { int index = leA(payload); payload.get(); return new Packet.NpcAction(player, 2, index); }
            case 31: { int index = be(payload); payload.get(); return new Packet.NpcAction(player, 3, index); }
            case 68: { int index = be(payload); payload.get(); return new Packet.NpcAction(player, 4, index); }
            case 46: return new Packet.NpcExamine(player, be(payload));
            // Location options: Static327 menu order and Protocol.java wire writes.
            case 73: { int id = le(payload), x = le(payload), z = le(payload); payload.get(); return new Packet.SceneryAction(player, 0, id, x, z); }
            case 3: { int z = leA(payload), id = beA(payload), x = be(payload); payload.get(); return new Packet.SceneryAction(player, 1, id, x, z); }
            case 40: { int id = leA(payload), z = leA(payload), x = be(payload); payload.get(); return new Packet.SceneryAction(player, 2, id, x, z); }
            case 72: { int id = leA(payload); payload.get(); int x = be(payload), z = le(payload); return new Packet.SceneryAction(player, 3, id, x, z); }
            case 47: { int z = beA(payload), id = le(payload), x = beA(payload); payload.get(); return new Packet.SceneryAction(player, 4, id, x, z); }
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
            case 33: // EVENT_FRAME_MAP_LOADED: desktop frame telemetry, LoginManager.java.
                payload.getInt();
                return null;
            case 55: // Client finished building the map. No reply is required.
                return null;
            case 8:
                return new Packet.TrackingDisplayUpdate(player, Byte.toUnsignedInt(payload.get()),
                        Short.toUnsignedInt(payload.getShort()), Short.toUnsignedInt(payload.getShort()),
                        Byte.toUnsignedInt(payload.get()));
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
                return new Packet.TrackingFocus(player, payload.get() == 1);
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
                payload.get(); // console flag
                String command = readText(payload);
                return command.isEmpty() ? null : new Packet.Command(player, command.toLowerCase(Locale.ROOT));
            // Revision 578 social packets: FriendsList, IgnoreList and ClanChat.
            case 18: return new Packet.AddFriend(player, readText(payload));
            case 53: return new Packet.RemoveFriend(player, readText(payload));
            case 24: return new Packet.RemoveIgnore(player, readText(payload));
            case 16: {
                String name = ByteBufferUtils.getString(payload);
                if (payload.remaining() != 1) return null;
                int temporary = Byte.toUnsignedInt(payload.get());
                // Existing ignore storage is permanent; do not persist temporary ignores.
                return temporary == 0 ? new Packet.AddIgnore(player, name) : null;
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
            case 22: { int id = leA(payload), z = beA(payload), x = leA(payload); payload.get(); return new Packet.GroundItemAction(player, 0, id, x, z); }
            case 5: { int z = le(payload), id = be(payload); payload.get(); int x = beA(payload); return new Packet.GroundItemAction(player, 1, id, x, z); }
            case 80: { int id = leA(payload); payload.get(); int z = be(payload), x = le(payload); return new Packet.GroundItemAction(player, 2, id, x, z); }
            case 79: { int id = be(payload); payload.get(); int x = beA(payload), z = be(payload); return new Packet.GroundItemAction(player, 3, id, x, z); }
            case 26: { int z = leA(payload), x = be(payload); payload.get(); int id = le(payload); return new Packet.GroundItemAction(player, 4, id, x, z); }
            case 39: payload.get(); return new Packet.PlayerAction(player, 0, leA(payload));
            case 32: { int id = leA(payload); payload.get(); return new Packet.PlayerAction(player, 1, id); }
            case 41: payload.get(); return new Packet.PlayerAction(player, 2, le(payload));
            case 69: payload.get(); return new Packet.PlayerAction(player, 3, le(payload));
            case 36: { int id = le(payload); payload.get(); return new Packet.PlayerAction(player, 4, id); }
            case 45: { int id = le(payload); payload.get(); return new Packet.PlayerAction(player, 5, id); }
            case 27: payload.get(); return new Packet.PlayerAction(player, 6, leA(payload));
            case 35: { int id = leA(payload); payload.get(); return new Packet.PlayerAction(player, 7, id); }
            // Protocol.method4427 and Static185.method3400: selected component target.
            case 75: {
                int source = payload.getInt(); payload.get();
                int item = leA(payload), slot = le(payload), npc = be(payload);
                if (!active(player, source) || npc <= 0 || npc >= core.ServerConstants.MAX_NPCS) return null;
                return item == 65535 ? new Packet.ComponentNpcAction(player, source >>> 16, source & 65535, npc)
                        : new Packet.UseWithNpc(player, item, npc, source >>> 16, slot);
            }
            case 48: {
                int source = Integer.reverseBytes(payload.getInt()); payload.get();
                int slot = le(payload), item = leA(payload), target = le(payload);
                if (!active(player, source) || target <= 0 || target >= core.ServerConstants.MAX_PLAYERS) return null;
                return item == 65535 ? new Packet.ComponentPlayerAction(player, target, source >>> 16, source & 65535)
                        : new Packet.UseWithPlayer(player, item, target, source >>> 16, slot);
            }
            case 6: {
                int item = be(payload), target = leA(payload), x = le(payload); payload.get();
                int slot = leA(payload), source = intAlt3(payload), z = leA(payload);
                if (!active(player, source) || x > 16383 || z > 16383) return null;
                return item == 65535 ? new Packet.ComponentGroundItemAction(player, source >>> 16, source & 65535, slot, target, x, z)
                        : new Packet.UseWithGroundItem(player, item, target, source >>> 16, source & 65535, slot, x, z);
            }
            case 9: {
                int target = le(payload), source = intAlt2(payload), item = be(payload); payload.get();
                int slot = leA(payload), x = be(payload), z = be(payload);
                if (!active(player, source) || x > 16383 || z > 16383) return null;
                if (item == 65535) return new Packet.ComponentSceneryAction(player, source >>> 16, source & 65535, target, x, z);
                // Existing item-on-scenery listener uses the inventory only.
                if ((source >>> 16) != 149) return null;
                return new Packet.UseWithScenery(player, item, slot, target, x, z);
            }
            case 11: { // Protocol.method6129: component target.
                int target = payload.getInt(), targetSlot = beA(payload), slot = beA(payload);
                int item = be(payload), source = intAlt3(payload), targetItem = be(payload);
                if (!active(player, source) || !active(player, target) || targetItem == 65535) return null;
                if (item == 65535) {
                    if ((target >>> 16) != 149) return null;
                    return new Packet.ComponentItemAction(player, source >>> 16, source & 65535, targetItem, targetSlot);
                }
                return new Packet.UseWithItem(player, item, targetItem, slot, targetSlot,
                        source >>> 16, target >>> 16, source & 65535, target & 65535);
            }
            case 13: { // Protocol.method5096 / Cs1ScriptRunner: source then destination.
                int source = payload.getInt(), item = leA(payload), slot = beA(payload);
                int target = intAlt3(payload), targetItem = leA(payload), targetSlot = le(payload);
                int iface = source >>> 16;
                if (!active(player, source) || !active(player, target) || iface != (target >>> 16)) return null;
                // Restrict legacy slot switching to its known inventory/bank containers.
                core.game.container.Container container;
                if (iface == 149 && (source & 65535) == 0 && (target & 65535) == 0) container = player.getInventory();
                else if (iface == 762 && (source & 65535) == 73 && (target & 65535) == 73) container = player.getBank();
                else return null;
                if (slot >= container.capacity() || targetSlot >= container.capacity() || slot == targetSlot) return null;
                core.game.node.item.Item from = container.get(slot), to = container.get(targetSlot);
                if (from == null || from.getId() != item || (to == null ? 65535 : to.getId()) != targetItem) return null;
                return new Packet.SlotSwitchMultiComponent(player, iface, source & 65535, slot, iface, target & 65535, targetSlot);
            }
            case 49: { // FriendsList.setRank: signed rank + 128, CP1252 name.
                int rank = (byte) (payload.get() - 128);
                String name = readText(payload);
                if (name.isEmpty() || rank < 0 || rank > 6) return null;
                return new Packet.SetClanRank(player, name, rank);
            }
            case 58: return new Packet.GESetOfferItem(player, be(payload)); // ScriptRunner 3110
            default:
                // Keep-alives and as-yet unmapped frames end here.
                return null;
        }
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
        return player.getInterfaceManager().getComponent(component >>> 16) != null;
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
