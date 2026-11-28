package content.region.misthalin.dig_site.dialogue

import content.region.misthalin.varrock.museum.plugin.MuseumPlugin
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.tools.END_DIALOGUE
import shared.consts.NPCs

/**
 * Represents Gate guard dialogue.
 */
class GateGuardDialogue : DialogueFile() {
    override fun handle(componentID: Int, buttonID: Int) {
        npc = NPC(NPCs.VM_MUSEUM_GUARD_5941)
        when (stage) {
            0 -> if (npc!!.id == NPCs.VM_MUSEUM_GUARD2_5942) {
                npc(FaceAnim.FRIENDLY, "Hello there! Sorry, I can't stop to talk. I'm guarding this", "workman's gate. I'm afraid you can't come through here -", "you'll need to find another way around.").also { stage = END_DIALOGUE }
            } else {
                npc(FaceAnim.HALF_GUILTY, "Welcome! Would you like to go into the Dig Site", "archaeology cleaning area?").also { stage++ }
            }
            1 -> options("Yes, I'll go in!", "No thanks, I'll take a look around out there.").also { stage++ }
            2 -> when (buttonID) {
                1 -> playerl(FaceAnim.HALF_GUILTY, "Yes, I'll go in!").also { stage++ }
                2 -> playerl(FaceAnim.HALF_GUILTY, "No thanks, I'll take a look around out there.").also { stage = END_DIALOGUE }
            }
            3 -> {
                end()
                MuseumPlugin.openMuseumGate(player!!)
            }
        }
    }
}
