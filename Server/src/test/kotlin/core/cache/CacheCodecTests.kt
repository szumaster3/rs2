package core.cache

import core.cache.crypto.ISAACCipher
import core.cache.crypto.XTEACryption
import core.cache.misc.ContainersInformation
import core.cache.misc.buffer.BufferInputStream
import core.cache.misc.buffer.ByteBufferUtils
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.zip.GZIPOutputStream

class CacheCodecTests {
    @Test
    fun readsUnsignedStreamBytesAndReportsEndOfInput() {
        val input = BufferInputStream(ByteBuffer.wrap(byteArrayOf(0, 127, 128.toByte(), 255.toByte())))
        assertEquals(listOf(0, 127, 128, 255, -1), List(5) { input.read() })
    }

    @Test
    fun readsSmartValuesAtEncodingBoundaries() {
        val buffer = ByteBuffer.wrap(byteArrayOf(0, 127, 128.toByte(), 128.toByte(), 255.toByte(), 255.toByte()))
        assertEquals(listOf(0, 127, 128, 32767), List(4) { ByteBufferUtils.getSmart(buffer) })
    }

    @Test
    fun encryptsOnlyCompleteXteaBlocksWithoutMovingTheBuffer() {
        val original = ByteArray(30) { it.toByte() }
        val buffer = ByteBuffer.wrap(original.copyOf()).apply { position(3) }
        val keys = intArrayOf(Int.MIN_VALUE, -1, 0, Int.MAX_VALUE)
        XTEACryption.encrypt(keys, buffer, 5, 26)
        assertEquals(3, buffer.position())
        assertArrayEquals(original.copyOfRange(0, 5), buffer.array().copyOfRange(0, 5))
        assertArrayEquals(original.copyOfRange(21, 30), buffer.array().copyOfRange(21, 30))
        XTEACryption.decrypt(keys, buffer, 5, 26)
        assertArrayEquals(original, buffer.array())
        assertEquals(3, buffer.position())
    }

    @Test
    fun unpacksGzipContainersWithTrailingRevisionBytes() {
        val data = ByteArray(2048) { (it * 37).toByte() }
        val compressed = ByteArrayOutputStream().also { output ->
            GZIPOutputStream(output).use { it.write(data) }
        }.toByteArray()
        val container = ByteBuffer.allocate(11 + compressed.size)
            .put(2).putInt(compressed.size).putInt(data.size).put(compressed).putShort(530)
        assertArrayEquals(data, ContainersInformation.unpackCacheContainer(container.array()))
        assertNull(ContainersInformation.unpackCacheContainer(byteArrayOf(0, -1, -1, -1, -1)))
    }

    @Test
    fun preservesDisabledOpcodeCipherAcrossStateRollovers() {
        val cipher = ISAACCipher(intArrayOf(Int.MIN_VALUE, -1, 0, Int.MAX_VALUE))
        repeat(1025) { assertEquals(0, cipher.nextValue) }
    }
}
