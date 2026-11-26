package core.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.ExecutorService;

/**
 * I/O event handling.
 *
 * @author Emperor
 */
public class IoEventHandler {

    /**
     * The executor service.
     */
    protected final ExecutorService service;

    /**
     * Constructs a new IoEventHandler.
     *
     * @param service The executor service used for handling events.
     */
    public IoEventHandler(ExecutorService service) {
        this.service = service;
    }

    /**
     * Called when making a new connection.
     */
    public void connect(SelectionKey key) throws IOException {
        // Empty.
    }

    /**
     * Used for accepting a new connection.
     */
    public void accept(SelectionKey key, Selector selector) throws IOException {
        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();

        if (channel == null) {
            return;
        }

        channel.configureBlocking(false);
        channel.socket().setTcpNoDelay(true);
        channel.register(selector, SelectionKey.OP_READ);
    }

    /**
     * Reads the incoming packet data.
     */
    public void read(SelectionKey key) throws IOException {
        if (!key.isValid()) {
            return;
        }

        ReadableByteChannel channel = (ReadableByteChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(100_000);
        IoSession session = (IoSession) key.attachment();

        final int bytesRead;

        try {
            bytesRead = channel.read(buffer);
        } catch (IOException e) {
            System.err.println("[Network] Read failed: " + e);
            disconnect(key, e);
            return;
        }

        if (bytesRead == -1) {
            System.out.println("[Network] Remote EOF: " + session);
            disconnect(key, null);
            return;
        }

        if (bytesRead == 0) {
            return;
        }

        buffer.flip();

        if (session == null) {
            session = new IoSession(key, service);
            key.attach(session);
        }

        IoReadEvent reader = session.getProducer()
                .produceReader(session, buffer);

        if (reader instanceof core.net.event.GameReadEvent) {
            // Preserve selector read order for the session ISAAC stream.
            reader.run();
        } else {
            service.execute(reader);
        }
    }

    /**
     * Writes the outgoing packet data.
     */
    public void write(SelectionKey key) {
        if (!key.isValid()) {
            return;
        }

        IoSession session = (IoSession) key.attachment();

        key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);

        if (session != null) {
            session.write();
        }
    }

    /**
     * Disconnects a connection.
     *
     * @param key The selection key.
     * @param t The occurred exception, if any.
     */
    public void disconnect(SelectionKey key, Throwable t) {
        try {
            IoSession session = (IoSession) key.attachment();

            if (t != null) {
                String cause = t.toString();

                if (!(t instanceof ClosedChannelException
                        || cause.contains("De externe host")
                        || cause.contains("De software op uw")
                        || cause.contains("An established connection was aborted")
                        || cause.contains("An existing connection")
                        || cause.contains("AsynchronousClose")
                        || cause.contains("reset by peer"))) {
                    t.printStackTrace();
                }
            }

            if (session != null) {
                session.disconnect();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            key.cancel();

            try {
                if (key.channel().isOpen()) {
                    key.channel().close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}