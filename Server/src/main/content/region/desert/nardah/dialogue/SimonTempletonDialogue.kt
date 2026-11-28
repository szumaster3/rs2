package content.region.desert.nardah.dialogue

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
 * Represents the Simon Templeton dialogue.
 */
@Initializable
class SimonTempletonDialogue(player: Player? = null) : Dialogue(player) {

    private val ARTIFACTS = mapOf(
        "clay" to listOf(
            Item(Items.POTTERY_SCARAB_9032) to 75,
            Item(Items.POTTERY_STATUETTE_9036) to 100,
            Item(Items.IVORY_COMB_9026) to 50
        ),
        "stone" to listOf(
            Item(Items.STONE_SEAL_9042) to 150,
            Item(Items.STONE_SCARAB_9030) to 175,
            Item(Items.STONE_STATUETTE_9038) to 200
        ),
        "gold" to listOf(
            Item(Items.GOLD_SEAL_9040) to 750,
            Item(Items.GOLDEN_SCARAB_9028) to 1000,
            Item(Items.GOLDEN_STATUETTE_9034) to 1250
        )
    )

    private fun hasArtifacts(): Boolean =
        ARTIFACTS.values.flatten().any { player.inventory.containsItem(it.first) }

    private fun sellArtifacts(selected: String) {
        ARTIFACTS[selected]?.forEach { (item, price) ->
            val amount = amountInInventory(player, item.id)
            if (removeItem(player, Item(item.id, amount), Container.INVENTORY)) {
                addItem(player, Items.COINS_995, price * amount, Container.INVENTORY)
            }
        }
    }

    private fun sellPyramidTopper() {
        val amount = amountInInventory(player, Items.PYRAMID_TOP_6970)
        if (removeItem(player, Item(Items.PYRAMID_TOP_6970, amount), Container.INVENTORY)) {
            val goldReward = (1000 + getStatLevel(player, 16) / 99.0 * 9000).toInt()
            addItem(player, Items.COINS_995, goldReward * amount, Container.INVENTORY)
        }
    }

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC

        if (args.size == 4 && (args[3] as Int) in arrayOf(Items.PHARAOHS_SCEPTRE_3_9044, Items.PHARAOHS_SCEPTRE_2_9046, Items.PHARAOHS_SCEPTRE_1_9048, Items.PHARAOHS_SCEPTRE_9050)) {
            npc(FaceAnim.FRIENDLY, "You sellin' me this gold colored", "stick thing. Looks fake to me.", "I'll give you 100 gold for it.")
            stage = 30
            return true
        }

        npc(FaceAnim.HALF_ASKING, "G'day, mate. Got any new", "pyramid artefacts for me?")
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        val hasPyramidTopper = inInventory(player, Items.PYRAMID_TOP_6970)

        when (stage) {
            0 -> {
                when {
                    hasPyramidTopper -> {
                        player(FaceAnim.FRIENDLY, "Yes, actually. The top of that pyramid.")
                        stage = 6
                    }
                    hasArtifacts() -> {
                        player(FaceAnim.HAPPY, "Why, yes I do!")
                        stage = 1
                    }
                    else -> {
                        player(FaceAnim.HALF_GUILTY, "No, I haven't.")
                        stage = 3
                    }
                }
            }

            1 -> { npc(FaceAnim.FRIENDLY, "Excellent! I would like to buy them."); stage = 2 }
            2 -> { sendOptions(player, "Sell artefacts?", "Yes, I need money!!", "No, I'll hang onto them."); stage = 10 }
            3 -> { npc(FaceAnim.FRIENDLY, "Well, keep my offer in mind", "if you do find one."); stage = 4 }
            4 -> { player(FaceAnim.FRIENDLY, "I will. Goodbye, Simon."); stage = 5 }
            5 -> { npc(FaceAnim.FRIENDLY, "Bye, mate."); stage = END_DIALOGUE }

            6 -> {
                val goldReward = (1000 + getStatLevel(player, 16) / 99.0 * 9000).toInt()
                npc(FaceAnim.FRIENDLY, "Hmmm, very nice. I'll buy them for $goldReward each.")
                stage = 7
            }

            7 -> { options("Sounds good!", "No thanks."); stage = 8 }
            8 -> when (buttonId) {
                1 -> { sellPyramidTopper(); end() }
                2 -> { npc("Have it your way."); stage = END_DIALOGUE }
            }

            10 -> when (buttonId) {
                1 -> { setTitle(player,4); sendOptions(player, "Which ones do you want to sell?", "Clay and Ivory", "Stone", "Gold", "Sell them all!"); stage = 20 }
                2 -> { npc(FaceAnim.FRIENDLY, "Aww, alright. Well, keep my", "offer in mind, will ya?"); stage = 11 }
            }

            11 -> { player(FaceAnim.FRIENDLY, "Sure thing, Simon."); stage = 12 }
            12 -> { npc(FaceAnim.FRIENDLY, "Thanks, mate."); stage = END_DIALOGUE }

            20 -> when (buttonId) {
                1 -> { sellArtifacts("clay"); end() }
                2 -> { sellArtifacts("stone"); end() }
                3 -> { sellArtifacts("gold"); end() }
                4 -> { ARTIFACTS.keys.forEach { sellArtifacts(it) }; end() }
            }

            30 -> { player(FaceAnim.FRIENDLY, "What! This is a genuine pharaoh's scepter - made out of", "solid gold and finely jewelled with precious gems", " by the finest craftsmen in the area."); stage = 31 }
            31 -> { npc(FaceAnim.FRIENDLY, "Strewth! I can tell a pile of croc when I hear it!", "You've got the patter mate, but I'm no mug.", "That's a fake."); stage = 32 }
            32 -> { player(FaceAnim.HAPPY, "It has magical powers!"); stage = 33 }
            33 -> { npc(FaceAnim.HALF_THINKING, "Oh, magical powers... yeah yeah yeah. Heard it all before", "mate. I'll give you 100 gold, or some 'magic beans'.", "Take it or leave it."); stage = 34 }
            34 -> { player(FaceAnim.FRIENDLY, "I don't think so! I'll find someone who'll give me", "what it's worth."); stage = 35 }
            35 -> { npc(FaceAnim.FRIENDLY, "Suit yerself..."); stage = END_DIALOGUE }
        }
        return true
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.AGILITY_PYRAMID_SIMON_3123)
}

