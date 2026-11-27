package core.net;

import core.auth.AuthResponse;
import core.cache.crypto.ISAACPair;
import core.game.node.entity.player.Player;
import core.game.node.entity.player.info.ClientInfo;
import core.game.world.map.Location;
import core.game.world.repository.Repository;
import core.net.producer.HSEventProducer;
import core.net.producer.LoginEventProducer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import core.net.event.GamePacketDecoder;
import core.net.producer.GameEventProducer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents a connected I/O session.
 *
 * @author Emperor
 */
public class IoSession {

    /**
     * The handshake event producer.
     */
    private static final EventProducer HANDSHAKE_PRODUCER = new HSEventProducer();

    /**
     * The selection key.
     */
    private final SelectionKey key;
    /**
     * The executor service.
     */
    private final ExecutorService service;
    /**
     * The address.
     */
    private final String address;
    /**
     * The JS-5 queue.
     */
    private final JS5Queue js5Queue;
    public final GamePacketDecoder gameInput = new GamePacketDecoder();
    public boolean reconnecting;
    public Location initialScene;
    public volatile core.game.world.update.PlayerInfo578 playerInfo;
    public final AtomicBoolean loginSubmitted = new AtomicBoolean();
    public String associatedUsername;
    /**
     * The event producer.
     */
    private EventProducer producer = HANDSHAKE_PRODUCER;
    /**
     * The currently queued writing data.
     */
    private List<ByteBuffer> writingQueue = new ArrayList<>(20);
    /**
     * The currently queued reading data.
     */
    private ByteBuffer readingQueue;
    /**
     * The writing lock.
     */
    private Lock writingLock = new ReentrantLock();
    /**
     * The ISAAC cipher pair.
     */
    private ISAACPair isaacPair;
    /**
     * The name hash.
     */
    private int nameHash;
    /**
     * The server key.
     */
    private long serverKey;
    /**
     * The JS-5 encryption value.
     */
    private int js5Encryption;
    /**
     * The object.
     */
    private Object object;
    /**
     * If the session is active.
     */
    private volatile boolean active = true;
    private volatile int lastTransmitVerifyId = -1;

    public void recordTransmitVerifyId(int id) {
        lastTransmitVerifyId = id;
    }

    public int getLastTransmitVerifyId() {
        return lastTransmitVerifyId;
    }
    /**
     * The last ping time stamp.
     */
    private long lastPing;
    /**
     * The client info.
     */
    private ClientInfo clientInfo;

    /**
     * Constructs a new {@code IoSession}.
     *
     * @param key     The selection key.
     * @param service The executor service.
     */
    public IoSession(SelectionKey key, ExecutorService service) {
        this.key = key;
        this.service = service;
        this.address = getRemoteAddress().replaceAll("/", "").split(":")[0];
        this.js5Queue = new JS5Queue(this);
    }

    /**
     * Fires a write event created using the current event producer.
     *
     * @param context The event context.
     */
    public void write(Object context) {
        write(context, false);
    }

    /**
     * Fires a write event created using the current event producer.
     *
     * @param context The event context.
     * @param instant If the event should be instantly executed on this thread.
     */
    public void write(Object context, boolean instant) {
        if (context == null) {
            throw new IllegalStateException("Invalid writing context!");
        }
        if (!(context instanceof AuthResponse) && producer instanceof LoginEventProducer) {
            // new Throwable().printStackTrace();
            return;
        }
        if (instant || producer instanceof GameEventProducer) {
            producer.produceWriter(this, context).run();
            return;
        }
        service.execute(producer.produceWriter(this, context));
    }

    /**
     * Sends the packet data (without write event encoding).
     *
     * @param buffer The buffer.
     */
    public void queue(ByteBuffer buffer) {
        enqueue(buffer, -1);
    }

    public void queueGameFrame(ByteBuffer buffer, int opcode) {
        enqueue(buffer, opcode);
    }

    private void enqueue(ByteBuffer buffer, int opcode) {
        writingLock.lock();
        try {
            if (!active) return;
            if (opcode >= 0) {
                if (!(producer instanceof GameEventProducer) || isaacPair == null) return;
                // PACKET_56 is the only revision 578 game packet whose payload is also ISAAC-coded.
                // Validate the whole writable frame before consuming any cipher values.
                final int start = buffer.position();
                if (buffer.isReadOnly() || !buffer.hasRemaining()) return;
                if (opcode == 56) {
                    if (buffer.remaining() < 3 || buffer.remaining() > 257
                            || (buffer.get(start + 1) & 0xFF) != buffer.remaining() - 2) return;
                }
                int isaac = isaacPair.getOutput().getNextValue();
                int encrypted = (opcode + isaac) & 0xFF;

                System.out.println(
                        "[SERVER OUT] Opcode: " + opcode
                                + ", encrypted: " + encrypted
                                + ", size: " + buffer.remaining()
                );

                buffer.put(start, (byte) encrypted);
                if (opcode == 56) {
                    // PacketBit.gisaac reads one cipher value per payload byte. The BYTE length is plain.
                    // Hold writingLock until the encrypted frame enters writingQueue, so another opcode
                    // cannot consume values between the opcode and these bytes.
                    for (int i = start + 2; i < buffer.limit(); i++) {
                        buffer.put(i, (byte) (buffer.get(i) + isaacPair.getOutput().getNextValue()));
                    }
                }
            }
            writingQueue.add(buffer);
        } finally {
            writingLock.unlock();
        }
        write();
    }

    /**
     * Handles the writing of all buffers in the queue.
     */
    public void write() {
        if (!key.isValid()) {
            disconnect();
            return;
        }
        writingLock.lock();
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            while (!writingQueue.isEmpty()) {
                ByteBuffer buffer = writingQueue.get(0);
                channel.write(buffer);
                if (buffer.hasRemaining()) {
                    key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                    break;
                }
                writingQueue.remove(0);
            }
        } catch (IOException e) {
            disconnect();
        } finally {
            writingLock.unlock();
        }
    }

    /**
     * Disconnects the session.
     */
    public void disconnect() {
        try {
            if (!active) {
                return;
            }
            System.out.println("[DEBUG] Disconnecting session: " + this);
            new Exception("Disconnect trace").printStackTrace();
            active = false;
            key.cancel();
            SocketChannel channel = (SocketChannel) key.channel();
            channel.socket().close();
            if (getPlayer() != null) {
                try {
                    getPlayer().clear();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            object = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the IP-address (without the port).
     *
     * @return The address.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Gets the remote address of this session.
     *
     * @return The remote address, as a String.
     */
    public String getRemoteAddress() {
        try {
            return ((SocketChannel) key.channel()).getRemoteAddress().toString();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Gets the current event producer.
     *
     * @return The producer.
     */
    public EventProducer getProducer() {
        return producer;
    }

    /**
     * Sets the event producer.
     *
     * @param producer The producer to set.
     */
    public void setProducer(EventProducer producer) {
        this.producer = producer;
    }

    /**
     * Gets the queued reading data.
     *
     * @return The readingQueue.
     */
    public ByteBuffer getReadingQueue() {
        synchronized (this) {
            return readingQueue;
        }
    }

    /**
     * Queues reading data.
     *
     * @param readingQueue The readingQueue to set.
     */
    public void setReadingQueue(ByteBuffer readingQueue) {
        synchronized (this) {
            this.readingQueue = readingQueue;
        }
    }

    /**
     * Gets the writing lock.
     *
     * @return The writing lock.
     */
    public Lock getWritingLock() {
        return writingLock;
    }

    /**
     * Gets the selection key.
     *
     * @return The selection key.
     */
    public SelectionKey getKey() {
        return key;
    }

    /**
     * @return The active.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @return The js5Encryption.
     */
    public int getJs5Encryption() {
        return js5Encryption;
    }

    /**
     * @param js5Encryption The js5Encryption to set.
     */
    public void setJs5Encryption(int js5Encryption) {
        this.js5Encryption = js5Encryption;
    }

    /**
     * @return The player.
     */
    public Player getPlayer() {
        if (object == null) {
            object = Repository.getPlayerByName(associatedUsername);
        }
        return object instanceof Player ? ((Player) object) : null;
    }

    /**
     * Gets the object.
     *
     * @return the object.
     */
    public Object getObject() {
        return object;
    }

    /**
     * @param object The object to set.
     */
    public void setObject(Object object) {
        this.object = object;
    }

    /**
     * Gets the lastPing.
     *
     * @return The lastPing.
     */
    public long getLastPing() {
        return lastPing;
    }

    /**
     * Sets the lastPing.
     *
     * @param lastPing The lastPing to set.
     */
    public void setLastPing(long lastPing) {
        this.lastPing = lastPing;
    }

    /**
     * Gets the nameHash.
     *
     * @return The nameHash.
     */
    public int getNameHash() {
        return nameHash;
    }

    /**
     * Sets the nameHash.
     *
     * @param nameHash The nameHash to set.
     */
    public void setNameHash(int nameHash) {
        this.nameHash = nameHash;
    }

    /**
     * Gets the serverKey.
     *
     * @return The serverKey.
     */
    public long getServerKey() {
        return serverKey;
    }

    /**
     * Sets the serverKey.
     *
     * @param serverKey The serverKey to set.
     */
    public void setServerKey(long serverKey) {
        this.serverKey = serverKey;
    }

    /**
     * Gets the isaacPair.
     *
     * @return The isaacPair.
     */
    public ISAACPair getIsaacPair() {
        return isaacPair;
    }

    /**
     * Sets the isaacPair.
     *
     * @param isaacPair The isaacPair to set.
     */
    public void setIsaacPair(ISAACPair isaacPair) {
        this.isaacPair = isaacPair;
    }

    /**
     * Gets the js5Queue.
     *
     * @return the js5Queue
     */
    public JS5Queue getJs5Queue() {
        return js5Queue;
    }

    /**
     * Gets the clientInfo value.
     *
     * @return The clientInfo.
     */
    public ClientInfo getClientInfo() {
        return clientInfo;
    }

    /**
     * Sets the clientInfo value.
     *
     * @param clientInfo The clientInfo to set.
     */
    public void setClientInfo(ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
    }

}
