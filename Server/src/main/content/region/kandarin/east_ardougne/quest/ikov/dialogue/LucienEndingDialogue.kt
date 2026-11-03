package content.region.kandarin.east_ardougne.quest.ikov.dialogue

import core.api.*
import core.game.dialogue.*
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.START_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Quests

/**
 * Represents Lucien ending dialogue.
 */
@Initializable
class LucienEndingDialogue(player: Player? = null) : Dialogue(player) {

    override fun newInstance(player: Player?): Dialogue = LucienEndingDialogue(player)

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        openDialogue(player, LucienEndingDialogueFile(), npc)
        return false
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.LUCIEN_272)
}

class LucienEndingDialogueFile : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            START_DIALOGUE -> {
                stage = when (getQuestStage(player!!, Quests.TEMPLE_OF_IKOV)) {
                    100 -> 100
                    in 1..7 -> 1
                    else -> 200
                }
            }

            // Quest completed
            100 -> {
                sendMessage(player!!, "You have completed the Temple of Ikov quest.")
                endFile()
            }

            // Ask for the Staff of Armadyl
            1 -> {
                npcl(FaceAnim.FRIENDLY, "Have you got the Staff of Armadyl yet?")
                stage = 2
            }

            2 -> {
                if (inInventory(player!!, Items.STAFF_OF_ARMADYL_84)) {
                    stage = 10
                } else {
                    stage = 20
                }
            }

            // Player has the staff
            10 -> {
                options("Yes! Here it is.", "No, not yet.")
                stage = 11
            }

            11 -> {
                when (buttonID) {
                    1 -> {
                        removeItem(player!!, Items.STAFF_OF_ARMADYL_84)
                        stage = 12
                    }

                    2 -> {
                        endFile()
                    }
                }
            }

            12 -> {
                sendItemDialogue(player!!,
                    Items.STAFF_OF_ARMADYL_84,
                    "You give Lucien the Staff of Armadyl."
                )
                stage = 13
            }

            13 -> {
                npcl(FaceAnim.FRIENDLY, "Muhahahhahahaha!")
                stage = 14
            }

            14 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "I can feel the power of the staff running through me! I will be more powerful and they shall bow down to me!"
                )
                stage = 15
            }

            15 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "I suppose you want your reward? I shall grant you much power!"
                )
                stage = 16
            }

            16 -> {
                if (getQuestStage(player!!, Quests.TEMPLE_OF_IKOV) == 6) {
                    finishQuest(player!!, Quests.TEMPLE_OF_IKOV)
                }
                endFile()
            }

            // Player does not have the staff
            20 -> {
                playerl(FaceAnim.FRIENDLY, "No, not yet.")
                endFile()
            }

            // Other quest stages
            200 -> {
                npcl("Not here. Meet me at the Flying Horse Inn in East Ardougne.")
                endFile()
            }
        }
    }
}
