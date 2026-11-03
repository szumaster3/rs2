package content.region.kandarin.east_ardougne.quest.ikov.dialogue

import content.data.GameAttributes
import core.api.*
import core.game.dialogue.Dialogue
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.game.node.entity.player.Player
import core.plugin.Initializable
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Quests

/**
 * Represents Guardian Of Armadyl dialogue.
 */
@Initializable
class GuardianOfArmadylDialogue(player: Player? = null) : Dialogue(player) {

    override fun newInstance(player: Player?): Dialogue = GuardianOfArmadylDialogue(player)

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        openDialogue(player, GuardianOfArmadylDialogueFile(), npc)
        return false
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.GUARDIAN_OF_ARMADYL_274, NPCs.GUARDIAN_OF_ARMADYL_275)
}

class GuardianOfArmadylDialogueFile : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            0 -> {
                if (inEquipment(player!!, Items.PENDANT_OF_LUCIEN_86)) {
                    npcl("Thou is a foul agent of Lucien! Such an agent must die!")
                    stage = 2
                } else {
                    npcl(
                        "Thou hast ventured deep into the tunnels, you have reached the temple of our master. It is many ages since a pilgrim has come here."
                    )
                    stage = 10
                }
            }

            2 -> {
                npc!!.attack(player)
                endFile()
            }

            // Main options
            10 -> {
                options(
                    "I seek the Staff of Armadyl.",
                    "Out of my way fool!",
                    "What are your kind and what are you doing here?"
                )
                stage = 11
            }

            11 -> {
                when (buttonID) {
                    // I seek the Staff of Armadyl
                    1 -> {
                        playerl(FaceAnim.FRIENDLY, "I seek the Staff of Armadyl.")
                        stage = 100
                    }

                    // Out of my way fool
                    2 -> {
                        playerl("Out of my way fool!")
                        stage = 200
                    }

                    // What are your kind?
                    3 -> {
                        playerl("What are your kind and what are you doing here?")
                        stage = 300
                    }
                }
            }

            // Staff of Armadyl
            100 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "We are the guardians of the staff, our fathers were guardians and our fathers' fathers before that. Why dost thou seek it?"
                )
                stage = 101
            }

            101 -> {
                options(
                    "Lucien will give me a grand reward for it!",
                    "Give it to me!",
                    "I collect rare and powerful artefacts."
                )
                stage = 102
            }

            102 -> {
                when (buttonID) {
                    1 -> {
                        playerl("Lucien will give me a grand reward for it!")
                        stage = 103
                    }

                    2 -> {
                        playerl("Give it to me!")
                        stage = 110
                    }

                    3 -> {
                        playerl("I collect rare and powerful artefacts.")
                        stage = 120
                    }
                }
            }

            // Lucien reward
            103 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "Thou art working for that spawn of evil?! Fool! You must be cleansed to save your soul!"
                )
                stage = 500
            }

            // Give it to me
            110 -> {
                npcl(FaceAnim.FRIENDLY, "The staff is sacred! You will not have it!")
                stage = 111
            }

            111 -> {
                npc!!.attack(player)
                endFile()
            }

            // Rare artefacts
            120 -> {
                npcl(FaceAnim.FRIENDLY, "Your worldly greed has darkened your soul!")
                stage = 121
            }

            121 -> {
                npc!!.attack(player)
                endFile()
            }

            // Out of my way fool
            200 -> {
                npcl(FaceAnim.FRIENDLY, "I may be a fool but I will not step aside!")
                stage = 201
            }

            201 -> {
                options(
                    "Why not?!",
                    "Then you must die!",
                    "You're right, I will go now."
                )
                stage = 202
            }

            202 -> {
                when (buttonID) {
                    1 -> {
                        playerl("Why not?!")
                        stage = 203
                    }

                    2 -> {
                        playerl("Then you must die!")
                        stage = 204
                    }

                    3 -> {
                        playerl("You're right, I will go now.")
                        stage = 205
                    }
                }
            }

            203 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "Only members of our order are allowed to handle the staff."
                )
                endFile()
            }

            204 -> {
                npc!!.attack(player)
                endFile()
            }

            205 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "That is a wise decision. Stay a while and let your soul be cleansed!"
                )
                endFile()
            }

            // Guardians of Armadyl
            300 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "We are the Guardians of Armadyl. We have kept the temple safe for many ages. The evil in the dungeons seek what lies here. The Mahjarrat are the worst."
                )
                stage = 301
            }

            301 -> {
                options(
                    "What is the Armadyl?",
                    "Who are the Mahjarrat?",
                    "Wow! You must be really old!"
                )
                stage = 302
            }

            302 -> {
                when (buttonID) {
                    // What is Armadyl?
                    1 -> {
                        playerl("What is the Armadyl?")
                        stage = 310
                    }

                    // Who are the Mahjarrat?
                    2 -> {
                        playerl("Who are the Mahjarrat?")
                        stage = 320
                    }

                    // Wow! You must be really old!
                    3 -> {
                        playerl("Wow! You must be really old!")
                        stage = 330
                    }
                }
            }

            // What is Armadyl?
            310 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "Armadyl is the god we serve. We have been charged with guarding his sacred artifacts until he requires them."
                )
                stage = 311
            }

            311 -> {
                options(
                    "Ah ok, thanks.",
                    "Someone told me there were only three gods."
                )
                stage = 312
            }

            312 -> {
                when (buttonID) {
                    1 -> {
                        playerl("Ah ok, thanks.")
                        stage = 313
                    }

                    2 -> {
                        playerl(
                            FaceAnim.FRIENDLY,
                            "Someone told me there were only three gods. Saradomin, Zamorak and Guthix."
                        )
                        stage = 314
                    }
                }
            }

            313 -> {
                npcl(FaceAnim.FRIENDLY, "Go in peace.")
                endFile()
            }

            314 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "Saradominists. Bleh. They only acknowledge those three. There are at least twenty gods!"
                )
                endFile()
            }

            // Who are the Mahjarrat?
            320 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "They are ancient and powerful beings! They are very evil! It is said that they once dominated this plane of existence, Zamorak was supposedly of their blood. They are far fewer in number now."
                )
                stage = 321
            }

            321 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "Some still have presence in this world in their liche forms. Mahjarrat such as Lucien and Azzanadra would become very powerful if they came into possession of the Staff of Armadyl."
                )
                stage = 322
            }

            322 -> {
                options(
                    "Did you say Lucien?",
                    "I hope you're doing a good job then!"
                )
                stage = 323
            }

            323 -> {
                when (buttonID) {
                    1 -> {
                        playerl(
                            FaceAnim.FRIENDLY,
                            "Did you say Lucien? It was Lucien that asked me to get the staff!"
                        )
                        stage = 324
                    }

                    2 -> {
                        playerl("I hope you're doing a good job then!")
                        stage = 325
                    }
                }
            }

            324 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "You are a fool to be working for Lucien! Your soul must be cleansed to save you!"
                )
                stage = 500
            }

            325 -> {
                npcl(FaceAnim.FRIENDLY, "Do not fear! We are devoted to our charge!")
                endFile()
            }

            // Wow! You must be really old!
            330 -> {
                npcl(
                    FaceAnim.ANGRY,
                    "No! I am not old! My family has guarded the staff for many generations."
                )
                endFile()
            }

            // Shared confrontation options
            500 -> {
                options(
                    "How dare you call me a fool!",
                    "I just thought of something I must do!",
                    "You're right, it's time for my yearly bath."
                )
                stage = 501
            }

            501 -> {
                when (buttonID) {
                    1 -> {
                        playerl(
                            FaceAnim.ANGRY,
                            "How dare you call me a fool! I will work for whom I want!"
                        )
                        stage = 510
                    }

                    2 -> {
                        playerl("I just thought of something I must do!")
                        stage = 520
                    }

                    3 -> {
                        playerl("You're right, it's time for my yearly bath.")
                        stage = 530
                    }
                }
            }

            // How dare you call me a fool
            510 -> {
                npcl(FaceAnim.ANGRY, "We must cleanse the temple!")
                stage = 511
            }

            511 -> {
                npc!!.attack(player)
                endFile()
            }

            // I just thought of something
            520 -> {
                npcl(FaceAnim.ANGRY, "An agent of evil cannot be allowed to leave!")
                stage = 521
            }

            521 -> {
                npc!!.attack(player)
                endFile()
            }

            // Yearly bath
            530 -> {
                dialogue("The guardian splashes holy water over you.")
                stage = 531
            }

            531 -> {
                npcl(FaceAnim.FRIENDLY, "You have been cleansed!")
                stage = 532
            }

            532 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "Lucien must not get hold of the staff! He would become too powerful!"
                )
                stage = 533
            }

            533 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "Hast thou come across the undead necromancer? It was he that raised an army of the undead against Varrock a generation ago. If you know where he is you can help us defeat him."
                )
                stage = 534
            }

            534 -> {
                options(
                    "Ok! I'll help!",
                    "No! I shall not turn against my employer!",
                    "I need time to think."
                )
                stage = 535
            }

            535 -> {
                when (buttonID) {
                    // Help the guardian
                    1 -> {
                        playerl("Ok! I'll help!")
                        stage = 540
                    }

                    // Refuse to help
                    2 -> {
                        playerl("No! I shall not turn against my employer!")
                        stage = 550
                    }

                    // Need time to think
                    3 -> {
                        playerl("I need time to think.")
                        stage = 560
                    }
                }
            }

            // Help the guardian
            540 -> {
                npcl(FaceAnim.FRIENDLY, "So he is close by?")
                stage = 541
            }

            541 -> {
                playerl(FaceAnim.FRIENDLY, "Yes!")
                stage = 542
            }

            542 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "He must be gaining in power again. If you can defeat him he will be banished from this plane for a while. You will need this pendant to attack him."
                )
                stage = 543
            }

            543 -> {
                sendItemDialogue(player!!,Items.ARMADYL_PENDANT_87, "The guardian has given you a pendant.")
                stage = 544
            }

            544 -> {
                setAttribute(player!!, GameAttributes.QUEST_IKOV_SELECTED_END, 1)

                if (getQuestStage(player!!, Quests.TEMPLE_OF_IKOV) == 5) {
                    setQuestStage(player!!, Quests.TEMPLE_OF_IKOV, 6)
                }

                addItemOrDrop(player!!, Items.ARMADYL_PENDANT_87)
                endFile()
            }

            // Refuse to help
            550 -> {
                npcl(FaceAnim.ANGRY, "Fool! You will die for your sins!")
                stage = 551
            }

            551 -> {
                npc!!.attack(player)
                endFile()
            }

            // Need time to think
            560 -> {
                npcl(FaceAnim.FRIENDLY, "Linger a while and be at peace.")
                endFile()
            }
        }
    }
}