package core.cache.def.impl

import core.api.log
import core.cache.Cache
import core.cache.misc.buffer.ByteBufferUtils
import core.tools.Log
import core.tools.StringUtils
import java.nio.ByteBuffer

class EnumType(val id: Int) {
    @JvmField var keyType = '?'
    @JvmField var valueType = '?'
    @JvmField var defaultString: String? = null
    @JvmField var defaultInt = 0
    @JvmField val dataStore = HashMap<Int, Any>()
    fun getInt(key: Int): Int {
        if (!dataStore.containsKey(key)) {
            log(javaClass, Log.ERR, "Invalid value passed for key: $key map: $id")
            return -1
        }
        return dataStore[key] as Int
    }

    fun getString(key: Int): String? = dataStore[key] as String?

    override fun toString(): String {
        val type = when (valueType) { 'K' -> "Normal"; 'J' -> "Struct Pointer"; else -> "Unknown" }
        return "DataMapDefinition{id=$id, keyType=$keyType, valueType=$type, defaultString='$defaultString', defaultInt=$defaultInt, dataStore=$dataStore}\n"
    }

    companion object {
        private val definitions = HashMap<Int, EnumType>()

        @JvmStatic
        fun get(id: Int): EnumType = definitions.getOrPut(id) {
            parse(id, Cache.getIndexes()[17]!!.getFileData(id ushr 8, id and 0xFF))
        }

        @JvmStatic
        fun parse(id: Int, data: ByteArray?): EnumType {
            return if (data == null) EnumType(id) else decode(id, ByteBuffer.wrap(data))
        }

        internal fun decode(id: Int, buffer: ByteBuffer): EnumType {
            val definition = EnumType(id)
            while (true) {
                when (val opcode = buffer.get().toInt() and 0xFF) {
                    0 -> return definition
                    1 -> definition.keyType = StringUtils.getFromByte(buffer.get())
                    2 -> definition.valueType = StringUtils.getFromByte(buffer.get())
                    3 -> definition.defaultString = ByteBufferUtils.getString(buffer)
                    4 -> definition.defaultInt = buffer.int
                    5, 6 -> repeat(buffer.short.toInt() and 0xFFFF) {
                        val key = buffer.int
                        definition.dataStore[key] = if (opcode == 5) ByteBufferUtils.getString(buffer) else buffer.int
                    }
                    else -> error("Unknown EnumType opcode $opcode at ${buffer.position() - 1}")
                }
            }
        }
    }
}
