package core.game.world.update;

import core.net.packet.ServerProt;

import core.net.packet.IoBuffer;
import core.net.packet.PacketHeader;
import java.util.Arrays;
import java.nio.ByteBuffer;
import java.nio.BufferOverflowException;

/** Per-connection GPI history, initialized from the login or reconnect snapshot. */
public final class PlayerInfo {
    private final int[] previous = new int[2048];
    private final int[] external = new int[2048];
    private final byte[] flags = new byte[2048];
    private final int[] current = new int[2048];
    private final byte[][] masks = new byte[2048][];
    private final int self;
    private boolean firstTick = true;

    public boolean needsAppearance(int slot) {
        return firstTick || previous[slot] == -1;
    }

    public PlayerInfo(int self, int x, int z, int plane) {
        this.self = self;
        Arrays.fill(previous, -1);
        previous[self] = position(x, z, plane);
        Arrays.fill(current, -1);
    }

    private static int position(int x, int z, int plane) {
        return ((plane & 3) << 28) | ((x & 16383) << 14) | (z & 16383);
    }

    public void track(int slot, int x, int z, int plane, byte[] mask) {
        current[slot] = position(x, z, plane);
        masks[slot] = mask;
    }

    /** Whether the last successfully encoded frame contains this slot. */
    public boolean isLocal(int slot) {
        return slot > 0 && slot < previous.length && previous[slot] != -1;
    }

    public IoBuffer encode() {
        if (current[self] == -1) throw new IllegalStateException("Missing local player");
        // Trial encoding must not change GPI history. Prefer existing local players;
        // if necessary, send a legal removal and re-admit in a later tick.
        long maskBytes = 0;
        for (byte[] mask : masks) if (mask != null) maskBytes += mask.length;
        while (maskBytes > ServerProt.MAX_PAYLOAD_SIZE) {
            int slot = evictionCandidate(true);
            if (slot == -1) slot = evictionCandidate(false);
            if (slot == -1) throw new BufferOverflowException();
            if (masks[slot] != null) maskBytes -= masks[slot].length;
            current[slot] = -1;
            masks[slot] = null;
        }
        while (true) {
            try {
                return encodeFrame();
            } catch (BufferOverflowException ex) {
                int slot = evictionCandidate(true);
                if (slot == -1) slot = evictionCandidate(false);
                if (slot == -1) throw ex; // self cannot be removed
                current[slot] = -1;
                masks[slot] = null;
            }
        }
    }

    private int evictionCandidate(boolean newOnly) {
        int candidate = -1;
        int largest = -1;
        for (int slot = 1; slot < current.length; slot++) {
            if (slot == self || current[slot] == -1 || (newOnly && previous[slot] != -1)) continue;
            int length = masks[slot] == null ? 0 : masks[slot].length;
            if (length > largest) {
                candidate = slot;
                largest = length;
            }
        }
        return candidate;
    }

    private IoBuffer encodeFrame() {
        final byte[] nextFlags = flags.clone();
        final int[] nextExternal = external.clone();
        // The bit stream alone (<=255 remote active players) fits in this buffer.
        IoBuffer frame = new IoBuffer(ServerProt.PLAYER_INFO, PacketHeader.SHORT,
                ByteBuffer.allocate(ServerProt.MAX_PAYLOAD_SIZE));
        IoBuffer blocks = new IoBuffer(-1, PacketHeader.NORMAL,
                ByteBuffer.allocate(ServerProt.MAX_PAYLOAD_SIZE));
        for (int pass = 0; pass < 4; pass++) {
            boolean active = pass < 2;
            int group = pass == 1 || pass == 2 ? 1 : 0;
            frame.setBitAccess();
            int skipped = 0;
            for (int slot = 1; slot < previous.length; slot++) {
                int old = previous[slot], next = current[slot];
                if ((old != -1) != active || (flags[slot] & 1) != group) continue;
                if (next == old && masks[slot] == null) {
                    skipped++;
                    nextFlags[slot] |= 2;
                    continue;
                }
                writeSkip(frame, skipped);
                skipped = 0;
                frame.putBits(1, 1);
                boolean masked = masks[slot] != null;
                if (!active) {
                    add(frame, slot, next, masked);
                    nextFlags[slot] |= 2;
                } else if (next == -1) {
                    frame.putBits(1, 0).putBits(2, 0).putBits(1, 0);
                    nextExternal[slot] = region(old);
                } else {
                    frame.putBits(1, masked ? 1 : 0);
                    movement(frame, old, next);
                }
                if (masked) blocks.put(masks[slot]);
            }
            writeSkip(frame, skipped);
            frame.setByteAccess();
        }
        frame.put(blocks);
        // Only after the complete frame fits can the next tick use this history.
        for (int i = 1; i < flags.length; i++) flags[i] = (byte) (nextFlags[i] >> 1);
        System.arraycopy(nextExternal, 0, external, 0, external.length);
        System.arraycopy(current, 0, previous, 0, current.length);
        Arrays.fill(current, -1);
        Arrays.fill(masks, null);
        firstTick = false;
        return frame;
    }

    private static void writeSkip(IoBuffer out, int count) {
        if (count == 0) return;
        int extra = count - 1;
        int bits = extra == 0 ? 0 : extra < 32 ? 5 : extra < 256 ? 8 : 11;
        out.putBits(1, 0).putBits(2, bits == 0 ? 0 : bits == 5 ? 1 : bits == 8 ? 2 : 3);
        if (bits != 0) out.putBits(bits, extra);
    }

    private static int region(int pos) {
        return (pos >>> 28 << 16) | ((pos >>> 20 & 255) << 8) | (pos >>> 6 & 255);
    }

    private void add(IoBuffer out, int slot, int pos, boolean masked) {
        int old = external[slot], next = region(pos);
        out.putBits(2, 0).putBits(1, old == next ? 0 : 1);
        if (old != next) {
            out.putBits(2, 3).putBits(2, (next >>> 16) - (old >>> 16) & 3)
                    .putBits(8, (next >>> 8) - (old >>> 8) & 255).putBits(8, next - old & 255);
        }
        out.putBits(6, pos >>> 14 & 63).putBits(6, pos & 63).putBits(1, masked ? 1 : 0);
    }

    private static void movement(IoBuffer out, int old, int next) {
        int dx = (next >>> 14 & 16383) - (old >>> 14 & 16383);
        int dz = (next & 16383) - (old & 16383);
        int dp = (next >>> 28) - (old >>> 28) & 3;
        int distance = Math.max(Math.abs(dx), Math.abs(dz));
        if (next == old) {
            out.putBits(2, 0);
        } else if (dp == 0 && distance == 1) {
            int index = (dz + 1) * 3 + dx + 1;
            out.putBits(2, 1).putBits(3, index > 4 ? index - 1 : index);
        } else if (dp == 0 && distance == 2) {
            int index;
            if (dz == -2) index = dx + 2;
            else if (dz == 2) index = 13 + dx;
            else index = 5 + (dz + 1) * 2 + (dx == 2 ? 1 : 0);
            out.putBits(2, 2).putBits(4, index);
        } else {
            out.putBits(2, 3);
            if (dx >= -16 && dx <= 15 && dz >= -16 && dz <= 15) {
                out.putBits(1, 0).putBits(2, dp).putBits(5, dx & 31).putBits(5, dz & 31);
            } else {
                out.putBits(1, 1).putBits(30, position(dx, dz, dp));
            }
        }
    }
}
