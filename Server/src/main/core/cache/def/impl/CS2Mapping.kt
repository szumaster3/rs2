package core.cache.def.impl

import core.cache.Cache
import core.cache.misc.buffer.ByteBufferUtils
import java.nio.ByteBuffer

class CS2Mapping(val scriptId: Int) {
    var unknown = 0
    var unknown1 = 0
    var defaultString: String? = null
    var defaultInt = 0
    var map: HashMap<Int, Any>? = null
    var array: Array<Any?>? = null
        private set
    internal fun load(buffer: ByteBuffer) {
        while (true) {
            when (val opcode = buffer.get().toInt() and 0xFF) {
                0 -> return
                1 -> unknown = buffer.get().toInt() and 0xFF
                2 -> unknown1 = buffer.get().toInt() and 0xFF
                3 -> defaultString = ByteBufferUtils.getString(buffer)
                4 -> defaultInt = buffer.int
                5, 6 -> {
                    val size = buffer.short.toInt() and 0xFFFF
                    val values = HashMap<Int, Any>(size)
                    val entries = arrayOfNulls<Any>(size)
                    for (index in entries.indices) {
                        val key = buffer.int
                        val value = if (opcode == 5) ByteBufferUtils.getString(buffer) else buffer.int
                        entries[index] = value
                        values[key] = value
                    }
                    map = values
                    array = entries
                }
                else -> error("Unknown CS2Mapping opcode $opcode at ${buffer.position() - 1}")
            }
        }
    }

    companion object {
        private val maps = HashMap<Int, CS2Mapping>()

        @JvmStatic
        fun forId(scriptId: Int): CS2Mapping? {
            maps[scriptId]?.let { return it }
            val data = Cache.getIndexes()[17]!!.getFileData(scriptId ushr 8, scriptId and 0xFF) ?: return null
            return CS2Mapping(scriptId).also {
                it.load(ByteBuffer.wrap(data))
                maps[scriptId] = it
            }
        }
    }
}
