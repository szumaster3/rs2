package content.region.asgarnia.falador.quest.rd.dialogue

import content.region.asgarnia.falador.quest.rd.RDUtils
import content.region.asgarnia.falador.quest.rd.RecruitmentDrive
import core.api.getAttribute
import core.api.removeAttribute
import core.api.setAttribute
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.tools.START_DIALOGUE

class SirSpishyusDialogue(private val dialogueNum: Int = 0) : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            START_DIALOGUE -> {
                when {
                    getAttribute(player!!, RecruitmentDrive.stagePass, false) -> {
                        npc(
                            FaceAnim.HAPPY,
                            "Excellent work, ${player!!.name}.",
                            "Please step through the portal to your next challenge."
                        )
                        stage = 100
                    }

                    dialogueNum == 2 || getAttribute(player!!, RecruitmentDrive.stageFail, false) -> {
                        setAttribute(player!!, RecruitmentDrive.stageFail, true)
                        npc(
                            FaceAnim.SAD,
                            "No... I am very sorry.",
                            "You are not up to the challenge.",
                            "Better luck in the future."
                        )
                        stage = 200
                    }

                    else -> {
                        npcl(FaceAnim.FRIENDLY, "Ah, welcome ${player!!.name}.")
                        stage = 1
                    }
                }
            }

            1 -> {
                playerl(FaceAnim.FRIENDLY, "Hello. What am I supposed to do here?")
                stage = 2
            }

            2 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "Take the fox, chicken, and grain across the bridge, but be careful!"
                )
                stage = 3
            }

            3 -> {
                npcl(
                    FaceAnim.FRIENDLY,
                    "You can only carry one at a time, and leaving the wrong pair alone will result in failure."
                )
                stage = 4
            }

            4 -> {
                playerl(FaceAnim.FRIENDLY, "Got it. I'll see what I can do.")
                stage = 100
            }

            100 -> endFile()

            200 -> {
                end()
                removeAttribute(player!!, "quest:recruitmentdrive-donotmove")
                setAttribute(player!!, RecruitmentDrive.stagePass, false)
                setAttribute(player!!, RecruitmentDrive.stageFail, false)
                RDUtils.failSequence(player!!)
            }
        }
    }
}
