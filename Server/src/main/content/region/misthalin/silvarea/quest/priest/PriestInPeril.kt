package content.region.misthalin.silvarea.quest.priest

import core.api.addItemOrDrop
import core.api.drawReward
import core.api.removeAttribute
import core.api.rewardXP
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.quest.Quest
import core.game.node.entity.skill.Skills
import core.plugin.Initializable
import shared.consts.Items
import shared.consts.Quests
import shared.consts.Vars

/**
 * Represents the Priest in peril quest journal.
 * @author Vexia
 */
@Initializable
class PriestInPeril : Quest(Quests.PRIEST_IN_PERIL, 99, 98, 1, Vars.VARP_QUEST_PRIEST_IN_PERIL_PROGRESS_302, 0, 1, 100) {

    override fun drawJournal(player: Player, stage: Int) {
        super.drawJournal(player, stage)
        var line = 12
        when (stage) {
            0 -> {
                line(player, "I can start this quest by speaking to !!King Roald?? in !!Varrock??", line++)
                line(player, "!!Palace??.",  line++)
                line(player, "I must be able to defeat a !!level 30 enemy??", line)
                limitScrolling(player, line, true)
            }
            10 -> {
                line(player, "I spoke to King Roald who asked me to investigate why his", line++, true)
                line(player, "friend Priest Drezel has stopped communicating with him.", line++, true)
                line(player, "!!Drezel?? lives in a !!temple?? to the east of Varrock Palace. I", line++)
                line(player, "should head there and !!investigate?? what's happened to him", line)
            }
            11 -> {
                line(player, "I spoke to King Roald who asked me to investigate why his", line++, true)
                line(player, "friend Priest Drezel has stopped communicating with him.", line++, true)
                line(player, "I headed to the temple where Drezel lives, but it was all", line++, true)
                line(player, "locked shut. I spoke through the locked door to Drezel.", line++, true)
                line(player, "He told me that there was an annoying !!dog?? below the", line++)
                line(player, "temple, and has asked me to !!kill it?? for him.", line)
            }
            12 -> {
                line(player, "I spoke to King Roald who asked me to investigate why his", line++, true)
                line(player, "friend Priest Drezel has stopped communicating with him.", line++, true)
                line(player, "I headed to the temple where Drezel lives, but it was all", line++, true)
                line(player, "locked shut. I spoke through the locked door to Drezel.", line++, true)
                line(player, "He told me that there was an annoying dog below the", line++, true)
                line(player, "temple, and asked me to kill it, which I did easily.", line++, true)
                line(player, "I should tell !!King Roald?? everything's fine with !!Drezel?? now I", line++)
                line(player, "have killed that !!dog?? for him, and claim my !!reward??.", line)
            }
            13 -> {
                line(player, "I spoke to King Roald who asked me to investigate why his", line++, true)
                line(player, "friend Priest Drezel has stopped communicating with him.", line++, true)
                line(player, "I headed to the temple where Drezel lives, but it was all", line++, true)
                line(player, "locked shut. I spoke through the locked door to Drezel.", line++, true)
                line(player, "He told me that there was an annoying dog below the", line++, true)
                line(player, "temple, and asked me to kill it, which I did easily.", line++, true)
                line(player, "When I told Roald what I had done, he was furious. The", line++, true)
                line(player, "person who told me to kill the dog wasn't Drezel at all!", line++, true)
                line(player, "I must return to the !!temple?? and find out what happened to", line++)
                line(player, "the real !!Drezel??, or the King will have me executed!", line)
            }
            14 -> {
                line(player, "I spoke to King Roald who asked me to investigate why his", line++, true)
                line(player, "friend Priest Drezel has stopped communicating with him.", line++, true)
                line(player, "I headed to the temple where Drezel lives, but it was all", line++, true)
                line(player, "locked shut. I spoke through the locked door to Drezel.", line++, true)
                line(player, "He told me that there was an annoying dog below the", line++, true)
                line(player, "temple, and asked me to kill it, which I did easily.", line++, true)
                line(player, "When I told Roald what I had done, he was furious. The", line++, true)
                line(player, "person who told me to kill the dog wasn't Drezel at all!", line++, true)
                line(player, "I returned to the temple and found the real Drezel locked", line++, true)
                line(player, "in a makeshift cell upstairs, guarded by a vampire.", line++, true)
                line(player, "I need to find the !!key?? to his cell and free him!", line)
            }
            15 -> {
                line(player, "I spoke to King Roald who asked me to investigate why his", line++, true)
                line(player, "friend Priest Drezel has stopped communicating with him.", line++, true)
                line(player, "I headed to the temple where Drezel lives, but it was all", line++, true)
                line(player, "locked shut. I spoke through the locked door to Drezel.", line++, true)
                line(player, "He told me that there was an annoying dog below the",  line++, true)
                line(player, "temple, and asked me to kill it, which I did easily.", line++, true)
                line(player, "When I told Roald what I had done, he was furious. The", line++, true)
                line(player, "person who told me to kill the dog wasn't Drezel at all!", line++, true)
                line(player, "I returned to the temple and found the real Drezel locked", line++, true)
                line(player, "in a makeshift cell upstairs, guarded by a vampire.", line++, true)
                line(player, "I used a key from the monument to open the cell door", line++, true)
                line(player, "but I still have to do something about that !!vampire??", line)
            }
            16 -> {
                line(player, "I spoke to King Roald who asked me to investigate why his", line++, true)
                line(player, "friend Priest Drezel has stopped communicating with him.", line++, true)
                line(player, "I headed to the temple where Drezel lives, but it was all", line++, true)
                line(player, "locked shut. I spoke through the locked door to Drezel.", line++, true)
                line(player, "He told me that there was an annoying dog below the", line++, true)
                line(player, "temple, and asked me to kill it, which I did easily.", line++, true)
                line(player, "When I told Roald what I had done, he was furious. The", line++, true)
                line(player, "person who told me to kill the dog wasn't Drezel at all!", line++, true)
                line(player, "I returned to the temple and found the real Drezel locked", line++, true)
                line(player, "in a makeshift cell upstairs, guarded by a vampire.", line++, true)
                line(player, "I used a key from the monument to open the cell door and", line++, true)
                line(player, "used Holy Water to trap the vampire in his coffin.",  line++, true)
                line(player, "I should speak to !!Drezel?? again.", line)
            }
            17 -> {
                line(player, "I spoke to King Roald who asked me to investigate why his", line++, true)
                line(player, "friend Priest Drezel has stopped communicating with him.", line++, true)
                line(player, "I headed to the temple where Drezel lives, but it was all", line++, true)
                line(player, "locked shut. I spoke through the locked door to Drezel.", line++, true)
                line(player, "He told me that there was an annoying dog below the", line++, true)
                line(player, "temple, and asked me to kill it, which I did easily.", line++, true)
                line(player, "When I told Roald what I had done, he was furious. The", line++, true)
                line(player, "person who told me to kill the dog wasn't Drezel at all!", line++, true)
                line(player, "I returned to the temple and found the real Drezel locked", line++, true)
                line(player, "in a makeshift cell upstairs, guarded by a vampire.", line++, true)
                line(player, "I used a key from the monument to open the cell door and", line++, true)
                line(player, "used Holy Water to trap the vampire in his coffin.", line++, true)
                line(player, "I should head downstairs to the !!monument?? like !!Drezel??",line++)
                line(player, "asked me to, and asses what !!damage?? has been done.", line)
            }
            18 -> {
                line(player, "I spoke to King Roald who asked me to investigate why his", line++, true)
                line(player, "friend Priest Drezel has stopped communicating with him.", line++, true)
                line(player, "I headed to the temple where Drezel lives, but it was all", line++, true)
                line(player, "locked shut. I spoke through the locked door to Drezel.", line++, true)
                line(player, "He told me that there was an annoying dog below the", line++, true)
                line(player, "temple, and asked me to kill it, which I did easily.", line++, true)
                line(player, "When I told Roald what I had done, he was furious. The", line++, true)
                line(player, "person who told me to kill the dog wasn't Drezel at all!", line++, true)
                line(player, "I returned to the temple and found the real Drezel locked", line++, true)
                line(player, "in a makeshift cell upstairs, guarded by a vampire.", line++, true)
                line(player, "I used a key from the monument to open the cell door and", line++, true)
                line(player, "used Holy Water to trap the vampire in his coffin.", line++, true)
                line(player, "I followed Drezel downstairs only to find that the Salve", line++, true)
                line(player, "had been contaminated and now needed purifying", line++, true)
                val amt = player.gameAttributes.getAttribute("priest-in-peril:rune", 50)
                line(player, "I need to bring !!$amt?? rune essence to undo the damage", line++)
                line(player, "done by the Zamorakians and !!purify the salve??", line)
            }
            100 -> {
                line(player, "I spoke to King Roald who asked me to investigate why his", line++, true)
                line(player, "friend Priest Drezel has stopped communicating with him.", line++, true)
                line(player, "I headed to the temple where Drezel lives, but it was all", line++, true)
                line(player, "locked shut. I spoke through the locked door to Drezel.", line++, true)
                line(player, "He told me that there was an annoying dog below the",  line++, true)
                line(player, "temple, and asked me to kill it, which I did easily.", line++, true)
                line(player, "When I told Roald what I had done, he was furious. The", line++, true)
                line(player, "person who told me to kill the dog wasn't Drezel at all!", line++, true)
                line(player, "I returned to the temple and found the real Drezel locked", line++, true)
                line(player, "in a makeshift cell upstairs, guarded by a vampire.", line++, true)
                line(player, "I used a key from the monument to open the cell door and", line++, true)
                line(player, "used Holy Water to trap the vampire in his coffin.", line++, true)
                line(player, "I followed Drezel downstairs only to find that the Salve", line++, true)
                line(player, "had been contaminated and now needed purifying", line++, true)
                line(player, "I brought Drezel fifty rune essences and the", line++, true)
                line(player, "contaminants were dissolved from the Salve, and Drezel", line++, true)
                line(player, "Rewarded me for all of my help with an ancient holy weapon", line++, true)
                line(player, "to fight with.", line++, true)
                line++
                line(player, "<col=FF0000>QUEST COMPLETE!</col>",  line)
            }
        }
        limitScrolling(player, line, false)
    }

    override fun finish(player: Player) {
        super.finish(player)
        var line = 10

        drawReward(player, Items.WOLFBANE_2952)
        drawReward(player, "1 Quest Point", line++)
        drawReward(player, "1406 Prayer XP", line++)
        drawReward(player, "Wolfbane dagger", line++)
        drawReward(player, "Route to Canifis", line)
        rewardXP(player, Skills.PRAYER, 1406.0)
        addItemOrDrop(player,Items.WOLFBANE_2952)

        removeAttribute(player, "priest_in_peril:key")
        removeAttribute(player, "priest-in-peril:rune")
    }

    override fun newInstance(`object`: Any?): Quest = this

}