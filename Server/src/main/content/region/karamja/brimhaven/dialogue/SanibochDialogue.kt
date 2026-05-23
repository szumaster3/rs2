package content.region.karamja.brimhaven.dialogue

import core.api.*
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs

@Initializable
class SanibochDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        stage = if (args.size > 1) args[1] as Int else 0
        if (stage == 0) {
            sendNPCDialogue(player, NPCs.SANIBOCH_1595, "Good day to you bwana.", FaceAnim.HALF_GUILTY)
        } else {
            handle(0, 0)
        }
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> options("Can I go through that door please?", "Where does this strange entrance lead?", "Good day to you too.", "I'm impressed, that tree  is growing on that shed.").also { stage++ }
            1 -> when (buttonId) {
                1 -> player(FaceAnim.HALF_ASKING,"Can I go through that door please?").also { stage = 10 }
                2 -> player(FaceAnim.HALF_ASKING,"Where does this strange entrance lead?").also { stage = 20 }
                3 -> player(FaceAnim.NEUTRAL, "Good day to you too.").also { stage = END_DIALOGUE }
                4 -> player(FaceAnim.NEUTRAL, "I'm impressed, that tree  is growing on that shed.").also { stage = 40 }
            }
            10 -> {
                if (getAttribute(player, "saniboch:paid", false)) {
                    npc(FaceAnim.NEUTRAL, "Most certainly, you have already given me lots of nice", "coins.")
                    stage = END_DIALOGUE
                } else {
                    npc(FaceAnim.FRIENDLY, "Most certainly, but I must charge you the sum of 875", "coins first.").also { stage = 11 }
                }
            }

            11 -> {
                if (!inInventory(player, Items.COINS_995, 875)) {
                    player(FaceAnim.HALF_GUILTY, "I don't have the money on me at the moment.")
                    return true
                } else {
                    options("Ok, here's 875 coins.", "Never mind.", "Why is it worth the entry cost?").also { stage = 12 }
                }
            }
            12 -> when (buttonId) {
                1 -> player(FaceAnim.NEUTRAL, "Ok, here's 875 coins.").also { stage = 32 }
                2 -> player(FaceAnim.HALF_GUILTY, "Never mind.").also { stage = END_DIALOGUE }
                3 -> player(FaceAnim.HALF_ASKING,"Why is it worth the entry cost?").also { stage = 30 }
            }
            20 -> npc(FaceAnim.NEUTRAL, "To a huge fearsome dungeon, populated by giants and", "strange dogs. Adventurers come from all around to", "explore its depths.").also { stage = 21 }
            21 -> npc(FaceAnim.NEUTRAL, "I know not what lies deeper in myself, for my skills in", "agility and woodcutting are inadequate.").also { stage = END_DIALOGUE }
            30 -> npc(FaceAnim.NEUTRAL, "It leads to a huge fearsome dungeon, populated by", "giants and strange dogs. Adventurers come from all", "around to explore its depths.").also { stage++ }
            31 -> npc(FaceAnim.NEUTRAL, "I know not what lies deeper in myself, for my skills in", "agility and woodcutting are inadequate, but I hear tell", "of even greater dangers deeper in.").also { stage = END_DIALOGUE }
            32 -> {
                if (removeItem(player, Item(Items.COINS_995, 875))) {
                    sendMessage(player, "You pay Saniboch 875 coins.")
                    setAttribute(player, "saniboch:paid", true)
                    sendItemDialogue(player, Items.COINS_6964, "You give Saniboch 875 coins.")
                    stage = 33
                } else {
                    sendMessage(player, "You don't have enough coins.")
                    stage = END_DIALOGUE
                }
            }

            33 -> npc(FaceAnim.FRIENDLY, "Many thanks. You may now pass the door. May your", "death be a glorious one!").also { stage = END_DIALOGUE }
            35 -> {
                if (getAttribute(player, "saniboch:paid", false)) {
                    end()
                    npc("You have already given me lots of nice coins, you may", "go in.")
                }
                if (!inInventory(player, Items.COINS_995, 875)) {
                    player("I don't have the money on me at the moment.")
                    stage = 36
                }
                stage = 12
                handle(interfaceId, 1)
            }
            36 -> npc(FaceAnim.NEUTRAL, "Well this is a dungeon for the more wealthy discerning", "adventurer, be gone with your riff raff.").also { stage++ }
            37 -> player(FaceAnim.NEUTRAL, "But you don't even have clothes, how can you seriously", "call anyone riff raff.").also { stage++ }
            38 -> npc(FaceAnim.NEUTRAL, "Hummph.").also { stage = END_DIALOGUE }
            40 -> npc(FaceAnim.NEUTRAL, "My employer tells me it is an uncommon sort of tree", "called the Fyburglars tree.").also { stage = END_DIALOGUE }
        }
        return true
    }

    override fun newInstance(player: Player?): Dialogue = SanibochDialogue(player)

    override fun getIds(): IntArray = intArrayOf(NPCs.SANIBOCH_1595)
}
