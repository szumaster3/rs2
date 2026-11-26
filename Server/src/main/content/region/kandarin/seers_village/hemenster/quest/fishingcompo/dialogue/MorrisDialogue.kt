package content.region.kandarin.seers_village.hemenster.quest.fishingcompo.dialogue

import content.data.Quests

import core.api.*
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Vars

/**
 * Represents the Morris dialogue.
 *
 * # Relations
 * - [Fishing Contest][content.region.kandarin.seers_village.hemenster.quest.fishingcompo.FishingContest]
 */
@Initializable
class MorrisDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        /*
         * Attempting to enter Hemenster without a fishing pass.
         */
        if (!inInventory(player, Items.FISHING_PASS_27, 1)) {
            npc("Competition pass please.")
        }
        /*
         * After Fishing contest quest.
         */
        else if(isQuestComplete(player, Quests.FISHING_CONTEST.questName) && getQuestStage(player, Quests.LAND_OF_THE_GOBLINS.questName) >= 1) {
            player("I need to catch a Hemenster whitefish.")
            stage = 8
        }
        /*
         * Attempting to enter Hemenster with a fishing pass.
         */
        else {
            player("What are you sitting around here for?")
            stage = 4
        }
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> player(FaceAnim.HALF_GUILTY, "I don't have one of them.").also { stage++ }
            1 -> npcl(FaceAnim.NEUTRAL, "Oh well. I can't let you past then.").also { stage++ }
            2 -> player("What do I need that for?").also { stage++ }
            3 -> npcl(FaceAnim.HALF_GUILTY, "This is the entrance to the Hemenster fishing competition. It's a high class competition. Invitation only.").also { stage = END_DIALOGUE }
            4 -> npc(FaceAnim.HALF_GUILTY, "I'm making sure only those with a competition pass enter", "the fishing contest.").also { stage++ }
            5 -> player(FaceAnim.HAPPY, "I have one here.").also { stage++ }
            6 -> sendItemDialogue(player!!, Items.FISHING_PASS_27, "You show Morris your pass.").also { stage++ }
            7 -> {
                end()
                npc("Move on through. Talk to Bonzo", "to enter the competition.")
                removeItem(player, Item(Items.FISHING_PASS_27, 1), Container.INVENTORY)
                setVarbit(player,  Vars.VARBIT_FISHING_CONTEST_PASS_SHOWN_2053, 1, true)
                stage = END_DIALOGUE
            }
            8 -> npcl(FaceAnim.HALF_GUILTY, "The whitefish, eh? Okay, since you've won the competition before then I'll let you in.").also { stage = END_DIALOGUE }
        }
        return true
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.MORRIS_227)
}
