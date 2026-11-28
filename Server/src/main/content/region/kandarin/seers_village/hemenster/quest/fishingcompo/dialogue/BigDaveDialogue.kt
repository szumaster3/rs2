package content.region.kandarin.seers_village.hemenster.quest.fishingcompo.dialogue

import core.game.dialogue.Dialogue
import core.game.dialogue.Topic
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs

/**
 * Represents the Big Dave dialogue.
 *
 * # Relations
 * - [Fishing Contest][content.region.kandarin.seers_village.hemenster.quest.fishingcompo.FishingContest]
 */
@Initializable
class BigDaveDialogue(player: Player? = null) : Dialogue(player) {
    
    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        npc("Hey lad! Always nice to see a fresh face!")
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> showTopics(
                Topic("So you're the champ?", 1),
                Topic("Can I fish here instead of you?", 2),
                Topic("Do you have any tips for me?", 3),
            )
            1 -> npc("That's right, lad!", "Ain't nobody better at fishing round here", "than me! That's for sure!").also { stage = END_DIALOGUE }
            2 -> npc("Sorry lad! This is my lucky spot!").also { stage = END_DIALOGUE }
            3 -> npc("Why would I help you? I wanna stay the best!", "I'm not givin' away my secrets like", "old Grandpa Jack does!").also { stage++ }
            4 -> player("Who's Grandpa Jack?").also { stage++ }
            5 -> npc("You really have no clue do you!", "He won this competition four years in a row!", "He lives in the house just outside the gate.").also { stage = END_DIALOGUE }
        }
        return true
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.BIGDAVE_228)
}
