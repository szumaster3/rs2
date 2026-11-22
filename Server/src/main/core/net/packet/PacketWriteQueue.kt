package core.net.packet

import core.api.log
import core.net.packet.out.Encoders530
import core.net.packet.out.PacketContext
import core.tools.Log
import java.util.ArrayDeque

object PacketWriteQueue {
    private val packetsToWrite = ArrayDeque<QueuedPacket>()

    internal fun handle(encoder: Encoders530, context: PacketContext) {
        if (encoder.immediate) {
            encoder.write(context)
        } else {
            synchronized(packetsToWrite) {
                packetsToWrite.addLast(QueuedPacket(encoder, context))
            }
        }
    }

    @JvmStatic
    fun flush() {
        val countThisCycle = synchronized(packetsToWrite) { packetsToWrite.size }
        repeat(countThisCycle) {
            val packet = synchronized(packetsToWrite) { packetsToWrite.pollFirst() } ?: return
            try {
                packet.encoder.write(packet.context)
            } catch (e: Exception) {
                log(PacketWriteQueue::class.java, Log.ERR, "Error flushing packet ${packet.encoder.name}: ${e.stackTraceToString()}")
            }
        }
    }

    private class QueuedPacket(val encoder: Encoders530, val context: PacketContext)
}
