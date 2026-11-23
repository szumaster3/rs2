package core.cache.def.impl

import core.cache.Cache
import core.cache.misc.buffer.ByteBufferUtils
import java.nio.ByteBuffer

/**
 * Represents an animation's definitions.
 *
 * @author Emperor
 */
class AnimationDefinition {
    var anInt2136: Int = 99
    var anIntArray2139: IntArray = intArrayOf()
    var anInt2140: Int = -1
    var aBoolean2141: Boolean = false
    var anInt2142: Int = 5
    var emoteItem: Int = -1
    var anInt2144: Int = -1
    var handledSounds: Array<IntArray?> = emptyArray()
    var aBooleanArray2149: BooleanArray? = null
    var anIntArray2151: IntArray = intArrayOf()
    var aBoolean2152: Boolean = false
    var durations: IntArray? = null
    var anInt2155: Int = 2
    var anInt2162: Int = -1
    var anInt2163: Int = -1

    private fun readValueLoop(buffer: ByteBuffer) {
        while (true) {
            val opcode = buffer.get().toInt() and 0xFF
            if (opcode == 0) {
                break
            }
            readValues(buffer, opcode)
        }
    }

    val duration: Int
        /**
         * Gets the duration of this animation in milliseconds.
         *
         * @return The duration.
         */
        get() {
            var duration = 0
            for (i in durations ?: return 0) {
                if (i > 100) {
                    continue
                }
                duration += i * 20
            }
            return duration
        }

    val cycles: Int
        get() {
            return durations?.sum() ?: 0
        }

    val durationTicks: Int
        /**
         * Gets the duration of this animation in (600ms) ticks.
         *
         * @return The duration in ticks.
         */
        get() {
            return (duration / 600).coerceAtLeast(1)
        }

    private fun readValues(buffer: ByteBuffer, opcode: Int) {
        when (opcode) {
            1 -> {
                val count = buffer.short.toInt() and 0xffff
                durations = IntArray(count) { buffer.short.toInt() and 0xffff }
                anIntArray2139 = IntArray(count) { buffer.short.toInt() and 0xffff }
                repeat(count) { index ->
                    anIntArray2139[index] += buffer.short.toInt() and (0xffff shl 16)
                }
            }
            2 -> anInt2163 = buffer.short.toInt() and 0xffff
            3 -> {
                val flags = BooleanArray(256)
                repeat(buffer.get().toInt() and 0xff) { flags[buffer.get().toInt() and 0xff] = true }
                aBooleanArray2149 = flags
            }
            4 -> aBoolean2152 = true
            5 -> anInt2142 = buffer.get().toInt() and 0xff
            6 -> anInt2144 = buffer.short.toInt() and 0xffff
            7 -> emoteItem = buffer.short.toInt() and 0xffff
            8 -> anInt2136 = buffer.get().toInt() and 0xff
            9 -> anInt2140 = buffer.get().toInt() and 0xff
            10 -> anInt2162 = buffer.get().toInt() and 0xff
            11 -> anInt2155 = buffer.get().toInt() and 0xff
            12 -> {
                val count = buffer.get().toInt() and 0xff
                anIntArray2151 = IntArray(count) { buffer.short.toInt() and 0xffff }
                repeat(count) { index ->
                    anIntArray2151[index] += buffer.short.toInt() and (0xffff shl 16)
                }
            }
            13 -> handledSounds = Array(buffer.short.toInt() and 0xffff) {
                val count = buffer.get().toInt() and 0xff
                if (count == 0) null else IntArray(count) { index ->
                    if (index == 0) ByteBufferUtils.getMedium(buffer) else buffer.short.toInt() and 0xffff
                }
            }
            14 -> aBoolean2141 = true
        }
    }

    fun method2394() {
        if (anInt2140 == -1) {
            anInt2140 = if (aBooleanArray2149 == null) 0
            else 2
        }
        if (anInt2162 == -1) {
            anInt2162 = if (aBooleanArray2149 == null) 0
            else 2
        }
    }

    companion object {
        private val animDefs: MutableMap<Int, AnimationDefinition> = HashMap()

        @JvmStatic
        fun forId(emoteId: Int): AnimationDefinition? {
            try {
                var defs = animDefs[emoteId]
                if (defs != null) {
                    return defs
                }
                val data = Cache.getIndexes()[20]!!.getFileData(emoteId ushr 7, emoteId and 0x7f, null)
                defs = AnimationDefinition()
                if (data != null) {
                    defs.readValueLoop(ByteBuffer.wrap(data))
                }
                defs.method2394()
                animDefs[emoteId] = defs
                return defs
            } catch (t: Throwable) {
                return null
            }
        }
    }
}
