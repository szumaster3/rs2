package core.cache.def.impl

import core.api.log
import core.cache.Cache
import core.cache.misc.buffer.ByteBufferUtils
import core.tools.Log
import java.nio.ByteBuffer

class StructType(val id: Int) {
    @JvmField val dataStore = HashMap<Int, Any>()
    fun getInt(key: Int): Int {
        if (!dataStore.containsKey(key)) {
            log(javaClass, Log.ERR, "Invalid value passed for key: $key struct: $id")
            return -1
        }
        return dataStore[key] as Int
    }

    fun getString(key: Int): String? = dataStore[key] as String?
    fun getParam(key: Int, defaultValue: Int): Int = dataStore[key] as? Int ?: defaultValue
    fun getParam(key: Int, defaultValue: String?): String? = dataStore[key] as? String ?: defaultValue

    override fun toString(): String = "StructType{id=$id, dataStore=$dataStore}"

    companion object {
        private val definitions = HashMap<Int, StructType>()

        @JvmStatic
        fun get(id: Int): StructType = definitions.getOrPut(id) { parse(id, Cache.getIndexes()[2]!!.getFileData(26, id)) }

        @JvmStatic
        fun getCount(): Int = Cache.getIndexes()[2]!!.getFilesSize(26)

        @JvmStatic
        fun parse(id: Int, data: ByteArray?): StructType {
            return if (data == null) StructType(id) else decode(id, ByteBuffer.wrap(data))
        }

        internal fun decode(id: Int, buffer: ByteBuffer): StructType {
            val definition = StructType(id)
            while (true) {
                when (val opcode = buffer.get().toInt() and 0xFF) {
                    0 -> return definition
                    249 -> repeat(buffer.get().toInt() and 0xFF) {
                        val string = buffer.get().toInt() and 0xFF == 1
                        val key = ByteBufferUtils.getMedium(buffer)
                        definition.dataStore[key] = if (string) ByteBufferUtils.getString(buffer) else buffer.int
                    }
                    else -> error("Unknown StructType opcode $opcode at ${buffer.position() - 1}")
                }
            }
        }
    }
}
