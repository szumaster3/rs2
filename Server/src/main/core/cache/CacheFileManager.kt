package core.cache

import core.cache.misc.ContainersInformation
import core.tools.StringUtils
import java.nio.ByteBuffer

class CacheFileManager(var cacheFile: CacheFile, var isDiscardFilesData: Boolean) {
    var information: ContainersInformation? = Cache.getReferenceFile().getContainerData(cacheFile.indexFileId)?.let(::ContainersInformation)
    var filesData: Array<Array<ByteArray?>?> = arrayOfNulls(information?.containers?.size ?: 0)
    val containersSize: Int get() = information!!.containers.size

    fun getFilesSize(containerId: Int): Int = information?.containers?.getOrNull(containerId)?.files?.size ?: -1

    fun resetFilesData() {
        filesData = arrayOfNulls(containersSize)
    }

    fun validContainer(containerId: Int): Boolean = information?.containers?.getOrNull(containerId) != null

    fun validFile(containerId: Int, fileId: Int): Boolean =
        information?.containers?.getOrNull(containerId)?.files?.getOrNull(fileId) != null

    fun getFileIds(containerId: Int): IntArray? = information?.containers?.getOrNull(containerId)?.filesIndexes

    fun getArchiveId(name: String?): Int {
        if (name == null) return -1
        val info = information ?: return -1
        val hash = StringUtils.getNameHash(name)
        return info.containersIndexes.firstOrNull { info.containers[it]?.nameHash == hash } ?: -1
    }

    fun loadFilesData(archiveId: Int, keys: IntArray?): Boolean {
        val container = information?.containers?.getOrNull(archiveId) ?: return false
        val data = cacheFile.getContainerUnpackedData(archiveId, keys) ?: return false
        val files = filesData[archiveId] ?: arrayOfNulls<ByteArray>(container.files.size).also { filesData[archiveId] = it }
        val ids = container.filesIndexes
        if (ids.size == 1) {
            files[ids[0]] = data
            return true
        }
        val chunks = data.last().toInt() and 0xFF
        val table = data.size - 1 - chunks * ids.size * 4
        val buffer = ByteBuffer.wrap(data).apply { position(table) }
        val sizes = IntArray(ids.size)
        repeat(chunks) {
            var size = 0
            for (file in ids.indices) {
                size += buffer.int
                sizes[file] += size
            }
        }
        val contents = Array(ids.size) { ByteArray(sizes[it]) }
        sizes.fill(0)
        buffer.position(table)
        var position = 0
        repeat(chunks) {
            var size = 0
            for (file in ids.indices) {
                size += buffer.int
                data.copyInto(contents[file], sizes[file], position, position + size)
                sizes[file] += size
                position += size
            }
        }
        for (file in ids.indices) files[ids[file]] = contents[file]
        return true
    }
    @JvmOverloads
    fun getFileData(containerId: Int, fileId: Int, xteaKeys: IntArray? = null): ByteArray? {
        if (!validFile(containerId, fileId)) return null
        if (filesData[containerId]?.get(fileId) == null && !loadFilesData(containerId, xteaKeys)) return null
        val files = filesData[containerId]!!
        val data = files[fileId]
        if (isDiscardFilesData) {
            if (files.size == 1) filesData[containerId] = null else files[fileId] = null
        }
        return data
    }
}
