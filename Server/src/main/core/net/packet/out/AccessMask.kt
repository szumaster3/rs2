package core.net.packet.out

import core.net.packet.IoBuffer
import core.net.packet.OutgoingPacket
import core.net.packet.context.Context
import shared.consts.Network

/**
 * The access mask outgoing packet.
 * @author Emperor
 */
class AccessMask : OutgoingPacket<Context.AccessMask> {
    override fun send(context: Context.AccessMask) {
        val buffer = IoBuffer(Network.ACCESS_MASK)
        buffer.putLEShort(context.player.interfaceManager.getPacketCount(1))
        buffer.putLEShort(context.length)
        buffer.putInt(context.interfaceId shl 16 or context.childId)
        buffer.putShortA(context.offset)
        buffer.putIntA(context.id)
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        context.player.session.write(buffer)
    }
}
