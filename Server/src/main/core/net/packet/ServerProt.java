package core.net.packet;

/**
 * Outgoing frame sizes copied from client.
 */
public final class ServerProt {
    private ServerProt() {}
    private static final int[] LENGTHS = {
        10, 6, 2, 3, 1, 8, -2, 2, 2, 8,
        -1, -1, 3, 5, 6, 10, 7, 2, -2, 0,
        6, 3, 8, 8, 12, 8, 28, 8, 6, 3,
        2, 8, -2, 15, 15, -2, 8, -2, 14, 17,
        1, 3, 3, -1, 8, 1, -1, 6, 5, -1,
        -1, -1, 9, -1, 12, -1, -1, 12, 0, -2,
        -2, 4, 6, -1, 3, 5, 10, -1, 4, -2,
        -2, 20, -2, 6, 4, 0, -1, 12, 6, 7,
        2, 8, 0, 7, -1, -2, 10, -2, -2, 6,
        12, 2, 6, 5, 3, 8, 6, 7, -2, -1,
        -2, 14, 12, 0, 4, -1
    };

    public static boolean validate(int opcode, PacketHeader header, int payload) {
        if (opcode < 0 || opcode >= LENGTHS.length) return false;
        int expected = LENGTHS[opcode];
        if (expected == -1) return header == PacketHeader.BYTE && payload <= 255;
        if (expected == -2) return header == PacketHeader.SHORT && payload <= 65535;
        return header == PacketHeader.NORMAL && payload == expected;
    }

    public static String expected(int opcode) {
        if (opcode < 0 || opcode >= LENGTHS.length) return "unknown opcode";
        int size = LENGTHS[opcode];
        return size == -1 ? "BYTE" : size == -2 ? "SHORT" : "NORMAL/" + size;
    }
}
