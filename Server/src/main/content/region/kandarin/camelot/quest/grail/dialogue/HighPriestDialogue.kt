package content.region.kandarin.camelot.quest.grail.dialogue

import content.global.activity.wom_tasks.WomDeliveryListener.Companion.handleNpcDelivery
import core.api.openDialogue
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.game.dialogue.Topic
import core.game.node.entity.npc.NPC
import shared.consts.NPCs

/**
 * Represents the High Priest dialogue.
 *
 * # Relations
 * - [Holy Grail][content.region.kandarin.camelot.quest.grail.HolyGrail]
 */
class HighPriestDialogue : DialogueFile() {
    override fun handle(componentID: Int, buttonID: Int) {
        npc = NPC(NPCs.HIGH_PRIEST_OF_ENTRANA_216)

        when (stage) {
            0 -> npcl(FaceAnim.NEUTRAL, "Many greetings. Welcome to our fair island.").also { stage++ }
            1 -> showTopics(
                Topic("Talk about the Holy Grail.", 2, true),
                Topic("Talk about something else...", 6, true)
            )
            2 -> playerl(FaceAnim.NEUTRAL, "Hello, I am in search of the Holy Grail.").also { stage++ }
            3 -> npcl(FaceAnim.NEUTRAL, "The object of which you speak did once pass through holy Entrana. I know not where it is now however.").also { stage++ }
            4 -> npcl(FaceAnim.NEUTRAL, "Nor do I really care.").also { stage++ }
            5 -> {
                openDialogue(player!!, CroneDialogue(true), NPCs.GRAIL_CRONE_217)
            }
            6 -> {
                end()
                handleNpcDelivery(player!!, npc!!)
            }
        }
    }
}
