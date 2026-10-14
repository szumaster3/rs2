package content.region.morytania.port_phasmatys.quest.ahoy

import content.region.morytania.port_phasmatys.quest.ahoy.plugin.GhostsAhoyUtils
import core.api.*
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.quest.Quest
import core.game.node.entity.skill.Skills
import core.plugin.Initializable
import shared.consts.Components
import shared.consts.Items
import shared.consts.Quests
import shared.consts.Vars

@Initializable
class GhostsAhoy : Quest(Quests.GHOSTS_AHOY, 68, 67, 2, Vars.VARBIT_QUEST_GHOST_AHOY_PROGRESS_217, 0, 1, 8) {

    override fun drawJournal(player: Player, stage: Int) {
        super.drawJournal(player, stage)
        var line = 11
        if (stage == 0) {

            fun questText(name: String, completed: Boolean) =
                if (completed) "<str><col=000000>$name</col></str>"
                else "!!$name??"

            fun reqLevelText(level: Int, skill: Int): String {
                val name = Skills.SKILL_NAME[skill]
                return if (hasLevelStat(player, skill, level))
                    "<str><col=000000>level $level $name</col></str>"
                else
                    "!!level $level $name??"
            }

            line(player, "To start this quest I need to speak to !!Velorina??,", line++, false)
            line(player, "a ghost of !!Port Phasmatys??", line++, false)
            line++

            val agilityReq = reqLevelText(25, Skills.AGILITY)
            val cookingReq = reqLevelText(20, Skills.COOKING)

            line(player, "I must have at least $agilityReq, $cookingReq,", line++, false)
            line(player, "and be able to defeat a !!level 32 monster??.", line++, false)
            line++

            line(player, "I must also have completed the following quests:", line++, false)

            line(player, questText(Quests.PRIEST_IN_PERIL, isQuestComplete(player, Quests.PRIEST_IN_PERIL)), line++, false)
            line(player, questText(Quests.THE_RESTLESS_GHOST, isQuestComplete(player, Quests.THE_RESTLESS_GHOST)), line++, false)

            line++
        }

        if (stage in 1..99) {
            fun write(crossed: Boolean, vararg texts: String) {
                texts.forEach { line(player, it, line++, crossed) }
                line++
            }

            write(
                true,
                "I have spoken with !!Velorina??,",
                "who has told me the sad history of the ghosts of !!Port Phasmatys??.",
            )

            if (stage == 1) {
                write(
                    false,
                    "She has asked me to plead with Necrovarus in the Phasmatyan Temple",
                    "to let any ghost who so wishes pass over into the next world.",
                )
            } else {
                write(
                    true,
                    "She asked me to plead with Necrovarus in the Phasmatyan Temple",
                    "to let any ghost who so wishes pass over into the next world.",
                    "I pleaded with Necrovarus, to no avail.",
                )
            }

            if (stage == 2) {
                write(false, "I should tell !!Velorina?? that !!Necrovarus?? would not listen.")
            } else if (stage >= 3) {
                write(
                    true,
                    "Velorina was crestfallen at !!Necrovarus'?? refusal to lift his ban,",
                    "and she told me of a woman who fled Port Phasmatys",
                    "before the townsfolk died, and to seek her out, as she",
                    "may know of a way around Necrovarus' stubbornness.",
                )
            }

            val enlightened = getAttribute(player, GhostsAhoyUtils.croneEnlightened, false)
            if (stage == 3 && !enlightened) {
                write(
                    false,
                    "She told me of a small wooden shack by the sea in",
                    "Morytania, with castles to the west and east. The old",
                    "woman there might like a nice hot cup of !!nettle tea??.",
                )
            }

            if (stage in 4..79 && !enlightened) {
                write(
                    false,
                    "I found the old woman. She wants !!nettle tea?? with milk,",
                    "in her own special !!porcelain cup??.",
                )
            }

            if (enlightened) {
                write(
                    true,
                    "I found the old woman, who told me of an enchantment",
                    "she can perform on the !!Amulet of Ghostspeak??,",
                    "which will then let me command Necrovarus to do my bidding.",
                )
            }

            if (stage in 4..79 && enlightened) {
                val robes = getAttribute(player, GhostsAhoyUtils.robesFound, false)
                val book = getAttribute(player, GhostsAhoyUtils.bookFound, false)
                val manual = getAttribute(player, GhostsAhoyUtils.manualGiven, false)
                val signed = getAttribute(player, GhostsAhoyUtils.getSignedBow, false)

                line(player, "To perform the enchantment she needs:", line++, false)
                line(player, "The !!Robes of Necrovarus?? from the coffin in the temple,", line++, robes)
                line(player, "the !!Book of Haricanto??, buried on Dragontooth Island,", line++, book)
                line(player, "and a !!translation manual??, which !!Ak-Haranu?? will trade", line++, manual)
                line(player, "for an oak longbow signed by !!Robin??.", line++, manual)
                line++

                if (getAttribute(player, GhostsAhoyUtils.petitionstart, false) && !getAttribute(player, GhostsAhoyUtils.petitionPresented, false)) {
                    line(player, "!!Gravingas?? gave me a petition to collect 10 signatures", line++, false)
                    line(player, "from the ghosts, which I could present to !!Necrovarus??.", line++, false)
                    line++
                }
                if (signed) {
                    line(player, "I beat !!Robin?? at Runedraw and he agreed to sign a longbow.", line++, true)
                    line++
                }
                if (stage >= 6) {
                    line(player, "I found the old woman's son and told her he is alive.", line++, true)
                    line++
                } else if (stage == 5) {
                    line(player, "I should tell the old woman I found her son.", line++, false)
                    line++
                }

                write(
                    false,
                    "Once I have everything, I should bring the items and my",
                    "!!Amulet of Ghostspeak?? to the old woman.",
                )
            }

            if (stage >= 80) {
                write(
                    true,
                    "I brought the old woman the !!Book of Haricanto??,",
                    "the !!Robes of Necrovarus??, and a translation manual.",
                )
            }

            if (stage in 80..98) {
                write(
                    false,
                    "The old woman used the items I brought her to",
                    "perform the enchantment on the Amulet of Ghostspeak.",
                    "I should wear it and command !!Necrovarus?? to remove his ban.",
                )
            }

            if (stage == 99) {
                write(
                    true,
                    "The old woman used the items I brought her to",
                    "perform the enchantment on the Amulet of Ghostspeak.",
                )
                write(
                    false,
                    "I have commanded !!Necrovarus?? to remove his ban.",
                    "I should tell !!Velorina?? that !!Necrovarus?? has been commanded",
                    "to remove his ban, and to allow any ghost who so desires",
                    "to pass over into the next plane of existence.",
                )
            }
        }

        if (stage == 100) {
            line(player, "<col=FF0000>QUEST COMPLETE!</col>", line++, false)
            line++
            line(player, "Velorina gave me the Ectophial in return,", line++, false)
            line(player, "which I can use to teleport to the !!Temple of Phasmatys??.", line++, false)
            line++
        }
    }

    override fun finish(player: Player) {
        super.finish(player)
        var ln = 10
        sendItemZoomOnInterface(player, Components.QUEST_COMPLETE_SCROLL_277, 5, Items.ECTOPHIAL_4251, 230)
        drawReward(player, "2 Quest Points", ln++)
        drawReward(player, "2,400 Prayer XP", ln++)
        drawReward(player, "Free passage into Port Phasmatys", ln)
        rewardXP(player, Skills.PRAYER, 2400.0)
        setVarbit(player, Vars.VARBIT_QUEST_GHOST_AHOY_PROGRESS_217, 8, true)
        removeAttributes(
            player,
            GhostsAhoyUtils.shipFlag,
            GhostsAhoyUtils.shipBottom,
            GhostsAhoyUtils.shipSkull,
            GhostsAhoyUtils.rightShip,
            GhostsAhoyUtils.colorMatching,
            GhostsAhoyUtils.windSpeed,
            GhostsAhoyUtils.lastMapScrap,
            GhostsAhoyUtils.petitionsigns,
            GhostsAhoyUtils.petitionstart,
            GhostsAhoyUtils.petitioncomplete,
            GhostsAhoyUtils.petitionPresented,
            GhostsAhoyUtils.getSignedBow,
            GhostsAhoyUtils.croneEnlightened,
            GhostsAhoyUtils.akharanuAsked,
            GhostsAhoyUtils.robesFound,
            GhostsAhoyUtils.bookFound,
            GhostsAhoyUtils.manualGiven,
        )
    }

    override fun newInstance(`object`: Any?): Quest = this
}
