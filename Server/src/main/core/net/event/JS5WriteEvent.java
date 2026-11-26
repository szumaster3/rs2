package core.net.event;

import core.cache.Cache;
import core.net.IoSession;
import core.net.IoWriteEvent;

import java.nio.ByteBuffer;

/**
 * Handles JS-5 writing events.
 *
 * @author Emperor
 */
public final class JS5WriteEvent extends IoWriteEvent {

    /**
     * Constructs a new {@code JS5WriteEvent}.
     *
     * @param session The session.
     * @param context The event context.
     */
    public JS5WriteEvent(IoSession session, Object context) {
        super(session, context);
    }

    @Override
    public void write(IoSession session, Object context) {
        int[] request = (int[]) context;
        int container = request[0];
        int archive = request[1];
        boolean priority = request[2] == 1;
        ByteBuffer response = Cache.getArchiveData(container, archive, priority, session.getJs5Encryption());
        if (response != null) {
            session.queue(response);
        }
    }

}
