package core.net.event;

import core.ServerConstants;
import core.net.IoReadEvent;
import core.net.IoSession;
import core.net.lobby.WorldList;
import core.net.registry.AccountRegister;
import core.tools.Log;
import core.tools.RandomFunction;

import java.nio.ByteBuffer;

import static core.api.ContentAPIKt.log;

/**
 * Handles handshake read events.
 *
 * @author Emperor
 */
public final class HSReadEvent extends IoReadEvent {

    /**
     * Constructs a new handshake read event.
     *
     * @param session The session.
     * @param buffer The buffer.
     */
    public HSReadEvent(IoSession session, ByteBuffer buffer) {
        super(session, buffer);
    }

    @Override
    public void read(IoSession session, ByteBuffer buffer) {
        if (!buffer.hasRemaining()) {
            return;
        }

        int opcode = buffer.get() & 0xFF;

        switch (opcode) {
            case 14: // LOGIN
                if (!buffer.hasRemaining()) {
                    queueBuffer(opcode);
                    return;
                }

                session.setNameHash(buffer.get() & 0xFF);
                session.setServerKey(RandomFunction.RANDOM.nextLong());
                session.write(true);
                break;

            case 15: // JS5
                if (buffer.remaining() < Integer.BYTES) {
                    queueBuffer(opcode);
                    return;
                }

                int revision = buffer.getInt();

                if (revision != ServerConstants.REVISION) {
                    session.queue(ByteBuffer.wrap(new byte[]{6}));
                    session.disconnect();
                    return;
                }

                session.write(false);
                break;

            case 20: // REGISTER_STEP1
                if (buffer.remaining() < 6) { queueBuffer(opcode); return; }
                AccountRegister.read(session, opcode, buffer);
                break;
            case 21: // VALIDATE_USERNAME
                if (buffer.remaining() < 8) { queueBuffer(opcode); return; }
                AccountRegister.read(session, opcode, buffer);
                break;
            case 22: // REGISTER_STEP2
                if (buffer.remaining() < 2) { queueBuffer(opcode); return; }
                int length = buffer.getShort(buffer.position()) & 0xFFFF;
                if (buffer.remaining() < length + 2) { queueBuffer(opcode); return; }
                AccountRegister.read(session, opcode, buffer);
                break;

            case 23: // GET_WORLDLIST
                if (buffer.remaining() < Integer.BYTES) {
                    queueBuffer(opcode);
                    return;
                }

                int checksum = buffer.getInt();
                WorldList.sendUpdate(session, checksum);
                break;

            default:
                log(getClass(), Log.FINE,
                        "Unknown handshake opcode: " + opcode);
                session.disconnect();
                break;
        }
    }
}