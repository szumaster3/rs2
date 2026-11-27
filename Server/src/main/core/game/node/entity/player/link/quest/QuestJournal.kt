package core.game.node.entity.player.link.quest

import core.api.sendString
import core.api.setComponentVisibility
import core.game.node.entity.player.Player
import shared.consts.Components

/**
 * Native journal rows, with compatibility for legacy quest line numbers.
 */
object QuestJournal {
    const val INTERFACE = Components.QUESTJOURNAL_SCROLL_275
    const val FIRST_ROW = 16
    const val ROW_COUNT = 300
    private const val LEGACY_FIRST = 11
    private const val USED = "quest-journal:used-rows"
    private const val TOP = "quest-journal:start-at-top"

    fun clear(player: Player, title: String) {
        player.setAttribute(USED, 0)
        player.setAttribute(TOP, true)
        sendString(player, title, INTERFACE, 2)
        setComponentVisibility(player, INTERFACE, 11, true)
        setComponentVisibility(player, INTERFACE, 316, true)
        for (row in 0 until ROW_COUNT) sendString(player, "", INTERFACE, FIRST_ROW + row)
    }

    fun writeLegacy(player: Player, line: Int, text: String) {
        writeRow(player, line - LEGACY_FIRST, text)
    }

    fun writeRow(player: Player, row: Int, text: String) {
        if (row !in 0 until ROW_COUNT) return
        sendString(player, text, INTERFACE, FIRST_ROW + row)
        player.setAttribute(USED, maxOf(player.getAttribute(USED, 0), row + 1))
    }

    fun setScrollPreference(player: Player, startAtTop: Boolean) {
        player.setAttribute(TOP, startAtTop)
    }

    fun finish(player: Player) {
        resize(player, player.getAttribute(USED, 0), player.getAttribute(TOP, true))
    }

    fun resize(player: Player, rows: Int, startAtTop: Boolean) {
        // Packet 87 reads arguments in reverse order: CS2 receives (rows, resetToTop).
        player.packetDispatch.sendRunScript(1207, "ii", if (startAtTop) 1 else 0, rows.coerceIn(0, ROW_COUNT))
    }
}
