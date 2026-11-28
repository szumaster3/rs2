package core.cache.def.impl

import core.cache.Cache
import java.nio.ByteBuffer

/** Revision 578 InvType: config index 2, group 5. Size is the client CS2 capacity, not a server capacity override. */
class InvType private constructor(val id: Int) {
    var size: Int = 0
        private set

    companion object {
        @JvmStatic fun forId(id: Int): InvType? {
            if (id < 0) return null
            val data = Cache.getIndexes().getOrNull(2)?.getFileData(5, id) ?: return null
            return parse(id, data)
        }

        @JvmStatic fun parse(id: Int, data: ByteArray): InvType {
            val result = InvType(id)
            val buffer = ByteBuffer.wrap(data)
            while (true) {
                when (val opcode = buffer.get().toInt() and 0xff) {
                    0 -> return result
                    2 -> result.size = buffer.short.toInt() and 0xffff
                    else -> error("Inv $id: unknown opcode $opcode at ${buffer.position() - 1}")
                }
            }
        }
    }
}
