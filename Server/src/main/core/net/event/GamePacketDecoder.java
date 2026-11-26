package core.net.event;

import core.cache.crypto.ISAACCipher;
import java.nio.ByteBuffer;
import java.util.function.BiConsumer;

/**
 * Incremental client-to-server framing. Each session owns one reader.
 */
public final class GamePacketDecoder {
    // Client/client/src/main/java/ClientProt.java:9-171, RT5-Server/util/ClientProt.js:34-115.
    private static final int[] LENGTHS = {
        2,-1,8,7,8,7,15,4,6,15, 8,16,8,16,8,-1,-1,8,-1,-1,
        4,6,7,-1,-1,2,7,3,3,-1, -1,3,3,4,-1,3,3,6,4,3,
        7,3,-1,8,1,3,2,7,11,-1, 3,0,12,-1,8,0,-1,8,2,18,
        -1,-1,3,8,-1,4,2,4,3,3, 3,0,7,7,-1,11,-1,-1,5,7,7,2
    };
    private int opcode = -1;
    private ByteBuffer payload;

    public void read(ByteBuffer input, ISAACCipher cipher, BiConsumer<Integer, ByteBuffer> consumer) {
        while (input.hasRemaining()) {
            if (opcode == -1) {
                opcode = (input.get() - cipher.getNextValue()) & 255;
                if (opcode >= LENGTHS.length) {
                    throw new IllegalArgumentException("Unknown client opcode " + opcode);
                }
            }
            if (payload == null) {
                int length = LENGTHS[opcode];
                if (length == -1) {
                    if (!input.hasRemaining()) return;
                    length = input.get() & 255;
                }
                payload = ByteBuffer.allocate(length);
            }
            int limit = input.limit();
            input.limit(input.position() + Math.min(input.remaining(), payload.remaining()));
            payload.put(input);
            input.limit(limit);
            if (payload.hasRemaining()) return;
            ByteBuffer complete = payload;
            int id = opcode;
            complete.flip();
            opcode = -1;
            payload = null;
            consumer.accept(id, complete);
        }
    }
}
