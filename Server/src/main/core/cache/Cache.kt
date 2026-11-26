package core.cache

import core.ServerConstants
import core.api.log
import core.cache.def.impl.ItemDefinition
import core.cache.def.impl.SceneryDefinition
import core.tools.Log
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer

object Cache {
    private lateinit var cacheFileManagers: Array<CacheFileManager?>
    private lateinit var reference: CacheFile

    @JvmStatic
    fun init(path: String) {
        openIndexes(path)
        ItemDefinition.parse()
        SceneryDefinition.parse()
    }

    internal fun openIndexes(path: String) {
        log(Cache::class.java, Log.FINE, "Initializing cache...")
        val scratch = ByteArray(520)
        val referenceIndex = RandomAccessFile(File(path, "main_file_cache.idx255"), "r")
        val data = RandomAccessFile(File(path, "main_file_cache.dat2"), "r")
        reference = CacheFile(255, referenceIndex, data, 500000, scratch)
        cacheFileManagers = arrayOfNulls((referenceIndex.length() / 6).toInt())
        for (index in cacheFileManagers.indices) {
            val file = File(path, "main_file_cache.idx$index")
            if (!file.exists() || file.length() == 0L) continue
            val manager = CacheFileManager(CacheFile(index, RandomAccessFile(file, "r"), data, 1000000, scratch), true)
            if (manager.information == null) {
                log(Cache::class.java, Log.ERR, "Error loading cache index $index: no information.")
            } else {
                cacheFileManagers[index] = manager
            }
        }
    }

    @JvmStatic
    fun init() {
        try {
            init(requireNotNull(ServerConstants.CACHE_PATH))
        } catch (error: Throwable) {
            error.printStackTrace()
        }
    }

    @JvmStatic
    fun getIndexes(): Array<CacheFileManager?> = cacheFileManagers

    @JvmStatic
    fun getReferenceFile(): CacheFile = reference

    @JvmStatic
    fun getArchiveData(index: Int, archive: Int, priority: Boolean, encryptionValue: Int): ByteBuffer? {
        val data = when {
            index == 255 && archive == 255 -> {
                val master = generateReferenceData()
                ByteBuffer.allocate(5 + master.size).put(0).putInt(master.size).put(master).array()
            }
            index == 255 -> reference.getContainerData(archive)
            else -> cacheFileManagers.getOrNull(index)?.cacheFile?.getContainerData(archive)
        }
        if (data == null || data.size < 5) {
            log(Cache::class.java, Log.ERR, "Invalid JS-5 request - $index, $archive, $priority, $encryptionValue!")
            return null
        }
        return encodeJs5Group(index, archive, priority, encryptionValue, data)
    }

    internal fun encodeJs5Group(index: Int, archive: Int, priority: Boolean, encryptionValue: Int, data: ByteArray): ByteBuffer? {
        if (data.size < 5) return null
        val compression = data[0].toInt() and 0xFF
        val length = ByteBuffer.wrap(data, 1, 4).int
        val header = if (compression == 0) 5 else 9
        if (length !in 0..(data.size - header)) return null
        val settings = compression or if (priority) 0 else 0x80
        val payloadLength = length + header - 5
        // The first block carries 504 payload bytes, subsequent blocks carry 511.
        val markers = (payloadLength + 6) / 511
        val buffer = ByteBuffer.allocate(8 + payloadLength + markers)
        buffer.put(index.toByte()).putShort(archive.toShort()).put(settings.toByte()).putInt(length)
        for (offset in 5 until payloadLength + 5) {
            if (buffer.position() % 512 == 0) buffer.put(255.toByte())
            buffer.put(data[offset])
        }
        if (encryptionValue != 0) {
            for (offset in 0 until buffer.position()) {
                buffer.put(offset, (buffer.get(offset).toInt() xor encryptionValue).toByte())
            }
        }
        buffer.flip()
        return buffer
    }

    @JvmStatic
    fun generateReferenceData(): ByteArray {
        val buffer = ByteBuffer.allocate(cacheFileManagers.size * 8)
        for ((index, manager) in cacheFileManagers.withIndex()) {
            buffer.putInt(manager?.information?.informationContainer?.crc ?: if (index == 24) 609698396 else 0)
            buffer.putInt(manager?.information?.revision ?: 0)
        }
        return buffer.array()
    }

    @JvmStatic fun getInterfaceDefinitionsComponentsSize(interfaceId: Int): Int = cacheFileManagers[3]!!.getFilesSize(interfaceId)
    @JvmStatic fun getInterfaceDefinitionsSize(): Int = cacheFileManagers[3]!!.containersSize
    @JvmStatic fun getNPCDefinitionsSize(): Int = definitionCount(18, 128)
    @JvmStatic fun getGraphicDefinitionsSize(): Int = definitionCount(21, 256)
    @JvmStatic fun getAnimationDefinitionsSize(): Int = definitionCount(20, 128)
    @JvmStatic fun getObjectDefinitionsSize(): Int = definitionCount(16, 256)
    @JvmStatic fun getItemDefinitionsSize(): Int = definitionCount(19, 256)
    @JvmStatic fun getRenderAnimationDefinitionsSize(): Int = cacheFileManagers[2]?.getFilesSize(32) ?: 0
    private fun definitionCount(index: Int, filesPerContainer: Int): Int {
        val manager = cacheFileManagers[index]!!
        val lastContainer = manager.containersSize - 1
        return lastContainer * filesPerContainer + manager.getFilesSize(lastContainer)
    }
}
