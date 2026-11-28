package content.global.skill.summoning.pets.dialogue

import core.api.inInventory
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs

@Initializable
class SheepdogDialogue(player: Player? = null) : Dialogue(player) {

    private var branch = 0

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC

        branch = when {
            npc.id in intArrayOf(NPCs.LORE_DOG_SHEEPDOG_PUPPY_1_6966, NPCs.LORE_DOG_SHEEPDOG_PUPPY_2_7253, NPCs.LORE_DOG_SHEEPDOG_PUPPY_3_7255) -> 0
            inInventory(player, Items.BALL_OF_WOOL_1759) -> 1
            else -> (2..5).random()
        }

        stage = 0

        when (branch) {
            0 -> npc(FaceAnim.CHILD_NORMAL, "Yapyap yip yap!", "(Hello!)")
            1 -> npc(FaceAnim.CHILD_NORMAL, "Whurf. Whine woof whine whurf.", "(I say. It's not on, stealing someone else's coat.)")
            2 -> npc(FaceAnim.CHILD_NORMAL, "Whurf. Woof whurf woofwoof?", "(I say. Where are we off to?)")
            3 -> npc(FaceAnim.CHILD_NORMAL, "Whurf woof woof woof, whurfwoof?", "(Are we lost, old sport?)")
            4 -> npc(FaceAnim.CHILD_NORMAL, "Whurf, woofwoof.", "(I say, old fruit.)")
            5 -> npc(FaceAnim.CHILD_NORMAL, "Whurfwoofwhurf, woof woof?", "(What's the plan, old bean?)")
        }

        return true
    }

    override fun handle(componentID: Int, buttonID: Int): Boolean {
        when (branch) {
            0 -> when (stage) {
                0 -> { player(FaceAnim.FRIENDLY, "Hello to you, too!"); stage++ }
                1 -> { npc(FaceAnim.CHILD_NORMAL, "Yipyipyip!", "(I'm a good dog!)"); stage++ }
                2 -> { player(FaceAnim.FRIENDLY, "Yes you are. You're a very good puppy."); stage++ }
                3 -> { npc(FaceAnim.CHILD_NORMAL, "Yappypyappyyap!", "(Huzzah!)"); stage = END_DIALOGUE }
            }

            1 -> when (stage) {
                0 -> { playerl(FaceAnim.HALF_ASKING, "What are you talking about?"); stage++ }
                1 -> { npc(FaceAnim.CHILD_NORMAL, "Whurf whurf whurf whiiiine.", "(All that fur you are holding there.)"); stage++ }
                2 -> { playerl(FaceAnim.HALF_ASKING, "You mean this wool? It's from a sheep, not a sheepdog."); stage++ }
                3 -> { npc(FaceAnim.CHILD_NORMAL, "Woof, woof? Whoof woof whurf.", "(Oh, really? How terribly embarrassing.)"); stage++ }
                4 -> { playerl(FaceAnim.CHILD_THINKING, "It was an honest mistake."); stage = END_DIALOGUE }
            }

            2 -> when (stage) {
                0 -> { playerl(FaceAnim.FRIENDLY, "Well, I've got a list of jobs to get done, and they do take me all over the place."); stage++ }
                1 -> { npc(FaceAnim.CHILD_NORMAL, "Whurfwhurf, woof? Whurf!", "(Bit of a trek, then? Masterful!)"); stage = END_DIALOGUE }
            }

            3 -> when (stage) {
                0 -> { playerl(FaceAnim.FRIENDLY, "No more than we were the last time you asked."); stage++ }
                1 -> { npc(FaceAnim.CHILD_NORMAL, "Woofwoofwhurf woof.", "(Just keeping tabs on the situation.)"); stage = END_DIALOGUE }
            }

            4 -> when (stage) {
                0 -> { playerl(FaceAnim.HALF_ASKING, "Yes? What do you want?"); stage++ }
                1 -> { npc(FaceAnim.CHILD_NORMAL, "Whurfwhurf.", "(Nothing, really.)"); stage++ }
                2 -> { playerl(FaceAnim.HALF_ASKING, "Oh, okay. Well, if you want something just bark, okay?"); stage++ }
                3 -> { npc(FaceAnim.CHILD_NORMAL, "Whurf woof woof.", "(Reading you loud and clear.)"); stage = END_DIALOGUE }
            }

            5 -> when (stage) {
                0 -> { playerl(FaceAnim.ANNOYED, "I haven't decided yet. When I do, you'll be the first to know."); stage++ }
                1 -> { npc(FaceAnim.CHILD_NORMAL, "Whurf whurf!", "(Smashing!)"); stage = END_DIALOGUE }
            }
        }
        return true
    }

    override fun getIds(): IntArray = intArrayOf(
        NPCs.LORE_DOG_SHEEPDOG_PUPPY_1_6966,
        NPCs.LORE_DOG_SHEEPDOG_1_6967,
        NPCs.LORE_DOG_SHEEPDOG_PUPPY_2_7253,
        NPCs.LORE_DOG_SHEEPDOG_2_7254,
        NPCs.LORE_DOG_SHEEPDOG_PUPPY_3_7255,
        NPCs.LORE_DOG_SHEEPDOG_3_7256,
    )
}
