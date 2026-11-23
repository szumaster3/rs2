package core.cache.crypto

import java.nio.ByteBuffer

object XTEACryption {
    private const val DELTA = -1640531527
    private const val SUM = -957401312

    @JvmStatic
    fun decrypt(keys: IntArray, buffer: ByteBuffer): ByteBuffer =
        decrypt(keys, buffer, buffer.position(), buffer.limit())

    @JvmStatic
    fun decrypt(keys: IntArray, buffer: ByteBuffer, offset: Int, length: Int): ByteBuffer {
        for (index in offset until offset + (length - offset) / 8 * 8 step 8) {
            var first = buffer.getInt(index)
            var second = buffer.getInt(index + 4)
            var sum = SUM
            repeat(32) {
                second -= (keys[(sum and 0x1933) ushr 11] + sum) xor (first + ((first shl 4) xor (first ushr 5)))
                sum -= DELTA
                first -= (((second shl 4) xor (second ushr 5)) + second) xor (keys[sum and 3] + sum)
            }
            buffer.putInt(index, first).putInt(index + 4, second)
        }
        return buffer
    }

    @JvmStatic
    fun encrypt(keys: IntArray, buffer: ByteBuffer) = encrypt(keys, buffer, buffer.position(), buffer.limit())

    @JvmStatic
    fun encrypt(keys: IntArray, buffer: ByteBuffer, offset: Int, length: Int) {
        for (index in offset until offset + (length - offset) / 8 * 8 step 8) {
            var first = buffer.getInt(index)
            var second = buffer.getInt(index + 4)
            var sum = 0
            repeat(32) {
                first += (((second shl 4) xor (second ushr 5)) + second) xor (keys[sum and 3] + sum)
                sum += DELTA
                second += (keys[(sum and 0x1933) ushr 11] + sum) xor (first + ((first shl 4) xor (first ushr 5)))
            }
            buffer.putInt(index, first).putInt(index + 4, second)
        }
    }
}
