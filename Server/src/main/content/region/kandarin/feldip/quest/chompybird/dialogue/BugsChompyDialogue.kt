package content.region.kandarin.feldip.quest.chompybird.dialogue

import content.region.kandarin.feldip.quest.chompybird.BigChompyBirdHunting
import core.api.*
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs
import shared.consts.Quests

/**
 * Represents the Bugs dialogue.
 *
 * # Relations
 * - [Big Chompy Bird Hunting][content.region.kandarin.feldip.quest.chompybird.BigChompyBirdHunting]
 */
@Initializable
class BugsChompyDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        stage = 0
        handle(0, 0)
        return true
    }

    override fun handle(componentId: Int, buttonId: Int): Boolean {
        when (getQuestStage(player!!, Quests.BIG_CHOMPY_BIRD_HUNTING)) {
            in 0 until 20 -> {
                when (stage) {
                    0 -> npc(FaceAnim.OLD_NORMAL, "You's better talk to Dad, him chasey sneaky da chompy.").also { stage = END_DIALOGUE }
                }
            }

            in 20 until 70 -> handleBellowsDialogue()
            in 70 until 90 -> {
                val bugsIngredient = getAttribute(player!!, BigChompyBirdHunting.ATTR_ING_BUGS, -1)
                npcl(FaceAnim.OLD_NORMAL, "Dad say's you's making da chompy for us! Slurp! Me's has to have ${getItemName(bugsIngredient)} wiv mine! Chompy is our favourite yummms!").also {
                    setAttribute(player!!, BigChompyBirdHunting.ATTR_BUGS_ASKED, true)
                    stage = END_DIALOGUE
                }
            }
            100 -> npcl(FaceAnim.OLD_NORMAL, "Thanks for da chompy, it was deloverly!").also { stage = END_DIALOGUE }
            else -> npcl(FaceAnim.OLD_NORMAL, "You's better talk to Dad, him chasey sneaky da chompy.").also { stage = END_DIALOGUE }
        }
        return true
    }

    private fun handleBellowsDialogue() {
        when (stage) {
            0 -> playerl(FaceAnim.HALF_THINKING,"Rantz said that you play with the fatsy toadies, what are they?").also { stage++ }
            1 -> npcl(FaceAnim.OLD_NORMAL, "Oh, we sometimes use da blower on da toadies but Dad don't let us get in da locked box no more. He he it was good fun making da toadies fat on da swamp gas.").also { stage = END_DIALOGUE }
        }
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.BUGS_1012)
}