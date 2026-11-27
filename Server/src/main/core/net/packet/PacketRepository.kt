package core.net.packet

import core.api.log
import core.net.packet.out.Encoders530
import core.net.packet.out.PacketContext
import core.tools.Log

object PacketRepository {
    @JvmStatic
    fun send(encoder: Encoders530, context: PacketContext) {
        val player = context.player
        if (player.session == null || player.isArtificial) {
            return
        }

        try {
            if (context is PacketContext.InterfaceContext) {
                if (encoder == Encoders530.IF_OPENSUB && core.cache.Cache.getIndexes()[3]?.getFileData(context.windowId, context.componentId) != null)
                    player.interfaceManager.recordInterfaceAttachment(context.windowId, context.componentId, context.interfaceId)
                else if (encoder == Encoders530.IF_CLOSESUB)
                    player.interfaceManager.removeInterfaceAttachment(context.windowId, context.componentId)
            }
            PacketWriteQueue.handle(encoder, context)
        } catch (e: Exception) {
            log(PacketRepository::class.java, Log.ERR, "Error writing packet ${encoder.name}: ${e.stackTraceToString()}")
        }
    }
}
