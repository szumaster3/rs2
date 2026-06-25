package core.net.packet.out

import core.net.packet.IoBuffer
import core.net.packet.OutgoingPacket
import core.net.packet.context.Context

/**
 * Handles the removal of the minimap flag.
 * @author Emperor
 */
class ClearMinimapFlag : OutgoingPacket<Context.PlayerContext> {
    override fun send(context: Context.PlayerContext) {
        val buffer = IoBuffer(153)
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        context.player.details.session.write(buffer)
    }
}