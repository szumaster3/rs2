package content.region.kandarin.east_ardougne.dialogue.market

import content.global.skill.thieving.ThievingDefinition
import content.region.kandarin.gnome_stronghold.quest.makinghistory.MHUtils
import content.region.kandarin.gnome_stronghold.quest.makinghistory.dialogue.SilverMerchantDialogueFile
import content.region.kandarin.gnome_stronghold.quest.makinghistory.plugin.EnchantedKeyTreasures
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
import shared.consts.Quests

@Initializable
class SilverMerchantDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        val baker = args[0] as NPC
        npc = baker
        val canTrade = ThievingDefinition.Stall.handleStallCooldown(
            player = player,
            stallName = "SILVER_STALL",
            shopNpc = npc,
            guardNpcIds = listOf(NPCs.GUARD_32)
        )
        if (!canTrade) return false
        npc(FaceAnim.HAPPY, "Silver! Silver! Best prices for buying and selling in all", "Kandarin!")
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        val hasKey = hasAnItem(player, Items.ENCHANTED_KEY_6754).container != null
        val hasJournal = hasAnItem(player, Items.JOURNAL_6755).container == player.inventory
        val questStage = getQuestStage(player, Quests.MAKING_HISTORY)
        val erinProgress = getVarbit(player, MHUtils.ERIN_PROGRESS)

        when (stage) {
            0 -> {
                if (questStage >= 1) {
                    options("Yes please.", "No, thank you.", "Ask about the outpost.").also { stage = 2 }
                } else if (isQuestComplete(player, Quests.MAKING_HISTORY)) {
                    npc(FaceAnim.NEUTRAL,"Hello, I hope Jorral was pleased with that Journal.").also { stage = 4 }
                } else {
                    options("Yes please.", "No, thank you.").also { stage++ }
                }
            }

            1 -> when (buttonId) {
                1 -> {
                    end()
                    openNpcShop(player, NPCs.SILVER_MERCHANT_569)
                }

                2 -> player(FaceAnim.NEUTRAL, "No, thank you.").also { stage = END_DIALOGUE }
            }

            2 -> when (buttonId) {
                1 -> {
                    end()
                    openNpcShop(player, NPCs.SILVER_MERCHANT_569)
                }
                2 -> player(FaceAnim.NEUTRAL, "No, thank you.").also { stage = END_DIALOGUE }
                3 -> {
                    end()
                    when {
                        questStage < 2 -> openDialogue(player, SilverMerchantDialogueFile(0))
                        erinProgress == 1 || !hasKey -> openDialogue(player, SilverMerchantDialogueFile(13))
                        inInventory(player, Items.CHEST_6759) -> openDialogue(player, SilverMerchantDialogueFile(19))
                        erinProgress in 1..3 && !hasJournal -> player("I found a chest, but I lost it and any contents", "it had.").also { stage++ }
                        inInventory(player, Items.JOURNAL_6755) -> openDialogue(player, SilverMerchantDialogueFile(21))
                        else -> npc("Hello, I hope Jorral was pleased with that Journal.").also { stage = 4 }
                    }
                }
            }
            3 -> npc(FaceAnim.NEUTRAL,"Well I suggest you go back to where you found it.").also { stage = END_DIALOGUE }
            4 -> npc(FaceAnim.NEUTRAL,"I'm sure it's been a valuable find.").also { stage = END_DIALOGUE }
            5 -> npc(FaceAnim.NEUTRAL,"I'm sure it's been a valuable find.").also { stage++ }
            6 -> {
                if (hasAnItem(player!!, Items.ENCHANTED_KEY_6754).exists()) {
                    end()
                    npc(FaceAnim.HALF_ASKING,
                        "You know you can use that enchanted key you have on",
                        "your keyring all over Gielinor. Who knows what you might find?",
                    )
                } else if (getAttribute(player!!, EnchantedKeyTreasures.ENCHANTED_KEY_ATTR, 0) == 11) {
                    player(FaceAnim.HALF_ASKING,"Oh, You know that key you gave me?").also { stage = 11 }
                } else {
                    player(FaceAnim.NEUTRAL,"What I came to ask was: I lost that key you gave me.").also { stage++ }
                }
            }

            7 -> npc(FaceAnim.NEUTRAL,"Oh dear, luckily I found it, but it'll cost you 500gp", "as I know it's valuable.").also { stage++ }
            8 -> options("Yes please.", "No thanks.").also { stage++ }
            9 -> when (buttonId) {
                1 -> player(FaceAnim.NEUTRAL,"Yes please.").also { stage++ }
                2 -> player(FaceAnim.NEUTRAL,"No thanks.").also { stage = END_DIALOGUE }
            }

            10 -> {
                end()
                if (freeSlots(player!!) < 0) {
                    npc(FaceAnim.CALM_TALK,"You don't have the space to carry it! Empty some space", "in your inventory.")
                    return true
                }
                if (!removeItem(player!!, Item(Items.COINS_995, 500))) {
                    npc(FaceAnim.CALM_TALK,"You don't have enough money, sorry.")
                } else {
                    npc(FaceAnim.FRIENDLY,"Thank you, enjoy!")
                    setAttribute(player!!, EnchantedKeyTreasures.ENCHANTED_KEY_ATTR, 0)
                    addItemOrDrop(player!!, Items.ENCHANTED_KEY_6754, 1)
                }
            }

            11 -> npc(FaceAnim.HALF_THINKING, "Yes?").also { stage++ }
            12 -> player(FaceAnim.CALM_TALK, "It dissolved!").also { stage++ }
            13 -> npc(FaceAnim.ASKING, "Really? I suppose it served its purpose.").also { stage = END_DIALOGUE }
        }
        return true
    }

    override fun newInstance(player: Player?): Dialogue = SilverMerchantDialogue(player)

    override fun getIds(): IntArray = intArrayOf(NPCs.SILVER_MERCHANT_569)
}