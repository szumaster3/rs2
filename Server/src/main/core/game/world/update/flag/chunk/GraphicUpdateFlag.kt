package core.game.world.update.flag.chunk

import core.game.world.map.Location
import core.game.world.update.flag.UpdateFlag
import core.game.world.update.flag.context.Graphics
import core.net.packet.IoBuffer
import core.net.packet.out.ZoneProt

/**
 * Handles the location graphic update.
 * @author Emperor
 */
class GraphicUpdateFlag(graphic: Graphics?,
    /**
     * The location.
     */
    private val location: Location
) : UpdateFlag<Graphics?>(graphic) {

    override fun write(buffer: IoBuffer) {
        ZoneProt.MAP_ANIM.start(buffer)
        buffer.put((location.chunkOffsetX shl 4) or (location.chunkOffsetY and 0x7))
        buffer.putShort(context!!.id)
        buffer.put(context!!.height)
        buffer.putShort(context!!.delay)
    }

    override fun data(): Int {
        return 0
    }

    override fun ordinal(): Int {
        return 3
    }
}
