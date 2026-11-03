
package content.region.asgarnia.burthorpe.quest.hero.dialogue

import core.api.*
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.tools.START_DIALOGUE
import shared.consts.Items
import shared.consts.Quests

/**
 * Represents the Katrine dialogue file.
 *
 * # Relations
 * - [Heroes Quest][content.region.asgarnia.burthorpe.quest.hero.HeroesQuest]
 */
class KatrineDialogueFile : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            START_DIALOGUE -> {
                when (getQuestStage(player!!, Quests.HEROES_QUEST)) {
                    1 -> {
                        player("Hey.")
                        stage = 1
                    }

                    2, 3, 4 -> {
                        playerl(FaceAnim.ASKING,"What am I supposed to be doing again?")
                        stage = 10
                    }

                    5 -> {
                        if (inInventory(player!!, Items.PETES_CANDLESTICK_1577)) {
                            options(
                                "Who are all those people in there?",
                                "I have a candlestick now."
                            )
                            stage = 20
                        } else {
                            playerl(FaceAnim.ASKING,"What am I supposed to be doing again?")
                            stage = 10
                        }
                    }

                    6 -> {
                        playerl("I have lost my master thief's armband...")
                        stage = 40
                    }
                }
            }

            // Quest stage 1.
            1 -> {
                npcl("Hey.")
                stage = 2
            }

            2 -> {
                options(
                    "Who are all those people in there?",
                    "Is there any way I can get the rank of master thief?"
                )
                stage = 3
            }

            3 -> {
                when (buttonID) {
                    1 -> {
                        playerl(FaceAnim.ASKING,"Who are all those people in there?")
                        stage = 4
                    }

                    2 -> {
                        playerl(FaceAnim.ASKING,"Is there any way I can get the rank of master thief?")
                        stage = 5
                    }
                }
            }

            4 -> {
                npcl("They're just various rogues and thieves.")
                stage = 6
            }

            6 -> {
                playerl("They don't say a lot...")
                stage = 7
            }

            7 -> {
                npc("Nope.")
                stage = 100
            }

            5 -> {
                npcl("Master thief? Ain't we the ambitious one!")
                stage = 8
            }

            8 -> {
                npcl("Well, you're gonna have to do something pretty amazing.")
                stage = 9
            }

            9 -> {
                playerl(FaceAnim.ASKING,"Anything you can suggest?")
                stage = 11
            }

            11 -> {
                npcl("Well, some of the MOST coveted prizes in thiefdom right now are in the pirate town of Brimhaven on Karamja.")
                stage = 12
            }

            12 -> {
                npcl("The pirate leader Scarface Pete has a pair of extremely valuable candlesticks.")
                stage = 13
            }

            13 -> {
                npc("His security is VERY good.")
                stage = 14
            }

            14 -> {
                npcl("We, of course, have gang members in a town like Brimhaven who may be able to help you.")
                stage = 15
            }

            15 -> {
                npcl("Visit our hideout in the alleyway on palm street.")
                stage = 16
            }

            16 -> {
                npcl("To get in you will need to tell them the secret password 'four leaved clover'.")
                stage = 17
            }

            17 -> {
                if (getQuestStage(player!!, Quests.HEROES_QUEST) == 1) {
                    setQuestStage(player!!, Quests.HEROES_QUEST, 2)
                }
                endFile()
            }

            // Quest stages 2, 3 and 4.
            10 -> {
                npcl("You told me you wanted to get the rank of master thief! Now pay attention.")
                stage = 18
            }

            18 -> {
                npcl(
                    "Some of the MOST coveted prizes in thiefdom right now are in the pirate town of Brimhaven on Karamja."
                )
                stage = 19
            }

            19 -> {
                npcl("The pirate leader Scarface Pete has a pair of extremely valuable candlesticks.")
                stage = 21
            }

            21 -> {
                npcl("His security is VERY good.")
                stage = 22
            }

            22 -> {
                npcl("We, of course, have gang members in a town like Brimhaven who may be able to help you.")
                stage = 23
            }

            23 -> {
                npcl("Visit our hideout in the alleyway on palm street.")
                stage = 24
            }

            24 -> {
                npcl("To get in you will need to tell them the secret password 'four leaved clover'.")
                stage = 100
            }

            // Quest stage 5, candlestick obtained.
            20 -> {
                when (buttonID) {
                    1 -> {
                        playerl(FaceAnim.ASKING,"Who are all those people in there?")
                        stage = 25
                    }

                    2 -> {
                        player("I have a candlestick now!")
                        stage = 30
                    }
                }
            }

            25 -> {
                npcl("They're just various rogues and thieves.")
                stage = 26
            }

            26 -> {
                playerl("They don't say a lot...")
                stage = 27
            }

            27 -> {
                npcl("Nope.")
                stage = 100
            }

            30 -> {
                npcl("Wow... is... it REALLY it?")
                stage = 31
            }

            31 -> {
                npcl("This really is a FINE bit of thievery.")
                stage = 32
            }

            32 -> {
                npcl("Us thieves have been trying to get hold of this one for a while!")
                stage = 33
            }

            33 -> {
                npc(
                    "You wanted to be ranked as a master thief didn't you?",
                    "Well, I guess this just about ranks as good enough!"
                )
                stage = 34
            }

            34 -> {
                dialogue("Katrine gives you a master thief armband.")
                stage = 35
            }

            35 -> {
                if (removeItem(player!!, Items.PETES_CANDLESTICK_1577)) {
                    addItemOrDrop(player!!, Items.THIEVES_ARMBAND_1579)

                    if (getQuestStage(player!!, Quests.HEROES_QUEST) == 5) {
                        setQuestStage(player!!, Quests.HEROES_QUEST, 6)
                    }
                }
                endFile()
            }

            // Quest stage 5, candlestick not obtained.
            // This follows the original branch without an inventory item.
            // The dialogue is handled by stages 10, 18-24.

            // Quest stage 6.
            40 -> {
                npcl("Lucky I 'ave a spare ain't it? Don't lose it again.")
                stage = 41
            }

            41 -> {
                addItemOrDrop(player!!, Items.THIEVES_ARMBAND_1579)
                endFile()
            }

            100 -> endFile()
        }
    }
}