package content.region.misthalin.varrock.quest.phoenixgang.dialogue

import content.data.Quests

import content.region.misthalin.varrock.quest.phoenixgang.ShieldofArrav
import core.api.*
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.dialogue.IfTopic
import core.game.dialogue.Topic
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.diary.DiaryType
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import core.tools.START_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs

@Initializable
class CuratorHaigHalenDialogue(player: Player? = null) : Dialogue(player) {

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            START_DIALOGUE -> npcl(FaceAnim.NEUTRAL, "Welcome to Varrock Museum!").also {
                if (getQuestStage(player, Quests.THE_DIG_SITE.questName) == 1 && inInventory(player, Items.UNSTAMPED_LETTER_682)) { stage = 11 }
                stage++
            }
            1 -> showTopics(
                Topic(FaceAnim.FRIENDLY, "Have you any interesting news?", 2),
                Topic(FaceAnim.FRIENDLY, "Do you know where I could find any treasure?", 8),
                IfTopic(FaceAnim.FRIENDLY, "I've lost the letter of recommendation.", 17, getQuestStage(player, Quests.THE_DIG_SITE.questName) == 2 && !inInventory(player, Items.SEALED_LETTER_683)),
                IfTopic("I have the ${Quests.SHIELD_OF_ARRAV.questName}", 18, getQuestStage(player, Quests.SHIELD_OF_ARRAV.questName) == 70, false),
                IfTopic("I'm looking for a statuette recovered from the city of Uzer.", 34, getQuestStage(player, Quests.THE_GOLEM.questName) == 3, false),
            )
            2 -> npcl(FaceAnim.FRIENDLY, "Yes, we found a rather interesting island to the north of Morytania. We believe that it may be of archaeological significance.").also { stage++ }
            3 -> playerl(FaceAnim.FRIENDLY, "Oh? That sounds interesting.").also { stage++ }
            4 -> npcl(FaceAnim.FRIENDLY, "Indeed. I suspect we'll be looking for qualified archaeologists once we have constructed our canal and barge.").also { stage++ }
            5 -> playerl(FaceAnim.FRIENDLY, "Would I qualify then?").also { stage++ }
            6 -> npcl(FaceAnim.FRIENDLY, "You've certainly done a lot to help out Varrock Museum, so we'd be silly not to ask for your expertise.").also { stage++ }
            7 -> playerl(FaceAnim.FRIENDLY, "Thank you. I'll look forward to it!").also { stage = END_DIALOGUE }
            8 -> npcl(FaceAnim.FRIENDLY, "Look around you! This museum is full of treasures!").also { stage++ }
            9 -> playerl(FaceAnim.FRIENDLY, "No, I meant treasures for ME.").also { stage++ }
            10 -> npcl(FaceAnim.FRIENDLY, "Any treasures this museum knows about it goes to great lengths to acquire.").also { stage = END_DIALOGUE }
            11 -> player(FaceAnim.FRIENDLY, "I've been given this letter by an examiner at the Dig", "Site. Can you stamp this for me?").also { stage++ }
            12 -> npc(FaceAnim.FRIENDLY, "What have we here? A letter of recommendation", "indeed...").also { stage++ }
            13 -> npcl(FaceAnim.FRIENDLY, "The letter here says you name is ${player.username}. Well ${player.username}, I wouldn't normally do this for just anyone, but as you did us such a great service with the Shield of Arrav I don't see why not.").also { stage++ }
            14 -> npcl(FaceAnim.FRIENDLY, "Run this letter back to the Examiner to begin your adventure into the world of Earth Sciences. Enjoy your studies, Student!").also { stage++ }
            15 -> npc(FaceAnim.FRIENDLY, "There you go, good luck student... Be sure to come", "back and show me your certificates. I would like to see", "how you get on.").also {
                if (removeItem(player, Items.UNSTAMPED_LETTER_682)) {
                    addItemOrDrop(player, Items.SEALED_LETTER_683)
                }
                stage++
            }
            16 -> playerl(FaceAnim.FRIENDLY, "Ok, I will. Thanks, see you later.").also {
                if (getQuestStage(player, Quests.THE_DIG_SITE.questName) == 1) {
                    setQuestStage(player, Quests.THE_DIG_SITE.questName, 2)
                }
                stage = 1
            }
            // The Dig Site quest.
            17 -> npc(FaceAnim.FRIENDLY, "Yes, I saw you drop it as you walked off last time. Here it is.").also {
                addItemOrDrop(player, Items.SEALED_LETTER_683)
                stage = 1
            }
            // The Shield of Arrav quest.
            18 -> {
                if (player.inventory.containsItem(ShieldofArrav.PHOENIX_SHIELD) ||
                    player.inventory.containsItem(ShieldofArrav.BLACKARM_SHIELD)) {
                    player("I have half the shield of Arrav here. Can I get a", "reward?")
                    stage++
                } else {
                    npc("Of course you won't actually be able to claim the", "reward with only half the reward certificate...")
                    stage = 13
                }
            }
            19 -> npc("The Shield of Arrav! Goodness, the Museum has been", "searching for that for years! The late King Roald II", "offered a reward for it years ago!").also { stage++ }
            20 -> player("Well, I'm here to claim it.").also { stage++ }
            21 -> npc("Let me have a look at it first.").also { stage++ }
            22 -> sendItemDialogue(player, ShieldofArrav.getShield(player), "The curator peers at the shield.").also { stage++ }
            23 -> npc("This is incredible!").also { stage++ }
            24 -> npc("That shield has been missing for over twenty-five years!").also { stage++ }
            25 -> npc("Leave the shield here with me and I'll write you out a", "certificate saying that you have returned the shield, so", "that you can claim your reward from the King.").also { stage++ }
            26 -> player("Can I have two certificates please?").also { stage++ }
            27 -> npc("Yes, certainly. Please hand over the shield.").also { stage++ }
            28 -> sendItemDialogue(player, ShieldofArrav.getShield(player), "You hand over the shield half.").also { stage++ }
            29 -> {
                val shield = ShieldofArrav.getShield(player)
                val certificate = if (shield == ShieldofArrav.BLACKARM_SHIELD) ShieldofArrav.BLACKARM_CERTIFICATE else ShieldofArrav.PHOENIX_CERTIFICATE
                if (player.inventory.remove(shield)) {
                    player.inventory.add(certificate)
                    interpreter!!.sendItemMessage(certificate, "The curator writes out two half-certificates.")
                    stage++
                }
            }
            30 -> npc("Of course you won't actually be able to claim the", "reward with only half the reward certificate...").also { stage++ }
            31 -> player("What? I went through a lot of trouble to get that shield", "piece and now you tell me it was for nothing? That's", "not very fair!").also { stage++ }
            32 -> npc("Well, if you were to get me the other half of the shield,", "I could give you the other half of the reward certificate.", "It's rumoured to be in the possession of the infamous", "Blackarm Gang, beyond that I can't help you.").also { stage++ }
            33 -> player("Okay, I'll see what I can do.").also { stage = END_DIALOGUE }
            // The Golem quest.
            34 -> npc(FaceAnim.OLD_BOWS_HEAD_SAD, "Ah yes, a very impressive artefact. The people of that city were excellent sculptors.").also { stage++ }
            35 -> npc(FaceAnim.OLD_BOWS_HEAD_SAD, "It's in the display case upstairs.").also { stage++ }
            36 -> player("No, I need to take it away with me.").also { stage++ }
            37 -> npc(FaceAnim.OLD_BOWS_HEAD_SAD, "What do you want it for?").also { stage++ }
            38 -> options("I want to open a portal to the lair of an elder-demon.", "Well, I, er, just want it.").also { stage++ }
            39 -> when (buttonId) {
                1 -> player("I want to open a portal to the lair of an elder-demon.").also { stage++ }

                2 -> player("Well, I, er, just want it.").also { stage = END_DIALOGUE }
            }
            40 -> npc(FaceAnim.OLD_BOWS_HEAD_SAD, "Good heavens! I'd never let you do such a dangerous thing.").also { stage = END_DIALOGUE }
        }
        return true
    }

    override fun newInstance(player: Player?): Dialogue = CuratorHaigHalenDialogue(player)

    override fun getIds(): IntArray = intArrayOf(NPCs.CURATOR_646)
}
