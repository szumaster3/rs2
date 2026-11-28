package content.global.skill.summoning.pets.dialogue

import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs

@Initializable
class RaccoonDialogue(player: Player? = null) : Dialogue(player) {

    companion object {
        private val babyRaccoon = intArrayOf(NPCs.LORE_BABY_RACCOON_1_6913, NPCs.LORE_BABY_RACCOON_2_7271, NPCs.LORE_BABY_RACCOON_3_7273)
        private val adultRaccoon = intArrayOf(NPCs.LORE_RACCOON_1_6914, NPCs.LORE_RACCOON_2_7272, NPCs.LORE_RACCOON_3_7274)
    }

    private var branch = 0

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC

        branch = when {
            npc.id in babyRaccoon -> 0
            else -> (1..3).random()
        }

        stage = 0

        when (branch) {
            0 -> npc(FaceAnim.CHILD_NORMAL, "Chitterchatterchittter chatter chatter!", "(I wanna go play, now!)")
            1 -> npc(FaceAnim.CHILD_NORMAL, "Chitter chatterchittter chitter?", "(When we gonna do somethin' fun?)")
            2 -> npc(FaceAnim.CHILD_NORMAL, "Chitterchatter chatterchitter chitter?", "(When are we gonna be done here?)")
            3 -> npc(FaceAnim.CHILD_NORMAL, "Chatter chatter chatter chatter?", "(When is we gonna go back to one of those evergreen forests?)")
            4 -> npc(FaceAnim.CHILD_NORMAL, "Chitter chatter chittterchatter chatter?", "(So where are ya takin' me today?)")
        }

        return true
    }

    override fun handle(componentID: Int, buttonID: Int): Boolean {
        when (branch) {
            0 -> when (stage) {
                0 -> { playerl(FaceAnim.FRIENDLY, "Well, why not just look at all this walking around after me as a game."); stage++ }
                1 -> { npc(FaceAnim.CHILD_NORMAL, "Chitter! Chatterchitter chitter!", "(Yay! I win at this here game!)"); stage = END_DIALOGUE }
            }

            1 -> when (stage) {
                0 -> { playerl(FaceAnim.FRIENDLY, "What do you mean 'fun'?"); stage++ }
                1 -> { npcl(FaceAnim.CHILD_NORMAL, "Chatter chatter chitter. (More fun than this. Like chasin' cats - I don't like kitties.)"); stage++ }
                2 -> { playerl(FaceAnim.FRIENDLY, "Oh, give me a minute and we'll see..."); stage++ }
                3 -> { npcl(FaceAnim.CHILD_NORMAL, "Chatter chatter chitter? (Don't you keep me waiting too long, now - ya hear?)"); stage = END_DIALOGUE }
            }

            2 -> when (stage) {
                0 -> { playerl(FaceAnim.FRIENDLY, "We'll go soon, I promise."); stage++ }
                1 -> { npcl(FaceAnim.CHILD_NORMAL, "Chatter chitter. (I like it when them other critters run with us.)"); stage++ }
                2 -> { playerl(FaceAnim.FRIENDLY, "Yeah, me to. The squirrels and the rabbits are really cute."); stage++ }
                3 -> { npc(FaceAnim.CHILD_NORMAL, "Chatter.", "(Yeah.)"); stage++ }
                4 -> { npcl(FaceAnim.CHILD_NORMAL, "Chatter chitter. (Don't ever refer to me as cute, though, " + if (player.isMale) "boy" else "girl" + ".)"); stage++ }
                5 -> { playerl(FaceAnim.FRIENDLY, "No chance. I know better than that by now."); stage = END_DIALOGUE }
            }

            3 -> when (stage) {
                0 -> { playerl(FaceAnim.FRIENDLY, "Oh, I think we'll be covering a lot of ground today."); stage++ }
                1 -> { npcl(FaceAnim.CHILD_NORMAL, "Chitter chatter chatter. (There'd better not be much walkin'. I don't like to walk much. You'd best carry me.)"); stage = END_DIALOGUE }
            }
        }
        return true
    }

    override fun getIds(): IntArray = intArrayOf(*babyRaccoon, *adultRaccoon)
}
