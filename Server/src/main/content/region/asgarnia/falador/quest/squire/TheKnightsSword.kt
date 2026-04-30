package content.region.asgarnia.falador.quest.squire

import core.api.*
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.quest.Quest
import core.game.node.entity.skill.Skills
import core.plugin.Initializable
import shared.consts.Items
import shared.consts.Quests
import shared.consts.Vars

/**
 * The knight's sword quest journal.
 */
@Initializable
class TheKnightsSword : Quest(Quests.THE_KNIGHTS_SWORD, 22, 21, 1, Vars.VARP_QUEST_KNIGHTS_SWORD_PROGRESS_122, 0, 1, 7) {

    override fun drawJournal(player: Player, stage: Int) {
        super.drawJournal(player, stage)
        var line = 11
        when (stage) {
            0 -> {
                line(player, "I can start this quest by speaking to the !!Squire?? in the", line++)
                line(player, "courtyard of the !!White Knights' Castle?? in !!southern Falador??", line++)
                line(player, "To complete this quest I need:", line++)
                line(player,"!!Level 10 Mining??", line++)
                line(player, "and to be unafraid of !!Level 57 Ice Warriors??.", line++)
            }

            10 -> {
                line(player, "I told the Squire I would help him to replace the sword he", line++, true)
                line(player, "has lost. It could only be made by an Imcando Dwarf.", line++, true)
                line(player, "The Squire suggests I speak to !!Reldo?? in the !!Varrock Palace??",  line++)
                line(player, "!!Library?? for information about the !!Imcando Dwarves??",  line++)
            }

            20 -> {
                line(player, "I told the Squire I would help him to replace the sword he", line++, true)
                line(player, "has lost. It could only be made by an Imcando Dwarf.", line++, true)
                line(player, "Reldo couldn't give me much information about the", line++)
                line(player, "!!Imcando?? except a few live on the !!southern peninsula of??", line++)
                line(player, "!!Asgarnia??, they dislike strangers, and LOVE !!redberry pies??.", line++)
            }

            30 -> {
                line(player, "I told the Squire I would help him to replace the sword he", line++, true)
                line(player, "has lost. It could only be made by an Imcando Dwarf.", line++, true)
                line(player, "I found an Imcando Dwarf named Thurgo thanks to", line++, true)
                line(player, "information provided by Reldo. He wasn't very talkative", line++, true)
                line(player, "until I gave him a Redberry pie, which he gobbled up.", line++, true)
                line(player, "He will help me now I have gained his trust through !!pie??", line++)
            }

            40 -> {
                line(player, "I told the Squire I would help him to replace the sword he", line++, true)
                line(player, "has lost. It could only be made by an Imcando Dwarf.", line++, true)
                line(player, "I found an Imcando Dwarf named Thurgo thanks to", line++, true)
                line(player, "information provided by Reldo. He wasn't very talkative", line++, true)
                line(player, "until I gave him a Redberry pie, which he gobbled up.", line++, true)
                line(player, "!!Thurgo?? needs a !!picture of the sword?? before he can help.", line++)
                line(player, "I should probably ask the !!Squire?? about obtaining one", line++)
            }

            50 -> {
                line(player, "I told the Squire I would help him to replace the sword he", line++, true)
                line(player, "has lost. It could only be made by an Imcando Dwarf.", line++, true)
                line(player, "I found an Imcando Dwarf named Thurgo thanks to", line++, true)
                line(player, "information provided by Reldo. He wasn't very talkative", line++, true)
                line(player, "until I gave him a Redberry pie, which he gobbled up.", line++, true)
                line(player, "Thurgo needed a picture of the sword to replace.", line++, true)
                if (!inInventory(player, PORTRAIT)) {
                    line(player, "The Squire told me about a !!portrait??", line++)
                    line(player, "which has a !!picture of the sword?? in !!Sir Vyvin's room??", line++)
                } else {
                    line(player, "I now have a picture of the !!Knight's Sword?? - I should take it", line++)
                    line(player, "to !!Thurgo?? so that he can duplicate it.", line++)
                }
            }

            60 -> {
                line(player, "I told the Squire I would help him to replace the sword he", line++, true)
                line(player, "has lost. It could only be made by an Imcando Dwarf.", line++, true)
                line(player, "I found an Imcando Dwarf named Thurgo thanks to", line++, true)
                line(player, "information provided by Reldo. He wasn't very talkative", line++, true)
                line(player, "until I gave him a Redberry pie, which he gobbled up.", line++, true)
                line(player, "Thurgo needed a picture of the sword before he could", line++, true)
                line(player, "start work on a replacement. I took him a portrait of it.", line++, true)
                if (inInventory(player, Items.BLURITE_SWORD_667, 1) || inEquipment(player, Items.BLURITE_SWORD_667, 1) || inBank(player, Items.BLURITE_SWORD_667, 1)) {
                    line(player, "Thurgo has now smithed me a replica of Sir Vyvin's sword.", line++, true)
                    line(player, "I should return it to the !!Squire?? for my !!reward??", line++)
                } else {
                    line(player, "according to !!Thurgo?? to make a !!replica sword?? he will need", line++)
                    line(player, "!!two Iron Bars?? and some !!Blurite Ore??. !!Blurite Ore?? can only be", line++)
                    line(player, "found !!deep in the caves below Thurgo's house??", line++)
                }
            }

            100 -> {
                line(player, "Thurgo needed a picture of the sword before he could", line++, true)
                line(player, "start work on a replacement. I took him a portrait of it.", line++, true)
                line(player, "After bringing Thurgo two iron bars and some blurite ore", line++, true)
                line(player, "he made me a fine replica of Sir Vyvin's Sword, which I", line++, true)
                line(player, "returned to the Squire for a reward.", line++, true)
                line(player, "<col=FF0000>QUEST COMPLETE!</col>", line++, false)
            }
        }
    }

    override fun finish(player: Player) {
        super.finish(player)
        var line = 10

        drawReward(player, Items.BLURITE_SWORD_667)
        drawReward(player, "1 Quest Point", line++)
        drawReward(player, "12,725 Smithing XP", line)
        rewardXP(player, Skills.SMITHING, 12725.0)
        updateQuestTab(player)
    }

    override fun newInstance(`object`: Any?): Quest {
        return this
    }

    companion object {
        private const val PORTRAIT = Items.PORTRAIT_666
    }
}