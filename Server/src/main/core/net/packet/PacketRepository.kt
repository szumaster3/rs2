package core.net.packet

import core.api.log
import core.net.packet.out.Encoders578
import core.net.packet.out.PacketContext
import core.tools.Log

object PacketRepository {
    @JvmStatic
    fun send(encoder: Encoders578, context: PacketContext) {
        val player = context.player
        if (player.session?.isActive != true || player.isArtificial) {
            return
        }

        try {
            if (context is PacketContext.InterfaceContext &&
                (encoder == Encoders578.IF_OPENSUB || encoder == Encoders578.IF_CLOSESUB)) {
                // Validate the same parent widget before changing the server attachment map
                // or queueing a frame that would consume a verify ID.
                if (context.windowId !in 0..65535 || context.componentId !in 0..65535 ||
                    core.cache.Cache.getIndexes()[3]?.getFileData(context.windowId, context.componentId) == null) {
                    log(PacketRepository::class.java, Log.WARN, "Rejected ${encoder.name} for missing parent component ${context.windowId}:${context.componentId}")
                    return
                }
                if (encoder == Encoders578.IF_OPENSUB)
                    player.interfaceManager.recordInterfaceAttachment(context.windowId, context.componentId, context.interfaceId, context.isWalkable)
                else
                    player.interfaceManager.removeInterfaceAttachment(context.windowId, context.componentId)
            }
            if (encoder == Encoders578.IF_MOVESUB && context is PacketContext.MoveInterfaceContext) {
                if (context.source == context.destination) return
                val index = core.cache.Cache.getIndexes()[3] ?: return
                if (index.getFileData(context.source ushr 16, context.source and 65535) == null ||
                    index.getFileData(context.destination ushr 16, context.destination and 65535) == null) {
                    log(PacketRepository::class.java, Log.WARN, "Rejected IF_MOVESUB for missing source/destination component")
                    return
                }
                player.interfaceManager.recordInterfaceMove(context.source, context.destination)
            }
            PacketWriteQueue.handle(encoder, context)
        } catch (e: Exception) {
            log(PacketRepository::class.java, Log.ERR, "Error writing packet ${encoder.name}: ${e.stackTraceToString()}")
        }
    }
}
