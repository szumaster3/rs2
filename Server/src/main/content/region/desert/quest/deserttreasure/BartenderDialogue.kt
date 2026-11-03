package content.region.desert.quest.deserttreasure

import core.api.*
import core.game.dialogue.*
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.plugin.Initializable
import core.tools.START_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs

/**
 * Represents Bartender dialogue.
 */
@Initializable
class BartenderDialogue(player: Player? = null) : Dialogue(player) {
    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        openDialogue(player!!, BartenderDialogueFile(), npc)
        return false
    }

    override fun newInstance(player: Player?): Dialogue {
        return BartenderDialogue(player)
    }

    override fun getIds(): IntArray {
        return intArrayOf(NPCs.BARTENDER_1921)
    }
}

class BartenderDialogueFile : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        val questStage = getQuestStage(player!!, DesertTreasure.questName)
        val boughtBeer = getAttribute(player!!, DesertTreasure.attributeBoughtBeer, false)

        when (stage) {
            START_DIALOGUE -> {
                when (questStage) {
                    in 0..4 -> {
                        npcl(FaceAnim.ANNOYED, "Get out of here. I have nothing to say to the likes of you.")
                        stage = 1
                    }

                    5 -> {
                        if (boughtBeer) {
                            npcl("You've had your drink, now get out of here. I have nothing to say to the-")
                            stage = 10
                        } else {
                            npcl("If you're not buying anything, I have nothing to say to you.")
                            stage = 20
                        }
                    }

                    in 6..11 -> {
                        if (boughtBeer) {
                            npcl("You've had your drink, now get out of here. I have nothing to say to the-")
                            stage = 30
                        } else {
                            endFile()
                        }
                    }

                    100 -> {
                        npcl("So you're the @g[fella,lass] that freed Azzanadra, huh? Fair play to ya. What will you have?")
                        stage = 1
                    }

                    else -> endFile()
                }
            }

            // Quest stages 0-4 and 100
            1 -> endFile()

            // Quest stage 5 - already bought beer
            10 -> {
                playerl("No, Wait! Look, I'm here on an archaeological expedition for the Museum of Varrock.")
                stage = 11
            }

            11 -> {
                playerl("I am only here looking for artefacts...")
                stage = 12
            }

            12 -> {
                npcl("Oh really? Our inheritance is only sand and death. What do you expect to find out here in this desert forsaken by the gods?")
                stage = 13
            }

            13 -> {
                options(
                    "I heard about treasure...",
                    "I heard about four diamonds...",
                    "I heard about a fortress..."
                )
                stage = 14
            }

            14 -> {
                when (buttonID) {
                    1 -> {
                        playerl("As I understand it there's some kind of hidden treasure in these parts...")
                        stage = 40
                    }

                    2 -> {
                        playerl("I heard a rumour about four diamonds or crystals...")
                        stage = 50
                    }

                    3 -> {
                        playerl("Certain things have led me to believe there may be some kind of ruined fortress around here...")
                        stage = 60
                    }
                }
            }

            // Quest stage 5 - not bought beer
            20 -> {
                options(
                    "Ask about Desert Treasure",
                    "Buy a drink"
                )
                stage = 21
            }

            21 -> {
                when (buttonID) {
                    1 -> {
                        playerl("As I understand it there's some kind of hidden treasure in these parts...")
                        stage = 40
                    }

                    2 -> {
                        if (inInventory(player!!, Items.COINS_995, 650)) {
                            npcl("What's that? You wanna buy a beer? It'll cost ya 650 coins.")
                            stage = 22
                        } else {
                            npcl("You ain't got the 650 coins it costs to buy it, and I'm glad 'cos I didn't want to serve you anyway.")
                            stage = 1
                        }
                    }
                }
            }

            // Buy beer options
            22 -> {
                options(
                    "Buy a beer",
                    "Don't buy anything"
                )
                stage = 23
            }

            23 -> {
                when (buttonID) {
                    1 -> {
                        if (removeItem(player!!, Item(Items.COINS_995, 650))) {
                            addItemOrDrop(player!!, Items.BANDITS_BREW_4627)
                            setAttribute(player!!, DesertTreasure.attributeBoughtBeer, true)
                        }

                        npcl("There you go. Now get out, we don't like your sort around here.")
                        stage = 1
                    }

                    2 -> {
                        npcl("Get out of my bar then! We don't like your sort round here!")
                        stage = 1
                    }
                }
            }

            // Quest stages 6-11 - already bought beer
            30 -> {
                playerl("No, Wait! Look, I'm here on an archaeological expedition for the Museum of Varrock.")
                stage = 31
            }

            31 -> {
                playerl("I am only here looking for artefacts...")
                stage = 32
            }

            32 -> {
                npcl("Oh really? Our inheritance is only sand and death. What do you expect to find out here in this desert forsaken by the gods?")
                stage = 33
            }

            33 -> {
                options(
                    "I heard about treasure...",
                    "I heard about four diamonds...",
                    "I heard about a fortress...",
                    "I heard of the Diamonds of Azzanadra."
                )
                stage = 34
            }

            34 -> {
                when (buttonID) {
                    1 -> {
                        playerl("As I understand it there's some kind of hidden treasure in these parts...")
                        stage = 40
                    }

                    2 -> {
                        playerl("I heard a rumour about four diamonds or crystals...")
                        stage = 70
                    }

                    3 -> {
                        playerl("Certain things have led me to believe there may be some kind of ruined fortress around here...")
                        stage = 60
                    }

                    4 -> {
                        playerl("Tell me all you know about the Diamonds of Azzanadra.")
                        stage = 80
                    }
                }
            }

            // I heard about treasure...
            40 -> {
                npcl("Look around @g[pal,lady]. Does it looks like there's any treasure near here?")
                stage = 41
            }

            41 -> {
                npcl("If I were you I'd get lost before someone takes a dislike to your face and removes it for you.")
                stage = 1
            }

            // I heard about four diamonds... (quest stage 5)
            50 -> {
                npcl("The four diamonds of Azzanadra??? How came you to know of this?")
                stage = 51
            }

            51 -> {
                playerl("You've heard of them then?")
                stage = 52
            }

            52 -> {
                npcl("It's just a fairy tale for children. Maybe one of the village elders might know more, but it's not really something I care about.")
                stage = 53
            }

            53 -> {
                npcl("Now get out of here, your sort isn't welcome in my bar.")
                stage = 54
            }

            54 -> {
                if (getQuestStage(player!!, DesertTreasure.questName) == 5) {
                    setQuestStage(player!!, DesertTreasure.questName, 6)
                }
                endFile()
            }

            // I heard about a fortress...
            60 -> {
                npcl("Doubt it. What in the world would someone need to guard against in the middle of this desert?")
                stage = 61
            }

            61 -> {
                npcl("A bad attack of sand? I think you're on the wrong track, mister so-called archaeologist.")
                stage = 1
            }

            // I heard about four diamonds... (quest stages 6-11)
            70 -> {
                npcl("The four diamonds of Azzanadra??? How came you to know of this?")
                stage = 71
            }

            71 -> {
                playerl("You've heard of them then?")
                stage = 72
            }

            72 -> {
                npcl("It's just a fairy tale for children. Maybe one of the village elders might know more, but it's not really something I care about.")
                stage = 73
            }

            73 -> {
                npcl("Now get out of here, your sort isn't welcome in my bar.")
                stage = 1
            }

            // I heard of the Diamonds of Azzanadra.
            80 -> {
                npcl("Not that I think it's any of your business, but when I was a child I remember hearing the legend.")
                stage = 81
            }

            81 -> {
                npcl("I don't recall it particularly well, other than they are said to contain an incredible power.")
                stage = 82
            }

            82 -> {
                npcl("If you really want to hear more about it you'd be best off finding someone who cares about the past, and the history of this area, and stop bothering me.")
                stage = 1
            }
        }
    }
}
