package content.region.other.tutorial_island.plugin

import core.api.*
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.appearance.Gender
import core.tools.RandomFunction
import kotlin.math.abs

/**
 * Handles the Character Design interface and customization logic.
 *
 * @author Emperor, Vexia
 */
object CharacterDesign {

    val MALE_LOOK_IDS = arrayOf(
        // Male head component ids.
        intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 91, 92, 93, 94, 95, 96, 97, 261, 262, 263, 264, 265, 266, 267, 268),
        // Male jaw component ids.
        intArrayOf(10, 11, 12, 13, 14, 15, 16, 17, 98, 99, 100, 101, 102, 103, 104, 305, 306, 307, 308),
        // Male torso component ids.
        intArrayOf(18, 19, 20, 21, 22, 23, 24, 25, 111, 112, 113, 114, 115, 116),
        // Male arms component ids.
        intArrayOf(26, 27, 28, 29, 30, 31, 105, 106, 107, 108, 109, 110),
        // Male hand component ids.
        intArrayOf(33, 34, 84, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126),
        // Male legs component ids.
        intArrayOf(36, 37, 38, 39, 40, 85, 86, 87, 88, 89, 90),
        // Male feet component ids.
        intArrayOf(42, 43)
    )

    val FEMALE_LOOK_IDS = arrayOf(
        // Female head component ids.
        intArrayOf(45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 145, 146, 269, 270, 271, 272, 273, 274, 275, 276, 277, 278, 279, 280),
        // Female jaw component ids.
        intArrayOf(1000),
        // Female torso component ids.
        intArrayOf(56, 57, 58, 59, 60, 153, 154, 155, 156, 157, 158),
        // Female arms component ids.
        intArrayOf(61, 62, 63, 64, 65, 147, 148, 149, 150, 151, 152),
        // Female hand component ids.
        intArrayOf(67, 68, 127, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168),
        // Female legs component ids.
        intArrayOf(70, 71, 72, 73, 74, 75, 76, 77, 128, 129, 130, 131, 132, 133, 134),
        // Female feet component ids.
        intArrayOf(79, 80)
    )

    // Global appearance components.
    private val HAIR_COLORS = intArrayOf(20, 19, 10, 18, 4, 5, 15, 7, 0, 6, 21, 9, 22, 17, 8, 16, 11, 24, 23, 3, 2, 1, 14, 13, 12)
    private val TORSO_COLORS = intArrayOf(24, 23, 2, 22, 12, 11, 6, 19, 4, 0, 9, 13, 25, 8, 15, 26, 21, 7, 20, 14, 10, 28, 27, 3, 5, 18, 17, 1, 16)
    private val LEG_COLORS = intArrayOf(26, 24, 23, 3, 22, 13, 12, 7, 19, 5, 1, 10, 14, 25, 9, 0, 21, 8, 20, 15, 11, 28, 27, 4, 6, 18, 17, 2, 16)
    private val FEET_COLORS = intArrayOf(0, 1, 2, 3, 4, 5)
    private val SKIN_COLORS = intArrayOf(7, 6, 5, 4, 3, 2, 1, 0)

    // Color map.
    private val COLOR_MAPPINGS = listOf(
        Triple(0, HAIR_COLORS, 100..124),
        Triple(2, TORSO_COLORS, 189..217),
        Triple(5, LEG_COLORS, 248..276),
        Triple(6, FEET_COLORS, 307..312),
        Triple(4, SKIN_COLORS, 151..158)
    )

    const val INTERFACE_ID = 900
    const val CONFIRM = 81
    private val categories = intArrayOf(33, 37, 41, 45, 49, 53, 57, 61)
    // Generated from CS2 2739 and cache enums/struct param 788 (uncovered hair).
    private val maleKits = arrayOf(
        intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 91, 92, 93, 94, 95, 96, 97, 261, 262, 263, 264, 265, 266, 267, 268, 309, 314, 313, 310, 315, 312),
        intArrayOf(14, 11, 12, 13, 10, 15, 16, 17, 98, 99, 100, 101, 102, 103, 104, 305, 306, 307, 308),
        intArrayOf(111, 113, 114, 115, 112, 116, 18, 19, 20, 21, 22, 23, 24, 25),
        intArrayOf(105, 108, 106, 107, 109, 110, 28, 26, 27, 29, 30, 31),
        intArrayOf(34, 33, 84, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126),
        intArrayOf(36, 85, 37, 89, 90, 40, 86, 88, 39, 38, 87),
        intArrayOf(42, 43)
    )
    private val femaleKits = arrayOf(
        intArrayOf(45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 145, 146, 269, 270, 271, 272, 273, 274, 275, 276, 277, 278, 279, 280, 353, 356, 362, 354, 355, 359, 357),
        intArrayOf(),
        intArrayOf(153, 155, 156, 157, 154, 158, 56, 57, 58, 59, 60),
        intArrayOf(147, 150, 148, 149, 151, 152, 64, 61, 63, 65, 62),
        intArrayOf(68, 67, 127, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168),
        intArrayOf(129, 130, 128, 74, 133, 134, 77, 131, 132, 75, 73, 76, 72, 70, 71, 363),
        intArrayOf(79, 80)
    )
    private val palette = arrayOf(
        intArrayOf(7, 0, 1, 2, 3, 4, 5, 6),
        intArrayOf(20, 19, 10, 18, 4, 5, 15, 7, 0, 6, 21, 9, 22, 17, 8, 16, 24, 11, 23, 3, 2, 1, 14, 13, 12),
        intArrayOf(),
        intArrayOf(24, 23, 2, 22, 12, 11, 6, 19, 4, 0, 9, 13, 25, 8, 15, 26, 21, 7, 20, 14, 10, 28, 27, 3, 5, 18, 17, 1, 16),
        intArrayOf(),
        intArrayOf(),
        intArrayOf(24, 23, 3, 22, 13, 12, 7, 19, 5, 1, 10, 14, 25, 9, 0, 26, 21, 8, 20, 15, 11, 28, 27, 4, 6, 18, 17, 2, 16),
        intArrayOf(0, 1, 2, 3, 4, 5)
    )
    private val colorParts = intArrayOf(4, 0, -1, 2, -1, -1, 5, 6)

    @JvmStatic
    fun open(player: Player) {
        if (player.interfaceManager.hasMainComponent(INTERFACE_ID)) return
        player.unlock()
        removeAttribute(player, "char-design:accepted")
        val design = core.game.component.Component(INTERFACE_ID)
        design.definition!!.type = core.game.component.InterfaceType.DEFAULT
        design.definition.isWalkable = false
        val component = player.interfaceManager.openTemporaryRoot(design) ?: return
        component.setUncloseEvent { p, _ -> p.getAttribute("char-design:accepted", false) }
        setAttribute(player, "char-design:category", 0)
        setVarbit(player, 6501, 0)
        for (button in categories + intArrayOf(15, 20, CONFIRM)) {
            sendIfaceSettings(player, 2, button, INTERFACE_ID, -1, -1)
        }
        syncDesign(player)
    }

    private fun syncDesign(player: Player) {
        val parts = player.appearance.appearanceCache
        for (i in parts.indices) setVarc(player, 1008 + i, parts[i].look)
        for ((i, part) in intArrayOf(0, 2, 5, 6, 4).withIndex()) {
            setVarc(player, 1015 + i, parts[part].color)
        }
        val maxKits = maxOf(maleKits.maxOf { it.size }, femaleKits.maxOf { it.size })
        sendIfaceSettings(player, 2, 66, INTERFACE_ID, 0, maxKits * 3 - 1)
        sendIfaceSettings(player, 2, 73, INTERFACE_ID, 0, palette.maxOf { it.size } * 2 - 1)
        refreshAppearance(player)
    }

    @JvmStatic
    fun handleButtons(player: Player, buttonId: Int, slot: Int): Boolean {
        val category = getAttribute(player, "char-design:category", 0)
        val parts = player.appearance.appearanceCache
        when (buttonId) {
            15, 20 -> {
                val gender = if (buttonId == 15) Gender.MALE else Gender.FEMALE
                if (player.appearance.gender != gender) player.appearance.changeGender(gender)
                setAttribute(player, "char-design:category", 0)
                setVarbit(player, 6501, 0)
            }
            in categories -> {
                val next = categories.indexOf(buttonId)
                setAttribute(player, "char-design:category", next)
                setVarbit(player, 6501, next)
            }
            66 -> {
                if (category !in 1..7 || slot < 0 || slot % 3 != 2) return false
                val kits = if (player.appearance.isMale) maleKits else femaleKits
                val look = kits[category - 1].getOrNull(slot / 3) ?: return false
                parts[category - 1].changeLook(look)
            }
            73 -> {
                if (slot < 0 || slot % 2 != 1) return false
                val part = colorParts[category]
                if (part < 0) return false
                val color = palette[category].getOrNull(slot / 2) ?: return false
                parts[part].changeColor(color)
            }
            CONFIRM -> {
                setAttribute(player, "char-design:accepted", true)
                if (!player.interfaceManager.close()) {
                    removeAttribute(player, "char-design:accepted")
                    return false
                }
                refreshAppearance(player)
                return true
            }
            else -> return false
        }
        syncDesign(player)
        return true
    }

    private fun changeGender(player: Player, male: Boolean) {
        player.setAttribute("male", male)
        setVarp(player, 1262, if (male) 1 else 0)
        setVarbit(player, 5008, if (male) 1 else 0)
        setVarbit(player, 5009, if (male) 0 else 1)
        reset(player)
    }

    private fun changeLook(player: Player, index: Int, increment: Boolean) {
        if (index < 2 && !player.getAttribute("first-click:$index", false)) {
            setAttribute(player, "first-click:$index", true)
            return
        }
        val currentIndex = getAttribute(player, "look:$index", 0)
        val appearanceIds = if (getAttribute(player,"male", player.appearance.isMale)) MALE_LOOK_IDS else FEMALE_LOOK_IDS
        val values = appearanceIds[index]
        val nextIndex = when {
            increment && currentIndex + 1 >= values.size -> 0
            !increment && currentIndex - 1 < 0 -> values.size - 1
            increment -> currentIndex + 1
            else -> currentIndex - 1
        }
        setAttribute(player, "look:$index", nextIndex)
        setAttribute(player, "look-val:$index", values[nextIndex])
    }

    private fun changeColor(player: Player, index: Int, array: IntArray, startId: Int, buttonId: Int) {
        val color = array[abs(buttonId - startId)]
        setAttribute(player, "color-val:$index", color)
    }

    private fun reset(player: Player) {
        for (i in player.appearance.appearanceCache.indices) {
            removeAttribute(player, "look:$i")
            removeAttribute(player, "look-val:$i")
            removeAttribute(player, "color-val:$i")
        }
        removeAttribute(player, "first-click:0")
        removeAttribute(player, "first-click:1")
    }

    @JvmStatic
    fun randomize(player: Player, head: Boolean) {
        if (head) {
            changeLook(player, 0, RandomFunction.random(2) == 1)
            changeLook(player, 1, RandomFunction.random(2) == 1)
            changeColor(player, 0, HAIR_COLORS, 100, RandomFunction.random(100, 124))
            changeColor(player, 4, SKIN_COLORS, 158, RandomFunction.random(151, 158))
        } else {
            for (i in player.appearance.appearanceCache.indices) {
                changeLook(player, i, RandomFunction.random(2) == 1)
            }
            listOf(1, 2, 3).forEach {
                val (index, colors, range) = COLOR_MAPPINGS[it]
                changeColor(player, index, colors, range.first, RandomFunction.random(range.first, range.last))
            }
        }
        confirm(player, false)
    }

    private fun confirm(player: Player, close: Boolean) {
        if (close) {
            setAttribute(player, "char-design:accepted", true)
            closeInterface(player)
        }
        player.appearance.gender = if (getAttribute(player, "male", player.appearance.isMale)) Gender.MALE else Gender.FEMALE
        for (i in player.appearance.appearanceCache.indices) {
            val cache = player.appearance.appearanceCache[i]
            cache.changeLook(getAttribute(player,"look-val:$i", cache.look))
            cache.changeColor(getAttribute(player,"color-val:$i", cache.color))
        }
        refreshAppearance(player)
    }
}