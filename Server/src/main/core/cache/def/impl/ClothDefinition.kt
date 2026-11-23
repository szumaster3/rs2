package core.cache.def.impl

import core.cache.Cache
import java.nio.ByteBuffer

/**
 * The definitions for player clothing/look.
 *
 * @author Emperor
 */
class ClothDefinition {
    var unknown: Int = 0
        private set

    var modelIds: IntArray = intArrayOf()
        private set

    var isUnknownBool: Boolean = false
        private set

    var originalColors: IntArray = intArrayOf()
        private set

    var modifiedColors: IntArray = intArrayOf()
        private set

    var originalTextureColors: IntArray = intArrayOf()
        private set

    var modifiedTextureColors: IntArray = intArrayOf()
        private set

    val models: IntArray = intArrayOf(-1, -1, -1, -1, -1)

    fun load(buffer: ByteBuffer) {
        while (true) {
            val opcode = buffer.get().toInt() and 0xFF
            if (opcode == 0) return
            parse(opcode, buffer)
        }
    }

    /**
     * Parses an opcode.
     *
     * @param opcode The opcode.
     * @param buffer The buffer to read the data from.
     */
    private fun parse(opcode: Int, buffer: ByteBuffer) {
        when (opcode) {
            1 -> unknown = buffer.get().toInt() and 0xFF
            2 -> {
                val length = buffer.get().toInt() and 0xFF
                modelIds = IntArray(length)
                for (i in 0 until length) {
                    modelIds[i] = buffer.getShort().toInt() and 0xFFFF
                }
            }

            3 -> isUnknownBool = true
            40 -> {
                val length = buffer.get().toInt() and 0xFF
                originalColors = IntArray(length)
                modifiedColors = IntArray(length)
                for (i in 0 until length) {
                    originalColors[i] = buffer.getShort().toInt()
                    modifiedColors[i] = buffer.getShort().toInt()
                }
            }

            41 -> {
                val length = buffer.get().toInt() and 0xFF
                originalTextureColors = IntArray(length)
                modifiedTextureColors = IntArray(length)
                for (i in 0 until length) {
                    originalTextureColors[i] = buffer.getShort().toInt()
                    modifiedTextureColors[i] = buffer.getShort().toInt()
                }
            }

            else -> if (opcode >= 60 && opcode < 70) {
                models[opcode - 60] = buffer.getShort().toInt() and 0xFFFF
            }
        }
    }

    companion object {
        /**
         * Gets the definitions for the given cloth id.
         *
         * @param clothId The clothing id.
         * @return The definition.
         */
        fun forId(clothId: Int): ClothDefinition {
            val def = ClothDefinition()
            val bs = Cache.getIndexes()[2]!!.getFileData(3, clothId, null)
            if (bs != null) {
                def.load(ByteBuffer.wrap(bs))
            }
            return def
        }

    }
}
