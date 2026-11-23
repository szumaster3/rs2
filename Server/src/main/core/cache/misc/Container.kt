package core.cache.misc

open class Container {
    var version = -1
    var crc = -1
    var nameHash = -1
    var isUpdated = false
        private set
    val nextVersion: Int get() = if (isUpdated) version else version + 1

    fun updateVersion() {
        version++
        isUpdated = true
    }
}
