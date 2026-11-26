package core.cache

import core.cache.crypto.ISAACCipher
import core.cache.crypto.XTEACryption
import core.cache.def.impl.AnimationDefinition
import core.cache.misc.ContainersInformation
import core.cache.misc.buffer.BufferInputStream
import core.cache.misc.buffer.ByteBufferUtils
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.RandomAccessFile
import java.nio.file.Path
import org.junit.jupiter.api.io.TempDir
import java.nio.ByteBuffer
import java.util.zip.GZIPOutputStream

class CacheCodecTests {
    @Test
    fun keepsBothWordsOfSequenceFrameIds() {
        val encoded = ByteBuffer.allocate(32)
        encoded.put(1).putShort(1).putShort(4).putShort(0x4567).putShort(0x1234)
        encoded.put(12).put(1).putShort(0x7654).putShort(0x4321).put(0).flip()
        val sequence = AnimationDefinition()
        sequence.readValueLoop(encoded)
        assertArrayEquals(intArrayOf(0x12344567), sequence.anIntArray2139)
        assertArrayEquals(intArrayOf(0x43217654), sequence.anIntArray2151)
    }

    @Test
    fun leavesDiskRevisionOutsideEncryptedContainer(@TempDir directory: Path) {
        val keys = intArrayOf(1, 2, 3, 4)
        val payload = ByteArray(14) { (it + 1).toByte() }
        val packed = ByteBuffer.allocate(5 + payload.size + 2)
        packed.put(0).putInt(payload.size).put(payload).putShort(1)
        XTEACryption.encrypt(keys, packed, 5, 5 + payload.size)
        RandomAccessFile(directory.resolve("index").toFile(), "rw").use { index ->
            RandomAccessFile(directory.resolve("data").toFile(), "rw").use { data ->
                index.write(byteArrayOf(0, 0, packed.capacity().toByte(), 0, 0, 1))
                data.seek(520)
                data.write(byteArrayOf(0, 0, 0, 0, 0, 0, 0, 5))
                data.write(packed.array())
                val cache = CacheFile(5, index, data, 1024, ByteArray(520))
                assertArrayEquals(payload, cache.getContainerUnpackedData(0, keys))
            }
        }
    }

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
    fun matchesRt5OpcodeCipherAcrossStateRollovers() {
        val cipher = ISAACCipher(intArrayOf(Int.MIN_VALUE, -1, 0, Int.MAX_VALUE))
        val expected = mapOf(0 to -865494573, 255 to -295081351, 256 to -1023850231,
            511 to 2074757517, 512 to 803662694, 1023 to 700053924, 1024 to 123248058)
        repeat(1025) { offset ->
            val word = cipher.nextValue
            expected[offset]?.let { assertEquals(it, word, "ISAAC word $offset") }
        }
    }
}
