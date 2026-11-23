package core.cache.def.impl

import core.api.log
import core.cache.Cache
import core.cache.misc.buffer.ByteBufferUtils
import core.tools.Log
import java.nio.ByteBuffer

class Struct(val id: Int) {
    @JvmField val dataStore = HashMap<Int, Any>()
    fun getInt(key: Int): Int {
        if (!dataStore.containsKey(key)) {
            log(javaClass, Log.ERR, "Invalid value passed for key: $key struct: $id")
            return -1
        }
        return dataStore[key] as Int
    }

    fun getString(key: Int): String? = dataStore[key] as String?

    override fun toString(): String = "Struct{id=$id, dataStore=$dataStore}"

    companion object {
        private val definitions = HashMap<Int, Struct>()

        @JvmStatic
        fun get(id: Int): Struct = definitions.getOrPut(id) { parse(id, Cache.getIndexes()[2]!!.getFileData(26, id)) }

        @JvmStatic
        fun getCount(): Int = Cache.getIndexes()[2]!!.getFilesSize(26)

        @JvmStatic
        fun parse(id: Int, data: ByteArray?): Struct {
            val definition = Struct(id)
            if (data == null) return definition
            val buffer = ByteBuffer.wrap(data)
            while (true) {
                when (buffer.get().toInt() and 0xFF) {
                    0 -> return definition
                    249 -> repeat(buffer.get().toInt() and 0xFF) {
                        val string = buffer.get().toInt() and 0xFF == 1
                        val key = ByteBufferUtils.getMedium(buffer)
                        definition.dataStore[key] = if (string) ByteBufferUtils.getString(buffer) else buffer.int
                    }
                }
            }
        }
    }
}
