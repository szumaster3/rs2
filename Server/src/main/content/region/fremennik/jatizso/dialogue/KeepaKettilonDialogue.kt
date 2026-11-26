package content.region.fremennik.jatizso.dialogue

import content.data.Quests

import core.api.isQuestComplete
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs

/**
 * Represents the Keepa Kettilon dialogue.
 */
@Initializable
class KeepaKettilonDialogue(player: Player? = null) : Dialogue(player) {

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when(stage) {
            0  -> if(!isQuestComplete(player, Quests.THE_FREMENNIK_ISLES.questName)) {
                npcl(FaceAnim.FRIENDLY, "Oh, hello stranger. If you're new to town you should speak with our ruler, King Gjuki Sorvott IV. He'll want to meet you - he always wants to have a chat with any strangers in town. They could be spies you know.").also { stage++ }
            } else {
                npcl(FaceAnim.FRIENDLY, "Go away and let me wallow in self loathing.").also { stage = 14 }
            }
            1  -> player(FaceAnim.HALF_ASKING, "Spies?").also { stage++ }
            2  -> npcl(FaceAnim.FRIENDLY, "Yes, spies from Neitiznot. They're everywhere. Apparently. That's what the King says anyway.").also { stage++ }
            3  -> player(FaceAnim.HALF_ASKING, "So what do you do here?").also { stage++ }
            4  -> npcl(FaceAnim.FRIENDLY, "Me? I'm a cook. Fish dishes a speciality. I hate fish. But fish is about all we have. Except for yak. But we're not allowed to eat yak.").also { stage++ }
            5  -> player(FaceAnim.HALF_ASKING, "Why not, is yak meat bad for you?").also { stage++ }
            6  -> npcl(FaceAnim.FRIENDLY, "Certainly not! Yak meat is both sweet and delicately flavoured. Yak is juicier than buffalo and elk - but never gamey. It goes wonderfully well with a glass of red wine, and it's healthy too!").also { stage++ }
            7  -> player(FaceAnim.HALF_ASKING, "So why aren't you allowed to eat it?").also { stage++ }
            8  -> npcl(FaceAnim.FRIENDLY, "King's orders. They breed and eat yaks on Neitiznot, so WE'RE not allowed to. It's unjust I tell you! I had a dream last night about a juicy fillet of yak, served on a bed of deep-fried seaweed and topped with a garnish").also { stage++ }
            9  -> npcl(FaceAnim.FRIENDLY, "of crumbly yak's cheese.").also { stage++ }
            10 -> player(FaceAnim.NEUTRAL, "That's a little sad. So do you have any cooked food I could buy?").also { stage++ }
            11 -> npcl(FaceAnim.FRIENDLY, "No. It would only be fish anyway. Why would you want to eat fish?").also { stage++ }
            12 -> player(FaceAnim.HALF_ASKING, "Well, I wouldn't mind a few shark and swordfish steaks as back-up you know.").also { stage++ }
            13 -> npcl(FaceAnim.FRIENDLY, "If you insist. Sorry the stock is so low, but I just can't be bothered to cook any more.").also { stage = END_DIALOGUE }
            14 -> player(FaceAnim.HALF_ASKING, "Do you want any help with that? I can loath you as well.").also { stage++ }
            15 -> npcl(FaceAnim.FRIENDLY, "*sigh*").also { stage = END_DIALOGUE }
        }
        return true
    }

    override fun newInstance(player: Player?): Dialogue = KeepaKettilonDialogue(player)

    override fun getIds(): IntArray = intArrayOf(NPCs.KEEPA_KETTILON_5487)
}
