package content.region.kandarin.seers_village.dialogue

import content.data.Quests

import content.region.fremennik.rellekka.quest.viking.FremennikTrials
import content.region.kandarin.seers_village.quest.murder.MurderMystery
import core.api.*
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.dialogue.IfTopic
import core.game.dialogue.Topic
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import core.tools.START_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs

/**
 * Represents the Poison Salesman dialogue.
 *
 * # Relations
 * - [FremennikTrials]
 * - [Murder Mystery][content.region.kandarin.seers_village.quest.murder.MurderMystery]
 */
@Initializable
class PoisonSalesmanDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        val murderMysteryStage = getQuestStage(player, Quests.MURDER_MYSTERY.questName)
        val fremennikTrialQuestStage = getQuestStage(player, Quests.THE_FREMENNIK_TRIALS.questName)
        if(murderMysteryStage == 0 && fremennikTrialQuestStage == 0) {
            npcl(FaceAnim.SAD, "I'm afraid I'm all sold out of poison at the moment. People know a bargain when they see it!")
        } else {
            options("Talk about the Murder Mystery Quest", "Talk about the Fremennik Trials")
            stage = START_DIALOGUE
        }
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        val murderMysteryStage = getQuestStage(player, Quests.MURDER_MYSTERY.questName)
        val fremennikTrialQuestStage = getQuestStage(player, Quests.THE_FREMENNIK_TRIALS.questName)
        when (stage) {
            START_DIALOGUE -> when (buttonId) {
                1 -> when (murderMysteryStage) {
                    0 -> npcl(FaceAnim.NEUTRAL, "I'm afraid I'm all sold out of poison at the moment. People know a bargain when they see it!").also { stage = END_DIALOGUE }
                    1 -> playerl(FaceAnim.NEUTRAL, "I'm investigating the murder at the Sinclair house.").also { stage = 50 }
                    100 -> npcl(FaceAnim.NEUTRAL, "I hear you're pretty smart to have solved the Sinclair Murder!").also { stage = END_DIALOGUE }
                }
                2 -> player(FaceAnim.FRIENDLY,"Hello.").also { stage = 10 }
            }
            10 -> when (fremennikTrialQuestStage) {
                0 -> {
                    npc(FaceAnim.HAPPY, "Come see me if you ever need low-alcohol beer!")
                    stage = END_DIALOGUE
                }
                in 1..30 -> {
                    npc(FaceAnim.HALF_ASKING, "Howdy! You seem like someone with discerning taste!", "Howsabout you try my brand new range of alcohol?")
                    stage++
                }
                in 31..99 -> {
                    npc(FaceAnim.HAPPY, "Thanks for buying out all that low-alcohol beer!")
                    stage = END_DIALOGUE
                }
                100 -> {
                    npcl(FaceAnim.HALF_ASKING, "Howdy! Thanks for buying all that low alcohol beer from me! Now I have the funds to whip up a new batch of patented multipurpose poison!")
                    stage = 74
                }
                else -> {
                    npc(FaceAnim.NEUTRAL, "I don't have anything for you right now.")
                    stage = END_DIALOGUE
                }
            }
            11 -> player(FaceAnim.HALF_ASKING,"Didn't you used to sell poison?").also { stage++ }
            12 -> npc(FaceAnim.FRIENDLY,"That I did indeed! Peter Potter's Patented", "Multipurpose poison! A miracle of modern apothecarys!", "My exclusive concoction has been tested on...").also { stage++ }
            13 -> player(FaceAnim.FRIENDLY,"Uh, yeah. I've already heard the sales pitch.").also { stage++ }
            14 -> npc(FaceAnim.HALF_GUILTY,"Sorry stranger, old habits die hard I guess.").also { stage++ }
            15 -> player(FaceAnim.HALF_ASKING,"So you don't sell poison anymore?").also { stage++ }
            16 -> npc(FaceAnim.FRIENDLY,"Well I would, but I ran out of stock. Business wasn't", "helped with that stuff that happened up at the Sinclair", "Mansion much either, I'll be honest.").also { stage++ }
            17 -> npc(FaceAnim.FRIENDLY,"So, being the man of enterprise that I am I decided to", "branch out a little bit!").also { stage++ }
            18 -> player(FaceAnim.HALF_ASKING,"Into alcohol?").also { stage++ }
            19 -> npc(FaceAnim.HAPPY,"Absolutely! The basic premise between alcohol and poison", "is pretty much the same, after all! The difference is that", "my alcohol has a unique property others do not!").also { stage++ }
            20 -> player(FaceAnim.HALF_ASKING,"And what is that?").also { stage++ }
            21 -> sendDialogue("The salesman takes a deep breath.").also { stage++ }
            22 -> npc(FaceAnim.FRIENDLY,"Ever been too drunk to find your own home? Ever", "wished that you could party away all night long, and", "still wake up fresh as a daisy the next morning?").also { stage++ }
            23 -> npc(FaceAnim.FRIENDLY,"Thanks to the miracles of modern magic we have come", "up with just the solution you need! Peter Potter's", "Patented Party Potions!").also { stage++ }
            24 -> npc(FaceAnim.FRIENDLY,"It looks just like beer! It tastes just like beer! It smells", "just like beer! But... it's not beer!").also { stage++ }
            25 -> npc(FaceAnim.FRIENDLY,"Our mages have mused for many moments to bring", "you this miracle of modern magic! It has all the great", "tastes you'd expect, but contains absolutely no alcohol!").also { stage++ }
            26 -> npc(FaceAnim.HAPPY,"That's right! You can drink Peter Potter's Patented", "Party Potion as much as you want, and suffer", "absolutely no ill effects whatsoever!").also { stage++ }
            27 -> npc(FaceAnim.FRIENDLY,"The clean fresh taste you know you can trust, from", "the people who brought you: Peter Potters Patented", "multipurpose poison, Pete Potters peculiar paint packs").also { stage++ }
            28 -> npc(FaceAnim.FRIENDLY,"and Peter Potters paralyzing panic pins. Available now", "from all good stockists! Ask your local bartender now,", "and experience the taste revolution of the century!").also { stage++ }
            29 -> sendDialogue("He seems to have finished for the time being.").also { stage++ }
            30 -> player("So.. when you say 'all good stockists'...").also { stage++ }
            31 -> npc(FaceAnim.HALF_ASKING,"Yes?").also { stage++ }
            32 -> player(FaceAnim.HALF_ASKING,"How many inns actually sell this stuff?").also { stage++ }
            33 -> npc(FaceAnim.HALF_GUILTY,"Well.. nobody has actually bought any yet. Everyone I", "try and sell it to always asks me what exactly the point", "of beer that has absolutely no effect on you is.").also { stage++ }
            34 -> player(FaceAnim.HALF_ASKING,"So what is the point?").also { stage++ }
            35 -> npc(FaceAnim.HALF_THINKING,"Well... Um... Er... Hmmm. You, er, don't get drunk.").also { stage++ }
            36 -> player(FaceAnim.SAD,"I see...").also { stage++ }
            37 -> npc(FaceAnim.FRIENDLY,"Aw man.. you don't want any now do you? I've really", "tried to push this product, but I just don't think the", "world is ready for beer that doesn't get you drunk.").also { stage++ }
            38 -> npc(FaceAnim.FRIENDLY,"I'm a man ahead of my time I tell you! It's not that", "my products are bad, it's that they're too good for the", "market!").also { stage++ }
            39 -> player(FaceAnim.HALF_ASKING,"Actually, I would like some. How much do you want for it?").also { stage++ }
            40 -> npc(FaceAnim.FRIENDLY,"Y-you would??? Um, okay! I knew I still had the old", "salesmans skills going on!").also { stage++ }
            41 -> npc(FaceAnim.FRIENDLY,"I will sell you a keg of it for only 250 gold pieces! So", "What do you say?").also { stage++ }
            42 -> options("Yes", "No").also { stage++ }
            43 -> when (buttonId) {
                1 -> player("Yes please!").also { stage++ }
                2 -> player("No, not really.").also { stage = END_DIALOGUE }
            }
            44 -> {
                if(freeSlots(player) == 0) {
                    end()
                    npcl(FaceAnim.SAD, "Sorry pal, doesn't look like you have room on you to carry it.")
                    return true
                }
                if (removeItem(player, Item(Items.COINS_995, 250))) {
                    addItemOrDrop(player, Items.LOW_ALCOHOL_KEG_3712, 1)
                    stage = END_DIALOGUE
                } else {
                    npcl(FaceAnim.SAD, "Sorry pal, we do not offer credit for any purchases made of Peter Potter's patented party potion! Come back when you have the cash!")
                    stage++
                }
            }
            45 -> npc(FaceAnim.FRIENDLY,"Well come back when you do!").also { stage = END_DIALOGUE }

            50 -> npcl(FaceAnim.NEUTRAL, "There was a murder at the Sinclair house??? That's terrible! And I was only there the other day too! They bought the last of my Patented Multi Purpose Poison!").also { stage++ }
            51 -> showTopics(
                Topic(FaceAnim.HALF_ASKING,"Patented Multi Purpose Poison?", 52),
                Topic(FaceAnim.HALF_ASKING,"Who did you sell Poison to at the house?", 61),
                Topic(FaceAnim.HALF_ASKING,"Can I buy some Poison?", 65),
                IfTopic("I have this pot I found at the murder scene...", 69, inInventory(player, Items.PUNGENT_POT_1812))
            )
            52 -> npcl(FaceAnim.NEUTRAL, "Aaaaah... a miracle of modern apothecaries!").also { stage++ }
            53 -> npcl(FaceAnim.NEUTRAL, "This exclusive concoction has been tested on all known forms of life and been proven to kill them all in varying dilutions from cockroaches to king dragons!").also { stage++ }
            54 -> npcl(FaceAnim.NEUTRAL, "So incredibly versatile, it can be used as pest control, a cleansing agent, drain cleaner, metal polish and washes whiter than white,").also { stage++ }
            55 -> npcl(FaceAnim.NEUTRAL, "all with our uniquely fragrant concoction that is immediately recognisable across the land as Peter Potter's Patented Poison potion!!!").also { stage++ }
            56 -> sendDialogue("The salesman stops for breath.").also { stage ++ }
            57 -> npcl(FaceAnim.NEUTRAL, "I'd love to sell you some but I've sold out recently. That's just how good it is! Three hundred and twenty eight people in this area alone cannot be wrong!").also { stage++ }
            58 -> npcl(FaceAnim.NEUTRAL, "Nine out of Ten poisoners prefer it in controlled tests!").also { stage++ }
            59 -> npcl(FaceAnim.NEUTRAL, "Can I help you with anything else? Perhaps I can take your name and add it to our mailing list of poison users? We will only send you information related to the use of poison and other Peter Potter Products!").also { stage++ }
            60 -> playerl(FaceAnim.NEUTRAL, "Uh... no, it's ok. Really.").also { stage = END_DIALOGUE }

            61 -> npcl(FaceAnim.HAPPY, "Well, Peter Potter's Patented Multi Purpose Poison is a product of such obvious quality that I am glad to say I managed to sell a bottle to each of the Sinclairs!").also { stage++ }
            62 -> npcl(FaceAnim.HAPPY, "Anna, Bob, Carol, David, Elizabeth and Frank all bought a bottle! In fact they bought the last of my supplies!").also { stage++ }
            63 -> npcl(FaceAnim.HAPPY, "Maybe I can take your name and address and I will personally come and visit you when stocks return?").also { stage++ }
            64 -> playerl(FaceAnim.THINKING, "Uh...no, it's ok.").also { setAttribute(player, MurderMystery.attributePoisonClue, 1); stage = END_DIALOGUE }
            65 -> npcl(FaceAnim.NEUTRAL, "I'm afraid I am totally out of stock at the moment after my successful trip to the Sinclairs' House the other day.").also { stage++ }
            66 -> npcl(FaceAnim.NEUTRAL, "But don't worry! Our factories are working overtime to produce Peter Potter's Patented Multi Purpose Poison!").also { stage++ }
            67 -> npcl(FaceAnim.NEUTRAL, "Possibly the finest multi purpose poison and cleaner yet available to the general market.").also { stage++ }
            68 -> npcl(FaceAnim.NEUTRAL, "And its unique fragrance makes it the number one choice for cleaners and exterminators the whole country over!").also { stage = END_DIALOGUE }

            69 -> sendDialogue("You show the poison salesman the pot you found at the murder", "scene with the unusual smell.").also { stage ++ }
            70 -> npcl(FaceAnim.THINKING, "Hmmm... yes, that smells exactly like my Patented Multi Purpose Poison, but I don't see how it could be. It quite clearly says on the label of all bottles").also { stage++ }
            71 -> npc(FaceAnim.THINKING, "'Not to be taken internally -","EXTREMELY POISONOUS'.").also { stage++ }
            72 -> playerl(FaceAnim.THINKING, "Perhaps someone else put it in his wine?").also { stage++ }
            73 -> npcl(FaceAnim.THINKING, "Yes... I suppose that could have happened...").also { stage = END_DIALOGUE }
            END_DIALOGUE -> end()
            74 -> npcl(FaceAnim.FRIENDLY, "Maybe I can take your name and add it to my mailing list for potential purchasers of Peter Potter's patented multipurpose poison?").also { stage++ }
            75 -> player("Thanks, but no thanks.").also { stage = END_DIALOGUE }
        }
        return true
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.POISON_SALESMAN_820)
}
