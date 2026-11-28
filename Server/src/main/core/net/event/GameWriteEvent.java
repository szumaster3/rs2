package core.net.event;

import core.net.IoSession;
import core.net.IoWriteEvent;
import core.net.packet.IoBuffer;
import core.net.packet.ServerProt;

import java.nio.ByteBuffer;

/**
 * Handles game packet writing events.
 *
 * @author Emperor
 */
public final class GameWriteEvent extends IoWriteEvent {

    /**
     * Constructs a new {@code GameWriteEvent}.
     *
     * @param session The session.
     * @param context The context.
     */
    public GameWriteEvent(IoSession session, Object context) {
        super(session, context);
    }

    @Override
    public void write(IoSession session, Object context) {
        // Login, JS5 and management responses have separate producers. A gameplay
        // write must carry a ServerProt opcode and go through the ISAAC frame queue.
        if (!(context instanceof IoBuffer)) {
            System.err.println("[Protocol] Rejected unframed gameplay write");
            return;
        }
        IoBuffer buffer = (IoBuffer) context;
        if (buffer.opcode() < 0) {
            System.err.println("[Protocol] Rejected gameplay buffer without an opcode");
            return;
        }
        ByteBuffer buf = (ByteBuffer) buffer.toByteBuffer().flip();
        if (!ServerProt.validate(buffer.opcode(), buffer.getHeader(), buf.remaining())) {
            System.err.println("[Protocol578] Rejected outgoing opcode " + buffer.opcode()
                    + ": expected " + ServerProt.expected(buffer.opcode())
                    + ", actual " + buffer.getHeader() + "/" + buf.remaining());
            return;
        }
        int packetLength = buf.remaining() + 4;
        ByteBuffer response = ByteBuffer.allocate(packetLength);
        response.put((byte) 0); // Fill the opcode when committing to the queue.
        switch (buffer.getHeader()) {
            case BYTE:
                if (buf.remaining() > 255) throw new IllegalArgumentException("Game frame exceeds byte length");
                response.put((byte) buf.remaining());
                break;
            case SHORT:
                if (buf.remaining() > 65535) throw new IllegalArgumentException("Game frame exceeds short length");
                response.putShort((short) buf.remaining());
                break;
            default:
                break;
        }
        response.put(buf).flip();
        session.queueGameFrame(response, buffer.opcode());
    }

}