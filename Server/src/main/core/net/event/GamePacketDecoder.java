package core.net.event;

import core.net.packet.ClientProt;

import core.cache.crypto.ISAACCipher;
import java.nio.ByteBuffer;
import java.util.function.BiConsumer;

/**
 * Incremental client-to-server framing. Each session owns one reader.
 */
public final class GamePacketDecoder {
    private int opcode = -1;
    private ByteBuffer payload;

    public void read(ByteBuffer input, ISAACCipher cipher, BiConsumer<Integer, ByteBuffer> consumer) {
        while (input.hasRemaining()) {
            if (opcode == -1) {
                opcode = (input.get() - cipher.getNextValue()) & 255;
                if (opcode >= ClientProt.COUNT) {
                    throw new IllegalArgumentException("Unknown client opcode " + opcode);
                }
            }
            if (payload == null) {
                int length = ClientProt.length(opcode);
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
