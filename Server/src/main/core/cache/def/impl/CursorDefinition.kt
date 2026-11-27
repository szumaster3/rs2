package core.cache.def.impl

import core.cache.Cache
import java.nio.ByteBuffer

/** Revision 578 CursorType: config index 2, group 33. Graphic references sprite index 8. */
class CursorDefinition private constructor(val id: Int) {
    var graphic: Int = 0
        private set
    var hotspotX: Int = 0
        private set
    var hotspotY: Int = 0
        private set

    companion object {
        @JvmStatic fun forId(id: Int): CursorDefinition? {
            if (id < 0) return null
            val data = Cache.getIndexes().getOrNull(2)?.getFileData(33, id) ?: return null
            return parse(id, data)
        }

        @JvmStatic fun parse(id: Int, data: ByteArray): CursorDefinition {
            val result = CursorDefinition(id)
            val buffer = ByteBuffer.wrap(data)
            while (true) {
                when (val opcode = buffer.get().toInt() and 0xff) {
                    0 -> return result
                    1 -> result.graphic = buffer.short.toInt() and 0xffff
                    2 -> {
                        result.hotspotX = buffer.get().toInt() and 0xff
                        result.hotspotY = buffer.get().toInt() and 0xff
                    }
                    else -> error("Cursor $id: unknown opcode $opcode at ${buffer.position() - 1}")
                }
            }
        }
    }
}
