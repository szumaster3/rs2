package core.cache.def

import core.game.node.Node
import core.tools.StringUtils

open class Definition<T : Node> {
    open var id = 0
    open var name = "null"
    open var options: Array<String?> = emptyArray()
    val handlers: MutableMap<String, Any?> = HashMap()
    protected var examineText: String? = null
    open val examine: String?
        get() {
            examineText?.let { return it }
            val text = handlers["examine"]?.toString()
                ?: if (name.isEmpty()) "null" else "It's a" + (if (StringUtils.isPlusN(name)) "n " else " ") + name + "."
            examineText = text
            return text
        }

    fun setExamine(value: String?) { examineText = value }
    @JvmOverloads
    fun hasOptions(examine: Boolean = true): Boolean =
        name != "null" && options.any { it != null && it != "null" && (examine || it != "Examine") }
    @Suppress("UNCHECKED_CAST")
    fun <V> getConfiguration(key: String): V? = handlers[key] as V?
    @Suppress("UNCHECKED_CAST")
    fun <V> getConfiguration(key: String, fail: V): V = handlers[key] as V? ?: fail
}
