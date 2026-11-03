
package content.region.asgarnia.falador.quest.rd.dialogue

import content.region.asgarnia.falador.quest.rd.RDUtils
import content.region.asgarnia.falador.quest.rd.RecruitmentDrive
import core.api.*
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.game.interaction.QueueStrength
import core.game.node.entity.Entity
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.world.map.Location
import core.game.world.map.zone.ZoneBorders
import core.tools.START_DIALOGUE
import core.tools.secondsToTicks
import shared.consts.NPCs

class SirTinleyDialogueFile(private val dialogueNum: Int = 0) : DialogueFile(), MapArea {

    companion object {
        /**
         * Attribute to prevent the player from moving during the test.
         */
        const val attributeDoNotMove = "quest:recruitmentdrive-donotmove"
    }

    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            START_DIALOGUE -> {
                when {
                    dialogueNum == 0 &&
                        !getAttribute(player!!, attributeDoNotMove, false) &&
                        !getAttribute(player!!, RecruitmentDrive.stageFail, false) -> {
                        npc(
                            "Ah, welcome ${player!!.name}.",
                            "I have but one clue for you to pass this room's puzzle:",
                            "'Patience'."
                        )
                        stage = 1
                    }

                    dialogueNum == 0 &&
                        !getAttribute(player!!, attributeDoNotMove, false) &&
                        getAttribute(player!!, RecruitmentDrive.stageFail, false) -> {
                        npc(
                            FaceAnim.FRIENDLY,
                            "Excellent work, ${player!!.name}.",
                            "Please step through the portal to meet your next",
                            "challenge."
                        )
                        stage = 10
                    }

                    (dialogueNum == 0 &&
                        getAttribute(player!!, attributeDoNotMove, false)) ||
                        dialogueNum == 2 -> {
                        setAttribute(player!!, RecruitmentDrive.stageFail, true)
                        npc(
                            FaceAnim.SAD,
                            "No... I am very sorry.",
                            "Apparently you are not up to the challenge.",
                            "I will return you where you came from, better luck in the",
                            "future."
                        )
                        stage = 20
                    }

                    dialogueNum == 1 -> {
                        npc(
                            "Ah, ${player!!.name}, you have arrived.",
                            "Speak to me to begin your task."
                        )
                        stage = 30
                    }

                    else -> endFile()
                }
            }

            // Start the test after the introductory dialogue.
            1 -> {
                end()
                setAttribute(player!!, attributeDoNotMove, true)

                queueScript(player!!, 0, QueueStrength.NORMAL) { scriptStage: Int ->
                    when (scriptStage) {
                        0 -> return@queueScript delayScript(player!!, secondsToTicks(9))

                        1 -> {
                            if (!getAttribute(player!!, RecruitmentDrive.stageFail, false)) {
                                removeAttribute(player!!, attributeDoNotMove)
                                setAttribute(player!!, RecruitmentDrive.stagePass, true)
                                npc(
                                    FaceAnim.HAPPY,
                                    "Excellent work, ${player!!.name}.",
                                    "Please step through the portal to meet your next",
                                    "challenge."
                                )
                            } else {
                                removeAttribute(player!!, attributeDoNotMove)
                                npc(
                                    FaceAnim.SAD,
                                    "You did not complete the challenge in time."
                                )
                            }

                            return@queueScript stopExecuting(player!!)
                        }

                        else -> return@queueScript stopExecuting(player!!)
                    }
                }
            }

            // Successful completion.
            10 -> end()

            // Failed test.
            20 -> {
                end()
                removeAttribute(player!!, attributeDoNotMove)
                removeAttribute(player!!, RecruitmentDrive.stageFail)
                RDUtils.failSequence(player!!)
            }

            // Next stage.
            30 -> {
                end()
                removeAttribute(player!!, attributeDoNotMove)
            }
        }
    }

    override fun defineAreaBorders(): Array<ZoneBorders> {
        return arrayOf(ZoneBorders(2474, 4959, 2478, 4957))
    }

    /**
     * Called whenever an entity moves in this map area.
     */
    override fun entityStep(entity: Entity, location: Location, lastLocation: Location) {
        if (entity !is Player) return

        val doNotMove = getAttribute(entity, attributeDoNotMove, false)
        val stagePass = getAttribute(entity, RecruitmentDrive.stagePass, false)

        if (stagePass) {
            removeAttribute(entity, attributeDoNotMove)
            return
        }

        if (doNotMove) {
            val borders = defineAreaBorders()[0]

            if (!inBorders(entity, borders)) {
                setAttribute(entity, RecruitmentDrive.stageFail, true)
                removeAttribute(entity, attributeDoNotMove)
                openDialogue(
                    entity,
                    SirTinleyDialogueFile(2),
                    NPC(NPCs.SIR_TINLEY_2286)
                )
            }
        }
    }
}
