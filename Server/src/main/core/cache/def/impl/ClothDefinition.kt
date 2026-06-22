package core.cache.def.impl

import core.ServerConstants
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

    /**
     * Loads the definitions.
     *
     * @param buffer The buffer.
     */
    fun load(buffer: ByteBuffer) {
        var opcode: Int
        while (((buffer.get().toInt() and 0xFF).also { opcode = it }) != 0) {
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
                var i = 0
                while (i < length) {
                    modelIds[i] = buffer.getShort().toInt() and 0xFFFF
                    i++
                }
            }

            3 -> isUnknownBool = true
            40 -> {
                val length = buffer.get().toInt() and 0xFF
                originalColors = IntArray(length)
                modifiedColors = IntArray(length)
                var i = 0
                while (i < length) {
                    originalColors[i] = buffer.getShort().toInt()
                    modifiedColors[i] = buffer.getShort().toInt()
                    i++
                }
            }

            41 -> {
                val length = buffer.get().toInt() and 0xFF
                originalTextureColors = IntArray(length)
                modifiedTextureColors = IntArray(length)
                var i = 0
                while (i < length) {
                    originalTextureColors[i] = buffer.getShort().toInt()
                    modifiedTextureColors[i] = buffer.getShort().toInt()
                    i++
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
            val bs = Cache.getIndexes()[2].getFileData(3, clothId, null)
            if (bs != null) {
                def.load(ByteBuffer.wrap(bs))
            }
            return def
        }

        /**
         * The main method.
         *
         * @param args The arguments cast on runtime.
         */
        @JvmStatic
        fun main(args: Array<String>) {
            try {
                Cache.init(ServerConstants.CACHE_PATH)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            val length = Cache.getIndexes()[2].getFilesSize(3)

            for (i in 0 until length) {
                val def = forId(i)
            }
        }
    }
}