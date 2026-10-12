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
 * Represents the Fycie dialogue.
 *
 * # Relations
 * - [Big Chompy Bird Hunting][content.region.kandarin.feldip.quest.chompybird.BigChompyBirdHunting]
 */
@Initializable
class FycieChompyDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        stage = 0
        handle(0, 0)
        return true
    }

    override fun handle(componentId: Int, buttonId: Int): Boolean {
        when (getQuestStage(player!!, Quests.BIG_CHOMPY_BIRD_HUNTING)) {
            in 0 until 70 -> {
                when (stage) {
                    0 -> npcl(FaceAnim.OLD_NORMAL, "You's better talk to Dad, We not talk to wierdly 'umans.").also { stage = END_DIALOGUE }
                }
            }

            in 70 until 90 -> {
                val fycieIngredient = getAttribute(player!!, BigChompyBirdHunting.ATTR_ING_FYCIE, -1)

                npcl(FaceAnim.OLD_NORMAL, "Dad say's you's roasting da chompy for us! Slurp! Me's wants ${getItemName(fycieIngredient)} wiv mine! Yummy can't wait to eats it.").also {
                    setAttribute(player!!, BigChompyBirdHunting.ATTR_FYCIE_ASKED, true)
                    stage = END_DIALOGUE
                }
            }
            100 -> npcl(FaceAnim.OLD_NORMAL, "Thanks for da chompy, it was deloverly!").also { stage = END_DIALOGUE }

            else -> {
                npcl(FaceAnim.OLD_NORMAL, "You's better talk to Dad, We not talk to wierdly 'umans.").also { stage = END_DIALOGUE }
            }
        }
        return true
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.FYCIE_1011)
}