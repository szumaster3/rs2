package core.net.packet.out

import core.net.packet.IoBuffer
import core.net.packet.OutgoingPacket
import core.net.packet.context.DisplayModelContext
import shared.consts.Network

/**
 * Represents the outgoing packet for the displaying of a node model on an interface.
 * @author Emperor
 */
class DisplayModel : OutgoingPacket<DisplayModelContext> {
    override fun send(context: DisplayModelContext) {
        val buffer: IoBuffer
        when (context.type) {
            DisplayModelContext.ModelType.PLAYER -> {
                buffer = IoBuffer(Network.DISPLAY_MODEL_PLAYER)
                buffer.putLEShortA(context.player.interfaceManager.getPacketCount(1))
                buffer.putIntA(context.interfaceId shl 16 or context.childId)
            }

            DisplayModelContext.ModelType.NPC -> {
                buffer = IoBuffer(Network.DISPLAY_MODEL_NPC)
                buffer.putShortA(context.nodeId)
                buffer.putLEInt((context.interfaceId shl 16) or context.childId)
                buffer.putLEShort(context.player.interfaceManager.getPacketCount(1))
            }

            DisplayModelContext.ModelType.ITEM -> {
                val value = if (context.amount > 0) context.amount else context.zoom
                buffer = IoBuffer(Network.DISPLAY_MODEL_ITEM)
                buffer.putInt(value)
                buffer.putIntB((context.interfaceId shl 16) or context.childId)
                buffer.putLEShortA(context.nodeId)
                buffer.putLEShort(context.player.interfaceManager.getPacketCount(1))
            }

            DisplayModelContext.ModelType.MODEL -> {
                buffer = IoBuffer(Network.DISPLAY_MODEL)
                buffer.putLEInt(context.interfaceId shl 16 or context.childId)
                buffer.putLEShortA(context.player.interfaceManager.getPacketCount(1))
                buffer.putShortA(context.nodeId)
            }

            else -> return
        }
        buffer.cypherOpcode(context.player.session.isaacPair.output)
        context.player.session.write(buffer)
    }
}