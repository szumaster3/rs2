package content.region.desert.quest.rescue

import content.region.desert.quest.rescue.npc.LadyKeliNPC
import content.region.desert.quest.rescue.plugin.PrinceAliRescuePlugin
import core.api.*
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.quest.Quest
import core.game.node.entity.skill.Skills
import core.game.node.item.GroundItemManager
import core.game.node.item.Item
import core.plugin.ClassScanner
import core.plugin.Initializable
import shared.consts.Items
import shared.consts.Quests
import shared.consts.Vars

@Initializable
class PrinceAliRescue : Quest(Quests.PRINCE_ALI_RESCUE, 24, 23, 3, Vars.VARP_QUEST_PRINCE_ALI_RESCUE_PROGRESS_273, 0, 1, 110) {

    companion object {
        private const val ROPE = Items.ROPE_954
        private const val SKIRT = Items.PINK_SKIRT_1013
        private const val YELLOW_WIG = Items.WIG_2419
        private const val PASTE = Items.PASTE_2424
        private val COINS = Item(Items.COINS_995, 700)
    }

    override fun newInstance(`object`: Any?): Quest {
        ClassScanner.definePlugins(LadyKeliNPC(), PrinceAliRescuePlugin())
        return this
    }

    override fun drawJournal(player: Player, stage: Int) {
        super.drawJournal(player, stage)
        var ln = 11
        when (stage) {
            0 -> {
                line(player, "I can start this quest by speaking to !!Hassan?? at the palace", ln++)
                line(player, "in !!Al-Kharid??.", ln)
            }

            10 -> {
                line(player, "I started this quest by speaking to Hassan in Al-Kharid",  ln++, true)
                line(player, "Palace. He told me I should speak to Osman the spymaster.",  ln++, true)
                line(player, "I should go and speak to !!Osman?? for details on the quest.",  ln)
            }

            20, 30, 40, 50, 60, 100 -> {
                val finished = stage >= 100
                val guardDrunk = getAttribute(player, "guard-drunk", false)

                line(player, "I started this quest by speaking to Hassan in Al-Kharid", ln++, true)
                line(player, "Palace. He told me I should speak to Osman the spymaster.", ln++, true)
                line(player, "I should go and speak to Osman for details on the quest.", ln++, true)

                if (stage == 20 || stage == 30) {
                    line(player, "!!Prince Ali?? has been !!kidnapped?? but luckily the spy !!Leela?? has", ln++)
                    line(player, "found he is being held near !!Draynor Village??. I will need to", ln++)
                    line(player, "!!disguise?? the !!Prince?? and !!tie?? up his !!captor?? to !!free?? him from", ln++)
                    line(player, "their clutches.", ln++)
                    line(player, "To do this I should:", ln++)
                    line(player, "Talk to !!Leela?? near !!Draynor Village?? for advice.", ln++)

                    if (stage == 20) {
                        line(player, "Get a !!duplicate?? of the !!key?? that is !!imprisoning?? the !!prince??.", ln++)
                    } else {
                        line(player, "I have duplicated a key, I need to get it from !!Leela??.", ln++)
                    }

                    line(
                        player,
                        if (inInventory(player, ROPE)) {
                            "<str>I have some rope with me."
                        } else {
                            "Get some !!rope?? to tie up the Princes' !!kidnapper??."
                        },
                        ln++,
                    )
                    line(
                        player,
                        if (inInventory(player, PASTE)) {
                            "<str>I have some skin paste suitable for disguise with me."
                        } else {
                            "Get something to !!colour?? the !!Princes' skin?? as a !!disguise??."
                        },
                        ln++,
                    )
                    line(
                        player,
                        if (inInventory(player, SKIRT)) {
                            "<str>I have a skirt suitable for a disguise with me."
                        } else {
                            "Get a !!skirt?? similar to his !!kidnapper?? as !!disguise??."
                        },
                        ln++,
                    )
                    line(
                        player,
                        if (inInventory(player, YELLOW_WIG)) {
                            "<str>I have a wig suitable for disguise with me."
                        } else {
                            "Get a !!Wig?? to !!help disguise?? the !!prince??."
                        },
                        ln++,
                    )
                } else {
                    line(player, "Prince Ali has been kidnapped but luckily the spy Leela has", ln++, true)
                    line(player, "found he is being held near Draynor Village. I will need to", ln++, true)
                    line(player, "disguise the Prince and tie up his captor to free him from", ln++, true)
                    line(player, "their clutches.", ln++,true)

                    if (stage == 40) {
                        if (guardDrunk) {
                            line(player, "I also had to prevent the Guard from seeing that I was up", ln++,true)
                            line(player, "to, by getting him drunk.", ln++,true)
                            line(player, "With the guard out of the way, all I have to do now is use", ln++)
                            line(player, "the !!Skin Potion??, !!Pink Skirt??, !!Rope??, !!Blonde Wig?? and !!Cell Key?? to", ln++)
                            line(player, "free !!Prince Ali?? from his cell somehow.", ln++)
                        } else {
                            line(player, "Do something to prevent !!Joe the Guard?? seeing the", ln++)
                            line(player, "escape.", ln++)
                            line(player, "Use the !!Skin Potion??, !!Pink Skirt??, !!Rope??, !!Blonde Wig?? and !!Cell", ln++)
                            line(player, "Key?? to free !!Prince Ali?? from his cell somehow.", ln++)
                        }
                    } else if (stage == 50) {
                        line(player, "I also had to prevent the Guard from seeing that I was up", ln++,true)
                        line(player, "to, by getting him drunk.", ln++,true)
                        line(player, "With the guard disposed of, I used my rope to tie up Lady", ln++,true)
                        line(player, "Keli in a cupboard, so I could disguise the Prince.", ln++,true)
                        line(player, "I need to !!unlock the cell door?? and then give the Prince the", ln++)
                        line(player, "!!Pink Skirt??, the !!Skin Paste?? and the !!Blonde Wig?? so that they", ln++)
                        line(player, "can safely !!escape?? disguised as !!Lady Keli??.", ln++)
                    } else {
                        line(player, "I also had to prevent the Guard from seeing that I was up", ln++,true)
                        line(player, "to, by getting him drunk.", ln++,true)
                        line(player, "With the guard disposed of, I used my rope to tie up Lady", ln++,true)
                        line(player, "Keli in a cupboard, so I could disguise the Prince.", ln++,true)
                        line(player, "I then used a wig, and some skin paste to make the", ln++,true)
                        line(player, "prince look like Lady Keli so he could escape to his", ln++,true)
                        line(player, "freedom with Leela after unlocking his cell door.", ln++,true)

                        if (!finished) {
                            line(player, "I should return to !!Hassan?? to claim my reward.", ln++)
                        } else {
                            line(player, "Hassan the chancellor rewarded me for all of my help.", ln++, true)
                            line(player, "I am now a friend of Al-Kharid and may pass through the", ln++, true)
                            line(player, "gate leading between Lumbridge and Al-Kharid for free.", ln++, true)
                            ln++
                            line(player, "<col=FF0000>QUEST COMPLETE!</col>", ln)
                        }
                    }
                }
            }
        }
    }

    override fun finish(player: Player) {
        super.finish(player)
        var ln = 10
        sendMessage(player,"The chancellor pays you 700 coins.")
        drawReward(player, Items.COINS_8894)
        drawReward(player, "3 Quests Points", ln++)
        drawReward(player, "700 Coins", ln)
        if (!player.inventory.add(COINS)) {
            GroundItemManager.create(COINS, player)
        }
        removeAttribute(player, "guard-drunk")
    }
}