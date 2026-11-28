package core.cache.def.impl

import core.cache.Cache
import core.tools.StringUtils
import java.nio.ByteBuffer

/** Revision 578 client variable definitions: JS5 index 2, groups 19 and 15. */
object VarcType {
    data class Metadata(val type: Char, val persistent: Boolean)

    /** VarcType.decode; persistent is a client-local setting, not a player save flag. */
    @JvmStatic fun forId(id: Int): Metadata? {
        if (id !in 0..65535) return null
        val data = Cache.getIndexes().getOrNull(2)?.getFileData(19, id) ?: return null
        return parse(data)
    }

    @JvmStatic fun parse(data: ByteArray): Metadata {
        val buffer = ByteBuffer.wrap(data)
        var type = '\u0000'
        var persistent = false
        while (true) {
            when (val opcode = buffer.get().toInt() and 0xff) {
                0 -> return Metadata(type, persistent)
                1 -> type = StringUtils.getFromByte(buffer.get())
                2 -> persistent = true
                else -> error("Unknown Varc opcode $opcode at ${buffer.position() - 1}")
            }
        }
    }

    fun exists(id: Int): Boolean = id in 0..65535 &&
        Cache.getIndexes().getOrNull(2)?.validFile(19, id) == true

    fun stringExists(id: Int): Boolean = id in 0..65535 &&
        Cache.getIndexes().getOrNull(2)?.validFile(15, id) == true
}
