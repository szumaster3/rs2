package content.region.misthalin.lumbridge.dialogue

import content.data.Quests

import content.data.GameAttributes
import content.region.misthalin.lumbridge.quest.sheep.dialogue.FredTheFarmerDialogue
import core.api.getAttribute
import core.api.getQuestStage
import core.api.openDialogue
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.dialogue.IfTopic
import core.game.dialogue.Topic
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import core.tools.START_DIALOGUE
import shared.consts.NPCs

/**
 * Represents the Fred The Farmer dialogue.
 */
@Initializable
class FredTheFarmerDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        if (getQuestStage(player, Quests.SHEEP_SHEARER.questName) in 1..99) {
            openDialogue(player, FredTheFarmerDialogue(getQuestStage(player, Quests.SHEEP_SHEARER.questName)), npc)
        } else {
            npc(FaceAnim.ANGRY, "What are you doing on my land? You're not the one", "who keeps leaving all my gates open and letting out all", "my sheep are you?").also { stage = START_DIALOGUE }
        }
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            START_DIALOGUE -> showTopics(
                IfTopic(FaceAnim.NEUTRAL, "I'm looking for a quest.", 1000, getQuestStage(player!!, Quests.SHEEP_SHEARER.questName) == 0),
                Topic(FaceAnim.HALF_GUILTY, "I'm looking for something to kill.", 100),
                Topic(FaceAnim.HALF_GUILTY, "I'm lost.", 200),
                IfTopic(FaceAnim.FRIENDLY, "Fred! Fred! I've seen 'The Thing'!", 300, getAttribute(player,GameAttributes.FRED_SEEN_THE_THING, false))
            )
            100 -> npc(FaceAnim.HALF_GUILTY, "What, on my land? Leave my livestock alone you", "scoundrel!").also { stage = END_DIALOGUE }
            200 -> npc(FaceAnim.HALF_GUILTY, "How can you be lost? Just follow the road east and", "south. You'll end up in Lumbridge fairly quickly.").also { stage = END_DIALOGUE }
            300 -> npcl(FaceAnim.EXTREMELY_SHOCKED, "You... you actually saw it? Run for the hills! Player, grab as many chickens as you can! We have to...").also { stage++ }
            301 -> player(FaceAnim.HALF_ASKING, "Fred!").also { stage++ }
            302 -> npcl(FaceAnim.HALF_THINKING, "...flee! Oh, woe is me! The shape-shifter is coming! We're all...").also { stage++ }
            303 -> player(FaceAnim.ASKING, "Fred!").also { stage++ }
            304 -> npcl(FaceAnim.THINKING, "...doomed. What?").also { stage++ }
            305 -> player(FaceAnim.HALF_GUILTY, "It's not a shape-shifter or any other kind of monster.").also { stage++ }
            306 -> npc(FaceAnim.ASKING, "Well, what is it, boy?").also { stage++ }
            307 -> player(FaceAnim.HALF_GUILTY, "Well, it's just two penguins... disguised as a sheep.").also { stage++ }
            308 -> npc(FaceAnim.CALM, "...").also { stage++ }
            309 -> npc(FaceAnim.HALF_ASKING, "Have you been out in the sun too long?").also { stage = END_DIALOGUE }
            1000 -> openDialogue(player, FredTheFarmerDialogue(getQuestStage(player, Quests.SHEEP_SHEARER.questName)), npc)
        }
        return true
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.FRED_THE_FARMER_758)
}
