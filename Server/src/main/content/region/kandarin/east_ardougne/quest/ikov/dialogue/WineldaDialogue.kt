package content.region.kandarin.east_ardougne.quest.ikov.dialogue

import content.data.GameAttributes
import core.api.*
import core.game.dialogue.*
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.game.world.map.Location
import core.plugin.Initializable
import core.tools.START_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Quests

/**
 * Represents Winelda dialogue.
 */
@Initializable
class WineldaDialogue(player: Player? = null) : Dialogue(player) {

    override fun newInstance(player: Player?): Dialogue = WineldaDialogue(player)

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        openDialogue(player, WineldaDialogueFile(), npc)
        return false
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.WINELDA_276)
}

class WineldaDialogueFile : DialogueFile() {
    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            START_DIALOGUE -> {
                stage = when {
                    getQuestStage(player!!, Quests.TEMPLE_OF_IKOV) in 5..7 ||
                            getQuestStage(player!!, Quests.TEMPLE_OF_IKOV) == 100 -> 1

                    getAttribute(player!!, GameAttributes.QUEST_IKOV_WINELDA_INTER, false) -> 10

                    else -> 100
                }
            }

            // Returning to Winelda after the previous interaction.
            1 -> {
                playerl(FaceAnim.FRIENDLY, "Hi again. Could you do the honours again please?")
                stage = 2
            }

            2 -> {
                npcl(FaceAnim.FRIENDLY, "Certainly! We helps those that helps poor Winelda!")
                stage = 3
            }

            3 -> {
                teleport(player!!, Location(2664, 9876, 0))
                endFile()
            }

            // Winelda is waiting for the limpwurt roots.
            10 -> {
                npcl("Have you got roots for poor Winelda?")
                stage = 11
            }

            11 -> {
                stage = when {
                    inInventory(player!!, Items.LIMPWURT_ROOT_225) && amountInInventory(player!!, Items.LIMPWURT_ROOT_225) >= 20 -> 12
                    inInventory(player!!, Items.LIMPWURT_ROOT_225) -> 20
                    else -> 30
                }
            }

            12 -> {
                playerl(FaceAnim.FRIENDLY, "Yes, I've got them.")
                stage = 13
            }

            13 -> {
                removeItem(player!!, Item(Items.LIMPWURT_ROOT_225, 20))
                stage = 14
            }

            14 -> {
                sendItemDialogue(player!!,Items.LIMPWURT_ROOT_225, "You give Winelda the limpwurt roots.")
                stage = 15
            }

            15 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "Good! Good! My potion is nearly ready! Bubble, bubble, toil and trouble!"
                )
                stage = 16
            }

            16 -> {
                npcl(FaceAnim.FRIENDLY, "Now we shows them ours magic! Hold on tight!")
                stage = 17
            }

            17 -> {
                if (getQuestStage(player!!, Quests.TEMPLE_OF_IKOV) == 4) {
                    setQuestStage(player!!, Quests.TEMPLE_OF_IKOV, 5)
                }
                teleport(player!!, Location(2664, 9876, 0))
                endFile()
            }

            20 -> {
                playerl(FaceAnim.FRIENDLY, "I've got some limpwurt roots!")
                stage = 21
            }

            21 -> {
                npcl(FaceAnim.FRIENDLY, "We needs 20 rooteses!")
                endFile()
            }

            30 -> {
                playerl(FaceAnim.FRIENDLY, "How many did you need again?")
                stage = 31
            }

            31 -> {
                npcl(FaceAnim.FRIENDLY, "We needs 20 Limpwurt roots for pot.")
                endFile()
            }

            // Initial interaction.
            100 -> {
                npcl(FaceAnim.FRIENDLY, "Hehe! We see you're in a pickle!")
                stage = 101
            }

            101 -> {
                npcl("Wants to be getting over the nasty lava do we?")
                stage = 102
            }

            102 -> {
                options()
                stage = 103
            }

            103 -> when (buttonID) {
                1 -> stage = 110
                2 -> stage = 120
                3 -> stage = 130
            }

            // Nah, not bothered!
            110 -> {
                playerl(FaceAnim.FRIENDLY, "Nah, not bothered!")
                stage = 111
            }

            111 -> {
                npcl(FaceAnim.FRIENDLY, "Hehe! Ye'll come back! They always come back!")
                endFile()
            }

            // Yes we do!
            120 -> {
                playerl(FaceAnim.FRIENDLY, "Yes we do!")
                stage = 140
            }

            // Yes I do!
            130 -> {
                playerl(FaceAnim.FRIENDLY, "Yes I do!")
                stage = 140
            }

            140 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "Mocking us are we? Clever one aren't we?"
                )
                stage = 141
            }

            141 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "I'm knowing some magic trickesses! I could get over easy as that!"
                )
                stage = 142
            }

            142 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "Don't tell them! They always come! They pester poor Winelda!"
                )
                stage = 143
            }

            143 -> {
                playerl(FaceAnim.FRIENDLY, "If you're such a great witch, get me over!")
                stage = 144
            }

            144 -> {
                npcl(FaceAnim.FRIENDLY, "See! They pester Winelda!")
                stage = 145
            }

            145 -> {
                playerl(FaceAnim.FRIENDLY, "I can do something for you!")
                stage = 146
            }

            146 -> {
                npcl(FaceAnim.FRIENDLY, "Good! Don't pester! Help!")
                stage = 147
            }

            147 -> {
                npcl(FaceAnim.FRIENDLY, "Get Winelda 20 limpwurt roots for my pot.")
                stage = 148
            }

            148 -> {
                npcl(FaceAnim.FRIENDLY, "Then we shows them some magic!")
                stage = 149
            }

            149 -> {
                setAttribute(player!!, GameAttributes.QUEST_IKOV_WINELDA_INTER, true)
                endFile()
            }
        }
    }
}