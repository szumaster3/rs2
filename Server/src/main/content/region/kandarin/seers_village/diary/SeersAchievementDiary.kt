package content.region.kandarin.seers_village.diary

import content.data.GameAttributes
import content.global.travel.fairyring.FairyRing
import content.region.kandarin.camelot.quest.grail.dialogue.GalahadDialogue
import core.api.*
import core.game.diary.DiaryEventHookBase
import core.game.diary.DiaryLevel
import core.game.event.*
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.SpellBookManager
import core.game.node.entity.player.link.diary.DiaryType
import core.game.node.entity.player.link.prayer.PrayerType
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import core.game.world.map.Location
import core.game.world.map.zone.ZoneBorders
import shared.consts.*

class SeersAchievementDiary : DiaryEventHookBase(DiaryType.SEERS_VILLAGE) {
    companion object {
        private val SEERS_VILLAGE_AREA = ZoneBorders(2687, 3455, 2742, 3507)
        private val SEERS_BANK_AREA = ZoneBorders(2721, 3490, 2730, 3493)
        private val SEERS_COAL_TRUCKS_AREA = ZoneBorders(2690, 3502, 2699, 3508)
        private val SEERS_COURTHOUSE_AREA = ZoneBorders(2732, 3467, 2739, 3471)
        private val RANGING_GUILD_LOCATION = Location(2657, 3439)

        private val COMBAT_BRACELETS = arrayOf(Items.COMBAT_BRACELET_11126, Items.COMBAT_BRACELET_4_11118, Items.COMBAT_BRACELET_3_11120, Items.COMBAT_BRACELET_2_11122, Items.COMBAT_BRACELET_1_11124)
        private val RANGING_GUILD_ARCHERS = arrayOf(NPCs.GUILD_TOWERARCHER1_688, NPCs.GUILD_TOWERARCHER2_689, NPCs.GUILD_TOWERARCHER3_690, NPCs.GUILD_TOWERARCHER4_691)
        private val WORKSHOP_ELEMENTALS = arrayOf(NPCs.ELEMENTAL_FIRE_1019, NPCs.ELEMENTAL_EARTH_1020, NPCs.ELEMENTAL_AIR_1021, NPCs.ELEMENTAL_WATER_1022)
        private val CHURN_PRODUCT = arrayOf(Items.CHEESE_1985, Items.POT_OF_CREAM_2130, Items.PAT_OF_BUTTER_6697)
        private val RANGING_GUILD_STOCK = arrayOf(Items.BARB_BOLTTIPS_47, Items.RUNE_ARROW_892, Items.GREEN_DHIDE_BODY_1135, Items.ADAMANT_JAVELIN_829, Items.STUDDED_BODY_1133, Items.COIF_1169)

        object EasyTasks {
            const val PICK_5_FLAX = 0
            const val WALK_CLOCKWISE_AROUND_MYSTERIOUS_STATUE = 1
            const val SIR_GALAHAD_MAKE_TEA = 2
            const val TAKE_POISON_TO_KING_ARTHUR = 3
            const val SPIN_5_BOW_STRINGS = 4
            const val SINCLAIR_MANSION_FILL_5_POTS_WITH_FLOUR = 5
            const val FORESTERS_ARMS_GIVE_5_LOCALS_GLASS_OF_CIDER = 6
            const val PLANT_JUTE = 7
            const val SINCLAIR_MANSION_USE_CHURN = 8
            const val BUY_CANDLE = 9
            const val PRAY_AT_ALTAR = 10
            const val CATCH_MACKEREL = 11
        }

        object MediumTasks {
            const val SINCLAIR_MANSION_USE_FREMENNIK_SHORTCUT = 0
            const val THORMAC_SORCERER_TALK_ABOUT_MYSTIC_STAVES = 1
            const val TRANSPORT_FULL_LOAD_OF_COAL = 2
            const val FIND_HIGHEST_POINT = 3
            const val DEFEAT_EACH_ELEMENTAL_TYPE = 4
            const val TELEPORT_TO_CAMELOT = 5
            const val RANGING_GUILD_KILL_EACH_TOWER_GUARD = 6
            const val RANGING_GUILD_JUDGE_1000_ARCHERY_TICKETS = 7
            const val RANGING_GUILD_BUY_SOMETHING_FOR_TICKETS = 8
            const val USE_FAMILIAR_TO_LIGHT_MAPLE_LOGS = 9
            const val HARRY_GET_PET_FISH = 10
            const val CATHERBY_CATCH_AND_COOK_BASS = 11
        }

        object HardTasks {
            const val RANGING_GUILD_TELEPORT = 0
            const val CUT_5_YEW_LOGS = 1
            const val FLETCH_MAGIC_SHORTBOW_INSIDE_BANK = 2
            const val ENTER_SEERS_COURTHOUSE_WITH_PIETY = 3
            const val DIAL_FAIRY_RING_MCGRUBORS_WOOD = 4
            const val LIGHT_MAGIC_LOG = 5
            const val HIGH_ALCH_MAGIC_SHORTBOW_INSIDE_BANK = 6
            const val CATHERBY_CATCH_5_SHARKS = 7
            const val CATHERBY_COOK_5_SHARKS_WITH_COOKING_GAUNTLETS = 8
            const val CHARGE_5_WATER_ORBS_AT_ONCE = 9
            const val GRAPPLE_FROM_WATER_OBELISK_TO_CATHERBY_SHORE = 10
        }
    }

    override fun onAreaVisited(player: Player) {
        if (inBorders(player, SEERS_COURTHOUSE_AREA) && player.prayer.equals(PrayerType.PIETY)) {
            finishTask(
                player,
                DiaryLevel.HARD,
                HardTasks.ENTER_SEERS_COURTHOUSE_WITH_PIETY,
                5809
            )
        }
    }

    override fun onResourceProduced(player: Player, event: ResourceProducedEvent) {
        if (event.source.id == Scenery.OBELISK_OF_WATER_2151 && event.amount >= 5) {
            finishTask(
                player,
                DiaryLevel.HARD,
                HardTasks.CHARGE_5_WATER_ORBS_AT_ONCE,
                5816
            )
        }

        when (player.viewport.region!!.id) {
            10805 -> {
                if (event.itemId == Items.FLAX_1779) {
                    progressIncrementalTask(
                        player,
                        DiaryLevel.EASY,
                        EasyTasks.PICK_5_FLAX,
                        5782,
                        5,
                    )
                }
            }

            10806 -> {
                if (event.itemId == Items.YEW_LOGS_1515) {
                    progressIncrementalTask(
                        player,
                        DiaryLevel.HARD,
                        HardTasks.CUT_5_YEW_LOGS,
                        5806,
                        5,
                    )
                }
            }

            10807 -> {
                if (event.itemId in CHURN_PRODUCT) {
                    finishTask(
                        player,
                        DiaryLevel.EASY,
                        EasyTasks.SINCLAIR_MANSION_USE_CHURN,
                        5791
                    )
                }
            }

            11317 ->
                when (event.itemId) {
                    Items.RAW_MACKEREL_353 -> {
                        finishTask(
                            player,
                            DiaryLevel.EASY,
                            EasyTasks.CATCH_MACKEREL,
                            5784
                        )
                    }

                    Items.RAW_BASS_363 -> {
                        fulfillTaskRequirement(
                            player,
                            DiaryLevel.MEDIUM,
                            MediumTasks.CATHERBY_CATCH_AND_COOK_BASS,
                            5801
                        )
                    }

                    Items.BASS_365 -> {
                        fulfillTaskRequirement(
                            player,
                            DiaryLevel.MEDIUM,
                            MediumTasks.CATHERBY_CATCH_AND_COOK_BASS,
                            5801
                        )
                    }

                    Items.RAW_SHARK_383 -> {
                        progressIncrementalTask(
                            player,
                            DiaryLevel.HARD,
                            HardTasks.CATHERBY_CATCH_5_SHARKS,
                            5814,
                            5,
                        )
                    }

                    Items.SHARK_385 -> {
                        if (inEquipment(player, Items.COOKING_GAUNTLETS_775)) {
                            progressIncrementalTask(
                                player,
                                DiaryLevel.HARD,
                                HardTasks.CATHERBY_COOK_5_SHARKS_WITH_COOKING_GAUNTLETS,
                                5815,
                                5,
                            )
                        }
                    }
                }
        }
    }

    override fun onNpcKilled(player: Player, event: NPCKillEvent) {
        when (player.viewport.region!!.id) {
            10906 -> {
                if (event.npc.id in WORKSHOP_ELEMENTALS) {
                    progressFlaggedTask(
                        player,
                        DiaryLevel.MEDIUM,
                        MediumTasks.DEFEAT_EACH_ELEMENTAL_TYPE,
                        5795,
                        1 shl (event.npc.id - NPCs.ELEMENTAL_FIRE_1019),
                        0xF,
                    )
                }
            }

            10549 -> {
                if (event.npc.id in RANGING_GUILD_ARCHERS) {
                    progressFlaggedTask(
                        player,
                        DiaryLevel.MEDIUM,
                        MediumTasks.RANGING_GUILD_KILL_EACH_TOWER_GUARD,
                        5796,
                        1 shl (event.npc.id - NPCs.GUILD_TOWERARCHER1_688),
                        0xF,
                    )
                }
            }
        }
    }

    override fun onTeleported(player: Player, event: TeleportEvent) {
        when (event.source) {
            is Item -> {
                if (event.source.id in COMBAT_BRACELETS) {
                    if (event.location == RANGING_GUILD_LOCATION) {
                        finishTask(
                            player,
                            DiaryLevel.HARD,
                            HardTasks.RANGING_GUILD_TELEPORT,
                            5811
                        )
                    }
                }
            }
        }
    }

    override fun onFireLit(player: Player, event: LitFireEvent) {
        when {
            inBorders(player, SEERS_VILLAGE_AREA) -> {
                if (event.logId == Items.MAGIC_LOGS_1513) {
                    finishTask(
                        player,
                        DiaryLevel.HARD,
                        HardTasks.LIGHT_MAGIC_LOG,
                        5808
                    )
                }
            }
        }
    }

    override fun onInteracted(player: Player, event: InteractionEvent) {
        if (!inBorders(player, SEERS_COAL_TRUCKS_AREA)) return

        if (event.option != "remove-coal") return

        fulfillTaskRequirement(
            player,
            DiaryLevel.MEDIUM,
            MediumTasks.TRANSPORT_FULL_LOAD_OF_COAL,
            5794
        )
    }

    override fun onDialogueOptionSelected(player: Player, event: DialogueOptionSelectionEvent) {
        when (event.dialogue) {
            is GalahadDialogue -> {
                if (event.currentStage == 5) {
                    finishTask(
                        player,
                        DiaryLevel.EASY,
                        EasyTasks.SIR_GALAHAD_MAKE_TEA,
                        5785
                    )
                }
            }
        }
    }

    override fun onItemAlchemized(player: Player, event: ItemAlchemizationEvent) {
        if (inBorders(player, SEERS_BANK_AREA)) {
            if (event.itemId == Items.MAGIC_SHORTBOW_861 && event.isHigh) {
                finishTask(
                    player,
                    DiaryLevel.HARD,
                    HardTasks.HIGH_ALCH_MAGIC_SHORTBOW_INSIDE_BANK,
                    5812
                )
            }
        }
    }

    override fun onFairyRingDialed(player: Player, event: FairyRingDialEvent) {
        if (event.fairyRing == FairyRing.ALS) {
            finishTask(
                player,
                DiaryLevel.HARD,
                HardTasks.DIAL_FAIRY_RING_MCGRUBORS_WOOD,
                5807
            )
        }
    }

    override fun onItemPurchasedFromShop(player: Player, event: ItemShopPurchaseEvent) {
        if (event.itemId == Items.CANDLE_36 && player.viewport.region!!.id == 11061) {
            finishTask(
                player,
                DiaryLevel.EASY,
                EasyTasks.BUY_CANDLE,
                5793
            )
        }
        if (event.itemId in RANGING_GUILD_STOCK && player.viewport.region!!.id == 10549) {
            finishTask(
                player,
                DiaryLevel.MEDIUM,
                MediumTasks.RANGING_GUILD_BUY_SOMETHING_FOR_TICKETS,
                5802
            )
        }
    }

    override fun onPrayerPointsRecharged(player: Player, event: PrayerPointsRechargeEvent) {
        if (player.viewport.region!!.id == 10806) {
            if (event.altar.id == Scenery.ALTAR_409 || event.altar.id == Scenery.ALTAR_19145) {
                finishTask(
                    player,
                    DiaryLevel.EASY,
                    EasyTasks.PRAY_AT_ALTAR,
                    5792
                )
            }
        }
    }

    override fun onInterfaceOpened(player: Player, event: InterfaceOpenEvent) {
        if(event.component.id == Components.STAFF_ENCHANT_332) {
            finishTask(
                player,
                DiaryLevel.MEDIUM,
                MediumTasks.THORMAC_SORCERER_TALK_ABOUT_MYSTIC_STAVES,
                5805
            )
        }
    }

    override fun onSpellCast(player: Player, event: SpellCastEvent) {
        if (event.spellBook == SpellBookManager.SpellBook.MODERN &&
            event.spellId == 26 && getStatLevel(player, Skills.MAGIC) >= 45 &&
            inInventory(player, Items.LAW_RUNE_563, 1) &&
            inInventory(player, Items.AIR_RUNE_556, 5) &&
            !hasTimerActive(player, GameAttributes.TELEBLOCK_TIMER)
        ) {
            finishTask(
                player,
                DiaryLevel.MEDIUM,
                MediumTasks.TELEPORT_TO_CAMELOT,
                5800
            )
        }
    }
}
