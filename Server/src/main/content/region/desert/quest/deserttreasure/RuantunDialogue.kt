package content.region.desert.quest.deserttreasure

import core.api.*
import core.game.dialogue.*
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.START_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs

/**
 * Represents Ruantun dialogue.
 */
@Initializable
class RuantunDialogue(player: Player? = null) : Dialogue(player) {
    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        openDialogue(player!!, RuantunDialogueFile(), npc)
        return false
    }

    override fun newInstance(player: Player?): Dialogue {
        return RuantunDialogue(player)
    }

    override fun getIds(): IntArray {
        return intArrayOf(NPCs.RUANTUN_1916)
    }
}

class RuantunDialogueFile : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        when (getQuestStage(player!!, DesertTreasure.questName)) {
            in 0..8 -> {
                when (stage) {
                    START_DIALOGUE -> {
                        playerl("Hello.")
                        stage = 1
                    }

                    1 -> {
                        npcl(FaceAnim.OLD_NORMAL, "You ssshould not be down here...")
                        stage = 2
                    }

                    2 -> {
                        options(
                            "Who are you?",
                            "Why are you down here?",
                            "Can I use your anvil?"
                        )
                        stage = 3
                    }

                    3 -> {
                        when (buttonID) {
                            1 -> {
                                playerl("Who are you?")
                                stage = 10
                            }

                            2 -> {
                                playerl("Why are you down here?")
                                stage = 20
                            }

                            3 -> {
                                playerl("Can I use your anvil?")
                                stage = 30
                            }
                        }
                    }

                    10 -> {
                        npcl(
                            FaceAnim.OLD_NORMAL,
                            "My name isss unimportant... I live only to ssserve my massster."
                        )
                        stage = 11
                    }

                    11 -> {
                        playerl("Um... Okay then.")
                        endFile()
                    }

                    20 -> {
                        npcl(
                            FaceAnim.OLD_NORMAL,
                            "Thisss isss where I belong... Beingsss sssuch as myssself cannot abide in the light... It is in the darknesss where we find our homesss..."
                        )
                        stage = 21
                    }

                    21 -> {
                        playerl("Uh... Okay then.")
                        endFile()
                    }

                    30 -> {
                        npcl(
                            FaceAnim.OLD_NORMAL,
                            "Of courssse you may... I have very little ussse for it nowadaysss..."
                        )
                        stage = 31
                    }

                    31 -> {
                        playerl("Uh... Thanks, I guess.")
                        endFile()
                    }
                }
            }

            in 9..10, 100 -> {
                when (stage) {
                    START_DIALOGUE -> {
                        playerl("Hello.")
                        stage = 1
                    }

                    1 -> {
                        npcl(FaceAnim.OLD_NORMAL, "You sshould not be down here...")
                        stage = 2
                    }

                    2 -> {
                        playerl("Are you an assistant to Count Draynor?")
                        stage = 3
                    }

                    3 -> {
                        npc(
                            FaceAnim.OLD_NORMAL,
                            "I usssed to have that honour...",
                            "Why do you ssseek me?"
                        )
                        stage = 4
                    }

                    4 -> {
                        if (inInventory(player!!, Items.SILVER_BAR_2355)) {
                            playerl("I have a silver bar with me, I was wondering if you could make it into a 'sacrificial offering pot' for me?")
                            stage = 11
                        } else {
                            playerl("I understand that you can make me a 'sacrificial offering pot' if I bring you a bar of silver?")
                            stage = 21
                        }
                    }

                    11 -> {
                        if (removeItem(player!!, Items.SILVER_BAR_2355)) {
                            addItemOrDrop(player!!, Items.SILVER_POT_4658)
                        }
                        stage = 12
                    }

                    12 -> {
                        npc(
                            FaceAnim.OLD_NORMAL,
                            "Yesss, of courssse...",
                            "There you are, put it to good ussse..."
                        )
                        endFile()
                    }

                    21 -> {
                        npcl(FaceAnim.OLD_NORMAL, "And where did you hear thisss?")
                        stage = 22
                    }

                    22 -> {
                        playerl("It was from Malak in Canifis.")
                        stage = 23
                    }

                    23 -> {
                        npc(
                            FaceAnim.OLD_NORMAL,
                            "Ah, I sssee...",
                            "Yesss, I know how to make sssuch an item...",
                            "It has been many yearsss sssince I have needed to however..."
                        )
                        stage = 24
                    }

                    24 -> {
                        npcl(
                            FaceAnim.OLD_NORMAL,
                            "It is not my wisssh to quessstion your desssire for sssuch an item, I wasss merely sssurprisssed that one sssuch as you would make sssuch a requessst..."
                        )
                        stage = 25
                    }

                    25 -> {
                        npcl(
                            FaceAnim.OLD_NORMAL,
                            "I will happily make you thisss pot, but you mussst bring me a bar of sssilver... Alasss, I can no longer collect my own ingredientsss, and mussst remain here..."
                        )
                        endFile()
                    }
                }
            }
        }
    }
}