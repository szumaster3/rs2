package content.region.kandarin.seers_village.hemenster.quest.fishingcompo.dialogue

import content.data.Quests

import content.data.GameAttributes
import content.region.kandarin.seers_village.hemenster.quest.fishingcompo.FishingContest.FishingQuestCompleteDialogue
import core.api.*
import core.game.component.Component
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.Components
import shared.consts.Items
import shared.consts.NPCs

/**
 * Represents the Austri & Vestri dwarfs dialogue.
 *
 * # Relations
 * - [Fishing Contest][content.region.kandarin.seers_village.hemenster.quest.fishingcompo.FishingContest]
 */
@Initializable
class DwarfDialogue(player: Player? = null) : Dialogue(player) {
    
    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC

        val questStage = getQuestStage(player, Quests.FISHING_CONTEST.questName)
        val hasFishingPass = inInventory(player, Items.FISHING_PASS_27, 1)
        val hasFishingTrophy = inInventory(player, Items.FISHING_TROPHY_26, 1)
        val hasWonFishingCompetition = getAttribute(player, GameAttributes.QUEST_FISHINGCOMPO_WON, false)

        when {
            /*
             * Lost fishing pass.
             */
            questStage in 1..19 && !hasFishingPass -> {
                player(FaceAnim.SAD, "I lost my fishing pass...")
                stage = 1000
            }
            /*
             * Token of Friendship.
             */
            hasFishingTrophy && hasWonFishingCompetition -> {
                npc(FaceAnim.OLD_NORMAL, "Have you won yet?")
                stage = 2000
            }
            questStage >= 10 && !hasWonFishingCompetition -> {
                npc(FaceAnim.OLD_NORMAL, "Have you won yet?")
                stage = 1500
            }

            /*
             * Post-quest dialogue.
             */
            questStage == 100 -> {
                npc(FaceAnim.OLD_NORMAL, "Welcome, oh great fishing champion!", "Feel free to pop by and use", "our tunnel any time!")
                stage = 2500
            }

            else -> {
                /*
                 * Secret Tunnel - start quest dialogue.
                 */
                npc(FaceAnim.OLD_NORMAL, "Hmph! What do you want?")
            }
        }
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> options(
                "I was wondering what was down that tunnel?",
                "I was just stopping to say hello!",
                "Do you have a brother?"
            ).also { stage++ }
            1 -> when (buttonId) {
                1 -> player("I was wondering what was down that tunnel?").also { stage += 3 }
                2 -> player("I was just stopping to say hello!").also { stage++ }
                3 -> player("Do you have a brother?").also { stage += 2 }
            }
            2 -> npcl(FaceAnim.OLD_DEFAULT, "Hello then.").also { stage = END_DIALOGUE }
            3 -> npc(FaceAnim.OLD_NORMAL, "What if I do! It's no business of yours.").also { stage = END_DIALOGUE }
            4 -> npcl(FaceAnim.OLD_ANGRY1, "You can't go down there!").also { stage++ }
            5 -> options("I didn't want to anyway.", "Why not?", "I'm bigger than you. Let me by.").also { stage++ }
            6 -> when (buttonId) {
                1 -> player("I didn't want to anyway.").also { stage = 10 }
                2 -> player("Why not?").also { stage = 21 }
                3 -> player("I'm bigger than you. Let me by.").also { stage++ }
            }
            7 -> npcl(FaceAnim.OLD_ANGRY3, "Go away! You're not going to bully your way in HERE!").also { stage = END_DIALOGUE }
            10 -> npc(FaceAnim.OLD_NORMAL, "Good. Because you can't.").also { stage++ }
            11 -> player("Because I don't want to.").also { stage++ }
            12 -> npc(FaceAnim.OLD_NORMAL, "Because you can't. So that's fine.").also { stage++ }
            13 -> player("Yes it is.").also { stage++ }
            14 -> npc(FaceAnim.OLD_NORMAL, "Yes. Fine.").also { stage++ }
            15 -> player("Absolutely.").also { stage++ }
            16 -> npc(FaceAnim.OLD_NORMAL, "Well then.").also { stage = END_DIALOGUE }

            20 -> player("Why not?").also { stage++ }
            21 -> npc(FaceAnim.OLD_NORMAL, "This is the home of the Mountain Dwarves.", "How would you like it if I", "wanted to take a shortcut through your home?").also { stage++ }
            22 -> options(
                "Ooh... is this a short cut to somewhere?",
                "Oh, sorry, I hadn't realized it was private.",
                "If you were my friend I wouldn't mind it."
            ).also { stage++ }
            23 -> when (buttonId) {
                1 -> player("Ooh... is this a short cut to somewhere?").also { stage = 30 }
                2 -> player("Oh, sorry, I hadn't realized it was private.").also { stage = 40 }
                3 -> player("If you were my friend I wouldn't mind it.").also { stage = 50 }

            }
            30 -> npc(FaceAnim.OLD_NORMAL, "Well, it is a lot easier to go this way", "to get past White Wolf Mountain than through", "those wolf filled passes.").also { stage = 22 }
            40 -> npc(FaceAnim.OLD_NORMAL, "Well, it is.").also { stage = 22 }
            50 -> npc(FaceAnim.OLD_NORMAL, "Yes, but I don't even know you.").also { stage++ }
            51 -> options(
                "Well, let's be friends!",
                "You're a grumpy little man aren't you?"
            ).also { stage++ }
            52 -> when (buttonId) {
                1 -> player("Well, let's be friends!").also { stage++ }
                2 -> player("You're a grumpy little man aren't you?").also { stage = 70 }
            }
            53 -> npc(FaceAnim.OLD_NORMAL, "I don't make friends easily.", "People need to earn my trust first.").also { stage++ }
            54 -> player(FaceAnim.HALF_ASKING, "And how am I meant to do that?").also { stage++ }
            55 -> npc(FaceAnim.OLD_NORMAL, "My, we are the persistent one aren't we?", "Well, there's a certain gold artefact we're after.", "We dwarves are big fans of gold! This artefact", "is the first prize at the Hemenster fishing competition.").also { stage++ }
            56 -> npc(FaceAnim.OLD_NORMAL, "Fortunately we have acquired a pass to enter", "that competition...").also { stage++ }
            57 -> npc(FaceAnim.OLD_NORMAL, "Unfortunately Dwarves don't make good fishermen.").also { stage++ }
            // Check for quest requirements.
            58 -> if(getStatLevel(player, Skills.FISHING) < 10) {
                npc(FaceAnim.OLD_NORMAL, "Seems to me like you're not much of anything.").also { stage++ }
            } else {
                npc(FaceAnim.OLD_NORMAL, "Okay, I entrust you with our competition pass.").also { stage = 61 }
            }
            59 -> player(FaceAnim.FRIENDLY, "I'll get better at fishing and come back!").also { stage++ }
            60 -> npcl(FaceAnim.OLD_NORMAL, "Aye, it shouldn't take you long. When it's up to 10, come back and find me.").also { stage = END_DIALOGUE }
            61 -> npc(FaceAnim.OLD_NORMAL, "Don't forget to take some gold", "with you for the entrance fee.").also { stage++ }
            62 -> {
                sendItemDialogue(player, Items.FISHING_PASS_27, "You got the Fishing Contest Pass!")
                addItemOrDrop(player, Items.FISHING_PASS_27, 1)
                setQuestStage(player, Quests.FISHING_CONTEST.questName, 10)
                stage++
            }
            63 -> npc(FaceAnim.OLD_HAPPY, "Go to Hemenster and do us proud!").also { stage = END_DIALOGUE }

            70 -> npc(FaceAnim.OLD_NORMAL, "Don't you know it.").also { stage = 51 }
            1000 -> {
                npc(FaceAnim.OLD_HAPPY, "Hmm. It's a good job they sent us spares.", "There you go. Try not to lose that one.")
                addItem(player, Items.FISHING_PASS_27, 1)
                stage++
            }
            1001 -> player("No, it takes preparation to win", "fishing competitions.").also { stage++ }
            1002 -> npc(FaceAnim.OLD_NORMAL, "Maybe that's where we are going wrong", "when we try fishing?").also { stage++ }
            1003 -> player(FaceAnim.NEUTRAL, "Probably.").also { stage++ }
            1004 -> npc(FaceAnim.OLD_NORMAL, "Maybe we should talk to that old Jack", "fella near the competition, everyone", "seems to be ranting about him.").also { stage = END_DIALOGUE }
            2000 -> player(FaceAnim.HAPPY, "Yes, I have!").also { stage++ }
            2001 -> npc(FaceAnim.OLD_HAPPY, "Well done! That's brilliant, do you have", "the trophy?").also { stage++ }
            2002 -> player(FaceAnim.HAPPY, "Yep, I have it right here!").also { stage++ }
            2003 -> npc(FaceAnim.OLD_HAPPY, "Oh, it's even more shiny and gold than", "I thought possible...").also { stage++ }
            2004 -> if (removeItem(player, Items.FISHING_TROPHY_26)) {
                end()
                setAttribute(player, "temp-npc", npc.id)
                finishQuest(player, Quests.FISHING_CONTEST.questName)
                stage = END_DIALOGUE
            } else {
                player(FaceAnim.HALF_GUILTY,"No, I haven't.").also { stage = END_DIALOGUE }
            }
            1500 -> player(FaceAnim.HALF_GUILTY,"No, I haven't.").also { stage = END_DIALOGUE }
            2500 -> player(FaceAnim.HALF_GUILTY,"Thanks, I think I will stop by.").also { stage = END_DIALOGUE }

        }
        return true
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.AUSTRI_232, NPCs.VESTRI_3679)
}
