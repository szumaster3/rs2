package content.region.asgarnia.falador.quest.rd.dialogue

import content.region.asgarnia.falador.quest.rd.plugin.MissCheeversPlugin
import core.api.*
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import shared.consts.NPCs

class MissCheeversDialogueFile(private val state: Int = 0) : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        npc = NPC(NPCs.MISS_CHEEVERS_2288)

        when (state) {
            0 -> handleInitialDialogue()
            1 -> handleChallengeDialogue()
        }
    }

    private fun handleInitialDialogue() {
        when (stage) {
            0 -> {
                val helpStatus = getAttribute(player!!, "/save:rd:help", -1)
                if (helpStatus > 1) {
                    playerl(FaceAnim.FRIENDLY, "Please... I am REALLY stuck... Isn't there ANYTHING you can do to help me...?").also { stage = 6 }
                } else {
                    playerl(FaceAnim.FRIENDLY, "Can you give me any help?").also { stage++ }
                }
            }
            1 -> npcl(FaceAnim.FRIENDLY, "No, I am sorry, but that is forbidden by our rules.").also { stage++ }
            2 -> npcl(FaceAnim.FRIENDLY, "If you are having a particularly tough time of it, I suggest you leave and come back later when you are in a more receptive frame of mind.").also { stage++ }
            3 -> npcl(FaceAnim.FRIENDLY, "Sometimes a break from concentration will yield fresh insight. Our aim is to test you, but not to the point of frustration!").also { stage++ }
            4 -> playerl(FaceAnim.FRIENDLY, "Okay, thanks!").also { stage++ }
            5 -> {
                end()
                setAttribute(player!!, "/save:rd:help", 2)
            }
            6 -> npcl(FaceAnim.FRIENDLY, "Well... Look, I really shouldn't say anything about this room, but...").also { stage++ }
            7 -> npcl(FaceAnim.FRIENDLY, "When I was attempting to join the Temple Knights I myself had to do this puzzle myself.").also { stage++ }
            8 -> npcl(FaceAnim.FRIENDLY, "It was slightly different, but the idea behind it was the same, and I left the notes I had made while doing it hidden in one of the bookcases.").also { stage++ }
            9 -> npcl(FaceAnim.FRIENDLY, "If you look carefully you may find them, and they may be of some use to you.").also { stage++ }
            10 -> npcl(FaceAnim.FRIENDLY, "I really can't be any more help than that I'm afraid, it is more than my job's worth to have given you the help I already have.").also { stage++ }
            11 -> playerl(FaceAnim.FRIENDLY, "Okay, thanks a lot, you've been very helpful!").also { stage++ }
            12 -> npcl(FaceAnim.FRIENDLY, "Best of luck with the test ${player!!.name}. I hope your application is successful.").also { stage++ }
            13 -> {
                end()
                setAttribute(player!!, "/save:rd:help", 3)
            }
        }
    }

    private fun handleChallengeDialogue() {
        clearAttributes()
        when (stage) {
            0 -> npcl(FaceAnim.FRIENDLY, "Greetings, ${player!!.name}. Welcome to my challenge.").also { player!!.faceLocation(location(2469, 4941, 0)) }.also { stage++ }
            1 -> npcl(FaceAnim.FRIENDLY, "All you need to do is leave from the opposite door to where you came in by.").also { stage++ }
            2 -> npcl(FaceAnim.FRIENDLY, "I will warn you that this is more complicated than it may at first appear.").also { stage++ }
            3 -> npcl(FaceAnim.FRIENDLY, "I should also warn you that there are limited supplies of the items in this room, so think carefully before using them, you may find yourself stuck and have to leave to start again!").also { stage++ }
            4 -> npcl(FaceAnim.FRIENDLY, "Best of luck!").also { stage++ }
            5 -> {
                end()
                setAttribute(player!!, "/save:rd:help", 1)
            }
        }
    }

    private fun clearAttributes() {
        with(MissCheeversPlugin) {
            removeAttribute(player!!, book)
            removeAttribute(player!!, magnet)
            removeAttribute(player!!, knife)
            removeAttribute(player!!, shears)
            removeAttribute(player!!, tin)
            removeAttribute(player!!, chisel)
            removeAttribute(player!!, wire)
            removeAttribute(player!!, vials)
            MissCheeversPlugin.Companion.Vials.vialMap.forEach { removeAttribute(player!!, it.value.attribute) }
            MissCheeversPlugin.Companion.DoorVials.doorVialsRequiredMap.forEach { removeAttribute(player!!, it.value.attribute) }
        }
    }
}