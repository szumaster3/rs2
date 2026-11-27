package core.cache.def.impl

import core.cache.misc.buffer.ByteBufferUtils
import java.nio.ByteBuffer

/** Parameter table opcode 249 in ObjType, NpcType, LocType and StructType. */
internal object DefinitionParams {
    fun read(buffer: ByteBuffer, existing: MutableMap<Int, Any>?): MutableMap<Int, Any> {
        val count = buffer.get().toInt() and 0xff
        val result = existing ?: HashMap(count)
        repeat(count) {
            val string = (buffer.get().toInt() and 0xff) == 1
            val key = ByteBufferUtils.getMedium(buffer)
            result[key] = if (string) ByteBufferUtils.getString(buffer) else buffer.int
        }
        return result
    }
}
