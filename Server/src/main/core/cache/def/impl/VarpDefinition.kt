package core.cache.def.impl

import core.cache.Cache
import java.nio.ByteBuffer

class VarpDefinition {
    var clientCode = 0
        private set

    internal fun decode(buffer: ByteBuffer) {
        while (true) {
            when (val opcode = buffer.get().toInt() and 0xff) {
                0 -> return
                5 -> clientCode = buffer.short.toInt() and 0xffff
                else -> error("Unknown varp opcode $opcode at ${buffer.position() - 1}")
            }
        }
    }

    companion object {
        fun exists(id: Int): Boolean = id >= 0 && Cache.getIndexes().getOrNull(2)?.validFile(16, id) == true

        fun forId(id: Int): VarpDefinition? {
            val data = Cache.getIndexes()[2]!!.getFileData(16, id) ?: return null
            return VarpDefinition().apply { decode(ByteBuffer.wrap(data)) }
        }
    }
}
