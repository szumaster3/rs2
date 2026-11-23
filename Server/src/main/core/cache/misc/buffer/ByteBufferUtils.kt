package core.cache.misc.buffer

import java.io.ObjectInputStream
import java.nio.ByteBuffer

object ByteBufferUtils {

    @JvmStatic
    fun getString(buffer: ByteBuffer): String = buildString {
        while (true) {
            val value = buffer.get()
            if (value == 0.toByte()) break
            append(value.toInt().toChar())
        }
    }

    @JvmStatic
    fun putString(value: String, buffer: ByteBuffer) {
        buffer.put(value.toByteArray(Charsets.UTF_8)).put(0)
    }

    @JvmStatic
    fun putGJ2String(value: String, buffer: ByteBuffer): ByteBuffer {
        val packed = ByteArray(256)
        val length = packGJString2(0, packed, value)
        return buffer.put(0).put(packed, 0, length).put(0)
    }

    @JvmStatic
    fun decodeXTEA(keys: IntArray, start: Int, end: Int, buffer: ByteBuffer) {
        val position = buffer.position()
        for (index in start until start + (end - start) / 8 * 8 step 8) {
            var first = buffer.getInt(index)
            var second = buffer.getInt(index + 4)
            var sum = -957401312
            repeat(32) {
                second -= (keys[(sum and 0x1C84) ushr 11] + sum) xor (((first ushr 5) xor (first shl 4)) + first)
                sum -= -1640531527
                first -= (((second ushr 5) xor (second shl 4)) + second) xor (keys[sum and 3] + sum)
            }
            buffer.putInt(index, first).putInt(index + 4, second)
        }
        buffer.position(position)
    }

    @JvmStatic
    fun isEmptyXtea(keys: IntArray): Boolean = keys.size != 4 || keys.all { it == 0 }

    @JvmStatic
    fun packGJString2(position: Int, buffer: ByteArray, string: String): Int {
        var offset = position
        for (character in string) {
            val value = character.code
            when {
                value > 2047 -> {
                    buffer[offset++] = ((value or 919275) shr 12).toByte()
                    buffer[offset++] = (128 or ((value shr 6) and 63)).toByte()
                    buffer[offset++] = (128 or (value and 63)).toByte()
                }
                value > 127 -> {
                    buffer[offset++] = ((value or 12309) shr 6).toByte()
                    buffer[offset++] = (128 or (value and 63)).toByte()
                } else -> buffer[offset++] = value.toByte()
            }
        }
        return offset - position
    }

    @JvmStatic
    fun getMedium(buffer: ByteBuffer): Int =
        ((buffer.get().toInt() and 0xFF) shl 16) or
            ((buffer.get().toInt() and 0xFF) shl 8) or (buffer.get().toInt() and 0xFF)

    @JvmStatic
    fun getSmart(buffer: ByteBuffer): Int {
        val first = buffer.get().toInt() and 0xFF
        return if (first <= 127) first else ((first shl 8) or (buffer.get().toInt() and 0xFF)) - 32768
    }

    @JvmStatic
    fun getBigSmart(buffer: ByteBuffer): Int {
        var value = 0
        while (true) {
            val part = getSmart(buffer)
            value += part
            if (part != 32767) return value
        }
    }

    @JvmStatic
    fun getObject(buffer: ByteBuffer): Any? {
        val length = buffer.int
        if (length <= 0) return null
        val data = ByteArray(length).also { buffer.get(it) }
        return try {
            ObjectInputStream(BufferInputStream(ByteBuffer.wrap(data))).use { it.readObject() }
        } catch (error: Throwable) {
            error.printStackTrace()
            null
        }
    }
}
