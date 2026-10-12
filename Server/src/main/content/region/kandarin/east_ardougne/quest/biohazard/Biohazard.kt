package content.region.kandarin.east_ardougne.quest.biohazard

import content.data.GameAttributes
import core.api.getAttribute
import core.api.isQuestComplete
import core.api.removeAttributes
import core.api.rewardXP
import core.api.sendItemZoomOnInterface
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.quest.Quest
import core.game.node.entity.skill.Skills
import core.plugin.Initializable
import shared.consts.Components
import shared.consts.Items
import shared.consts.Quests
import shared.consts.Vars

@Initializable
class Biohazard : Quest(Quests.BIOHAZARD, 36, 35, 3, Vars.VARP_QUEST_BIOHAZARD_PROGRESS_68, 0, 1, 16) {

    override fun drawJournal(player: Player, stage: Int) {
        super.drawJournal(player, stage)
        var line = 11
        if (stage == 0) {
            line(player, "I can start this quest by speaking to !!Elena?? who is in !!East??", line++)
            line(player, "!!Ardougne??.", line++)
            line++
            line(player, "Requirements:", line++)
            line(player, "!!I need to complete ${Quests.PLAGUE_CITY} before I can attempt this??", line++, isQuestComplete(player, Quests.PLAGUE_CITY))
            line(player, "!!Quest??.", line++, isQuestComplete(player, Quests.PLAGUE_CITY))
            line++
        }

        if (stage > 0) {
            line(player, "I've spoken to Elena in East Ardougne. She told me that", line++, true)
            line(player, "the Mourners confiscated her Distillator while she was", line++, true)
            line(player, "helping plague victims in West Ardougne. She's asked me", line++, true)
            line(player, "to help her recover it.", line++, true)
            line++
        }

        if (stage == 1) {
            line(player, "I need to get into !!West Ardougne?? to find !!Elena's Distillator??.", line++)
            line(player, "However, the !!Mourners?? have blocked the tunnel I used", line++)
            line(player, "before. !!Elena?? suggested I talk to !!Jerico??, an old friend of", line++)
            line(player, "her father's, and ask for his help in crossing the wall. His", line++)
            line(player, "home is north of the chapel in !!East Ardougne??.", line++)
            line++
        } else if (stage > 1) {
            line(player, "I spoke to Jerico, an old friend of Elena's father, about", line++, true)
            line(player, "finding another way into West Ardougne.", line++, true)
            line++
        }

        if (stage == 2) {
            line(player, "!!Jerico?? told me that !!Omart??, a friend of his, should be able", line++)
            line(player, "to help me get into !!West Ardougne??. He is waiting for me at", line++)
            line(player, "the southern end of the wall. However, I need to distract", line++)
            line(player, "the guards in the !!Watchtower?? first. !!Jerico?? may have an idea.", line++)
            line++
        } else if (stage > 2) {
            line(player, "Omart, a friend of Jerico, agreed to help me cross the wall", line++, true)
            line(player, "into West Ardougne, but I had to distract the guards in the", line++, true)
            line(player, "Watchtower first.", line++, true)
            line++
        }

        if (stage == 3) {
            line(player, "I found some !!Bird Feed?? in !!Jerico's?? cupboard. I should throw", line++)
            line(player, "it near the tower, then release some !!Pigeons?? from the", line++)
            line(player, "corner of the fence and the guards should be distracted.", line++)
            line++
        } else if (stage > 3) {
            line(player, "I threw some Bird Feed near the tower and released some", line++, true)
            line(player, "Pigeons to distract the guards.", line++, true)
            line++
        }

        if (stage in 4..5) {
            line(player, "The guards are distracted! I should speak to !!Omart?? and cross", line++)
            line(player, "the wall into !!West Ardougne??. !!Elena's Distillator?? should be", line++)
            line(player, "somewhere in the !!Mourner Headquarters?? in the north east", line++)
            line(player, "of the city. I need to get in and recover it.", line++)
            line++
        } else if (stage > 5) {
            line(player, "Omart and Kilron, friends of Jerico, helped me cross the", line++, true)
            line(player, "wall into West Ardougne.", line++, true)
            line++
        }

        if (stage == 6) {
            line(player, "I used a !!Rotten Apple?? to poison the !!Mourners' Stew??. The", line++)
            line(player, "mourners are waiting for a doctor, so I need a !!Doctor's Gown??", line++)
            line(player, "to get into the !!Mourner Headquarters??.", line++)
            line++
        } else if (stage > 6) {
            line(player, "I poisoned the Mourners' Stew with a Rotten Apple and, wearing", line++, true)
            line(player, "a Doctor's Gown, got into the Mourner Headquarters.", line++, true)
            line++
        }

        if (stage in 7..9) {
            line(player, "I managed to get into the !!Mourner Headquarters?? in !!West??", line++)
            line(player, "!!Ardougne??. I should see if I can find !!Elena's Distillator??", line++)
            line(player, "inside.", line++)
            line++
        }

        if (stage == 10) {
            line(player, "I found !!Elena's Distillator?? in the !!Mourner Headquarters??. I", line++)
            line(player, "should return it to her.", line++)
            line++
        } else if (stage > 10) {
            line(player, "I found Elena's Distillator and returned it to her.", line++, true)
            line++
        }

        if (stage == 10 && getAttribute(player, GameAttributes.ELENA_REPLACE, false)) {
            line(player, "!!Elena?? ran some tests on a !!Plague Sample?? using her", line++)
            line(player, "!!Distillator??. However, she wasn't able to discover anything.", line++)
            line(player, "She asked me to go to the !!Chemist?? in !!Rimmington?? to pick up", line++)
            line(player, "some !!Touch Paper?? before continuing on to take some", line++)
            line(player, "items to !!Guidor?? in !!Varrock??.", line++)
            line++
        } else if (stage in 11..15) {
            line(player, "Elena ran some tests on a Plague Sample using her", line++, true)
            line(player, "Distillator. However, she wasn't able to discover anything.", line++, true)
            line++
            line(player, "I have !!Touch Paper?? from the !!Chemist?? in !!Rimmington??. I need to", line++)
            line(player, "take the vials, the sample and the paper to !!Guidor??, who", line++)
            line(player, "lives in the south east of !!Varrock??. The !!Chemist?? warned me", line++)
            line(player, "that carrying the vials into !!Varrock?? is risky and suggested", line++)
            line(player, "I give one to each of his !!Errand Boys??. They like to steal,", line++)
            line(player, "so I should not give them something I need. They will", line++)
            line(player, "wait at the !!Dancing Donkey Inn?? in south east !!Varrock??.", line++)
            line++
        } else if (stage > 15) {
            line(player, "Elena ran some tests on a Plague Sample using her", line++, true)
            line(player, "Distillator, but couldn't discover anything. She sent me to", line++, true)
            line(player, "Guidor in Varrock, with the help of the Chemist's Errand Boys.", line++, true)
            line++
        }

        if (stage in 16..98) {
            line(player, "!!Guidor?? ran his own tests on the !!Plague Sample?? and", line++)
            line(player, "discovered a disturbing truth, that the !!Plague?? of !!West??", line++)
            line(player, "!!Ardougne?? is a lie. I should return to !!Elena?? and tell her.", line++)
            line++
        } else if (stage >= 99) {
            line(player, "Guidor ran his tests and discovered a disturbing truth,", line++, true)
            line(player, "that the Plague of West Ardougne is a lie.", line++, true)
            line++
        }

        if (stage == 99) {
            line(player, "I returned to !!Elena?? and told her the truth about the !!Plague??.", line++)
            line(player, "She asked me to confront !!King Lathas?? immediately. I can", line++)
            line(player, "find him in !!Ardougne Castle??.", line++)
            line++
        }

        if (stage == 100) {
            line(player, "I confronted King Lathas and he revealed the truth to me.", line++, true)
            line(player, "The Plague is a hoax he created to keep people safe from", line++, true)
            line(player, "his brother, King Tyras, who was corrupted by the Dark", line++, true)
            line(player, "Lord on an expedition to the west. King Lathas rewarded", line++, true)
            line(player, "me and warned me to prepare for the challenges to come.", line++, true)
            line++
            line(player, "%%QUEST COMPLETE!&&", line)
        }
    }

    override fun finish(player: Player) {
        super.finish(player)
        var line = 10
        sendItemZoomOnInterface(player, Components.QUEST_COMPLETE_SCROLL_277, 5, Items.DISTILLATOR_420, 230)
        drawReward(player, "3 Quest Points", line++)
        drawReward(player, "1250 Thieving XP", line)
        rewardXP(player, Skills.THIEVING, 1250.0)
        removeAttributes(
            player,
            GameAttributes.FEED_ON_FENCE,
            GameAttributes.ELENA_REPLACE,
            GameAttributes.FIRST_VIAL_CORRECT,
            GameAttributes.FIRST_VIAL_WRONG,
            GameAttributes.SECOND_VIAL_CORRECT,
            GameAttributes.SECOND_VIAL_WRONG,
            GameAttributes.THIRD_VIAL_CORRECT,
            GameAttributes.THIRD_VIAL_WRONG,
        )
    }

    override fun newInstance(`object`: Any?): Quest = this
}
