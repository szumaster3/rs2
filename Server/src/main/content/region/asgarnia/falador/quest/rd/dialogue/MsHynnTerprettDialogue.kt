package content.region.asgarnia.falador.quest.rd.dialogue

import content.region.asgarnia.falador.quest.rd.RDUtils
import content.region.asgarnia.falador.quest.rd.RecruitmentDrive
import core.api.*
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.tools.START_DIALOGUE
import shared.consts.NPCs

class MsHynnTerprettDialogue(private val dialogueNum: Int = 0) : DialogueFile() {

    companion object {
        const val ATTRIBUTE_RANDOM_RIDDLE = "rd:randomriddle"
        const val ATTRIBUTE_CORRECT_ANSWER = "rd:recentlycorrect"
    }

    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            START_DIALOGUE -> {
                when {
                    getAttribute(player!!, ATTRIBUTE_CORRECT_ANSWER, false) -> {
                        removeAttribute(player!!, ATTRIBUTE_CORRECT_ANSWER)

                        if (!getAttribute(player!!, RecruitmentDrive.stageFail, false)) {
                            setAttribute(player!!, RecruitmentDrive.stagePass, true)
                            removeAttribute(player!!, ATTRIBUTE_RANDOM_RIDDLE)
                        }

                        npc(
                            FaceAnim.HAPPY,
                            "Excellent work, ${player!!.name}",
                            "Please step through the portal to meet your next",
                            "challenge."
                        )
                        stage = 100
                    }

                    getAttribute(player!!, RecruitmentDrive.stageFail, false) -> {
                        npc(
                            FaceAnim.SAD,
                            "No... I am very sorry.",
                            "Apparently you are not up to the challenge.",
                            "I will return you where you came from, better luck in the",
                            "future."
                        )
                        stage = 200
                    }

                    getAttribute(player!!, RecruitmentDrive.stagePass, false) -> {
                        npc(
                            "You certainly have the wits to be a Temple Knight.",
                            "Pass on through the portal to find your next challenge."
                        )
                        stage = 300
                    }

                    else -> {
                        if (getAttribute(player!!, ATTRIBUTE_RANDOM_RIDDLE, 5) == 5) {
                            setAttribute(player!!, ATTRIBUTE_RANDOM_RIDDLE, (0..4).random())
                        }

                        npc(
                            "Greetings, ${player!!.name}",
                            "I am here to test your wits with a simple riddle."
                        )
                        stage = 1
                    }
                }
            }

            // Correct answer / successful completion.
            100 -> end()

            // Failed test.
            200 -> {
                end()
                removeAttribute(player!!, ATTRIBUTE_RANDOM_RIDDLE)
                setAttribute(player!!, RecruitmentDrive.stagePass, false)
                setAttribute(player!!, RecruitmentDrive.stageFail, false)
                RDUtils.failSequence(player!!)
            }

            // Already passed the test.
            300 -> end()

            // Select a random riddle.
            1 -> {
                when (getAttribute(player!!, ATTRIBUTE_RANDOM_RIDDLE, 0)) {
                    0 -> {
                        npc(
                            FaceAnim.THINKING,
                            "Here is my riddle:",
                            "I estimate there to be one million inhabitants in the world",
                            "of @servername, creatures and people both."
                        )
                        stage = 10
                    }

                    1 -> {
                        npc(
                            FaceAnim.THINKING,
                            "Here is my riddle:",
                            "Which of the following statements is true?"
                        )
                        stage = 20
                    }

                    2 -> {
                        npc(
                            FaceAnim.THINKING,
                            "Here is my riddle:",
                            "I have both a husband and daughter."
                        )
                        stage = 30
                    }

                    3 -> {
                        npc(
                            FaceAnim.THINKING,
                            "Here is my riddle:",
                            "Imagine that you have been captured by an enemy.",
                            "You are to be killed, but in a moment of mercy, the",
                            "enemy has allowed you to pick your own demise."
                        )
                        stage = 40
                    }

                    4 -> {
                        npc(
                            FaceAnim.THINKING,
                            "Here is my riddle:",
                            "I dropped four identical stones, into four identical",
                            "buckets, each containing an identical amount of water."
                        )
                        stage = 50
                    }
                }
            }

            // Riddle 0: Enter the amount.
            10 -> {
                npc(FaceAnim.THINKING, "What number would you get if you multiply", "the number of fingers on everything's left hand, to the", "nearest million?")
                stage = 11
            }

            11 -> {
                end()
                sendInputDialogue(player!!, true, "Enter the amount:") { value: Any ->
                    if (value == 5000000) {
                        setAttribute(player!!, ATTRIBUTE_CORRECT_ANSWER, true)
                    } else {
                        setAttribute(player!!, RecruitmentDrive.stageFail, true)
                    }

                    openDialogue(
                        player!!,
                        MsHynnTerprettDialogue(),
                        NPC(NPCs.MS_HYNN_TERPRETT_2289)
                    )
                }
            }

            // Riddle 1: Select the true statement.
            20 -> {
                options(
                    "The number of false statements here is one.",
                    "The number of false statements here is two.",
                    "The number of false statements here is three.",
                    "The number of false statements here is four."
                )
                stage = 21
            }

            21 -> {
                if (buttonID == 3) {
                    removeAttribute(player!!, ATTRIBUTE_CORRECT_ANSWER)

                    if (!getAttribute(player!!, RecruitmentDrive.stageFail, false)) {
                        setAttribute(player!!, RecruitmentDrive.stagePass, true)
                        removeAttribute(player!!, ATTRIBUTE_RANDOM_RIDDLE)
                    }

                    npc(
                        FaceAnim.HAPPY,
                        "Excellent work, ${player!!.name}",
                        "Please step through the portal to meet your next",
                        "challenge."
                    )
                    stage = 100
                } else {
                    npc(
                        FaceAnim.SAD,
                        "No... I am very sorry.",
                        "Apparently you are not up to the challenge.",
                        "I will return you where you came from, better luck in the",
                        "future."
                    )
                    stage = 200
                }
            }

            // Riddle 2: Enter the daughter's age.
            30 -> {
                npc(
                    FaceAnim.THINKING,
                    "My husband is four times older than my daughter.",
                    "In twenty years time, he will be twice as old as my",
                    "daughter."
                )
                stage = 31
            }

            31 -> {
                npc(
                    FaceAnim.THINKING,
                    "How old is my daughter now?"
                )
                stage = 32
            }

            32 -> {
                end()
                sendInputDialogue(player!!, true, "Enter the amount:") { value: Any ->
                    if (value == 10) {
                        setAttribute(player!!, ATTRIBUTE_CORRECT_ANSWER, true)
                    } else {
                        setAttribute(player!!, RecruitmentDrive.stageFail, true)
                    }

                    openDialogue(
                        player!!,
                        MsHynnTerprettDialogue(),
                        NPC(NPCs.MS_HYNN_TERPRETT_2289)
                    )
                }
            }

            // Riddle 3: Choose the wolves.
            40 -> {
                npc(FaceAnim.THINKING, "Your first choice is to be drowned in a lake of acid.")
                stage++
            }

            41 -> {
                npc(FaceAnim.THINKING, "Your second choice is to be burned on a fire.")
                stage++
            }

            42 -> {
                npc(FaceAnim.THINKING, "Your third choice is to be thrown to a pack of wolves", "that have not been fed in over a month.")
                stage++
            }

            43 -> {
                npc(FaceAnim.THINKING, "Your final choice of fate is to be thrown from the walls", "of a castle, many hundreds of feet high.")
                stage++
            }

            44 -> {
                npc(FaceAnim.THINKING, "Which fate would you be wise to choose?")
                stage++
            }

            45 -> {
                options(
                    "The lake of acid.",
                    "The large fire.",
                    "The wolves.",
                    "The castle walls."
                )
                stage = 46
            }

            46 -> {
                if (buttonID == 3) {
                    removeAttribute(player!!, ATTRIBUTE_CORRECT_ANSWER)

                    if (!getAttribute(player!!, RecruitmentDrive.stageFail, false)) {
                        setAttribute(player!!, RecruitmentDrive.stagePass, true)
                        removeAttribute(player!!, ATTRIBUTE_RANDOM_RIDDLE)
                    }

                    npc(
                        FaceAnim.HAPPY,
                        "Excellent work, ${player!!.name}",
                        "Please step through the portal to meet your next",
                        "challenge."
                    )
                    stage = 100
                } else {
                    npc(
                        FaceAnim.SAD,
                        "No... I am very sorry.",
                        "Apparently you are not up to the challenge.",
                        "I will return you where you came from, better luck in the",
                        "future."
                    )
                    stage = 200
                }
            }

            // Riddle 4: Choose bucket A.
            50 -> {
                npc(
                    FaceAnim.THINKING,
                    "The first bucket's water was at 32 degrees Fahrenheit,",
                    "the second was 33 degrees, the third at 34 and the",
                    "fourth was at 35 degrees."
                )
                stage++
            }

            51 -> {
                npc(
                    FaceAnim.THINKING,
                    "Which bucket's stone dropped to the bottom of the bucket",
                    "last?"
                )
                stage++
            }

            52 -> {
                options(
                    "Bucket A (32 degrees)",
                    "Bucket B (33 degrees)",
                    "Bucket C (34 degrees)",
                    "Bucket D (35 degrees)"
                )
                stage = 53
            }

            53 -> {
                if (buttonID == 1) {
                    removeAttribute(player!!, ATTRIBUTE_CORRECT_ANSWER)

                    if (!getAttribute(player!!, RecruitmentDrive.stageFail, false)) {
                        setAttribute(player!!, RecruitmentDrive.stagePass, true)
                        removeAttribute(player!!, ATTRIBUTE_RANDOM_RIDDLE)
                    }

                    npc(
                        FaceAnim.HAPPY,
                        "Excellent work, ${player!!.name}",
                        "Please step through the portal to meet your next",
                        "challenge."
                    )
                    stage = 100
                } else {
                    npc(
                        FaceAnim.SAD,
                        "No... I am very sorry.",
                        "Apparently you are not up to the challenge.",
                        "I will return you where you came from, better luck in the",
                        "future."
                    )
                    stage = 200
                }
            }
        }
    }
}
