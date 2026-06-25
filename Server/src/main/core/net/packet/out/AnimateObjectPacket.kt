package core.net.packet.out

import core.game.world.update.flag.context.Animation
import core.net.packet.IoBuffer
import core.net.packet.OutgoingPacket
import core.net.packet.context.Context

/**
 * Represents the packet used to animate an object.
 * @author Vexia (10/11/2013)
 */
class AnimateObjectPacket : OutgoingPacket<Context.AnimateObject> {

    override fun send(context: Context.AnimateObject) {
        val player = context.player
        val obj = context.animation.`object`
        val buffer = write(UpdateAreaPosition.getBuffer(player, obj.location.chunkBase), context.animation)
        buffer.cypherOpcode(player.session.isaacPair.output)
        player.session.write(buffer)
    }

    companion object {
        fun write(buffer: IoBuffer, animation: Animation): IoBuffer {
            val obj = animation.getObject()
            val location = obj.location
            buffer.put(20)
            buffer.putS((location.chunkOffsetX shl 4) or (location.chunkOffsetY and 0x7))
            buffer.putS((obj.type shl 2) + (obj.rotation and 0x3))
            buffer.putLEShort(animation.id)
            return buffer
        }
    }
}
