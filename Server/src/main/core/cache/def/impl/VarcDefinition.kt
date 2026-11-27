package core.cache.def.impl

import core.cache.Cache

/** Revision 578 client variable definitions: JS5 index 2, groups 19 and 15. */
object VarcDefinition {
    fun exists(id: Int): Boolean = id in 0..65535 &&
        Cache.getIndexes().getOrNull(2)?.validFile(19, id) == true

    fun stringExists(id: Int): Boolean = id in 0..65535 &&
        Cache.getIndexes().getOrNull(2)?.validFile(15, id) == true
}
