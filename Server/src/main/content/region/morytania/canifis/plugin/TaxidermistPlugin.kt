package content.region.morytania.canifis.plugin

import core.api.inInventory
import core.api.openDialogue
import core.api.removeItem
import core.api.replaceSlot
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.item.Item
import core.tools.END_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs

class TaxidermistPlugin : InteractionListener {
    override fun defineListeners() {

        /*
         * Handles stuffing the remains of specific items.
         * https://runescape.fandom.com/wiki/Taxidermist
         */

        onUseWith(IntType.NPC, ITEM_IDS, NPCs.POH_TAXIDERMIST_4246) { player, used, with ->
            val stuffed = StuffedItem.product[used.id] ?: return@onUseWith true
            val npc = with.asNpc()
            openDialogue(player, TaxidermistDialogue(stuffed, used.asItem()), npc)
            return@onUseWith true
        }
    }

    /**
     * Represents taxidermist dialogue (stuffed trophy preservation).
     */
    inner class TaxidermistDialogue(private val stuffed: StuffedItem?, private val used: Item) : DialogueFile() {

        override fun handle(componentID: Int, buttonID: Int) {
            val player = player ?: return end()
            val stuffed = stuffed ?: return end()
            when (stage) {
                0 -> {
                    npcl(FaceAnim.FRIENDLY, stuffed.message)
                    stage = 1
                }

                1 -> {
                    player(FaceAnim.FRIENDLY, "I want to preserve this item.")
                    stage = 2
                }

                2 -> {
                    npcl(FaceAnim.HALF_THINKING, "I can preserve that for you for ${stuffed.price} coins.")
                    stage = 3
                }

                3 -> {
                    options("Yes please.", "No thanks.")
                    stage = 4
                }

                4 -> when (buttonID) {
                    1 -> {
                        if (stuffed.dropId != used.id) {
                            npcl(FaceAnim.HALF_THINKING, "Don't be silly, I can't preserve that!")
                            stage = END_DIALOGUE
                            return
                        }
                        if (!inInventory(player, Items.COINS_995, stuffed.price)) {
                            npcl(FaceAnim.HALF_THINKING, "You don't have enough coins in order to do that.")
                            stage = END_DIALOGUE
                            return
                        }
                        removeItem(player, Item(Items.COINS_995, stuffed.price))
                        replaceSlot(player, used.slot, Item(stuffed.stuffedId, 1))
                        npc(FaceAnim.FRIENDLY, "There you go!")
                        stage = END_DIALOGUE
                    }

                    2 -> {
                        npcl(FaceAnim.HAPPY, "All right, come back if you change your mind, eh?")
                        stage = END_DIALOGUE
                    }

                    else -> end()
                }

            }
        }
    }

    companion object {
        private val ITEM_IDS = StuffedItem.values.map { it.dropId }.toIntArray()
    }

    enum class StuffedItem(val dropId: Int, val stuffedId: Int, val price: Int, val message: String) {
        CRAWLING_HAND_DROP(Items.CRAWLING_HAND_7975, Items.CRAWLING_HAND_7982, 1000, "That's a very fine crawling hand."),
        COCKATRICE_HEAD_DROP(Items.COCKATRICE_HEAD_7976, Items.COCKATRICE_HEAD_7983, 2000, "A cockatrice! Beautiful, isn't it? Look at the plumage!"),
        BASILISK_HEAD_DROP(Items.BASILISK_HEAD_7977, Items.BASILISK_HEAD_7984, 4000, "My, he's a scary-looking fellow, isn't he? He'll look good on your wall!"),
        KURASK_HEAD_DROP(Items.KURASK_HEAD_7978, Items.KURASK_HEAD_7985, 6000, "A kurask? Splendid! Look at those horns!"),
        ABYSSAL_HEAD_DROP(Items.ABYSSAL_HEAD_7979, Items.ABYSSAL_HEAD_7986, 12000, "Goodness, an abyssal demon! See how it's still glowing?  I'll have to use some magic to preserve that."),
        KBD_HEADS_DROP(Items.KBD_HEADS_7980, Items.KBD_HEADS_7987, 50000, "This must be a King Black Dragon! I'll have to get out my heavy duty tools, this skin's as tough as iron!"),
        KQ_HEAD_DROP(Items.KQ_HEAD_7981, Items.KQ_HEAD_7988, 50000, "That must be the biggest kalphite I've ever seen! Preserving insects is always tricky. I'll have to be careful..."),
        BIG_BASS_DROP(Items.BIG_BASS_7989, Items.BIG_BASS_7990, 1000, "That's a mighty fine sea bass you've caught there."),
        BIG_SWORDFISH_DROP(Items.BIG_SWORDFISH_7991, Items.BIG_SWORDFISH_7992, 2500, "Don't point that thing at me!"),
        BIG_SHARK_DROP(Items.BIG_SHARK_7993, Items.BIG_SHARK_7994, 5000, "That's quite a fearsome shark! You've done everyone a service by removing it from the sea!"), ;

        companion object {
            val values = enumValues<StuffedItem>()
            val product = values.associateBy { it.dropId }
        }
    }
}
