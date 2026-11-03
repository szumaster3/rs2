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
 * Represents Lucien dialogue.
 */
@Initializable
class LucienDialogue(player: Player? = null) : Dialogue(player) {

    override fun newInstance(player: Player?): Dialogue = LucienDialogue(player)

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        openDialogue(player, LucienDialogueFile(), npc)
        return false
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.LUCIEN_273)
}

class LucienDialogueFile : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            START_DIALOGUE -> {
                stage = when (getQuestStage(player!!, Quests.TEMPLE_OF_IKOV)) {
                    100 -> 100
                    in 1..7 -> 200
                    0 -> 1
                    else -> {
                        endFile()
                        return
                    }
                }
            }

            // Quest completed
            100 -> {
                sendMessage(player!!, "You have completed the Temple of Ikov quest.")
                endFile()
            }

            // Quest stages 1-7
            200 -> {
                npcl("I told you not to meet me here again!")
                stage = 201
            }

            201 -> {
                if (inInventory(player!!, Items.PENDANT_OF_LUCIEN_86)) {
                    playerl("Sorry! Can you remind me of my mission?")
                    stage = 203
                } else {
                    playerl("I've lost the pendant you gave me.")
                    stage = 211
                }
            }

            203 -> {
                npcl("My patience grows thin hero!")
                stage = 204
            }

            204 -> {
                npcl(
                    "I need the Staff of Armadyl. It's in the Temple of Ikov, near Hemenster, north east of here."
                )
                stage = 205
            }

            205 -> {
                playerl("I'm up for it!")
                endFile()
            }

            211 -> {
                npcl("Imbecile!")
                stage = 212
            }

            212 -> {
                addItemOrDrop(player!!, Items.PENDANT_OF_LUCIEN_86)
                stage = 213
            }

            213 -> {
                sendItemDialogue(player!!,
                    Items.PENDANT_OF_LUCIEN_86,
                    "Lucien has given you another pendant!"
                )
                endFile()
            }

            // Initial quest dialogue
            1 -> {
                npcl("I seek a hero to go on an important mission!")
                stage = 2
            }

            2 -> {
                options(
                    "I'm a mighty hero!",
                    "Yep, lots of heros about these days."
                )
                stage = 3
            }

            3 -> {
                when (buttonID) {
                    1 -> {
                        playerl(FaceAnim.ANGRY, "I'm a mighty hero!")
                        stage = 4
                    }

                    2 -> {
                        playerl("Yep, lots of heros about these days.")
                        stage = 5
                    }
                }
            }

            4 -> {
                npcl(
                    "I require the Staff of Armadyl. It is in the deserted Temple of Ikov, near Hemenster, north east of here."
                )
                stage = 6
            }

            5 -> {
                npcl("Well, if you see any be sure to point them in my direction.")
                endFile()
            }

            6 -> {
                npcl("Take care hero! There is a dangerous monster somewhere in the temple!")
                stage = 7
            }

            // Main mission options
            7 -> {
                options(
                    "Why can't you get it yourself?",
                    "That sounds like a laugh!",
                    "Oh no! Sounds far too dangerous!",
                    "What's the reward?!"
                )
                stage = 8
            }

            8 -> {
                when (buttonID) {
                    // Why can't you get it yourself?
                    1 -> {
                        playerl("Why can't you get it yourself?")
                        stage = 10
                    }

                    // That sounds like a laugh!
                    2 -> {
                        playerl(FaceAnim.FRIENDLY, "That sounds like a laugh!")
                        stage = 20
                    }

                    // Oh no! Sounds far too dangerous!
                    3 -> {
                        playerl("Oh no! Sounds far too dangerous!")
                        stage = 30
                    }

                    // What's the reward?
                    4 -> {
                        playerl("What's the reward?!")
                        stage = 40
                    }
                }
            }

            // Why can't you get it yourself?
            10 -> {
                npcl("The guardians of the Staff of Armadyl fear me!")
                stage = 11
            }

            11 -> {
                npcl("They have set up a magical barrier which even my power cannot overcome!")
                stage = 7
            }

            // That sounds like a laugh!
            20 -> {
                npcl(
                    "It's not as easy as it sounds. The monster can only be killed with a weapon of ice. There are many other dangers."
                )
                stage = 21
            }

            21 -> {
                playerl(FaceAnim.FRIENDLY, "I'm up for it!")
                stage = 22
            }

            22 -> {
                npcl("Take this pendant. Without it you will not be enter the Chamber of Fear.")
                stage = 23
            }

            23 -> {
                addItemOrDrop(player!!, Items.PENDANT_OF_LUCIEN_86)
                stage = 24
            }

            24 -> {
                sendItemDialogue(player!!,
                    Items.PENDANT_OF_LUCIEN_86,
                    "Lucien has given you a pendant!"
                )
                stage = 25
            }

            25 -> {
                npcl("I cannot stay here much longer. ")
                stage = 26
            }

            26 -> {
                npcl("I will be west of the Grand Exchange in Varrock. I have a small holding up there.")
                stage = 27
            }

            27 -> {
                if (getQuestStage(player!!, Quests.TEMPLE_OF_IKOV) == 0) {
                    setQuestStage(player!!, Quests.TEMPLE_OF_IKOV, 1)
                }
                endFile()
            }

            // Too dangerous
            30 -> {
                npcl("Wimp! Call yourself a hero?! My daughter is more a hero than you!")
                endFile()
            }

            // What's the reward?
            40 -> {
                npcl("I see you are the mercenary type.")
                stage = 41
            }

            41 -> {
                playerl("It's a living.")
                stage = 42
            }

            42 -> {
                npcl("I will reward you well if you bring me the staff.")
                stage = 7
            }
        }
    }
}