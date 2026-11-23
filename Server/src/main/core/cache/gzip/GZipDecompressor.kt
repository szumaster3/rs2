package core.cache.gzip

import java.nio.ByteBuffer
import java.util.zip.Inflater

object GZipDecompressor {
    private val inflater = Inflater(true)

    @JvmStatic
    fun decompress(buffer: ByteBuffer, data: ByteArray) {
        synchronized(inflater) {
            try {
                if (buffer.get(buffer.position()) != 31.toByte() || buffer.get(buffer.position() + 1) != 139.toByte()) return
                inflater.setInput(buffer.array(), buffer.position() + 10, buffer.limit() - buffer.position() - 18)
                inflater.inflate(data)
            } catch (_: Exception) {
            } finally {
                inflater.reset()
            }
        }
    }

    @JvmStatic
    fun decompress(compressed: ByteArray, data: ByteArray, offset: Int, length: Int): Boolean =
        synchronized(inflater) {
            if (data[offset] != 31.toByte() || data[offset + 1] != 139.toByte()) return@synchronized false
            try {
                inflater.setInput(data, offset + 10, length - offset - 18)
                inflater.inflate(compressed)
                true
            } catch (error: Exception) {
                error.printStackTrace()
                false
            } finally {
                inflater.reset()
            }
        }
}
