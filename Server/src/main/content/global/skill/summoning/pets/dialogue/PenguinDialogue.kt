package content.global.skill.summoning.pets.dialogue

import core.api.getRegionBorders
import core.api.inBorders
import core.api.inZone
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.NPCs

@Initializable
class PenguinDialogue(player: Player? = null) : Dialogue(player) {

    companion object{
        private val babyPenguin = intArrayOf(NPCs.LORE_BABY_PENGUIN_1_6908, NPCs.LORE_BABY_PENGUIN_2_7313, NPCs.LORE_BABY_PENGUIN_3_7316)
        private val adultPenguin = intArrayOf(NPCs.LORE_PENGUIN_1_6909, NPCs.LORE_PENGUIN_SLIDE_1_6910, NPCs.LORE_PENGUIN_2_7314, NPCs.LORE_PENGUIN_SLIDE_2_7315, NPCs.LORE_PENGUIN_3_7317, NPCs.LORE_PENGUIN_SLIDE_3_7318)
    }

    private var branch = 0

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        branch = (0..3).random()

        stage = 0

        when (branch) {
            0 -> npc(FaceAnim.CHILD_NORMAL, "Awk Awk!", "(This is fun!)")
            1 -> npc(FaceAnim.CHILD_NORMAL, "Awkawk awk awk.", "(Dis place is rather dull.)")
            2 -> npc(FaceAnim.CHILD_NORMAL, "Awwk...", "(I'm much too warm here...)")
            3 -> if (inZone(player, "snow") || inBorders(player, getRegionBorders(11830)) || inBorders(player, getRegionBorders(11318))) {
                npc(FaceAnim.CHILD_NORMAL, "Awk!", "(Slide!)")
            } else {
                npc(FaceAnim.CHILD_NORMAL, "Awwwwwwwwk?", "(Are we gonna be here much longer?)")
            }
        }

        return true
    }

    override fun handle(componentID: Int, buttonID: Int): Boolean {
        when (branch) {
            0 -> when (stage) {
                0 -> { playerl(FaceAnim.FRIENDLY, "What's fun?"); stage++ }
                1 -> { npc(FaceAnim.CHILD_NORMAL, "Awk!", "(This!)"); stage++ }
                2 -> { playerl(FaceAnim.FRIENDLY, "I'm glad someone's enjoying themself..."); stage = END_DIALOGUE }
            }

            1 -> when (stage) {
                0 -> { playerl(FaceAnim.FRIENDLY, "It'll liven up in a while, I'm sure."); stage++ }
                1 -> { npc(FaceAnim.CHILD_NORMAL, "Awkawk awk awk!", "(I know, let's go visit the zoo!)"); stage++ }
                2 -> { playerl(FaceAnim.FRIENDLY, "Hmm, I dunno, the penguins there are a bit shifty. You shouldn't mix with them."); stage = END_DIALOGUE }
            }

            2 -> when (stage) {
                0 -> { playerl(FaceAnim.FRIENDLY, "Well, just give me a little while longer and maybe we'll go find some ice for you."); stage++ }
                1 -> { npc(FaceAnim.CHILD_NORMAL, "Awk! Awkawk awk awk. (Yay! Don't be too long.)"); stage = END_DIALOGUE }
            }

            3 -> when (stage) {
                0 -> { playerl(FaceAnim.FRIENDLY, "Why? Is there some place else where you'd rather be?"); stage++ }
                1 -> { npc(FaceAnim.CHILD_NORMAL, "Awk awk awk!", "(For sure. Somewhere colder! Like the Motherland...)"); stage = END_DIALOGUE }
            }

            4 -> when (stage) {
                0 -> { playerl(FaceAnim.FRIENDLY, "You want me to slide?"); stage++ }
                1 -> { npc(FaceAnim.CHILD_NORMAL, "Awwwwwwwwwwwwk!", "(Sliiiiiiide!)"); stage++ }
                2 -> { playerl(FaceAnim.FRIENDLY, "I can't; I don't know how. You have fun with it though."); stage++ }
                3 -> { npc(FaceAnim.CHILD_NORMAL, "Awk!", "(Slide!)"); stage = END_DIALOGUE }
            }
        }
        return true
    }

    override fun getIds(): IntArray = intArrayOf(*babyPenguin, *adultPenguin)
}
