package core.game.worldevents.events.christmas

import core.api.addItem
import core.api.hasSpaceFor
import core.api.inInventory
import core.api.removeItem
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.dialogue.Topic
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.item.Item

import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs

/**
 * Represents the (2007 Christmas event) Dwarf Snowman dialogue.
 */
@Initializable
class DwarfSnowmanDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        npcl(FaceAnim.FRIENDLY, "Merry Christmas! Do you want to buy some snow?")
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when(stage) {
            0 -> showTopics(
                Topic("Merry Christmas!", 7, true),
                Topic("You're selling snow?", 6),
                Topic("I want to go home.", 14)
            )
            1 -> player(FaceAnim.HAPPY, "Merry Christmas!").also { stage++ }
            2 -> npcl(FaceAnim.FRIENDLY, "I hope you get lots of exciting presents.").also { stage++ }
            3 -> player(FaceAnim.HALF_ASKING, "What is the Land of Snow?").also { stage++ }
            4 -> npcl(FaceAnim.FRIENDLY, "This is! This land produces the special magical snow that we living snowmen are made of.").also { stage++ }
            5 -> npcl(FaceAnim.FRIENDLY, "I don't agree with the Queen of Snow's idea of giving it all away, though. If we could charge people for it, that would be much better.").also { stage = 0 }

            6 -> npcl(FaceAnim.FRIENDLY, "Yes! Only five coins per snowball. Do you want to buy some?").also { stage++ }
            7 -> player(FaceAnim.THINKING, "But there's snow lying around on the ground here. I can pick it up for free!").also { stage++ }
            8 -> npcl(FaceAnim.FRIENDLY, "You could...but wouldn't you prefer to buy some?").also { stage++ }
            9 -> showTopics(
                Topic("Alright, I'll buy some.", 10),
                Topic("No, thanks.", 11),
                Topic("But what about the spirit of Christmas?", 12)
            )
            10 -> {
                end()
                val hasSpace = hasSpaceFor(player, Item(Items.SNOWBALL_10501))
                if(hasSpace && inInventory(player, Items.COINS_995)){
                    removeItem(player,Item(Items.COINS_995, 5))
                    addItem(player, Items.SNOWBALL_10501, 1)
                    npcl(FaceAnim.HAPPY, "There you go. Enjoy!")
                }
            }
            11 -> npcl(FaceAnim.FRIENDLY, "Suit yourself.").also { stage = END_DIALOGUE }
            12 -> npcl(FaceAnim.FRIENDLY, "That's right! The spirit of Christmas IS buying things!").also { stage++ }
            13 -> player(FaceAnim.ANNOYED, "No it isn't!").also { stage = END_DIALOGUE }
            14 -> npcl(FaceAnim.FRIENDLY, "Very well.").also { stage++ }
            15 -> {
                end()
                // TODO: TELEPORT
            }
        }
        return true
    }

    override fun getIds(): IntArray {
        return intArrayOf(NPCs.XMAS07_SNOWMAN_DWARF_6744)
    }


}