package content.region.kandarin.seers_village.hemenster.quest.fishingcompo.dialogue

import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.dialogue.Topic
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE

/**
 * Represents the Sinister Stranger dialogue.
 *
 * ```
 * NPC Wrapper ID: 226
 * Varbit ID: 2054
 * set values:
 *   0 = NPC_ID: 3677
 *   1 = NPC_ID: 3678
 * ```
 *
 * # Relations
 * - [Fishing Contest][content.region.kandarin.seers_village.hemenster.quest.fishingcompo.FishingContest]
 */
@Initializable
class SinisterStrangerDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        npc(FaceAnim.NEUTRAL,"...")
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> showTopics(
                Topic(FaceAnim.NEUTRAL,"...?",5),
                Topic(FaceAnim.HALF_ASKING,"Who are you?",8),
                Topic(FaceAnim.HALF_THINKING,"So... you like fishing?",10),
            )
            2 -> showTopics(
                Topic(FaceAnim.HALF_ASKING,"You're a vampire aren't you?",9),
                Topic(FaceAnim.HALF_ASKING,"Is it nice there?",13),
                Topic("So... you like fishing?",10)
            )
            3 -> showTopics(
                Topic(FaceAnim.HALF_ASKING,"You're a vampire aren't you?",9),
                Topic(FaceAnim.HALF_ASKING,"So you like fishing?",10),
                Topic(FaceAnim.HAPPY,"Well, good luck with the fishing.",14)
            )
            4 -> showTopics(
                Topic(FaceAnim.HALF_ASKING,"You're a vampire aren't you?",9),
                Topic(FaceAnim.FRIENDLY,"If you get thirsty you should drink something.",12),
                Topic(FaceAnim.HAPPY,"Well, good luck with the fishing.",14)
            )
            5 -> npc(FaceAnim.NEUTRAL,"...").also { stage++ }
            6 -> player(FaceAnim.NEUTRAL,"......?").also { stage++ }
            7 -> npc(FaceAnim.NEUTRAL,"......").also { stage = END_DIALOGUE }
            8 -> npc(FaceAnim.FRIENDLY,"My name is Vlad.", "I come from far avay,", "vere the sun iz not so bright.").also { stage = 2 }
            9 -> npc(FaceAnim.FRIENDLY,"Just because I can't stand ze smell ov garlic", "and I don't like bright sunlight doesn't", "necessarily mean I'm ein vampire!").also { stage = END_DIALOGUE }
            10 -> npc(FaceAnim.FRIENDLY,"My doctor told me to take up ein velaxing hobby.", "Vhen I am stressed I tend to get ein little...").also { stage++ }
            11 -> npc(FaceAnim.NEUTRAL,"...thirsty.").also { stage = 4 }
            12 -> npc(FaceAnim.NEUTRAL,"I tsink I may do zat soon...").also { stage = END_DIALOGUE }
            13 -> npc(FaceAnim.HAPPY,"It is vonderful!", "Zev omen are beautiful und ze nights are long!").also { stage = 3 }
            14 -> npc(FaceAnim.JOLLY,"Luck haz notsing to do vith it.", "It is all in ze technique.").also { stage = END_DIALOGUE }

        }
        return true
    }

    override fun getIds(): IntArray = intArrayOf(226)
    // NPCs.SINISTER_STRANGER0_3677, NPCs.SINISTER_STRANGER1_3678
}
