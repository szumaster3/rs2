package core.cache.def.impl

import core.cache.Cache
import core.cache.misc.buffer.ByteBufferUtils
import java.nio.ByteBuffer
import kotlin.math.max

/**
 * Represents an animation's definitions.
 *
 * @author Emperor
 */
class AnimationDefinition {
    var anInt2136: Int = 99
    var anIntArray2139: IntArray = intArrayOf()
    var anInt2140: Int = 0
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
    var anInt2162: Int = 0
    var anInt2163: Int = 0

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
            if (durations == null) {
                return 0
            }
            var duration = 0
            for (i in durations!!) {
                if (i > 100) {
                    continue
                }
                duration += i * 20
            }
            return duration
        }

    val cycles: Int
        get() {
            if (durations == null) return 0
            var duration = 0
            for (i in durations!!) duration += i
            return duration
        }

    val durationTicks: Int
        /**
         * Gets the duration of this animation in (600ms) ticks.
         *
         * @return The duration in ticks.
         */
        get() {
            val ticks = duration / 600
            return max(ticks.toDouble(), 1.0).toInt()
        }

    private fun readValues(buffer: ByteBuffer, opcode: Int) {
        if (opcode == 1) {
            val length = buffer.getShort().toInt() and 0xFFFF
            durations = IntArray(length)
            for (i in 0 until length) {
                durations!![i] = buffer.getShort().toInt() and 0xFFFF
            }
            anIntArray2139 = IntArray(length)
            for (i in 0 until length) {
                anIntArray2139[i] = buffer.getShort().toInt() and 0xFFFF
            }
            for (i in 0 until length) {
                anIntArray2139[i] = ((buffer.getShort().toInt() and (0xFFFF shl 16)) + anIntArray2139[i])
            }
        } else if (opcode != 2) {
            if (opcode != 3) {
                if (opcode == 4) aBoolean2152 = true
                else if (opcode == 5) anInt2142 = buffer.get().toInt() and 0xFF
                else if (opcode != 6) {
                    if (opcode == 7) emoteItem = buffer.getShort().toInt() and 0xFFFF
                    else if ((opcode xor -0x1) != -9) {
                        if (opcode != 9) {
                            if (opcode != 10) {
                                if (opcode == 11) anInt2155 = buffer.get().toInt() and 0xFF
                                else if (opcode == 12) {
                                    val i = buffer.get().toInt() and 0xFF
                                    anIntArray2151 = IntArray(i)
                                    var i_19_ = 0
                                    while (((i_19_ xor -0x1) > (i xor -0x1))) {
                                        anIntArray2151[i_19_] = buffer.getShort().toInt() and 0xFFFF
                                        i_19_++
                                    }
                                    var i_20_ = 0
                                    while (i > i_20_) {
                                        anIntArray2151[i_20_] =
                                            ((buffer.getShort().toInt() and (0xFFFF shl 16)) + anIntArray2151[i_20_])
                                        i_20_++
                                    }
                                } else if (opcode == 13) {
                                    // opcode 13
                                    val i = buffer.getShort().toInt() and 0xFFFF
                                    handledSounds = arrayOfNulls(i)
                                    for (i_21_ in 0 until i) {
                                        val i_22_ = buffer.get().toInt() and 0xFF
                                        if ((i_22_ xor -0x1) < -1) {
                                            handledSounds[i_21_] = IntArray(i_22_)
                                            handledSounds[i_21_]!![0] = ByteBufferUtils.getMedium(buffer)
                                            var i_23_ = 1
                                            while (((i_22_ xor -0x1) < (i_23_ xor -0x1))) {
                                                handledSounds[i_21_]!![i_23_] = buffer.getShort().toInt() and 0xFFFF
                                                i_23_++
                                            }
                                        }
                                    }
                                } else if (opcode == 14) {
                                    aBoolean2141 = true
                                } else {
                                }
                            } else anInt2162 = buffer.get().toInt() and 0xFF
                        } else anInt2140 = buffer.get().toInt() and 0xFF
                    } else anInt2136 = buffer.get().toInt() and 0xFF
                } else anInt2144 = buffer.getShort().toInt() and 0xFFFF
            } else {
                aBooleanArray2149 = BooleanArray(256)
                val length = buffer.get().toInt() and 0xFF
                for (i in 0 until length) {
                    aBooleanArray2149!![buffer.get().toInt() and 0xFF] = true
                }
            }
        } else anInt2163 = buffer.getShort().toInt() and 0xFFFF
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

    init {
        emoteItem = -1
        anInt2140 = -1
        anInt2163 = -1
        anInt2162 = -1
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
                val data = Cache.getIndexes()[20].getFileData(emoteId ushr 7, emoteId and 0x7f, null)
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