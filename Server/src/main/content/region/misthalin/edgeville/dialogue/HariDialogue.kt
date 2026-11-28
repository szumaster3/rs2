package content.region.misthalin.edgeville.dialogue

import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.dialogue.Topic
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs

/**
 * Represents the Hari dialogue.
 */
@Initializable
class HariDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        player("Hello there.")
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> npc("Hello.").also { stage++ }
            1 -> showTopics(
                Topic("Who are you?", 2, true),
                Topic("Can you teach me about Canoeing?", 12, false)
            )
            2 -> player(FaceAnim.HALF_ASKING, "Who are you?").also { stage++ }
            3 -> npcl(FaceAnim.FRIENDLY, "My name is Hari.").also { stage++ }
            4 -> player(FaceAnim.HALF_ASKING, "And what are you doing here Hari?").also { stage++ }
            5 -> npcl(FaceAnim.FRIENDLY, "Like most people who come to Edgeville, I am here to seek adventure in the Wilderness.").also { stage++ }
            6 -> npcl(FaceAnim.FRIENDLY, "I found a secret underground river that will take me quite a long way north.").also { stage++ }
            7 -> player(FaceAnim.HALF_ASKING, "Underground river? Where does it come out?").also { stage++ }
            8 -> npcl(FaceAnim.FRIENDLY, "It comes out in a pond located deep in the Wilderness.").also { stage++ }
            9 -> npcl(FaceAnim.FRIENDLY, "I had to find a very special type of canoe to get me up the river though, would you like to know more?").also { stage++ }
            10 -> options("Yes", "No").also { stage++ }
            11 -> when(buttonId) {
                1 -> player("Could you teach me about canoes?").also { stage++ }
                2 -> player("No thanks, not right now.").also { stage = END_DIALOGUE }
            }

            12 -> when (player.skills.getLevel(Skills.WOODCUTTING)) {
                in 1..11 -> {
                    npcl(FaceAnim.NEUTRAL, "Well, you don't look like you have the skill to make a canoe.").also { stage++ }
                }
                in 12..26 -> {
                    npcl(FaceAnim.FRIENDLY, "I can sense you're still a novice woodcutter, you will only be able to make a log canoe at present.").also { stage = 14 }
                }
                in 27..41 -> {
                    npcl(FaceAnim.FRIENDLY, "You are an average woodcutter. You should be able to make a Dugout canoe quite easily. It will take you 2 stops along the river.").also { stage = 16 }
                }
                in 42..56 -> {
                    npcl(FaceAnim.FRIENDLY, "You seem to be an accomplished woodcutter. You will easily be able to make a Stable Dugout.").also { stage = 19 }
                }
                in 57..99 -> {
                    npcl(FaceAnim.FRIENDLY, "Your skills rival mine friend. You will certainly be able to build a Waka.").also { stage = 22 }
                }
                else -> npcl(FaceAnim.FRIENDLY, "It's really quite simple. Just walk down to that tree on the bank and chop it down.").also { stage = END_DIALOGUE }
            }

            // 1-11 Woodcutting followup
            13 -> npcl(FaceAnim.FRIENDLY, "You need to have at least level 12 woodcutting. Once you are able to make a canoe it makes travel along the river much quicker!").also { stage = END_DIALOGUE }

            // 12-26 Woodcutting followup
            14 -> player(FaceAnim.HALF_ASKING, "Is that good?").also { stage++ }
            15 -> npcl(FaceAnim.FRIENDLY, "A log will take you one stop along the river. But you won't be able to travel into the Wilderness on it.").also { stage = END_DIALOGUE }

            // 27-41 Woodcutting followup
            16 -> player(FaceAnim.HALF_ASKING, "Can I take a dugout canoe to reach the Wilderness?").also { stage++ }
            17 -> npcl(FaceAnim.FRIENDLY, "You would never make it there alive.").also { stage++ }
            18 -> player(FaceAnim.HALF_ASKING, "Best not to try then.").also { stage = END_DIALOGUE }

            // 42-56 Woodcutting followup
            19 -> npcl(FaceAnim.FRIENDLY, "They are reliable enough to get you anywhere on this river, except to the Wilderness of course. Only a Waka can take you there.").also { stage++ }
            20 -> player(FaceAnim.HALF_ASKING, "A Waka? What's that?").also { stage++ }
            21 -> npcl(FaceAnim.FRIENDLY, "Come and ask me when you have improved your skills as a woodcutter.").also { stage = END_DIALOGUE }

            // 57-99 Woodcutting followup
            22 -> player(FaceAnim.HALF_ASKING, "A Waka? What's that?").also { stage++ }
            23 -> npcl(FaceAnim.FRIENDLY, "A Waka is an invention of my people, it's an incredible strong and fast canoe and will carry you safely to any destination on the river.").also { stage++ }
            24 -> player(FaceAnim.HALF_ASKING, "Any destination?").also { stage++ }
            25 -> npcl(FaceAnim.FRIENDLY, "Yes, you can take a waka north through the underground portion of this river. It will bring you out at a pond in the heart of the Wilderness. Be careful up there, many have lost more than their lives in that dark and twisted place.").also { stage = END_DIALOGUE }
        }
        return true
    }

    override fun newInstance(player: Player?): Dialogue = HariDialogue(player)

    override fun getIds(): IntArray = intArrayOf(NPCs.CANOEING_HARI_3330)
}
