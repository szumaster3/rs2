package content.global.skill.summoning.pets.dialogue

import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs

@Initializable
class SquirrelDialogue(player: Player? = null) : Dialogue(player) {

    private val babySquirrel = intArrayOf(
        NPCs.LORE_BABY_SQUIRREL_1_6919,
        NPCs.LORE_BABY_SQUIRREL_2_7301,
        NPCs.LORE_BABY_SQUIRREL_3_7303,
        NPCs.LORE_BABY_SQUIRREL_4_7305,
        NPCs.LORE_BABY_SQUIRREL_5_7307
    )

    private val adultSquirrel = intArrayOf(
        NPCs.LORE_SQUIRREL_1_6920,
        NPCs.LORE_SQUIRREL_2_7302,
        NPCs.LORE_SQUIRREL_3_7304,
        NPCs.LORE_SQUIRREL_4_7306,
        NPCs.LORE_SQUIRREL_5_7308
    )

    private var branch = 0

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC

        branch = if (npc.id in babySquirrel) 0 else (1..4).random()
        stage = 0

        when (branch) {
            0 -> npcl(FaceAnim.CHILD_NORMAL, "Throw a ball for me!")
            1 -> npcl(FaceAnim.CHILD_NORMAL, "Gimme a nut!")
            2 -> npcl(FaceAnim.CHILD_NORMAL, "Stop doing that and play with me!")
            3 -> npcl(FaceAnim.CHILD_NORMAL, "This is boring, take me someplace fun.")
            4 -> npcl(FaceAnim.CHILD_NORMAL, "Is it nearly nut time?")
        }

        return true
    }

    override fun handle(componentID: Int, buttonID: Int): Boolean {
        when (branch) {
            0 -> when (stage) {
                0 -> { playerl(FaceAnim.FRIENDLY, "Are you part-dog or something?"); stage++ }
                1 -> { npcl(FaceAnim.CHILD_NORMAL, "What's a dog?"); stage++ }
                2 -> { playerl(FaceAnim.FRIENDLY, "It's another kind of animal. Squirrels are more famous for eating nuts than chasing balls."); stage++ }
                3 -> { npcl(FaceAnim.CHILD_NORMAL, "Give me a nut, then!"); stage++ }
                4 -> { playerl(FaceAnim.FRIENDLY, "I walked into that one, didn't I?"); stage = END_DIALOGUE }
            }

            1 -> when (stage) {
                0 -> { playerl(FaceAnim.FRIENDLY, "Not just now, I wouldn't want to spoil your dinner with one."); stage++ }
                1 -> { npcl(FaceAnim.CHILD_NORMAL, "Awww..."); stage = END_DIALOGUE }
            }

            2 -> when (stage) {
                0 -> { playerl(FaceAnim.FRIENDLY, "Okay, but just for a minute, I'm busy."); stage++ }
                1 -> { npcl(FaceAnim.CHILD_NORMAL, "Yay!"); stage = END_DIALOGUE }
            }

            3 -> when (stage) {
                0 -> { playerl(FaceAnim.FRIENDLY, "I'll be done in a moment, and you can play here if you want."); stage++ }
                1 -> { npcl(FaceAnim.CHILD_NORMAL, "Huzzah!"); stage = END_DIALOGUE }
            }

            4 -> when (stage) {
                0 -> { playerl(FaceAnim.FRIENDLY, "Didn't you just ask me that a little while ago?"); stage++ }
                1 -> { npcl(FaceAnim.CHILD_NORMAL, "Maybe."); stage++ }
                2 -> { playerl(FaceAnim.FRIENDLY, "Then whatever answer I gave still applies now."); stage = END_DIALOGUE }
            }
        }
        return true
    }

    override fun getIds(): IntArray = intArrayOf(*babySquirrel, *adultSquirrel)
}
