package content.region.misthalin.lumbridge.dialogue

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
 * Represents the Barfy Bill dialogue.
 */
@Initializable
class BarfyBillDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        player("Hello there.")
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> npc("Oh! Hello there.").also { stage++ }
            1 -> showTopics(
                Topic("Who are you?", 2, true),
                Topic("Can you teach me about Canoeing?", 16, false)
            )
            2  -> player(FaceAnim.HALF_ASKING, "Who are you?").also { stage++ }
            3  -> npcl(FaceAnim.FRIENDLY, "My name is Ex Sea Captain Barfy Bill.").also { stage++ }
            4  -> player(FaceAnim.HALF_ASKING, "Ex sea captain?").also { stage++ }
            5  -> npcl(FaceAnim.FRIENDLY, "Yeah, I bought a lovely ship and was planning to make a fortune running her as a merchant vessel.").also { stage++ }
            6  -> player(FaceAnim.HALF_ASKING, "Why are you not still sailing?").also { stage++ }
            7  -> npcl(FaceAnim.FRIENDLY, "Chronic sea sickness. My first, and only, voyage was spent dry heaving over the rails.").also { stage++ }
            8  -> npcl(FaceAnim.FRIENDLY, "If I had known about the sea sickness I could have saved myself a lot of money.").also { stage++ }
            9  -> player(FaceAnim.HALF_ASKING, "What are you up to now then?").also { stage++ }
            10 -> npcl(FaceAnim.FRIENDLY, "Well my ship had a little fire related problem. Fortunately it was well insured.").also { stage++ }
            11 -> npcl(FaceAnim.FRIENDLY, "Anyway, I don't have to work anymore so I've taken to canoeing on the river.").also { stage++ }
            12 -> npcl(FaceAnim.FRIENDLY, "I don't get river sick!").also { stage++ }
            13 -> npcl(FaceAnim.FRIENDLY, "Would you like to know how to make a canoe?").also { stage++ }
            14 -> options("Yes", "No").also { stage++ }
            15 -> when(buttonId) {
                1 -> player("Could you teach me about canoes?").also { stage++ }
                2 -> player(" No thanks, not right now.").also { stage = END_DIALOGUE }
            }
            16 -> when(player.skills.getLevel(Skills.WOODCUTTING)){
                in 57..99 -> npcl(FaceAnim.FRIENDLY, "Hoo! You look like you know which end of an axe is which!").also { stage++ }
                in 42..56 -> npcl(FaceAnim.FRIENDLY, "The best canoe you can make is a Stable Dugout, one step beyond a normal Dugout.").also { stage = 20 }
                in 27..41 -> npcl(FaceAnim.FRIENDLY, "With your skill in woodcutting you could make my favourite canoe, the Dugout. They might not be the best canoe on the river, but they get you where you're going.").also { stage = 24 }
                in 12..26 -> npcl(FaceAnim.FRIENDLY, "Hah! I can tell just by looking that you lack talent in woodcutting.").also { stage = 26 }
                else -> npcl(FaceAnim.FRIENDLY, "It's really quite simple. Just walk down to that tree on the bank and chop it down.").also { stage = 29 }
            }
            17 -> npcl(FaceAnim.FRIENDLY, "You can easily build one of those Wakas. Be careful if you travel into the Wilderness though.").also { stage++ }
            18 -> npcl(FaceAnim.FRIENDLY, "I've heard tell of great evil in that blasted wasteland.").also { stage++ }
            19 -> player("Thanks for the warning Bill.").also { stage = END_DIALOGUE }
            20 -> npcl(FaceAnim.FRIENDLY, "With a Stable Dugout you can travel to any place on the river.").also { stage++ }
            21 -> player(FaceAnim.HALF_ASKING, "Even into the Wilderness?").also { stage++ }
            22 -> npcl(FaceAnim.FRIENDLY, "Not likely! I've heard tell of a man up near Edgeville who claims he can use a Waka to get up into the Wilderness.").also { stage++ }
            23 -> npcl(FaceAnim.FRIENDLY, "I can't think why anyone would wish to venture into that hellish landscape though.").also { stage = END_DIALOGUE }
            24 -> playerl(FaceAnim.HALF_ASKING, "How far will I be able to go in a Dugout canoe?").also { stage++ }
            25 -> npcl(FaceAnim.FRIENDLY, "You will be able to travel 2 stops on the river.").also { stage = END_DIALOGUE }
            26 -> playerl(FaceAnim.HALF_ASKING, "What do you mean?").also { stage++ }
            27 -> npcl(FaceAnim.FRIENDLY, "No Callouses! No Splinters! No camp fires littering the trail behind you.").also { stage++ }
            28 -> npcl(FaceAnim.FRIENDLY, "Anyway, the only 'canoe' you can make is a log. You'll be able to travel 1 stop along the river with a log canoe.").also { stage = END_DIALOGUE }
            29 -> npcl(FaceAnim.FRIENDLY, "When you have done that you can shape the log further with your axe to make a canoe.").also { stage = END_DIALOGUE }
        }
        return true
    }

    override fun newInstance(player: Player?): Dialogue = BarfyBillDialogue(player)

    override fun getIds(): IntArray = intArrayOf(NPCs.CANOEING_BILL_3331)
}
