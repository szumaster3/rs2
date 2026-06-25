package core.net.packet.out

import core.net.packet.IoBuffer
import core.net.packet.OutgoingPacket
import core.net.packet.context.ClearChunkContext
import core.net.packet.context.Context

/**
 * Handles the clear region chunk outgoing packet.
 * @author Emperor
 */
class ClearRegionChunk : OutgoingPacket<Context.ClearChunk> {
    override fun send(context: Context.ClearChunk) {
        val l = context.player.playerFlags.lastSceneGraph
        val x = context.chunk.currentBase.getSceneX(l)
        val y = context.chunk.currentBase.getSceneY(l)
        if (x >= 0 && y >= 0 && x < 96 && y < 96) {
            val buffer = IoBuffer(112).put(x).putC(y)
            buffer.cypherOpcode(context.player.session.isaacPair.output)
            context.player.session.write(buffer)
        }
    }
}
