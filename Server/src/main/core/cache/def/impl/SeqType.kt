package core.cache.def.impl

import core.cache.Cache
import core.cache.misc.buffer.ByteBufferUtils
import java.nio.ByteBuffer

/**
 * Represents an animation's definitions.
 *
 * @author Emperor
 */
/** Sequence definition (client Class157.java / SeqTypes; semantic name, not an original recovered symbol). */
class SeqType {
    var maxLoops: Int = 99
    var frameIds: IntArray = intArrayOf()
    var preAnimationMovement: Int = -1
    var tween = false
    // The client consumes opcode 16, but its functional meaning is not established.
    var opcode16Flag = false
    // Opcode 14 affects model animation; exact semantics not established.
    var opcode14Flag: Boolean = false
    var playbackPriority: Int = 5
    var rightHandItem: Int = -1
    var leftHandItem: Int = -1
    var handledSounds: Array<IntArray?> = emptyArray()
    var walkMergeMask: BooleanArray? = null
    var interfaceFrameIds: IntArray = intArrayOf()
    var stretches: Boolean = false
    var durations: IntArray? = null
    var replayMode: Int = 2
    var postAnimationMovement: Int = -1
    var loopFrameCount: Int = -1

    internal fun readValueLoop(buffer: ByteBuffer) {
        while (true) {
            val opcode = buffer.get().toInt() and 0xFF
            if (opcode == 0) {
                break
            }
            readValues(buffer, opcode)
        }
    }

    /** Raw single-pass sequence duration, including long hold frames (client cycles). */
    val fullDurationMillis: Int get() = cycles * 20
    val fullDurationTicks: Int get() = ((cycles + 29) / 30).coerceAtLeast(1)

    // Legacy gameplay duration deliberately excludes long hold frames; retain existing callers.
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
                frameIds = IntArray(count) { buffer.short.toInt() and 0xffff }
                repeat(count) { index ->
                    frameIds[index] += (buffer.short.toInt() and 0xffff) shl 16
                }
            }
            2 -> loopFrameCount = buffer.short.toInt() and 0xffff
            3 -> {
                val flags = BooleanArray(256)
                repeat(buffer.get().toInt() and 0xff) { flags[buffer.get().toInt() and 0xff] = true }
                walkMergeMask = flags
            }
            4 -> stretches = true
            5 -> playbackPriority = buffer.get().toInt() and 0xff
            6 -> leftHandItem = buffer.short.toInt() and 0xffff
            7 -> rightHandItem = buffer.short.toInt() and 0xffff
            8 -> maxLoops = buffer.get().toInt() and 0xff
            9 -> preAnimationMovement = buffer.get().toInt() and 0xff
            10 -> postAnimationMovement = buffer.get().toInt() and 0xff
            11 -> replayMode = buffer.get().toInt() and 0xff
            12 -> {
                val count = buffer.get().toInt() and 0xff
                interfaceFrameIds = IntArray(count) { buffer.short.toInt() and 0xffff }
                repeat(count) { index ->
                    interfaceFrameIds[index] += (buffer.short.toInt() and 0xffff) shl 16
                }
            }
            13 -> handledSounds = Array(buffer.short.toInt() and 0xffff) {
                val count = buffer.get().toInt() and 0xff
                if (count == 0) null else IntArray(count) { index ->
                    if (index == 0) ByteBufferUtils.getMedium(buffer) else buffer.short.toInt() and 0xffff
                }
            }
            14 -> opcode14Flag = true
            15 -> tween = true
            16 -> opcode16Flag = true
            else -> error("Unknown sequence opcode $opcode at ${buffer.position() - 1}")
        }
    }

    fun applyMovementDefaults() {
        if (preAnimationMovement == -1) {
            preAnimationMovement = if (walkMergeMask == null) 0
            else 2
        }
        if (postAnimationMovement == -1) {
            postAnimationMovement = if (walkMergeMask == null) 0
            else 2
        }
    }

    companion object {
        private val animDefs: MutableMap<Int, SeqType> = HashMap()

        @JvmStatic
        fun forId(emoteId: Int): SeqType? {
            try {
                var defs = animDefs[emoteId]
                if (defs != null) {
                    return defs
                }
                val data = Cache.getIndexes()[20]!!.getFileData(emoteId ushr 7, emoteId and 0x7f, null)
                defs = SeqType()
                if (data != null) {
                    val buffer = ByteBuffer.wrap(data)
                    defs.readValueLoop(buffer)
                    check(!buffer.hasRemaining()) { "Trailing bytes in sequence $emoteId" }
                }
                defs.applyMovementDefaults()
                animDefs[emoteId] = defs
                return defs
            } catch (t: Throwable) {
                return null
            }
        }
    }
}
