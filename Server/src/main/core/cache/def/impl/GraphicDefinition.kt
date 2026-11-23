package core.cache.def.impl

import core.cache.Cache
import java.nio.ByteBuffer
import java.util.HashMap

class GraphicDefinition {
    @JvmField var aShortArray1435: ShortArray? = null
    @JvmField var aShortArray1438: ShortArray? = null
    @JvmField var anInt1440: Int = 0
    @JvmField var aBoolean1442: Boolean = false
    @JvmField var defaultModel: Int = 0
    @JvmField var anInt1446: Int = 128
    @JvmField var aBoolean1448: Boolean = false
    @JvmField var anInt1449: Int = 128
    @JvmField var animationId: Int = -1
    @JvmField var anInt1451: Int = 0
    @JvmField var graphicsId: Int = 0
    @JvmField var anInt1454: Int = 0
    @JvmField var aShortArray1455: ShortArray? = null
    @JvmField var aShortArray1456: ShortArray? = null
    @JvmField var byteValue: Byte = 0
    @JvmField var intValue: Int = -1
    private fun readValueLoop(buffer: ByteBuffer) {
        while (true) {
            val opcode = buffer.get().toInt() and 0xff
            if (opcode == 0) return
            readValues(buffer, opcode)
        }
    }

    fun readValues(buffer: ByteBuffer, opcode: Int) {
        when (opcode) {
            1 -> defaultModel = buffer.short.toInt()
            2 -> animationId = buffer.short.toInt()
            4 -> anInt1446 = buffer.short.toInt() and 0xffff
            5 -> anInt1449 = buffer.short.toInt() and 0xffff
            6 -> anInt1454 = buffer.short.toInt() and 0xffff
            7 -> anInt1440 = buffer.get().toInt() and 0xff
            8 -> anInt1451 = buffer.get().toInt() and 0xff
            9 -> { byteValue = 3; intValue = 8224 }
            10 -> aBoolean1448 = true
            11 -> byteValue = 1
            12 -> byteValue = 4
            13 -> byteValue = 5
            14 -> { byteValue = 2; intValue = (buffer.get().toInt() and 0xff) * 256 }
            15 -> { byteValue = 3; intValue = buffer.short.toInt() and 0xffff }
            16 -> { byteValue = 3; intValue = buffer.int }
            40, 41 -> {
                val count = buffer.get().toInt() and 0xff
                val source = ShortArray(count)
                val target = ShortArray(count)
                repeat(count) { index ->
                    source[index] = buffer.short
                    target[index] = buffer.short
                }
                if (opcode == 40) {
                    aShortArray1438 = source
                    aShortArray1456 = target
                } else {
                    aShortArray1455 = source
                    aShortArray1435 = target
                }
            }
        }
    }

    companion object {
        private val graphicDefinitions: MutableMap<Int, GraphicDefinition> = HashMap<Int, GraphicDefinition>()

        @JvmStatic
        fun forId(gfxId: Int): GraphicDefinition {
            var def = graphicDefinitions.get(gfxId)
            if (def != null) {
                return def
            }
            val data = Cache.getIndexes()[21]!!.getFileData(gfxId.ushr(8), gfxId and 0xff, null)
            def = GraphicDefinition()
            def.graphicsId = gfxId
            if (data != null) {
                def.readValueLoop(ByteBuffer.wrap(data))
            }
            graphicDefinitions.put(gfxId, def)
            return def
        }
    }
}
