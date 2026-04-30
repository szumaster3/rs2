package content.region.desert.uzer.quest.golem

import core.api.drawReward
import core.api.getAttribute
import core.api.getStatLevel
import core.api.rewardXP
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.quest.Quest
import core.game.node.entity.skill.Skills
import core.plugin.Initializable
import shared.consts.Items
import shared.consts.Quests
import shared.consts.Vars

@Initializable
class TheGolem : Quest(Quests.THE_GOLEM, 70, 69, 1, Vars.VARBIT_QUEST_THE_GOLEM_PROGRESS_346, 0, 1, 10) {
    override fun drawJournal(player: Player, stage: Int) {
        super.drawJournal(player, stage)
        var ln = 11
        if (stage == 0) {
            line(player, "I can start this quest by talking to !!the golem?? who is in the:", ln++, false)
            line(player, "Ruined city of !!Uzer??, which is in the desert to the east of", ln++, false)
            line(player, "the !!Shantay Pass.", ln++, false)
            line(player, "I will need to have !!level 20 crafting?? and !!level 25 thieving", ln++, false)
        }
        if (stage >= 1) {
            line(player, "I've found !!the golem??, and offered to !!repair?? it.", ln++, stage > 1)
        }
        if (stage >= 2) {
            line(player, "I've !!repaired?? the golem with some !!soft clay??.", ln++, stage > 2)
        }
        if (stage >= 3) {
            line(player, "The golem wants me to open a portal to help it defeat", ln++, stage > 3)
            line(player, "the demon that attacked its city.", ln++, stage > 3)
        }
        val readLetter = getAttribute(player, "the-golem:read-elissa-letter", false)
        val readBook = getAttribute(player, "the-golem:varmen-notes-read", false)
        if (readLetter) {
            line(player, "I've found a letter that mentions !!The Digsite", ln++, readBook)
        }
        if (readBook) {
            line(player, "I've found a !!book?? that mentions that golems are !!programmed by??", ln++, stage > 7)
            line(player, "!!writing instructions?? on !!papyrus?? with a !!phoenix quill pen??.", ln++, stage > 7)
        }
        val hasStatuette = TheGolemListener.hasStatuette(player)
        val doorOpen = getAttribute(player, "the-golem:door-open", false)
        if (hasStatuette) {
            line(player, "I've acquired a statuette that fits a !!mechanism?? in the !!ruins??", ln++, doorOpen)
            line(player, "of !!Uzer?? from the !!Varrock museum??.", ln++, doorOpen)
        }
        val seenDemon = getAttribute(player, "the-golem:seen-demon", false)
        if (doorOpen) {
            line(player, "I've opened the portal in the !!ruins of Uzer??.", ln++, seenDemon)
        }
        if (seenDemon) {
            line(player, "It turns out that !!the demon?? is !!already dead??!", ln++, stage > 4)
            line(player, "I should tell the golem the good news.", ln++, stage > 4)
        }
        if (stage > 4) {
            line(player, "The demon doesn't think its task is complete.", ln++, stage > 7)
        }
        if (stage >= 100) {
            ln++
            line(player, "<col=FF0000>QUEST COMPLETE!</col>", ln, false)
        }
    }

    override fun hasRequirements(player: Player?): Boolean =
        getStatLevel(player!!, Skills.CRAFTING) >= 20 && getStatLevel(player, Skills.THIEVING) >= 25

    override fun finish(player: Player) {
        super.finish(player)
        var ln = 10
        drawReward(player, Items.STATUETTE_4618)
        drawReward(player, "1 quest point", ln++)
        drawReward(player, "1,000 Crafting XP", ln++)
        drawReward(player, "1,000 Theiving XP", ln)
        rewardXP(player, Skills.CRAFTING, 1000.0)
        rewardXP(player, Skills.THIEVING, 1000.0)
    }

    override fun newInstance(`object`: Any?): Quest = this

    override fun updateVarps(player: Player?) {
        TheGolemListener.updateVarps(player!!)
    }
}