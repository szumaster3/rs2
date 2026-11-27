package core.net.packet

import core.api.log
import core.net.IoSession
import core.net.packet.out.Encoders578
import core.net.packet.out.PacketContext
import core.tools.Log
import java.util.ArrayDeque

object PacketWriteQueue {
    private val packetsToWrite = ArrayDeque<QueuedPacket>()

    internal fun handle(encoder: Encoders578, context: PacketContext) {
        val session = context.player.session ?: return
        if (!session.isActive) return
        if (encoder.immediate) {
            encoder.write(context)
        } else {
            synchronized(packetsToWrite) {
                packetsToWrite.addLast(QueuedPacket(encoder, context, session))
            }
        }
    }

    @JvmStatic
    fun flush() {
        val countThisCycle = synchronized(packetsToWrite) { packetsToWrite.size }
        repeat(countThisCycle) {
            val packet = synchronized(packetsToWrite) { packetsToWrite.pollFirst() } ?: return
            // Do not deliver queued packets to a replacement login session.
            if (!packet.session.isActive || packet.context.player.session !== packet.session) return@repeat
            try {
                packet.encoder.write(packet.context)
            } catch (e: Exception) {
                log(PacketWriteQueue::class.java, Log.ERR, "Error flushing packet ${packet.encoder.name}: ${e.stackTraceToString()}")
            }
        }
    }

    private class QueuedPacket(val encoder: Encoders578, val context: PacketContext, val session: IoSession)
}
