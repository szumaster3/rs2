package content.region.asgarnia.falador.quest.rd.dialogue


import content.region.asgarnia.falador.quest.rd.RecruitmentDrive
import content.region.asgarnia.falador.quest.rd.plugin.SirLeyeNPC
import core.api.getAttribute
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.tools.START_DIALOGUE

class SirKuamFerentseDialogue(
    private val dialogueNum: Int = 0,
) : DialogueFile() {

    companion object {
        const val spawnSirLeye = "rd:generatedsirleye"
    }

    override fun handle(componentID: Int, buttonID: Int) {
        when (stage) {
            START_DIALOGUE -> {
                if (getAttribute(player!!, RecruitmentDrive.stagePass, false)) {
                    npc(FaceAnim.HAPPY, "Excellent work, ${player!!.name}!", "Please step through the portal to meet your next", "challenge.")
                    stage = 10
                } else {
                    npc("Ah, ${player!!.name}, you're finally here.", "Your task for this room is to defeat Sir Leye.", "He has been blessed by Saradomin to be undefeatable", "by any man, so it should be quite the challenge for you.")
                    stage = 1
                }
            }

            1 -> {
                npc("If you are having problems, remember", "A true warrior uses his wits as much as his brawn.", "Fight smarter, not harder.")
                stage = 2
            }

            2 -> {
                end()
                SirLeyeNPC.init(player!!)
            }

            10 -> endFile()
        }
    }
}
