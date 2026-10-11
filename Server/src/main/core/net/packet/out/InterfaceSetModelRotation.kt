package core.net.packet.out

import core.net.packet.IoBuffer
import core.net.packet.OutgoingPacket
import core.net.packet.context.InterfaceSetModelRotationContext

class InterfaceSetModelRotation : OutgoingPacket<InterfaceSetModelRotationContext> {

    override fun send(ctx: InterfaceSetModelRotationContext) {
        val player = ctx.player
        val buffer = IoBuffer(207)
        val componentHash = (ctx.id shl 16) or ctx.componentId
        val track = player.interfaceManager.getPacketCount(1)
        val pitch = ctx.pitch
        val yaw = ctx.yaw
        buffer.putIntB(componentHash)
        buffer.putShortA(track)
        buffer.putShort(pitch)
        buffer.putShortA(yaw)
        buffer.cypherOpcode(player.session.isaacPair.output)
        player.details.session.write(buffer)
    }
}