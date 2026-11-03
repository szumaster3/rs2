package content.region.asgarnia.burthorpe.quest.hero.dialogue

import content.region.asgarnia.burthorpe.quest.hero.HeroesQuest
import core.api.*
import core.game.dialogue.Dialogue
import core.game.dialogue.FaceAnim
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.tools.END_DIALOGUE
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Quests

/**
 * Represents the Achietties dialogue.
 *
 * Relations:
 * - [HeroesQuest]
 */
@Initializable
class AchiettiesDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        when (getQuestStage(player!!, Quests.HEROES_QUEST)) {
            0 -> npc(FaceAnim.FRIENDLY, "Greetings. Welcome to the Heroes' Guild.").also { stage = 0 }
            1, 2, 3, 4 -> npc(FaceAnim.FRIENDLY, "Greetings. Welcome to the Heroes' Guild.").also { stage = 10 }
            6 -> npc(FaceAnim.FRIENDLY, "Greetings. Welcome to the Heroes' Guild.").also { stage = 20 }
            else -> return false
        }

        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> npc(FaceAnim.FRIENDLY, "Only the greatest heroes of this land may gain entrance to this guild.").also { stage++ }
            1 -> options("I'm a hero, may I apply to join?", "Good for the foremost heroes of the land.").also { stage++ }
            2 -> when (buttonId) {
                1 -> playerl(FaceAnim.NEUTRAL, "I'm a hero. May I apply to join?").also { stage = 3 }
                2 -> playerl(FaceAnim.NEUTRAL, "Good for the foremost heroes of the land.").also { stage = 9 }
            }
            3 -> {
                if (HeroesQuest.hasRequirements(player)) {
                    if (getQuestStage(player, Quests.HEROES_QUEST) == 0) {
                        setQuestStage(player, Quests.HEROES_QUEST, 1)
                    }
                    npcl(FaceAnim.FRIENDLY, "Well you seem to meet our initial requirements, so you may now begin the tasks to earn membership in the Heroes' Guild.").also { stage++ }
                } else {
                    npcl(FaceAnim.FRIENDLY, "You're a hero? I've never heard of YOU. You are required to possess at least 55 quest points to file an application.").also { stage = 4 }
                }
            }
            4 -> npcl(FaceAnim.FRIENDLY, "Additionally you must have completed the Shield of Arrav, Lost City, Merlin's Crystal and Dragon Slayer quests.").also { stage = END_DIALOGUE }
            5 -> npcl(FaceAnim.FRIENDLY, "The three items required for entrance are: An Entranan Firebird feather, a Master Thieves' armband, and a cooked Lava Eel.").also { stage++ }
            6 -> options("Any hints on getting the thieves armband?", "Any hints on getting the feather?", "Any hints on getting the eel?", "I'll start looking for all those things then.").also { stage = 100 }
            9 -> npcl(FaceAnim.FRIENDLY, "Yes. Yes it is.").also { stage = END_DIALOGUE }
            // Quest progress dialogue.
            10 -> npcl(FaceAnim.FRIENDLY, "How goes thy quest adventurer?").also { stage++ }
            11 -> playerl(FaceAnim.NEUTRAL, "It's tough. I've not done it yet.").also { stage++ }
            12 -> npcl(FaceAnim.FRIENDLY, "Remember, the items you need to enter are:").also { stage++ }
            13 -> npcl(FaceAnim.FRIENDLY, "An Entranan Firebirds' feather, A Master Thieves armband, and a cooked Lava Eel.").also { stage++ }
            14 -> options("Any hints on getting the thieves armband?", "Any hints on getting the feather?", "Any hints on getting the eel?", "I'll start looking for all those things then.").also { stage = 100 }
            // Final task.
            20 -> npcl(FaceAnim.FRIENDLY, "How goes thy quest adventurer?").also { stage++ }
            21 -> {
                if (HeroesQuest.allItemsInInventory(player)) {
                    playerl(FaceAnim.NEUTRAL, "I have all the required items.").also { stage = 30 }
                } else {
                    playerl(FaceAnim.NEUTRAL, "It's tough. I've not done it yet.").also { stage = 11 }
                }
            }
            30 -> npcl(FaceAnim.FRIENDLY, "I see that you have. Well done. Now, to complete the quest, and gain entry to the Heroes' Guild in your final task all that you have to do is...").also { stage++ }
            31 -> playerl(FaceAnim.NEUTRAL, "W-what? What do you mean? There's MORE?").also { stage++ }
            32 -> npcl(FaceAnim.FRIENDLY, "I'm sorry, I was just having a little fun with you. Just a little Heroes' Guild humour there. What I really meant was").also { stage++ }
            33 -> npcl(FaceAnim.FRIENDLY, "Congratulations! You have completed the Heroes' Guild entry requirements! You will find the door now open for you! Enter, Hero! And take this reward!").also { stage++ }
            34 -> {
                if (HeroesQuest.allItemsInInventory(player)) {
                    removeItem(player, Items.FIRE_FEATHER_1583)
                    removeItem(player, Items.LAVA_EEL_2149)
                    removeItem(player, Items.THIEVES_ARMBAND_1579)

                    if (getQuestStage(player, Quests.HEROES_QUEST) == 6) {
                        finishQuest(player, Quests.HEROES_QUEST)
                    }
                }

                stage = END_DIALOGUE
            }
            // Hint options.
            100 -> when (buttonId) {
                1 -> playerl(FaceAnim.NEUTRAL, "Any hints on getting the thieves armband?").also { stage++ }
                2 -> playerl(FaceAnim.NEUTRAL, "Any hints on getting the feather?").also { stage = 102 }
                3 -> playerl(FaceAnim.NEUTRAL, "Any hints on getting the eel?").also { stage = 103 }
                4 -> playerl(FaceAnim.NEUTRAL, "I'll start looking for all those things then.").also { stage = 104 }
            }
            101 -> npcl(FaceAnim.FRIENDLY, "I'm sure you have the relevant contacts to find out about that.").also { stage = END_DIALOGUE }
            102 -> npcl(FaceAnim.FRIENDLY, "Not really - other than Entranan firebirds tend to live on Entrana.").also { stage = END_DIALOGUE }
            103 -> npcl(FaceAnim.FRIENDLY, "Maybe go and find someone who knows a lot about fishing?").also { stage = END_DIALOGUE }
            104 -> npcl(FaceAnim.FRIENDLY, "Good luck with that.").also { stage = END_DIALOGUE }
        }

        return true
    }

    override fun newInstance(player: Player?): Dialogue = AchiettiesDialogue(player)

    override fun getIds(): IntArray = intArrayOf(NPCs.ACHIETTIES_796)
}
