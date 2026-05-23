package content.region.kandarin.feldip.quest.zogre.dialogue

import content.region.kandarin.feldip.quest.zogre.plugin.ZogreUtils
import core.api.*
import core.game.dialogue.DialogueFile
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.item.Item
import core.tools.END_DIALOGUE
import core.tools.START_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Vars

/**
 * Represents the Bartender dialogue file (Zogre flesh eaters).
 */
class BartenderDialogues : DialogueFile() {

    companion object {
        private const val DEFAULT = 1
        private const val TANKARD = 100
        private const val TORN_PAGE = 200
        private const val BLACK_PRISM = 300
        private const val WRONG_PORTRAIT = 400
        private const val CORRECT_PORTRAIT = 500
    }

    override fun handle(componentID: Int, buttonID: Int) {
        val p = player!!
        npc = NPC(NPCs.BARTENDER_739)

        val hasTankard = inInventory(p, Items.DRAGON_INN_TANKARD_4811)
        val hasTornPage = inInventory(p, Items.TORN_PAGE_4809)
        val hasBlackPrism = inInventory(p, Items.BLACK_PRISM_4808)
        val hasPortraitWrong = inInventory(p, ZogreUtils.UNREALIST_PORTRAIT)
        val hasPortraitCorrect = inInventory(p, ZogreUtils.REALIST_PORTRAIT)

        when (stage) {
            START_DIALOGUE -> npcl(FaceAnim.HALF_ASKING, "What can I get you?").also { stage = DEFAULT }

            DEFAULT -> {
                stage = when {
                    hasTankard -> TANKARD
                    hasTornPage -> TORN_PAGE
                    hasBlackPrism -> BLACK_PRISM
                    hasPortraitWrong -> WRONG_PORTRAIT
                    hasPortraitCorrect -> CORRECT_PORTRAIT
                    else -> 10
                }
                handle(componentID, buttonID)
            }

            10 -> player("What's on the menu?").also { stage++ }
            11 -> npcl("Dragon Bitter and Greenman's Ale, oh and some cheap beer.").also { stage++ }
            12 -> options("I'll give it a miss I think.", "I'll try the Dragon Bitter.", "Can I have some Greenman's Ale?", "One cheap beer please!").also { stage++ }

            13 -> when (buttonID) {
                1 -> player("I'll give it a miss I think.").also { stage = 14 }
                2 -> player("I'll try the Dragon Bitter.").also { stage = 20 }
                3 -> player("Can I have some Greenman's Ale?").also { stage = 30 }
                4 -> player("One cheap beer please!").also { stage = 40 }
            }

            14 -> npcl("Come back when you're a little thirstier.").also { stage = END_DIALOGUE }
            20 -> npcl("Ok, that'll be two coins.").also { stage++ }
            21 -> {
                end()
                if (!removeItem(p, Item(Items.COINS_995, 2))) {
                    sendMessage(p, "You don't have enough coins.")
                } else {
                    sendMessage(p, "You buy a pint of Dragon Bitter.")
                    addItemOrDrop(p, Items.DRAGON_BITTER_1911)
                }
            }
            30 -> npcl("Ok, that'll be ten coins.").also { stage++ }
            31 -> {
                end()
                if (!removeItem(p, Item(Items.COINS_995, 10))) {
                    sendMessage(p, "You don't have enough coins.")
                } else {
                    sendMessage(p, "You buy a pint of Greenman's Ale.")
                    addItemOrDrop(p, Items.GREENMANS_ALE_1909)
                }
            }

            40 -> npcl("That'll be 2 gold coins please!").also { stage++ }
            41 -> {
                if (!removeItem(p, Item(Items.COINS_995, 2))) {
                    end()
                    sendMessage(p, "You don't have enough coins.")
                } else {
                    sendDialogue(p, "You buy a pint of cheap beer.")
                    addItemOrDrop(p, Items.BEER_1917)
                    stage = 42
                }
            }
            42 -> npcl(FaceAnim.HAPPY, "Have a super day!").also { stage = END_DIALOGUE }
            TANKARD -> sendItemDialogue(p, Items.DRAGON_INN_TANKARD_4811, "You show the tankard to the Inn Keeper.").also { stage++ }
            101 -> {
                if (getAttribute(p, ZogreUtils.TALK_ABOUT_TANKARD_AGAIN, false)) {
                    player("Hello again. Can you tell me what you know about this tankard again please?").also { stage = 110 }
                } else {
                    player("Hello there, I found this tankard in an ogre tomb cavern. It has the emblem of this Inn on it and I wondered if you knew anything about it?").also { stage++ }
                }
            }
            102 -> npcl("Oh yes, this is Brentle's mug... I'm surprised he left it just lying around down some cave. He's quite protective of it.").also { stage++ }
            103 -> player("Brentle you say? So you knew him then?").also { stage++ }
            104 -> npcl("Yeah, this belongs to 'Brentle Vahn', he's quite a common customer, though I've not seen him in a while.").also { stage++ }
            105 -> npcl("He was talking to some shifty looking wizard the other day. I don't know his name, but I'd recognise him if I saw him.").also { stage++ }
            106 -> player("I'm sorry to tell you this, but Brentle Vahn is dead - I believe he was murdered.").also { stage++ }
            107 -> npc(FaceAnim.SCARED, "Noooo! I'm shocked...").also { stage++ }
            108 -> npcl(FaceAnim.FRIENDLY, "...but not surprised. He was a good customer... but I knew he would sell his sword arm and do many a dark deed if paid enough.").also { stage++ }
            109 -> npcl(FaceAnim.NEUTRAL, "If you need help bringing the culprit to justice, you let me know.").also { stage = END_DIALOGUE }

            110 -> npcl(FaceAnim.NEUTRAL,"Oh yes, Brentle's tankard. Yeah, you've shown me this already. It belonged to Brentle Vahn, he was quite a common customer, though I've not seen him in a while.").also { stage++ }
            111 -> npcl(FaceAnim.NEUTRAL,"He was talking to some shifty looking wizard the other day. I don't know his name, but I'd recognise him if I saw him.").also {
                setVarbit(p, Vars.VARBIT_QUEST_ZORGE_FLESH_EATERS_PROGRESS_487, 4, true)
                stage = END_DIALOGUE
            }

            TORN_PAGE -> sendItemDialogue(p, Items.TORN_PAGE_4809, "You show the bar tender the torn page.").also { stage++ }
            201 -> player(FaceAnim.HALF_ASKING,"Do you have any clue what this might be?").also { stage++ }
            202 -> npcl(FaceAnim.NEUTRAL,"Oooh, don't show me that sort of stuff, it's probably all magical and wizardy, probably turn me into a frog as soon as I look at it...").also { stage = END_DIALOGUE }

            BLACK_PRISM -> sendItemDialogue(p, Items.BLACK_PRISM_4808, "You show the bar tender the black prism.").also { stage++ }
            301 -> player(FaceAnim.FRIENDLY,"Hello there, I found this black prism, I wondered if you knew anything about it.").also { stage++ }
            302 -> npcl(FaceAnim.NEUTRAL,"Hmmm, it's not really familiar to me, sorry. Looks magical to me... maybe someone else in Yanille can help you?").also { stage = END_DIALOGUE }

            WRONG_PORTRAIT -> sendItemDialogue(p, ZogreUtils.UNREALIST_PORTRAIT, "You show the sketch to the Inn keeper.").also { stage++ }
            401 -> npcl(FaceAnim.HALF_ASKING, "Who's that? I mean, I guess it's a picture of a person isn't it? Sorry... you've got me? And before you ask, you're not putting it up on my wall!").also { stage++ }
            402 -> playerl(FaceAnim.FRIENDLY, "It's a portrait of Sithik Ints... don't you recognise him?").also { stage++ }
            403 -> npcl(FaceAnim.HALF_GUILTY, "I'm sorry, I really am, but I just don't see it... can you make a better picture?").also { stage++ }
            404 -> playerl(FaceAnim.NEUTRAL, "I'll try...").also { stage = END_DIALOGUE }

            CORRECT_PORTRAIT -> sendItemDialogue(p, ZogreUtils.REALIST_PORTRAIT, "You show the portrait to the Inn keeper.").also { stage++ }
            501 -> npcl(FaceAnim.NEUTRAL,"Yeah, that's the guy who was talking to Brentle Vahn the other day! Look at those eyes, never a more shifty looking pair will you ever see!").also { stage++ }
            502 -> player(FaceAnim.NEUTRAL,"You've just identified the man who I think sent Brentle Vahn to his death.").also { stage++ }
            503 -> player(FaceAnim.HALF_ASKING,"I'm bringing him to justice with the wizards' guild grand secretary. Can you sign this portrait to confirm he was talking to Brentle Vahn?").also { stage++ }
            504 -> npcl(FaceAnim.HAPPY, "I can and I will!").also {
                removeItem(p, ZogreUtils.REALIST_PORTRAIT)
                addItem(p, ZogreUtils.SIGNED_PORTRAIT)
                stage++
            }
            505 -> sendItemDialogue(p, ZogreUtils.SIGNED_PORTRAIT, "The Dragon Inn bartender signs the portrait.").also { stage++ }
            506 -> player(FaceAnim.FRIENDLY,"Thanks for your help, it's really very good of you.").also { stage++ }
            507 -> npcl(FaceAnim.NEUTRAL,"Not at all, just doing my part.").also {
                setAttribute(p, ZogreUtils.TALK_ABOUT_SIGN_PORTRAIT, true)
                stage = END_DIALOGUE
            }
        }
    }
}
