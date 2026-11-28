package core.cache.def.impl

import core.cache.Cache
import java.nio.ByteBuffer
import java.util.HashMap

class SpotAnimType {
    @JvmField var retextureDestination: ShortArray? = null
    @JvmField var recolourSource: ShortArray? = null
    @JvmField var ambient: Int = 0
    @JvmField var alignToTerrain: Boolean = false
    @JvmField var defaultModel: Int = 0
    @JvmField var resizeHorizontal: Int = 128
    /** SpotAnimType opcode 10: allows sequence looping and movement-dependent playback. */
    var loopsSequence: Boolean = false
    @JvmField var resizeVertical: Int = 128
    @JvmField var animationId: Int = -1
    @JvmField var contrast: Int = 0
    @JvmField var graphicsId: Int = 0
    @JvmField var rotation: Int = 0
    @JvmField var retextureSource: ShortArray? = null
    @JvmField var recolourDestination: ShortArray? = null
    @JvmField var byteValue: Byte = 0
    @JvmField var intValue: Int = -1
    /** SpotAnimType opcode 10: allows sequence looping and movement-dependent playback. */
    val sequenceDefinition: SeqType? get() = if (animationId == -1) null else SeqType.forId(animationId)

    internal fun readValueLoop(buffer: ByteBuffer) {
        while (true) {
            val opcode = buffer.get().toInt() and 0xff
            if (opcode == 0) return
            readValues(buffer, opcode)
        }
    }

    fun readValues(buffer: ByteBuffer, opcode: Int) {
        when (opcode) {
            1 -> defaultModel = buffer.short.toInt() and 0xffff
            2 -> animationId = buffer.short.toInt() and 0xffff
            4 -> resizeHorizontal = buffer.short.toInt() and 0xffff
            5 -> resizeVertical = buffer.short.toInt() and 0xffff
            6 -> rotation = buffer.short.toInt() and 0xffff
            7 -> ambient = buffer.get().toInt() and 0xff
            8 -> contrast = buffer.get().toInt() and 0xff
            9 -> alignToTerrain = true // SpotAnimType.aBoolean178
            10 -> loopsSequence = true
            40, 41 -> {
                val count = buffer.get().toInt() and 0xff
                val source = ShortArray(count)
                val target = ShortArray(count)
                repeat(count) { index ->
                    source[index] = buffer.short
                    target[index] = buffer.short
                }
                if (opcode == 40) {
                    recolourSource = source
                    recolourDestination = target
                } else {
                    retextureSource = source
                    retextureDestination = target
                }
            }
            else -> error("Unknown graphic opcode $opcode at ${buffer.position() - 1}")
        }
    }

    companion object {
        private val graphicDefinitions: MutableMap<Int, SpotAnimType> = HashMap<Int, SpotAnimType>()

        @JvmStatic
        fun forId(gfxId: Int): SpotAnimType {
            var def = graphicDefinitions.get(gfxId)
            if (def != null) {
                return def
            }
            val data = Cache.getIndexes()[21]!!.getFileData(gfxId.ushr(8), gfxId and 0xff, null)
            def = SpotAnimType()
            def.graphicsId = gfxId
            if (data != null) {
                val buffer = ByteBuffer.wrap(data)
                def.readValueLoop(buffer)
                check(!buffer.hasRemaining()) { "Trailing bytes in spot animation $gfxId" }
            }
            graphicDefinitions.put(gfxId, def)
            return def
        }
    }
}
