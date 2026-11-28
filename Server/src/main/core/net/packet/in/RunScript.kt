package core.net.packet.`in`

import core.api.removeAttribute
import core.api.sendDialogue
import core.game.dialogue.InputType
import core.game.node.entity.player.Player
import core.net.packet.ClientProt

/** Responses to the meslayer input scripts (108, 109 and 110).
 * CS2 meslayer_onkey expands K/M before RESUME_P_COUNTDIALOG is sent.
 * Name and string replies are distinct packets even though both carry a string.
 */
object RunScript {
    fun processInput(player: Player, packet: Packet.InputPromptResponse, script: (Any) -> Unit) {
        val type = player.getAttribute("input-type", InputType.NUMERIC)
        val expected = when (type) {
            InputType.NUMERIC, InputType.AMOUNT -> ClientProt.RESUME_P_COUNTDIALOG
            InputType.STRING_SHORT -> ClientProt.RESUME_P_NAMEDIALOG
            InputType.STRING_LONG -> ClientProt.RESUME_P_STRINGDIALOG
            // meslayer_mode6 sends MESSAGE_PRIVATE, not a resume-dialog packet.
            InputType.MESSAGE -> return
        }
        if (packet.wireOpcode != expected) return
        // Consume this prompt before invoking content. The callback may open another
        // prompt; clearing attributes afterwards would delete that new callback.
        removeAttribute(player, "runscript")
        removeAttribute(player, "input-type")
        removeAttribute(player, "parseamount")
        val input = packet.response
        when (type) {
            InputType.NUMERIC, InputType.AMOUNT -> if (input !is Int || input <= 0) return
            InputType.STRING_SHORT, InputType.STRING_LONG -> if (input !is String || input.isEmpty()) return
            InputType.MESSAGE -> return
        }
        try {
            script(input)
        } catch (_: NumberFormatException) {
            sendDialogue(player, "That number's a bit large, don't you think?")
        } catch (_: ClassCastException) {
            sendDialogue(player, "Something went wrong here. Try again.")
        }
    }
}
