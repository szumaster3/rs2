package content.region.asgarnia.falador.quest.rd.dialogue

import content.region.asgarnia.falador.quest.rd.RDUtils
import content.region.asgarnia.falador.quest.rd.RecruitmentDrive
import core.api.getAttribute
import core.api.lock
import core.api.removeAttribute
import core.api.setAttribute
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.tools.START_DIALOGUE

class SirReenItchoodDialogue(private val dialogueNum: Int = 0) : DialogueFile() {

    companion object {
        const val ATTRIBUTE_CLUE = "rd:cluenumber"
    }

    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            START_DIALOGUE -> {
                when {
                    dialogueNum in 0..1 && !getAttribute(player!!, RecruitmentDrive.stageFail, false) -> {
                        if (getAttribute(player!!, ATTRIBUTE_CLUE, 6) == 6) {
                            setAttribute(player!!, ATTRIBUTE_CLUE, (0..5).random())
                        }

                        npc(
                            "Greetings friend, and welcome here,",
                            "you'll find my puzzle not so clear.",
                            "Hidden amongst my words, it's true,",
                            "the password for the door as a clue.",
                        )
                        stage = 1
                    }

                    dialogueNum == 2 || getAttribute(player!!, RecruitmentDrive.stageFail, false) -> {
                        npc(
                            FaceAnim.SAD,
                            "It's sad to say,",
                            "this test beat you.",
                            "I'll send you to Tiffy,",
                            "what to do?",
                        )
                        stage = 90
                    }

                    else -> endFile()
                }
            }

            // Show clue options.
            1 -> {
                options(
                    "Can I have the clue for the door?",
                    "Can I have a different clue?",
                )
                stage = 2
            }

            // Handle selected clue option.
            2 -> {
                when (buttonID) {
                    1 -> {
                        playerl("Can I have the clue for the door?")
                        stage = 10
                    }

                    2 -> {
                        player(
                            "I don't get that riddle...",
                            "Can I have a different one?",
                        )
                        stage = 20
                    }
                }
            }

            // First clue.
            10 -> {
                when (getAttribute(player!!, ATTRIBUTE_CLUE, 0)) {
                    0 -> npc(
                        "Better than me, you'll not find",
                        "In rhyming and in puzzles.",
                        "This clue so clear will tax your mind",
                        "Entirely as it confuzzles!",
                    )

                    1 -> npc(
                        "Feel the aching of your mind",
                        "In puzzlement, confused.",
                        "See the clue hidden behind",
                        "His words, as you perused.",
                    )

                    2 -> npc(
                        "Look closely at the words i speak;",
                        "And study closely every part.",
                        "See for yourself the word you seek",
                        "Trapped for you if you're smart.",
                    )

                    3 -> npc(
                        "More than words, i have not for you",
                        "Except the things i say today.",
                        "Aware are you, this is a clue?",
                        "Take note of what i say!",
                    )

                    4 -> npc(
                        "Rare it is that you will see",
                        "A puzzle such as this!",
                        "In many ways it tickles me",
                        "Now, watching you hit and miss!",
                    )

                    5 -> npc(
                        "This riddle of mine may confuse,",
                        "I am quite sure of that.",
                        "Mayhap you should closely peruse",
                        "Every word i have spat?",
                    )
                }
                end()
            }

            // Second clue.
            20 -> {
                when (getAttribute(player!!, ATTRIBUTE_CLUE, 0)) {
                    0 -> npc(
                        "Before you hurry through that door",
                        "Inspect the words i spoke.",
                        "There is a simple hidden flaw",
                        "Ere you think my rhyme a joke.",
                    )

                    1 -> npc(
                        "First my clue you did not see,",
                        "I really wish you had.",
                        "Such puzzling wordplay devilry",
                        "Has left you kind of mad!",
                    )

                    2 -> npc(
                        "Last time my puzzle did not help",
                        "Apparently, so you've bidden.",
                        "Study my speech carefully, whelp",
                        "To find the answer, hidden.",
                    )

                    3 -> npc(
                        "Many types have passed through here",
                        "Even such as you amongst their sort.",
                        "And in the end, the puzzles clear;",
                        "The hidden word you sought.",
                    )

                    4 -> npc(
                        "Repetition, once again",
                        "Against good sense it goes.",
                        "In my words, the answers plain",
                        "Now that you see rhyme flows.",
                    )

                    5 -> npc(
                        "Twice it is now, i have stated",
                        "In a rhyme, what is the pass.",
                        "Maybe my words obfuscated",
                        "Entirely beyond your class.",
                    )
                }
                end()
            }

            // Failed recruitment test.
            90 -> {
                end()

                lock(player!!, 10)
                removeAttribute(player!!, ATTRIBUTE_CLUE)
                setAttribute(player!!, RecruitmentDrive.stagePass, false)
                setAttribute(player!!, RecruitmentDrive.stageFail, false)
                RDUtils.failSequence(player!!)
            }
        }
    }
}