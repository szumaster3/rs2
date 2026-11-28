package core.game.node.entity.player.link

/** Layout of the fixed and large top-level interfaces. See CS2 script8, script2458,
 * script722, script709 and subchanged, and the two IF3 root definitions. */
internal object Gameframe {
    const val FIXED = 548
    const val LARGE = 746

    fun isGameframe(root: Int): Boolean = root == FIXED || root == LARGE
    fun root(resizable: Boolean): Int = if (resizable) LARGE else FIXED

    private val fixedToLarge = linkedMapOf<Int, Int>().apply {
        put(5, 6)   // full viewport (script709)
        put(6, 5)   // walkable overlay
        put(16, 8)  // 512x334 modal, bank height is adjusted by script722
        put(147, 30) // side modal (subchanged)
        put(20, 15) // chat filters
        put(142, 18) // chat top
        put(12, 11) // status icons (82x25 / 115x35, subchanged)
        put(134, 169); put(136, 170); put(137, 171); put(139, 172)
        for (index in 0..15) put(152 + index, 33 + index)
        put(169, 50) // summoning details (script8(95))
        put(170, 51) // logout (script8(99))
    }
    private val largeToFixed = fixedToLarge.entries.associate { (fixed, large) -> large to fixed }

    fun destination(oldRoot: Int, child: Int): Int? = when (oldRoot) {
        FIXED -> fixedToLarge[child]
        LARGE -> largeToFixed[child]
        else -> null
    }

    fun tabChild(root: Int, index: Int): Int = when (index) {
        95 -> if (root == LARGE) 50 else 169
        99 -> if (root == LARGE) 51 else 170
        else -> (if (root == LARGE) 33 else 152) + index
    }

    fun tabButton(root: Int, index: Int): Int? = when (index) {
        99 -> if (root == LARGE) 168 else 133
        in 0..15 -> if (root == LARGE) 124 + index else if (index < 8) 81 + index else 51 + index - 8
        else -> null
    }

    fun statusIcons(root: Int): Int = if (root == LARGE) 11 else 12
}
