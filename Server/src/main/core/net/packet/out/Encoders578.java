package core.net.packet.out;

import core.net.packet.IoBuffer;
import core.net.packet.PacketHeader;
import java.nio.charset.Charset;

public final class Encoders578 {
    private static final Charset CP1252 = Charset.forName("windows-1252");

    private Encoders578() {}

    /** Clears the client's primary entity animations, not server animator state. */
    public static IoBuffer resetEntityAnimations() {
        return new IoBuffer(103);
    }

    /** Resets the client's resettable varps; callers must resynchronize server values. */
    public static IoBuffer resetClientVarps() {
        return new IoBuffer(75);
    }

    /** Runs onDialogAbort hooks recursively from the active top-level interface. */
    public static IoBuffer runDialogAbortHooks() {
        return new IoBuffer(82);
    }

    /** Empty payload restores the client's localized default walk option. */
    public static IoBuffer walkOption(String option) {
        if (option.length() > 254 || option.indexOf(0) >= 0) {
            throw new IllegalArgumentException("Invalid walk option");
        }
        IoBuffer buffer = new IoBuffer(11, PacketHeader.BYTE);
        if (!option.isEmpty()) buffer.putString(option);
        return buffer;
    }

    /** Packed target: bit 30 for tiles, bit 29 for NPCs, bit 28 for players. */
    public static IoBuffer globalSpotAnimation(int target, int id, int delay, int height) {
        boolean tile = (target & 0x40000000) != 0;
        boolean npc = (target & 0xffff8000) == 0x20000000;
        boolean player = (target & 0xfffff800) == 0x10000000;
        if (target < 0 || (!tile && !npc && !player) || id < -1 || id > 65534
                || delay < 0 || delay > 65535 || height < 0 || height > 65535
                || (tile && (id == -1 || delay != 0))) {
            throw new IllegalArgumentException("Invalid global spot animation");
        }
        return new IoBuffer(0).putShortA(id).putShortA(delay).putInt(target).putLEShort(height);
    }

    public static IoBuffer logout() {
        return new IoBuffer(58);
    }

    public static IoBuffer clearMinimapFlag() {
        return new IoBuffer(17).putC(255).putS(255);
    }

    public static IoBuffer minimapState(int state) {
        return new IoBuffer(40).put(state);
    }

    public static IoBuffer varc(int id, int value, int verify) {
        if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            return new IoBuffer(48).putA(value).putShortA(verify).putLEShortA(id);
        }
        return new IoBuffer(22).putShort(verify).putInt(value).putLEShort(id);
    }

    /** Short and long string varc frames share the same delayed client state. */
    public static IoBuffer varcString(int id, String value, int verify) {
        if (id < 0 || id > 65535 || value == null || value.indexOf(0) >= 0 || value.length() > 65530) {
            throw new IllegalArgumentException("Invalid varc string");
        }
        if (value.length() <= 250) {
            return new IoBuffer(53, PacketHeader.BYTE).putShort(id).putString(value).putLEShort(verify);
        }
        return new IoBuffer(85, PacketHeader.SHORT).putString(value).putLEShortA(id).putLEShort(verify);
    }

    public static IoBuffer openTop(int type, int id, int verify) {
        return new IoBuffer(93).put(type).putLEShort(id).putLEShort(verify);
    }

    public static IoBuffer openSub(int component, int type, int id, int verify) {
        return new IoBuffer(52).putShortA(verify).putS(type).putLEInt(component).putShort(id);
    }

    public static IoBuffer settings(int component, int first, int last, int events, int verify) {
        return new IoBuffer(38).putIntA(component).putLEShort(first).putIntB(events)
                .putShortA(last).putLEShort(verify);
    }

    public static IoBuffer targetParam(int component, int first, int last, int param, int verify) {
        return new IoBuffer(77).putShort(first).putLEShort(param).putShortA(last)
                .putShort(verify).putIntA(component);
    }

    public static IoBuffer interaction(String name, int slot, boolean primary) {
        return new IoBuffer(46, PacketHeader.BYTE).put(text(name, 250)).put(0)
                .putShortA(-1).putS(slot).putS(primary ? 1 : 0);
    }

    public static IoBuffer message(String message, int timestamp) {
        return new IoBuffer(99, PacketHeader.BYTE).put(0).putInt(timestamp).put(0)
                .put(text(message, 248)).put(0);
    }

    private static byte[] text(String value, int maxLength) {
        byte[] encoded = value.replace('\0', '?').getBytes(CP1252);
        return encoded.length <= maxLength ? encoded : java.util.Arrays.copyOf(encoded, maxLength);
    }
}
