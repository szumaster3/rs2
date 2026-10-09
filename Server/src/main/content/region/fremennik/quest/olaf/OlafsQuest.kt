package content.region.fremennik.quest.olaf

import core.api.*
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.quest.Quest
import core.game.node.entity.skill.Skills
import core.plugin.Initializable
import shared.consts.Items
import shared.consts.Quests
import shared.consts.Vars

@Initializable
class OlafsQuest : Quest(Quests.OLAFS_QUEST, 137, 136, 1, Vars.VARBIT_QUEST_OLAFS_QUEST_PROGRESS_3534, 0, 1, 80) {

    override fun drawJournal(player: Player, stage: Int) {
        super.drawJournal(player, stage)
        var line = 11


        if (stage == 0){
            line(player, "I can begin this quest by talking to !!Olaf Hradson?? who is", line++)
            line(player, "!!north-east of Rellekka??.", line++)
            line(player, "Requirements:", line++)
            line(player, "50 Woodcutting", line++, getStatLevel(player, Skills.WOODCUTTING) >= 50)
            line(player, "40 Firemaking", line++, getStatLevel(player, Skills.FIREMAKING) >= 40)
            line(player, "</col>Good Agility would be an advantage.", line++)
            line(player, "</col>Must be able to defeat a level 100 monster.", line++)
            line(player,     if (isQuestComplete(player, Quests.THE_FREMENNIK_TRIALS)
            ) {
                "---I need to have completed the Fremennik Trails Quest./--"
            } else {
                "!!I need to have completed the Fremennik Trails Quest..??"
            }, line++)
        }

        if(stage == 1){
            line(player, "Having spoken to Olad Hradson, I have agreed to help", line++, false)
            line(player, "him convince his family that he is a good navigator.", line++, false)
        }

        if(stage == 2){
            line(player, "Having spoken to Olad Hradson, I have agreed to help", line++, true)
            line(player, "him convince his family that he is a good navigator.", line++, true)
            line++
            line(player, "I have taken the logs from the windswept tree", line++, true)
            line(player, "to Olaf Hradson.", line++, true)
            line++
            if(getVarbit(player, Vars.VARBIT_QUEST_OLAFS_QUEST_PROGRESS_3534) >= 3) {
                line(player, "I have taken the carvings to Olaf Hradson's", line++, true)
                line(player, "wife, Ingrid, and his son, Volf.", line++, true)
                line(player, "I should talk with Olaf Hradson to get my reward.", line, inInventory(player, Items.DAMP_PLANKS_11031))
            }
            if(inInventory(player, Items.DAMP_PLANKS_11031)) {
                line++
                line(player, "Olaf Hradson is freezing to death, but is too stubborn to go to town.", line++, false)
                line(player, "He has given me some damp firewood to use on his fire.", line, false)
            }
        }

        if(stage == 3){
            line(player, "Having spoken to Olad Hradson, I have agreed to help", line++, true)
            line(player, "him convince his family that he is a good navigator.", line++, true)
            line++
            line(player, "I have taken the logs from the windswept tree", line++, true)
            line(player, "to Olaf Hradson.", line++, true)
            line++
            line(player, "I have taken the carvings to Olaf Hradson's", line++, true)
            line(player, "wife, Ingrid, and his son, Volf.", line++, true)
            line++
            line(player, "Olaf Hradson is freezing to death, but is too stubborn to go to town.", line++, true)
            line(player, "He has given me some damp firewood to use on his fire.", line++, true)
            line++
            line(player, "I have started the fire.", line++, true)
            line(player, "I should talk to Olaf Hradson to see if he will give me my reward now.", line++, false)
        }

        if(stage == 4) {
            line(player, "Having spoken to Olad Hradson, I have agreed to help", line++, true)
            line(player, "him convince his family that he is a good navigator.", line++, true)
            line++
            line(player, "I have taken the logs from the windswept tree", line++, true)
            line(player, "to Olaf Hradson.", line++, true)
            line++
            line(player, "I have taken the carvings to Olaf Hradson's", line++, true)
            line(player, "wife, Ingrid, and his son, Volf.", line++, true)
            line++
            line(player, "Olaf Hradson is freezing to death, but is too stubborn to go to town.", line++, true)
            line(player, "He has given me some damp firewood to use on his fire.", line++, true)
            line++
            line(player, "I have started the fire.", line++, true)
            line++
            line(player, "Olaf Hradson has given me a treasure map, which he believes", line++, false)
            line(player, "to be cursed.", line++, false)
            line(player, "I doubt it is - he simply had it upside down.", line++, false)
            line(player, "There is a treasure buried somewhere, as indicated by the X, and", line++, false)
            line(player, "I intend to claim it!", line++, false)
        }

        if(stage == 98){
            line(player, "Having spoken to Olad Hradson, I have agreed to help", line++, true)
            line(player, "him convince his family that he is a good navigator.", line++, true)
            line++
            line(player, "I have taken the logs from the windswept tree", line++, true)
            line(player, "to Olaf Hradson.", line++, true)
            line++
            line(player, "I have taken the carvings to Olaf Hradson's", line++, true)
            line(player, "wife, Ingrid, and his son, Volf.", line++, true)
            line++
            line(player, "Olaf Hradson is freezing to death, but is too stubborn to go to town.", line++, true)
            line(player, "He has given me some damp firewood to use on his fire.", line++, true)
            line++
            line(player, "I have started the fire.", line++, true)
            line++
            line(player, "Olaf Hradson has given me a treasure map, which he believes", line++, true)
            line(player, "to be cursed.", line++, true)
            line(player, "I doubt it is - he simply had it upside down.", line++, true)
            line(player, "There is a treasure buried somewhere, as indicated by the X, and", line++, true)
            line(player, "I intend to claim it!", line++, true)
            line++
            line(player, "I have gained access to Sven and Helmsman's cave.", line++, false)
            line(player, "I should be careful of the creatures down here, and", line++, false)
            line(player, "keep an eye out for both the treasure and any traps that guard it.", line++, false)
        }

        if(stage == 99){
            line(player, "Having spoken to Olad Hradson, I have agreed to help", line++, true)
            line(player, "him convince his family that he is a good navigator.", line++, true)
            line++
            line(player, "I have taken the logs from the windswept tree", line++, true)
            line(player, "to Olaf Hradson.", line++, true)
            line++
            line(player, "I have taken the carvings to Olaf Hradson's", line++, true)
            line(player, "wife, Ingrid, and his son, Volf.", line++, true)
            line++
            line(player, "Olaf Hradson is freezing to death, but is too stubborn to go to town.", line++, true)
            line(player, "He has given me some damp firewood to use on his fire.", line++, true)
            line++
            line(player, "I have started the fire.", line++, true)
            line++
            line(player, "Olaf Hradson has given me a treasure map, which he believes", line++, true)
            line(player, "to be cursed.", line++, true)
            line(player, "I doubt it is - he simply had it upside down.", line++, true)
            line(player, "There is a treasure buried somewhere, as indicated by the X, and", line++, true)
            line(player, "I intend to claim it!", line++, true)
            line++
            line(player, "I have gained access to Sven and Helmsman's cave.", line++, true)
            line(player, "I should be careful of the creatures down here, and", line++, true)
            line(player, "keep an eye out for both the treasure and any traps that guard it.", line++, true)
            line++
            line(player, "I have found the treasure and defeated Ulfric.", line++, true)
        }

        if (stage == 100) {
            line++
            line(player, "<col=FF0000>QUEST COMPLETE!", line, false)
        }
    }

    override fun finish(player: Player) {
        super.finish(player)
        var ln = 10
        drawReward(player,Items.KEY_11039)
        drawReward(player, "1 Quest Point", ln++)
        drawReward(player, "Access to brine rat cave", ln++)
        drawReward(player, "20,000gp and 4 rubies", ln++)
        drawReward(player, "12k Defence XP", ln)
        rewardXP(player, Skills.DEFENCE, 12000.0)
        addItemOrDrop(player, Items.RUBY_1603, 4)
        addItemOrDrop(player, Items.PARCHMENT_11036)
        addItemOrDrop(player, Items.COINS_995, 20000)
        setVarbit(player, Vars.VARBIT_QUEST_OLAFS_QUEST_PROGRESS_3534, 80, true)
    }

    override fun newInstance(`object`: Any?): Quest {
        return this
    }

}