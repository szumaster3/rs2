package core.cache.misc

import core.cache.bzip2.BZip2Decompressor
import core.cache.gzip.GZipDecompressor
import java.nio.ByteBuffer
import java.util.zip.CRC32

class ContainersInformation(packedData: ByteArray) {
    val data = packedData.copyOf()
    val informationContainer = Container().apply {
        version = ((packedData[packedData.size - 2].toInt() and 0xFF) shl 8) or (packedData.last().toInt() and 0xFF)
        crc = CRC32().apply { update(packedData) }.value.toInt()
    }
    var revision = 0
        private set
    var containersIndexes = IntArray(0)
        private set
    var containers: Array<FilesContainer?> = emptyArray()
        private set
    var isWhirpool = false
        private set

    init {
        decodeContainersInformation(requireNotNull(unpackCacheContainer(packedData)))
    }

    fun decodeContainersInformation(data: ByteArray) {
        val buffer = ByteBuffer.wrap(data)
        val protocol = buffer.get().toInt() and 0xFF
        require(protocol == 5 || protocol == 6) { "Unsupported cache reference protocol $protocol" }
        revision = if (protocol == 6) buffer.int else 0
        val flags = buffer.get().toInt() and 0xFF
        val named = flags and 1 != 0
        isWhirpool = flags and 2 != 0
        var id = 0
        containersIndexes = IntArray(buffer.short.toInt() and 0xFFFF) { id += buffer.short.toInt() and 0xFFFF; id }
        containers = arrayOfNulls((containersIndexes.maxOrNull() ?: -1) + 1)
        val entries = containersIndexes.map { index -> FilesContainer().also { containers[index] = it } }
        if (named) entries.forEach { it.nameHash = buffer.int }
        val hashes = if (isWhirpool) Array(entries.size) { ByteArray(64).also { buffer.get(it) } } else null
        entries.forEach { it.crc = buffer.int }
        entries.forEach { it.version = buffer.int }
        entries.forEach { it.filesIndexes = IntArray(buffer.short.toInt() and 0xFFFF) }
        for (entry in entries) {
            var fileId = 0
            for (index in entry.filesIndexes.indices) {
                fileId += buffer.short.toInt() and 0xFFFF
                entry.filesIndexes[index] = fileId
            }
            entry.files = arrayOfNulls((entry.filesIndexes.maxOrNull() ?: -1) + 1)
            for (index in entry.filesIndexes) entry.files[index] = Container()
        }
        if (hashes != null) {
            for ((index, entry) in entries.withIndex()) {
                for (fileId in entry.filesIndexes) entry.files[fileId]!!.version = hashes[index][fileId].toInt()
            }
        }
        if (named) entries.forEach { entry -> entry.filesIndexes.forEach { entry.files[it]!!.nameHash = buffer.int } }
    }

    companion object {

        @JvmStatic
        fun unpackCacheContainer(packedData: ByteArray): ByteArray? {
            val buffer = ByteBuffer.wrap(packedData)
            val compression = buffer.get().toInt() and 0xFF
            val size = buffer.int
            if (size !in 0..5000000) return null
            if (compression == 0) return ByteArray(size).also { buffer.get(it) }
            val unpackedSize = buffer.int
            if (unpackedSize !in 0..20000000) return null
            return ByteArray(unpackedSize).also {
                if (compression == 1) BZip2Decompressor.decompress(it, packedData, size, 9)
                else GZipDecompressor.decompress(buffer, it)
            }
        }
    }
}
