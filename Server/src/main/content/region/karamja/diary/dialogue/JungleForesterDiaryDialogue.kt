package content.region.karamja.diary.dialogue

import core.game.dialogue.Dialogue
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.diary.Diary
import core.game.node.entity.player.link.diary.DiaryType
import core.plugin.Initializable
import shared.consts.NPCs

/**
 * Represents the Jungle forester dialogue.
 */
@Initializable
class JungleForesterDiaryDialogue(player: Player? = null) : Dialogue(player) {

    override fun open(vararg args: Any?): Boolean {
        npc = args[0] as NPC
        player("I have a question about my Achievement Diary.")
        return true
    }

    override fun handle(interfaceId: Int, buttonId: Int): Boolean {
        when (stage) {
            0 -> {
                if (Diary.canClaimLevelRewards(player, DiaryType.KARAMJA, 2)) {
                    player("I've done all the hard tasks in my Karamja", "Achievement Diary.").also { stage = 9 }
                } else if (Diary.canReplaceReward(player, DiaryType.KARAMJA, 2)) {
                    player("I've seemed to have lost my gloves...").also { stage = 14 }
                } else {
                    options("What is the Achievement Diary?", "What are the rewards?", "How do I claim the rewards?", "See you later.")
                    stage++
                }
            }
            1 -> when (buttonId) {
                1 -> player("What is the Achievement Diary?").also { stage++ }
                2 -> player("What are the rewards?").also { stage = 5 }
                3 -> player("How do I claim the rewards?").also { stage = 7 }
                4 -> end()
            }
            2 -> npc("It's a diary that helps you keep track of particular", "achievements. Here on Karamja it can help you", "discover some quite useful things. Eventually, with", "enough exploration, the people of Karamja will reward").also { stage++ }
            3 -> npc("you.").also { stage++ }
            4 -> npc("You can see what tasks you have listed by clicking on", "the green button in the Quest List.").also { stage = 0 }
            5 -> npc("Well, there's three different pairs of Karamja gloves,", "which match up with the three levels of difficulty. Each", "has the same rewards as the previous level, and an", "additional one too... but I won't spoil your surprise.").also { stage++ }
            6 -> npc("Rest assured, the people of Karamja are happy to see", "you visiting the island.").also { stage = 0 }
            7 -> npc("Just complete the tasks so they're all ticked off, then", "you can claim yer reward. Most of them are", "straightforward; you might find some require quests to", "be started, if not finished.").also { stage++ }
            8 -> npc("To claim the different Karamja gloves, speak to Pirate", "Jackie the Fruit in Brim Haven, one of the jungle foresters", "near the Kharazi Jungle, or me.").also { stage = 0 }
            9 -> npc("Yes I see that, you'll be wanting your", "reward then I assume?").also { stage++ }
            10 -> player("Yes please.").also { stage++ }
            11 -> {
                Diary.flagRewarded(player, DiaryType.KARAMJA, 2)
                npc("These Karamja gloves are a symbol of your exploring'", "on the island. All the merchants will recognise them", "and maybe give you a discount. I'll", "have a word with some of the seafaring' folk who sail to")
                stage++
            }
            12 -> npc("Port Sarim and Ardougne, so they'll take you on board", "half price if you're wearing them. Take this lamp I", "found washed ashore too.").also { stage++ }
            13 -> player("Wow, thanks!").also { stage = 0 }
            14 -> {
                Diary.grantReplacement(player, DiaryType.KARAMJA, 2)
                npc("You better be more careful this time.")
                stage = 0
            }
        }
        return true
    }

    override fun getIds(): IntArray = intArrayOf(
        NPCs.JUNGLEFORESTER_M_401,
        NPCs.JUNGLEFORESTER_F_402
    )

}