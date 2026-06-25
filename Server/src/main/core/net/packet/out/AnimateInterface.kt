package core.net.packet.out

import core.net.packet.IoBuffer
import core.net.packet.OutgoingPacket
import core.net.packet.context.AnimateInterfaceContext
import core.net.packet.context.Context
import shared.consts.Network

/**
 * The animate interface outgoing packet.
 * @author Emperor
*/
class AnimateInterface : OutgoingPacket<Context.AnimateInterface> {
    override fun send(context: Context.AnimateInterface) {
        val buffer = IoBuffer(Network.ANIMATE_INTERFACE)
        buffer.putIntB((context.interfaceId shl 16) + context.childId)
        buffer.putLEShort(context.animationId)
        buffer.putShortA(context.player.interfaceManager.getPacketCount(1))
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        context.player.details.session.write(buffer)
    }
}