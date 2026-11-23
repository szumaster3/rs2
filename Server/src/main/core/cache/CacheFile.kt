package core.cache

import core.cache.crypto.XTEACryption
import core.cache.misc.ContainersInformation
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer

class CacheFile(
    val indexFileId: Int,
    private val indexFile: RandomAccessFile,
    private val dataFile: RandomAccessFile,
    private val maxContainerSize: Int,
    private val cacheFileBuffer: ByteArray,
) {
    @JvmOverloads
    fun getContainerUnpackedData(containerId: Int, xteaKeys: IntArray? = null): ByteArray? {
        var packed = getContainerData(containerId) ?: return null
        if (xteaKeys != null && xteaKeys.any { it != 0 }) {
            packed = XTEACryption.decrypt(xteaKeys, ByteBuffer.wrap(packed), 5, packed.size).array()
        }
        return ContainersInformation.unpackCacheContainer(packed)
    }

    fun getContainerData(containerId: Int): ByteArray? = synchronized(dataFile) {
        try {
            if (containerId < 0 || indexFile.length() < 6L * (containerId + 1)) return@synchronized null
            indexFile.seek(6L * containerId)
            indexFile.readFully(cacheFileBuffer, 0, 6)
            val size = medium(0)
            var sector = medium(3)
            if (size > maxContainerSize || sector <= 0 || sector > dataFile.length() / 520) return@synchronized null
            val data = ByteArray(size)
            var position = 0
            var part = 0
            while (position < size) {
                if (sector == 0) return@synchronized null
                dataFile.seek(520L * sector)
                val count = minOf(size - position, 512)
                dataFile.readFully(cacheFileBuffer, 0, 8 + count)
                val storedId = ((cacheFileBuffer[0].toInt() and 0xFF) shl 8) or (cacheFileBuffer[1].toInt() and 0xFF)
                val storedPart = ((cacheFileBuffer[2].toInt() and 0xFF) shl 8) or (cacheFileBuffer[3].toInt() and 0xFF)
                val nextSector = medium(4)
                if (storedId != containerId || storedPart != part || cacheFileBuffer[7].toInt() and 0xFF != indexFileId ||
                    nextSector > dataFile.length() / 520) return@synchronized null
                cacheFileBuffer.copyInto(data, position, 8, 8 + count)
                position += count
                part++
                sector = nextSector
            }
            data
        } catch (error: IOException) {
            error.printStackTrace()
            null
        }
    }
    private fun medium(offset: Int): Int =
        ((cacheFileBuffer[offset].toInt() and 0xFF) shl 16) or
            ((cacheFileBuffer[offset + 1].toInt() and 0xFF) shl 8) or
            (cacheFileBuffer[offset + 2].toInt() and 0xFF)
}
