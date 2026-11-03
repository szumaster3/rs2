package content.region.desert.quest.deserttreasure

import core.api.*
import core.game.dialogue.Dialogue
import core.game.dialogue.DialogueFile
import core.game.node.entity.combat.ImpactHandler.HitsplatType
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.START_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs

/**
 * Represents Malak dialogue.
 */
@Initializable
class MalakDialogue(player: Player? = null) : Dialogue(player) {
    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        openDialogue(player!!, MalakDialogueFile(), npc)
        return false
    }

    override fun newInstance(player: Player?): Dialogue {
        return MalakDialogue(player)
    }

    override fun getIds(): IntArray {
        return intArrayOf(NPCs.MALAK_1920)
    }
}

class MalakDialogueFile : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        when (getQuestStage(player!!, DesertTreasure.questName)) {
            0, 1, 2, 3, 4, 5, 6, 7, 8 -> {
                when (stage) {
                    START_DIALOGUE -> {
                        npc("Away from me, dog.", "I have business to discuss with the barkeeper.")
                        endFile()
                    }
                }
            }

            9 -> {
                val bloodStage = DesertTreasure.getSubStage(
                    player!!,
                    DesertTreasure.attributeBloodStage
                )

                when (if (bloodStage == 1) 0 else bloodStage) {
                    0 -> {
                        when (stage) {
                            START_DIALOGUE -> {
                                npcl("A human, eh? Give me one good reason why I should not just take you to Lord Drakan now.")
                                stage++
                            }

                            1 -> {
                                npcl("You had better make it a good one too, or you will not survive the night, I'll wager.")
                                stage++
                            }

                            2 -> {
                                options(
                                    "I am here to worship Zamorak",
                                    "I am here to praise Lord Drakan",
                                    "I am here to worship you, oh mighty Malak",
                                    "I am here to kill Lord Drakan",
                                    "I am looking for a special Diamond..."
                                )
                                stage++
                            }

                            3 -> {
                                when (buttonID) {
                                    1 -> {
                                        playerl("I am here to worship the almighty Zamorak! Yay! Go Zamorak!")
                                        stage = 10
                                    }

                                    2 -> {
                                        playerl("I am here only to serve the mighty Drakan. Yup, Drakan, he's the man.")
                                        stage = 11
                                    }

                                    3 -> {
                                        playerl("I am here only to serve the mighty Drakan.")
                                        stage = 12
                                    }

                                    4 -> {
                                        playerl("I am here to kill Lord Drakan, and every stinking one of his vampyre brood!")
                                        stage = 13
                                    }

                                    5 -> {
                                        playerl("I am here looking for a special diamond... I have reason to believe it is somewhere in this vicinity, and it is probably in the possession of a warrior of Zamorak.")
                                        stage = 20
                                    }
                                }
                            }

                            10 -> {
                                npcl("I see. You are a moron. You have probably settled right in with the rest of the idiots in this pathetic excuse for a village.")
                                stage++
                            }

                            11 -> {
                                npcl("I see. I would perhaps be more inclined to believe you if I could not smell the death blood of his brother Draynor upon you.")
                                stage++
                            }

                            12 -> {
                                playerl("I came here looking for you, oh mighty Malak, so that I might serve your glory.")
                                stage++
                            }

                            13 -> {
                                npcl("Hah! Most entertaining, human!")
                                stage++
                            }

                            14 -> {
                                npcl("Unfortunately for you, that is not a good enough reason to explain your presence here.")
                                stage = 30
                            }

                            15 -> {
                                npcl("What are you real intentions here?")
                                stage = 30
                            }

                            16 -> {
                                npcl("Please. Do not think that I am so vain and foolish as to allow you to avoid my question with such obvious sycophancy.")
                                stage++
                            }

                            17 -> {
                                npcl("Now tell me the reason behind your being here, or I will ensure that you suffer.")
                                stage = 30
                            }

                            18 -> {
                                npcl("Now tell me the reason you are here, or we shall soon see who will be killing whom.")
                                stage = 30
                            }

                            20 -> {
                                playerl("I'm fairly sure it will have some kind of magical aura or something too. I don't suppose you've seen it, or know where it might be?")
                                stage++
                            }

                            21 -> {
                                npcl("Interesting... Well perhaps we can come to a little... arrangement, human.")
                                stage++
                            }

                            22 -> {
                                npcl("I may have information that may assist you, but you in turn will have to do something for me. What do you say? Do you think we could come to some form of")
                                stage++
                            }

                            23 -> {
                                npcl("agreement?")
                                stage++
                            }

                            24 -> {
                                playerl("Well, what kind of something? No offence, but you're not exactly the trustworthy type...")
                                stage++
                            }

                            25 -> {
                                npcl("Ah, you have a healthy sense of paranoia, I see. It is not a particularly unfair request on my part...")
                                stage++
                            }

                            26 -> {
                                npcl("All I ask is that you ensure that the current owner of the diamond is killed. For my part, I will let you know his whereabouts, and how exactly to kill him.")
                                stage++
                            }

                            27 -> {
                                npcl("When he is dead, you may take the diamond from his corpse and do with it what you will. I have no interest in such baubles.")
                                stage++
                            }

                            28 -> {
                                npcl("So what say you? A life for a diamond. As a mark of good faith, I will give you some information free:")
                                stage++
                            }

                            29 -> {
                                npcl("The current owner of this diamond is named Dessous.")

                                if (DesertTreasure.getSubStage(player!!, DesertTreasure.attributeBloodStage) == 0) {
                                    DesertTreasure.setSubStage(player!!, DesertTreasure.attributeBloodStage, 1)
                                }

                                stage++
                            }

                            30 -> {
                                npcl("Now tell me the reason for your coming here, or I will ensure you suffer a horrible fate indeed.")
                                stage = 31
                            }

                            31 -> {
                                options("Agree to this arrangement?", "Yes", "No")
                                stage = 32
                            }

                            32 -> {
                                when (buttonID) {
                                    1 -> {
                                        options("Yes", "No")
                                        stage = 33
                                    }

                                    2 -> {
                                        options("Yes", "No")
                                        stage = 33
                                    }
                                }
                            }

                            33 -> {
                                when (buttonID) {
                                    1 -> {
                                        if (DesertTreasure.getSubStage(player!!, DesertTreasure.attributeBloodStage) == 1) {
                                            DesertTreasure.setSubStage(player!!, DesertTreasure.attributeBloodStage, 2)
                                        }

                                        playerl("Well... I can't see any drawback. Okay, I accept your offer. Now tell me what you know.")
                                        stage = 40
                                    }

                                    2 -> {
                                        playerl("I don't trust you, or anything you say. I'm afraid I have to decline.")
                                        stage++
                                    }
                                }
                            }

                            34 -> {
                                npcl("As you wish human. I doubt anybody near here knows of the diamond you seek, however.")
                                stage++
                            }

                            35 -> {
                                npcl("If you wish to claim it as your own, you have little choice but to accept my bargain. I will wait here until such time as you change your mind.")
                                endFile()
                            }

                            40 -> {
                                npcl("What I know? Hah! After you have been alive for as long as I, the things I know are enough to fill a library.")
                                stage++
                            }

                            41 -> {
                                npcl("I'm afraid you will need to be a little more specific.")
                                stage++
                            }

                            42 -> {
                                options(
                                    "Why do you want Dessous dead?",
                                    "Where can I find Dessous?",
                                    "How can I kill Dessous?",
                                    "Actually, I don't need to know anything."
                                )
                                stage++
                            }

                            43 -> {
                                when (buttonID) {
                                    1 -> {
                                        playerl("I don't see exactly how this bargain benefits you... Why exactly do you want Dessous killed anyway?")
                                        stage = 50
                                    }

                                    2 -> {
                                        playerl("Where can I find this Dessous?")
                                        stage = 60
                                    }

                                    3 -> {
                                        playerl("So what advice can you give me on killing Dessous?")
                                        stage = 70
                                    }

                                    4 -> {
                                        playerl("Never mind, I will figure out all I need to know by myself.")
                                        stage = 82
                                    }
                                }
                            }

                            // Why do you want Dessous dead?
                            50 -> {
                                npcl("That is an impertinent question to demand from myself.")
                                stage++
                            }

                            51 -> {
                                npcl("However, if it will help seal our deal, I will let you know some of the details.")
                                stage++
                            }

                            52 -> {
                                npcl("As you may or may not know, myself and my blood kin are the rulers of this land, all serving under Lord Drakan. However, we tend not to die of old age or similar")
                                stage++
                            }

                            53 -> {
                                npcl("natural causes, which means that to gain another Lords tithe or land, there often need to be...")
                                stage++
                            }

                            54 -> {
                                npcl("Unnatural causes of death involved. Let us just say, that Dessous is in control of some land that I myself would like some say in, and that it is not in my interest to be seen to be responsible for the")
                                stage++
                            }

                            55 -> {
                                npcl("death of a fellow Lord.")
                                stage++
                            }

                            56 -> {
                                npcl("It would however be extremely advantageous to myself should some random human adventurer take it upon themself to remove this rival for me... Do you understand?")
                                stage++
                            }

                            57 -> {
                                playerl("Yes... I think so...")
                                stage++
                            }

                            58 -> {
                                npcl("Good, we understand each other then. We will both benefit from the death of Dessous.")
                                stage = 42
                            }

                            // Where can I find Dessous?
                            60 -> {
                                npcl("He currently resides in a graveyard to the South-East of here. You will not be able to move the gravestone which he lies beneath however, you will need to find some way to")
                                stage++
                            }

                            61 -> {
                                npcl("lure him out from his tomb.")
                                stage++
                            }

                            62 -> {
                                playerl("And how exactly would I go about doing that?")
                                stage++
                            }

                            63 -> {
                                npcl("Well, he is a vampyre, so fresh blood would almost certainly entice him out.")
                                stage++
                            }

                            64 -> {
                                npcl("However, even though he is a frail and decrepit example of our species, he will be able to kill a weakling human such as yourself extremely easily. Having him in a bloodlust as he does so, will not make")
                                stage++
                            }

                            65 -> {
                                npcl("your job any easier.")
                                stage = 42
                            }

                            // How can I kill Dessous?
                            70 -> {
                                npcl("As ancient and weak as Dessous is, he is still more than a match for the likes of you.")
                                stage++
                            }

                            71 -> {
                                npcl("That is, assuming you were to fight him fairly.")
                                stage++
                            }

                            72 -> {
                                npcl("My proposal would be for you to even the odds up a little bit...")
                                stage++
                            }

                            73 -> {
                                playerl("How would I go about doing that?")
                                stage++
                            }

                            74 -> {
                                npcl("Well, my plan would be as follows. First, take a silver bar to the man living in the sewers in Draynor. He was an assistant to Count Draynor in some of his...")
                                stage++
                            }

                            75 -> {
                                npcl("more interesting experiments many years past. Tell him you need a sacrificial offering pot. He will know what you speak of, it is a unique type of container used in various ancient vampyric ceremonies.")
                                stage++
                            }

                            76 -> {
                                npcl("Then take the pot to Entrana and get it blessed by the Head Monk. This will lend the pot some holy power.")
                                stage++
                            }

                            77 -> {
                                npcl("If you then bring that silver pot back to me, I will provide you with some fresh blood, to put into it. To that pot of blood, you will add some crushed garlic, and some spice to disguise the smell.")
                                stage++
                            }

                            78 -> {
                                npcl("Use that pot of blood upon Dessous' tomb, and he will be unable to resist rising and drinking from it.")
                                stage++
                            }

                            79 -> {
                                npcl("The combination of garlic, silver, and blessings from Saradomin will act upon him as a poison, and allow you to kill him.")
                                stage++
                            }

                            80 -> {
                                npcl("This is just my suggestion of course, you may ignore it if you wish, although I offer no guarantees of your ability to defeat him otherwise.")
                                stage++
                            }

                            81 -> {
                                npcl("Was there anything else you wanted?")
                                stage = 42
                            }

                            // End the information conversation.
                            82 -> {
                                npcl("As you wish. Come and see me when you have managed to kill Dessous.")
                                endFile()
                            }
                        }
                    }

                    2 -> {
                        if (stage == START_DIALOGUE) {
                            if (inInventory(player!!, Items.BLESSED_POT_4659) ||
                                inInventory(player!!, Items.SILVER_POT_4658)
                            ) {
                                player(
                                    "I found Ruantun in Draynors sewers.",
                                    "He made me this pot, now where can I get some fresh",
                                    "blood to fill it with?"
                                )

                                sendMessage(player!!, "Malak cuts you and pours some of your blood into the pot.")

                                if (removeItem(player!!, Items.SILVER_POT_4658)) {
                                    addItemOrDrop(player!!, Items.SILVER_POT_4660)
                                } else if (removeItem(player!!, Items.BLESSED_POT_4659)) {
                                    addItemOrDrop(player!!, Items.BLESSED_POT_4661)
                                }

                                animate(npc!!, 1264)
                                impact(player!!, 5, HitsplatType.NORMAL)
                                stage = 10
                            } else {
                                npcl("Why are you still here? I notice Dessous still lives.")
                                stage = 20
                            }
                        } else {
                            when (stage) {
                                10 -> {
                                    dialogue("Malak cuts you and pours some of your blood into the pot.")
                                    stage++
                                }

                                11 -> {
                                    playerl("Ow!")
                                    stage++
                                }

                                12 -> {
                                    npcl("There you go. As fresh as it gets.")
                                    stage++
                                }

                                13 -> {
                                    playerl("Thanks for nothing.")
                                    stage++
                                }

                                14 -> {
                                    npcl("Come and speak to me again when you have managed to kill Dessous.")
                                    endFile()
                                }

                                20 -> {
                                    options(
                                        "Where can I find Dessous?",
                                        "How do I kill Dessous again?",
                                        "Actually, I don't need to know anything."
                                    )
                                    stage++
                                }

                                21 -> {
                                    when (buttonID) {
                                        1 -> {
                                            playerl("Where can I find this Dessous?")
                                            stage = 30
                                        }

                                        2 -> {
                                            playerl("How am I supposed to kill Dessous again?")
                                            stage = 40
                                        }

                                        3 -> {
                                            playerl("Never mind, I will figure out all I need to know by myself.")
                                            stage = 50
                                        }
                                    }
                                }

                                30 -> {
                                    npcl("He currently resides in a graveyard to the South-East of here. You will not be able to move the gravestone which he lies beneath however, you will need to find some way to")
                                    stage++
                                }

                                31 -> {
                                    npcl("lure him out from his tomb.")
                                    stage++
                                }

                                32 -> {
                                    playerl("And how exactly would I go about doing that?")
                                    stage++
                                }

                                33 -> {
                                    npcl("Come and see my when you have prepared a silver ritual pot in the manner I have told you.")
                                    stage++
                                }

                                34 -> {
                                    npcl("I will ensure that you get some fresh blood that you may taint with garlic and spices, to lure our Dessous.")
                                    endFile()
                                }

                                40 -> {
                                    npcl("Take a silver bar to the man in the Draynor sewers. He will fashion a ritualistic pot for you, which you should then take to Entrana and get blessed.")
                                    stage++
                                }

                                41 -> {
                                    npcl("When you have done that, come back here and speak to me, I will provide you with some fresh blood which you will then crush some garlic into, and then add some spices to hide the garlic.")
                                    stage++
                                }

                                42 -> {
                                    npcl("In this way, you will be able to lure him from his tomb and he should be sufficiently weakened to be vulnerable to your attacks.")
                                    endFile()
                                }

                                50 -> {
                                    npcl("As you wish. Come and see me when you have managed to kill Dessous.")
                                    endFile()
                                }
                            }
                        }
                    }

                    3 -> {
                        when (stage) {
                            START_DIALOGUE -> {
                                npcl("Ah, the wandering hero returns! I take it you have dispatched poor old Dessous for me?")
                                stage++
                            }

                            1 -> {
                                playerl("Quit playing games with me, Malak. I want that diamond, and I want it now!")

                                if (DesertTreasure.getSubStage(player!!, DesertTreasure.attributeBloodStage) == 3) {
                                    addItemOrDrop(player!!, Items.BLOOD_DIAMOND_4670)
                                    DesertTreasure.setSubStage(player!!, DesertTreasure.attributeBloodStage, 100)
                                }

                                stage++
                            }

                            2 -> {
                                npcl("Do not take that tone of voice with me, meat. You should be thankful I have allowed you your life.")
                                stage++
                            }

                            3 -> {
                                npcl("Here, take your precious little bauble.")
                                stage++
                            }

                            4 -> {
                                npcl("I will take that silver pot from you as well, humans are not meant to possess such artefacts. Now get out of my sight, our deal is complete, and if I see you here again I will not hesistate to take you to")
                                stage++
                            }

                            5 -> {
                                npcl("Lord Drakan.")
                                stage++
                            }

                            6 -> {
                                npcl("He will be extremely pleased to meet the murderer of poor Dessous, I suspect.")
                                endFile()
                            }
                        }
                    }

                    100 -> {
                        when (stage) {
                            START_DIALOGUE -> {
                                if (!inInventory(player!!, Items.BLOOD_DIAMOND_4670)) {
                                    playerl("Where is the Diamond of Blood? I know you have it!")

                                    addItemOrDrop(player!!, Items.BLOOD_DIAMOND_4670)
                                    stage++
                                } else {
                                    npcl("Be lucky I have let you live, meat. Our deal is done, I wish no further dealing with you.")
                                    endFile()
                                }
                            }

                            1 -> {
                                npcl("Do not take that tone of voice with me, meat. Here, take your bauble. I have no use for it.")
                                endFile()
                            }
                        }
                    }
                }
            }

            10, 11, 12, 13, 100 -> {
                when (stage) {
                    START_DIALOGUE -> {
                        npcl("Be lucky I have let you live, meat. Our deal is done, I wish no further dealing with you.")
                        endFile()
                    }
                }
            }
        }
    }
}