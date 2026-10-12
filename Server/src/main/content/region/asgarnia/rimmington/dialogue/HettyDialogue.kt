package content.region.asgarnia.rimmington.dialogue

import core.api.*
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Quests

/**
 * Represents the Hetty dialogue.
 */
@Initializable
class HettyDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        if (isQuestComplete(player, Quests.WITCHS_POTION)) {
            npc(FaceAnim.ASKING, "How's your magic coming along?")
            stage = 0
        }
        when (getQuestStage(player, Quests.WITCHS_POTION)) {
            0 -> npc(FaceAnim.NEUTRAL, "What could you want with an old woman like me?").also { stage = 11}
            20 -> npc(FaceAnim.HAPPY, "So have you found the things for the potion?").also { stage = 100}
            40 -> if (args.size == 2) {
                sendDialogueLines(player, "You drink from the cauldron, it tastes horrible! You feel yourself", "imbued with power.")
                stage = 35
            } else {
                npc(FaceAnim.HALF_GUILTY, "Well are you going to drink the potion or not?")
                stage = END_DIALOGUE
            }
        }
        if (getQuestStage(player, Quests.SWEPT_AWAY) >= 1) {
            player("Hello there. Maggie sent me to ask for you help in", "enchanting her broom.")
            stage = 200
        }
        if (getQuestStage(player, Quests.SWEPT_AWAY) >= 1 && inInventory(player, Items.NEWT_14064)) {
            npc("Have you managed to get that newt yet?")
            stage = 210
        }
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> player(FaceAnim.HALF_GUILTY, "I'm practicing and slowly getting better.").also { stage++ }
            1 -> npc(FaceAnim.HALF_GUILTY, "Good, good.").also { stage = END_DIALOGUE }
            11 -> options("I am in search of a quest.", "I've heard that you are a witch.").also { stage++ }
            12 -> when (buttonId) {
                1 -> player(FaceAnim.NEUTRAL, "I am in search of a quest.").also { stage++ }
                2 -> player(FaceAnim.NEUTRAL, "I've heard that you are a witch.").also { stage = 20 }

            }
            13 -> npc(FaceAnim.HAPPY, "Hmmm... Maybe I can think of something for you.").also { stage++ }
            14 -> npc(FaceAnim.HAPPY, "Would you like to become more proficient in the dark", "arts?").also { stage++ }
            15 -> options("Yes help me become one with my darker side.", "No I have my principles and honour.").also { stage++ }
            16 -> when (buttonId) {
                1 -> player(FaceAnim.HAPPY, "Yes help me become one with my darker side.").also { stage = 30 }
                2 -> player(FaceAnim.NEUTRAL, "No I have my principles and honour.").also { stage++ }

            }
            17 -> npc(FaceAnim.HALF_GUILTY, "Suit yourself, but you're missing out.").also { stage = END_DIALOGUE }
            20 -> npc(FaceAnim.HALF_GUILTY, "Yes it does seem to be getting fairly common", "knowledge.").also { stage++ }
            21 -> npc(FaceAnim.HALF_GUILTY, "I fear I may get a visit from the witch hunter of", "Falador before long.").also { stage = END_DIALOGUE }
            30 -> npc(FaceAnim.NEUTRAL, "Ok I'm going to make a potion to help bring out your", "darker self.").also { stage++ }
            31 -> npc(FaceAnim.NEUTRAL, "You will need certain ingredients.").also { stage++ }
            32 -> player(FaceAnim.NEUTRAL, "What do I need?").also { stage++ }
            33 -> npc(FaceAnim.NEUTRAL, "You need an eye of newt, a rat's tail, and onion... Oh", "and a piece of burnt meat.").also { stage++ }
            34 -> player(FaceAnim.HAPPY, "Great, I'll go get them.").also {
                setQuestStage(player, Quests.WITCHS_POTION, 20)
                stage = END_DIALOGUE
            }
            35 -> {
                end()
                finishQuest(player, Quests.WITCHS_POTION)
                updateQuestTab(player)
            }
            100 -> if (!allInInventory(player, Items.ONION_1957, Items.RATS_TAIL_300, Items.BURNT_MEAT_2146, Items.EYE_OF_NEWT_221)) {
                player(FaceAnim.HALF_GUILTY, "I'm afraid I don't have all of them yet.").also { stage = 101 }
            } else {
                player(FaceAnim.HAPPY, "Yes I have everything!").also { stage = 102 }
            }
            101 -> npc(FaceAnim.NEUTRAL, "Well I can't make the potion without them! Remember...", "You need an eye of newt, a rat's tail, an onion, and a", "piece of burnt meat. Off you go dear!").also { stage = END_DIALOGUE }
            102 -> npc(FaceAnim.HAPPY, "Excellent, can I have them then?").also { stage++ }
            103 -> sendDialogueLines(player, "You pass the ingredients to Hetty and she puts them all into her", "cauldron. Hetty closes her eyes and begins to chant. The cauldron", "bubbles mysteriously.").also { stage++ }
            104 -> player(FaceAnim.HALF_ASKING, "Well, is it ready?").also { stage++ }
            105 -> {
                end()
                if (player.inventory.remove(Item(Items.ONION_1957), Item(Items.RATS_TAIL_300), Item(Items.BURNT_MEAT_2146), Item(Items.EYE_OF_NEWT_221))) {
                    setQuestStage(player, Quests.WITCHS_POTION, 40)
                    npc(FaceAnim.HAPPY, "Ok, now drink from the cauldron.")
                }
            }
            200 -> npc("Ah, brewing up a cauldron of her infamous 'good", "stuff' is she? Well of course I'll help.").also { stage++ }
            201 -> npc("I think this calls for one of my specialties: my famous", "theurgical broom ointment.").also { stage++ }
            202 -> npc("Now, I can provide you with the ointment; all you'll", "need to do is apply the ointment to the broom.").also { stage++ }
            203 -> player("Excellent!").also { stage++ }
            204 -> npc("But, first, I'll need a newt.").also { stage++ }
            205 -> player("A newt?").also { stage++ }
            206 -> npc("Yes, a newt. Now, if you'll just pop down to my cellar,", "through that trapdoor out back, I've just had a recent", "delivery of newts and toads.").also { stage++ }
            207 -> npc("The delivery ghoul is still here - I can't think what", "could be taking him so long.").also { stage++ }

            208 -> npc("In any case, once the crates are unloaded, just bring", "me a newt from the newt crate and I'll set you up with", "the ointment.").also { stage++ }
            209 -> player("Okay, will do.").also { stage = END_DIALOGUE }

            210 -> player("Yes, it's right here.").also { stage++ }
            211 -> npc("Excellent. I'll just take it and add a little newt slime to", "the ointment.").also { stage++ }
            212 -> {
                val slot = player.inventory.getSlot(Item(Items.NEWT_14064))
                if (slot != -1) {
                    replaceSlot(player, slot, Item(Items.BROOM_OINTMENT_14062, 1))
                }
                npc("That should do it. Just rub this ointment on the broom;", "that should suit Maggie's purposes.")
                stage++
            }

            213 -> player("Wonderful, thanks.").also { stage = END_DIALOGUE }
        }
        return true
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.HETTY_307)
}
