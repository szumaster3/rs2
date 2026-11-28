package content.region.fremennik.rellekka.dialogue

import content.data.Quests

import core.api.addItem
import core.api.getQuestStage
import core.api.inInventory
import core.api.removeItem
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.tools.END_DIALOGUE
import core.tools.START_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs

/**
 * Represents the Council Worker dialogue file.
 */
class CouncilWorkerDialogue(val questStage: Int, var isBeerInteraction: Boolean = false, val beerId: Int? = null) :
    DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        if (isBeerInteraction) {
            when (stage) {
                START_DIALOGUE -> {
                    npc(NPCs.VT_COUNCIL_WORKMEN_1287, "Oh, thank you much ${if (player!!.isMale) "sir" else "miss"}.")
                    stage++
                }

                1 -> {
                    npc(NPCs.VT_COUNCIL_WORKMEN_1287, "Ta very much like. That'll hit the spot nicely.. Here,", "You can have this. I picked it up as a souvenir on me", "last holz.")
                    if (beerId != null) {
                        if (removeItem(player!!, beerId)) {
                            addItem(player!!, Items.STRANGE_OBJECT_3713)
                        }
                    } else if (removeItem(player!!, Items.BEER_3803) || removeItem(player!!, Items.BEER_1917)) {
                        addItem(player!!, Items.STRANGE_OBJECT_3713)
                    }
                    stage = END_DIALOGUE
                }
            }
        } else if (questStage in 1..99) {
            when (stage) {
                START_DIALOGUE -> if (getQuestStage(player!!, Quests.THE_FREMENNIK_TRIALS.questName) > 0) {
                    player(FaceAnim.HALF_ASKING, "I know this is an odd question, but are you", "a member of the elder council?")
                    stage = 1
                } else {
                    end()
                }

                1 -> {
                    npc(NPCs.VT_COUNCIL_WORKMEN_1287, "'fraid not, ${if (player!!.isMale) "sir" else "miss"}.")
                    stage++
                }

                2 -> {
                    npc(NPCs.VT_COUNCIL_WORKMEN_1287, "Say, would you do me a favor? I'm quite parched.", "If you bring me a beer, I'll make it worthwhile.")
                    stage++
                }

                3 -> if (inInventory(player!!, Items.BEER_3803) || inInventory(player!!, Items.BEER_1917)) {
                    player("Oh, I have one here! Take it.")
                    stage = 0
                    isBeerInteraction = true
                } else {
                    end()
                }
            }
        }
    }
}