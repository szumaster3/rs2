package content.region.fremennik.quest.olaf.dialogue

import content.data.GameAttributes
import core.api.*
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Quests
import shared.consts.Vars

/**
 * Represents the Volf Olafson dialogue.
 */
@Initializable
class VolfOlafsonDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg objects: Any?): Boolean {
        npc = objects[0] as NPC
        if (!isQuestComplete(player, Quests.THE_FREMENNIK_TRIALS)) {
            npc(FaceAnim.ANNOYED, "Sorry, outlander, but I have things to be doing.")
            return false
        }

        val progress = getVarbit(player, Vars.VARBIT_QUEST_OLAFS_QUEST_PROGRESS_3534)
        val delivered = getAttribute(player, GameAttributes.OLAF_VOLF_DELIVERED, false)

        when {
            isQuestComplete(player, Quests.OLAFS_QUEST) || progress >= 3 || delivered -> {
                npcl(FaceAnim.ASKING, "Hello again, friend! Does my father send any word... or treasures like before?")
                stage = 12
            }
            getQuestStage(player, Quests.OLAFS_QUEST) >= 2 -> {
                player("Volf? Son of Olaf Hradson?")
                stage = 1
            }
            else -> {
                npc(FaceAnim.FRIENDLY, "Hello there. Enjoying the view?")
                stage = 10
            }
        }
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            1 -> npc(FaceAnim.HALF_THINKING, "Yes? Do I know you?").also { stage++ }
            2 -> player("No, but I know your father. He sent me with a","present for you from, err...").also { stage++ }
            3 -> player("From the Island of Obscurity.").also { stage++ }
            4 -> npc(FaceAnim.FRIENDLY, "Oh! That was fast! He only set off recently, so I was", "wondering how he was doing. So, where is this gift?").also { stage++ }
            5 -> if(removeItem(player, Items.CRUDER_CARVING_11033)) {
                player("Right here. Enjoy!")
                stage++
            } else {
                playerl(FaceAnim.HALF_GUILTY, "Well, I'm sure it's around here somewhere. I'll get back to you later when I find where I left it.").also { stage = END_DIALOGUE }
            }
            6 -> npc(FaceAnim.HALF_ASKING, "What is it?").also { stage++ }
            7 -> player("Very valuable. Have a nice time!").also { stage++ }
            8 -> {
                npc(FaceAnim.FRIENDLY, "Wait! Here, take this. I was going to have it for my", "lunch, but I suppose you can have it in payment.")
                addItemOrDrop(player, Items.SHARK_385)
                val progress = getVarbit(player, Vars.VARBIT_QUEST_OLAFS_QUEST_PROGRESS_3534)
                if (progress == 2) {
                    setVarbit(player, Vars.VARBIT_QUEST_OLAFS_QUEST_PROGRESS_3534, 3, true)
                } else if (progress < 2) {
                    setAttribute(player, GameAttributes.OLAF_VOLF_DELIVERED_SAVE, true)
                }
                stage++
            }
            9 -> player("Thanks! Enjoy your new...thing!").also { stage = END_DIALOGUE }

            10 -> player(FaceAnim.FRIENDLY, "Yes I am. You have a lovely yurt.").also { stage++ }
            11 -> npcl(FaceAnim.FRIENDLY,"Thanks! I exercise it regularly.").also { stage = END_DIALOGUE }
            12 -> npcl(FaceAnim.NEUTRAL, "Not today, but if he does, you will be the first to know.").also { stage = END_DIALOGUE }
        }

        return true
    }

    override fun getIds(): IntArray = intArrayOf(
        NPCs.VOLF_OLAFSON_3695
    )
}