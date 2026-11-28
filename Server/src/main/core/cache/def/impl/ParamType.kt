package core.cache.def.impl

import core.cache.Cache
import core.cache.misc.buffer.ByteBufferUtils
import core.tools.StringUtils
import java.nio.ByteBuffer

/** Revision 578 ParamType: config index 2, group 11. Auto-disable is client free-world metadata. */
class ParamType private constructor(val id: Int) {
    var type: Char = '\u0000'
        private set
    var defaultInt: Int = 0
        private set
    var defaultString: String? = null
        private set
    var autoDisable: Boolean = true
        private set
    fun isString(): Boolean = type == 's'

    companion object {
        @JvmStatic fun forId(id: Int): ParamType? {
            if (id < 0) return null
            val data = Cache.getIndexes().getOrNull(2)?.getFileData(11, id) ?: return null
            return parse(id, data)
        }

        @JvmStatic fun parse(id: Int, data: ByteArray): ParamType {
            val result = ParamType(id)
            val buffer = ByteBuffer.wrap(data)
            while (true) {
                when (val opcode = buffer.get().toInt() and 0xff) {
                    0 -> return result
                    1 -> result.type = StringUtils.getFromByte(buffer.get())
                    2 -> result.defaultInt = buffer.int
                    4 -> result.autoDisable = false
                    5 -> result.defaultString = ByteBufferUtils.getString(buffer)
                    else -> error("Param $id: unknown opcode $opcode at ${buffer.position() - 1}")
                }
            }
        }
    }
}
