package content.region.morytania.port_phasmatys.dialogue

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

/**
 * Represents the Metarialus dialogue.
 */
@Initializable
class MetarialusDialogue(player: Player? = null) : Dialogue(player) {

    private var hasGhostSpeak = false

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        hasGhostSpeak = inEquipment(player, Items.GHOSTSPEAK_AMULET_552) || inEquipment(player, Items.GHOSTSPEAK_AMULET_4250)

        if (!hasGhostSpeak) {
            npc("Woooo wooo wooooo woooo")
            stage = 0
            return true
        }

        player(FaceAnim.HAPPY, "Hello, I wonder if you could help me on this whole brewing thing...")
        npc(FaceAnim.NEUTRAL, "I might be able to - what do you need to know?")
        stage = 1
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> sendDialogue(player, "You cannot understand the ghost.").also { stage = END_DIALOGUE }
            1 -> options("How do I brew ales?", "How do I brew cider?", "What do I do once my ale has matured?", "Do you have any spare ale yeast?", "That's all I need to know, thanks.").also { stage++ }
            2 -> when (buttonId) {
                1 -> {
                    player(FaceAnim.HALF_ASKING, "How do I brew ales?")
                    stage = 10
                }
                2 -> {
                    player(FaceAnim.HALF_ASKING, "How do I brew cider?")
                    stage = 20
                }
                3 -> {
                    player(FaceAnim.HALF_ASKING, "What do I do once my ale has matured?")
                    stage = 30
                }
                4 -> {
                    player(FaceAnim.HALF_ASKING, "Do you have any spare ale yeast?")
                    stage = 40
                }
                5 -> {
                    player(FaceAnim.HAPPY, "That's all I need to know, thanks.")
                    stage = END_DIALOGUE
                }
            }

            10 -> {
                npc(FaceAnim.NEUTRAL, "Well first off you need to fill the vat with water - two bucketfuls should do the trick. Then you'll need two handfuls of barley malt.")
                stage++
            }
            11 -> {
                npc(FaceAnim.NEUTRAL, "After that you add your main ingredient which decides the ale type. There are recipes around for guidance.")
                stage++
            }
            12 -> {
                npc(FaceAnim.NEUTRAL, "Lastly add ale yeast and wait for fermentation.")
                stage = 44
            }

            20 -> {
                npc(FaceAnim.NEUTRAL, "You need apples. Crush them using a cider press - four apples make a bucket of mush.")
                stage++
            }
            21 -> {
                npc(FaceAnim.NEUTRAL, "Fill the vat with four buckets of mush and add ale yeast. Then wait.")
                stage = 44
            }

            30 -> {
                npc(FaceAnim.NEUTRAL, "When the vat is ready, turn the valve and it fills eight pints into an empty barrel.")
                stage = 44
            }

            40 -> {
                npc(FaceAnim.NEUTRAL, "I do, but it's not really spare.", "I can fill a pot with ale yeast for 5 ecto-tokens.")
                stage = 41
            }

            41 -> options("That's a good deal.", "No thanks.").also { stage++ }

            42 -> when (buttonId) {
                1 -> {
                    player(FaceAnim.HAPPY, "That's a good deal.")
                    stage = 43
                }
                2 -> end()
            }

            43 -> {
                if (!inInventory(player, Items.EMPTY_POT_1931, 1)) {
                    npc(FaceAnim.NEUTRAL, "I'm afraid you'll need a pot for me to put the yeast in.")
                    stage = END_DIALOGUE
                    return true
                }

                if (!inInventory(player, Items.ECTO_TOKEN_4278, 5)) {
                    player(FaceAnim.SAD, "I'll want 5 ectotokens for each pot of yeast.")
                    stage = END_DIALOGUE
                    return true
                }

                val pots = amountInInventory(player, Items.EMPTY_POT_1931)
                val tokens = amountInInventory(player, Items.ECTO_TOKEN_4278)
                val max = minOf(pots, tokens / 5)

                if (removeItem(player, Item(Items.EMPTY_POT_1931, max)) && removeItem(player, Item(Items.ECTO_TOKEN_4278, max * 5))) {
                    addItem(player, Items.ALE_YEAST_5767, max)
                }

                stage = 44
            }

            44 -> npcl(FaceAnim.HALF_ASKING, "Can I help you with anything else?").also { stage = 1 }
        }
        return true
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.METARIALUS_2322)
}
