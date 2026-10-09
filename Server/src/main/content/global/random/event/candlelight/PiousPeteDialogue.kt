package content.global.random.event.candlelight

import core.api.*
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.game.interaction.QueueStrength
import core.game.node.entity.npc.NPC
import core.tools.END_DIALOGUE
import shared.consts.*

/**
 * Represents the Pious Pete random event dialogue.
 */
class PiousPeteDialogue : DialogueFile() {

    override fun handle(componentID: Int, buttonID: Int) {
        npc = NPC(NPCs.PIOUS_PETE_3207)
        when (stage) {
            0 -> if (CandlelightInterface.areCandlesLit(player!!)) {
                queueScript(player!!, 0, QueueStrength.NORMAL) { stage: Int ->
                    when (stage) {
                        0 -> {
                            sendNPCDialogueLines(player!!, NPCs.PIOUS_PETE_3207,FaceAnim.FRIENDLY,true, "Wonderful! All the candles are lit.")
                            return@queueScript delayScript(player!!, 4)
                        }

                        1 -> {
                            sendNPCDialogueLines(player!!, NPCs.PIOUS_PETE_3207,FaceAnim.FRIENDLY,true, "Here, I'll take you back.")
                            return@queueScript delayScript(player!!, 4)
                        }

                        2 -> {
                            sendNPCDialogueLines(player!!, NPCs.PIOUS_PETE_3207,FaceAnim.FRIENDLY,true, "Have a good day!")
                            return@queueScript delayScript(player!!, 3)
                        }
                        3 -> {
                            closeInterface(player!!)
                            CandlelightUtils.cleanup(player!!)
                            addItemOrDrop(player!!, Items.COINS_995, (100..500).random())
                            sendMessage(player!!, "You were awarded some coins!")
                            return@queueScript stopExecuting(player!!)
                        }

                        else -> return@queueScript stopExecuting(player!!)
                    }
                }
            } else {
                npcl(FaceAnim.SAD, "Brother ${player?.username}, I'm sorry to drag you away from your tasks, but I need a little help with something.").also { stage = 1 }
            }

            // Start.
            1 -> playerl(FaceAnim.THINKING, "What is it you need doing?").also { stage++ }
            2 -> npc(FaceAnim.NEUTRAL, "I need someone to help me light that row of candles. I","can't reach them myself and I keep getting dazzled by", "the light whenever I try.").also { stage++ }
            3 -> npc(FaceAnim.NEUTRAL, "You will need to light the tall candles by moving the","taper.").also { stage++ }
            4 -> npc(FaceAnim.NEUTRAL, "You do that by clicking on the button at the bottom of","the screen until the taper is in front of a tall, unlit","candle, and then hitting the light button.").also { stage++ }
            5 -> npc(FaceAnim.NEUTRAL, "There are two rows of candles, so you will need to use","the back and forward buttons too.").also { stage++ }
            6 -> npc(FaceAnim.NEUTRAL,"Just ignore the burned down candles, I will deal with","them myself later.").also { stage++ }
            7 -> player(FaceAnim.NEUTRAL,"Sounds easy enough. I'll get started.").also { stage = END_DIALOGUE }
        }
    }
}