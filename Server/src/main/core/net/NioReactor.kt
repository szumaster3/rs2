package core.net

import core.net.amsc.MSEventHandler
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Handles (NIO-based) networking events using the reactor pattern.
 *
 * Converted to Kotlin (original by Emperor).
 */
class NioReactor private constructor(
    private val eventHandler: IoEventHandler
) : Runnable {

    private val service: ExecutorService =
        Executors.newSingleThreadExecutor()

    private lateinit var connection: ServerSocketConnection

    @Volatile
    private var running = false

    companion object {

        /**
         * Creates and configures a new NioReactor with a pool size of 1.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun configure(port: Int): NioReactor =
            configure(port, poolSize = 1)

        /**
         * Creates and configures a new NioReactor with a custom thread pool size.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun configure(port: Int, poolSize: Int): NioReactor {
            val reactor = NioReactor(
                IoEventHandler(Executors.newFixedThreadPool(poolSize))
            )

            val selector = Selector.open()

            val channel = ServerSocketChannel.open().apply {
                bind(InetSocketAddress(port))
                configureBlocking(false)
                register(selector, SelectionKey.OP_ACCEPT)
            }

            reactor.connection = ServerSocketConnection(selector, channel)

            return reactor
        }

        /**
         * Creates and configures a new NioReactor for client connections.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun connect(address: String, port: Int): NioReactor {
            val reactor = NioReactor(MSEventHandler())
            val selector = Selector.open()

            val channel = SocketChannel.open().apply {
                configureBlocking(false)

                socket().apply {
                    keepAlive = true
                    tcpNoDelay = true
                }

                connect(InetSocketAddress(address, port))
                register(selector, SelectionKey.OP_CONNECT)
            }

            reactor.connection = ServerSocketConnection(selector, channel)

            return reactor
        }
    }

    /**
     * Starts the reactor on a background thread.
     */
    @Synchronized
    fun start() {
        if (running || service.isShutdown) {
            return
        }

        running = true
        service.execute(this)
    }

    override fun run() {
        Thread.currentThread().name = "NioReactor"

        val selector = connection.selector

        while (running) {
            try {
                selector.select()
            } catch (e: ClosedSelectorException) {
                break
            } catch (e: IOException) {
                e.printStackTrace()
                continue
            }

            if (!running) {
                break
            }

            val iterator = selector.selectedKeys().iterator()

            while (iterator.hasNext()) {
                val key = iterator.next()
                iterator.remove()

                try {
                    if (!key.isValid || !key.channel().isOpen) {
                        key.cancel()
                        continue
                    }

                    if (key.isValid && key.isConnectable) {
                        eventHandler.connect(key)
                    }

                    if (key.isValid && key.isAcceptable) {
                        eventHandler.accept(key, selector)
                    }

                    if (key.isValid && key.isReadable) {
                        eventHandler.read(key)
                    }

                    if (key.isValid && key.isWritable) {
                        eventHandler.write(key)
                    }

                } catch (t: Throwable) {
                    eventHandler.disconnect(key, t)
                }
            }
        }

        running = false
    }

    /**
     * Stops the reactor loop.
     */
    @Synchronized
    fun terminate() {
        running = false

        try {
            connection.selector.wakeup()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            service.shutdownNow()
        }
    }
}