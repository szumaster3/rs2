package core.game.node.entity.player.link.emote

import content.data.Quests

import core.api.*
import core.game.container.impl.EquipmentContainer
import core.game.interaction.QueueStrength
import core.game.node.entity.player.Player
import core.game.node.entity.player.info.Rights
import core.game.node.entity.player.link.diary.DiaryType
import core.game.system.task.Pulse
import core.game.world.map.Direction
import core.game.world.map.Location
import core.game.world.map.RegionManager.getLocalNpcs
import core.game.world.update.flag.context.Animation
import core.game.world.update.flag.context.Graphics
import shared.consts.Animations
import shared.consts.Items
import shared.consts.NPCs

enum class Emotes(
    val buttonId: Int,
    val animation: Int? = -1,
    val graphics: Int? = -1,
    val lockedMessage: String? = null,
) {
    YES(
        buttonId = 2,
        animation = Animations.EMOTE_YES_855
    ),
    NO(
        buttonId = 3,
        animation = Animations.EMOTE_NO_856
    ),
    BOW(
        buttonId = 4,
        animation = Animations.EMOTE_BOW_858
    ) {
        override fun play(player: Player) {
            val hat = player.equipment[EquipmentContainer.SLOT_LEGS]
            if (hat?.id == Items.PANTALOONS_10396) {
                forceEmote(player, Animation(Animations.TRAIL_BOW_EMOTE_5312), Graphics(-1))
            } else {
                super.play(player)
            }
        }
    },
    ANGRY(
        buttonId = 5,
        animation = Animations.EMOTE_ANGRY_859
    ) {
        override fun play(player: Player) {
            val hat = player.equipment[EquipmentContainer.SLOT_HAT]
            if (hat?.id == Items.A_POWDERED_WIG_10392) {
                forceEmote(player, Animation(Animations.TRAIL_ANGRY_EMOTE_5315), Graphics(-1))
            } else {
                super.play(player)
            }
        }
    },
    THINK(
        buttonId = 6,
        animation = Animations.EMOTE_THINK_857
    ),
    WAVE(
        buttonId = 7,
        animation = Animations.EMOTE_WAVE_863
    ) {
        override fun play(player: Player) {
            val weapon = player.equipment[EquipmentContainer.SLOT_WEAPON]
            if (weapon?.id == Items.SLED_4084) {
                forceEmote(player, Animation(Animations.TROLLROMANCE_EMOTE_WAVE_1483), Graphics(-1))
            } else {
                super.play(player)
            }
        }
    },
    SHRUG(
        buttonId = 8,
        animation = Animations.EMOTE_SHRUG_2113
    ),
    CHEER(
        buttonId = 9,
        animation = Animations.EMOTE_CHEER_862
    ) {
        override fun play(player: Player) {
            val weapon = player.equipment[EquipmentContainer.SLOT_WEAPON]
            if (weapon?.id == Items.SLED_4084) {
                forceEmote(player, Animation(Animations.TROLLROMANCE_EMOTE_CHEER_1482), Graphics(-1))
            } else {
                super.play(player)
            }
        }
    },
    BECKON(
        buttonId = 10,
        animation = Animations.EMOTE_BECKON_864
    ) {
        override fun play(player: Player) {
            val weapon = player.equipment[EquipmentContainer.SLOT_WEAPON]
            val requiredItem = anyInEquipment(player, Items.HARD_HAT_10862, Items.BUILDERS_SHIRT_10863, Items.BUILDERS_TROUSERS_10864, Items.BUILDERS_BOOTS_10865, Items.PLAIN_SATCHEL_10877, Items.GREEN_SATCHEL_10878, Items.RED_SATCHEL_10879, Items.BLACK_SATCHEL_10880, Items.GOLD_SATCHEL_10881, Items.RUNE_SATCHEL_10882)
            when {
                weapon?.id == Items.SLED_4084 -> forceEmote(player, Animation(Animations.TROLLROMANCE_EMOTE_BECKON_1484), Graphics(-1))
                requiredItem -> forceEmote(player, Animation(Animations.TOL_PLAYER_WHISTLE_BECKON_5845), Graphics(-1))
                else -> super.play(player)
            }
        }
    },
    JUMP_FOR_JOY(
        buttonId = 11,
        animation = Animations.EMOTE_JUMP_WITH_JOY_2109
    ),
    LAUGH(
        buttonId = 12,
        animation = Animations.EMOTE_LAUGH_861
    ),
    YAWN(
        buttonId = 13,
        animation = Animations.EMOTE_YAWN_2111
    ) {
        override fun play(player: Player) {
            val hat = player.equipment[EquipmentContainer.SLOT_HAT]
            if (hat?.id == Items.SLEEPING_CAP_10398) {
                forceEmote(player, Animation(Animations.TRAIL_YAWN_EMOTE_5313), Graphics(-1))
            } else {
                super.play(player)
            }
        }
    },
    DANCE(
        buttonId = 14,
        animation = Animations.EMOTE_DANCE_866
    ) {
        override fun play(player: Player) {
            val legs = player.equipment[EquipmentContainer.SLOT_LEGS]
            if (legs?.id == Items.FLARED_TROUSERS_10394) {
                forceEmote(player, Animation(Animations.TRAIL_DANCE_EMOTE_5316), Graphics(-1))
            } else {
                super.play(player)
            }
        }
    },
    JIG(
        buttonId = 15,
        animation = Animations.EMOTE_DANCE_SCOTTISH_2106
    ),
    SPIN(
        buttonId = 16,
        animation = Animations.EMOTE_DANCE_SPIN_2107
    ),
    HEADBANG(
        buttonId = 17,
        animation = Animations.EMOTE_DANCE_HEADBANG_2108
    ),
    CRY(
        buttonId = 18,
        animation = Animations.EMOTE_CRY_860
    ),
    BLOW_KISS(
        buttonId = 19,
        animation = Animations.EMOTE_BLOW_KISS_1374,
        graphics = shared.consts.Graphics.SIGHERO_ARIANE_ADVENTURER_WALK_574
    ),
    PANIC(
        buttonId = 20,
        animation = Animations.EMOTE_PANIC_2105
    ),
    RASPBERRY(
        buttonId = 21,
        animation = Animations.EMOTE_YA_BOO_SUCKS_2110
    ),
    CLAP(
        buttonId = 22,
        animation = Animations.EMOTE_CLAP_865
    ) {
        override fun play(player: Player) {
            val weapon = player.equipment[EquipmentContainer.SLOT_WEAPON]
            if (weapon?.id == Items.SLED_4084) {
                forceEmote(player, Animation(Animations.TROLLROMANCE_EMOTE_CLAP_1485), Graphics(-1))
            } else {
                super.play(player)
            }
        }
    },
    SALUTE(
        buttonId = 23,
        animation = Animations.EMOTE_FREMMENIK_SALUTE_2112
    ) {
        override fun play(player: Player) {
            if (isEligibleForSalute(player)) {
                forceEmote(player, Animation(Animations.EMOTE_FREMMENIK_SALUTE_2112), Graphics(-1))
                finishDiaryTask(player, DiaryType.FALADOR, 1, 8)
                setVarbit(player, 5714,1, true)
            } else {
                super.play(player)
            }
        }
    },
    GOBLIN_BOW(
        buttonId = 24,
        animation = Animations.HUMAN_CAVE_GOBLIN_BOW_2127,
        lockedMessage = "This emote can be unlocked during the Lost Tribe quest.",
    ) {
        override fun play(player: Player) {
            if (isEligibleForGoblinBow(player) && getQuestStage(player, Quests.THE_LOST_TRIBE.questName) == 45) {
                val mistag = getLocalNpcs(player).firstOrNull { npc ->
                    npc.id == NPCs.LOST_TRIBE_MISTAG_2084 && withinDistance(npc, player.location, 3)
                }

                mistag?.let { npc ->
                    player.dialogueInterpreter.open(NPCs.LOST_TRIBE_MISTAG_2084, npc, "greeting")
                    setAttribute(player, "/save:mistag-greeted", true)
                }
            }
            super.play(player)
        }
    },
    GOBLIN_SALUTE(
        buttonId = 25,
        animation = Animations.HUMAN_CAVE_GOBLIN_DANCE_2128,
        lockedMessage = "This emote can be unlocked during the Lost Tribe quest.",
    ),
    GLASS_BOX(
        buttonId = 26,
        animation = Animations.EMOTE_GLASS_BOX_1131,
        lockedMessage = "This emote can be unlocked during the Mime random event.",
    ),
    CLIMB_ROPE(
        buttonId = 27,
        animation = Animations.EMOTE_CLIMBING_ROPE_1130,
        lockedMessage = "This emote can be unlocked during the Mime random event.",
    ),
    LEAN_ON_AIR(
        buttonId = 28,
        animation = Animations.EMOTE_MIME_LEAN_1129,
        lockedMessage = "This emote can be unlocked during the Mime random event.",
    ),
    GLASS_WALL(
        buttonId = 29,
        animation = Animations.EMOTE_GLASS_WALL_1128,
        lockedMessage = "This emote can be unlocked during the Mime random event.",
    ),
    IDEA(
        buttonId = 33,
        animation = Animations.EMOTE_LIGHTBULB_4276,
        graphics = shared.consts.Graphics.EMOTE_LIGHTBULB_SPOTANIM_712,
        lockedMessage = "You can't use this emote yet. Visit the Stronghold of Security to unlock it.",
    ),
    STOMP(
        buttonId = 31,
        animation = Animations.EMOTE_STAMPFEET_4278,
        lockedMessage = "You can't use this emote yet. Visit the Stronghold of Security to unlock it.",
    ),
    FLAP(
        buttonId = 32,
        animation = Animations.EMOTE_PANIC_FLAP_4280,
        lockedMessage = "You can't use this emote yet. Visit the Stronghold of Security to unlock it.",
    ) {
        override fun play(player: Player) {
            if (isWearingChickenOutfit(player)) {
                forceEmote(player, Animation(Animations.EASTER07_FLAPPING_EMOTE_3859), Graphics(-1))
            } else {
                super.play(player)
            }
        }
    },
    SLAP_HEAD(
        buttonId = 30,
        animation = Animations.EMOTE_SLAP_HEAD_4275,
        lockedMessage = "You can't use this emote yet. Visit the Stronghold of Security to unlock it.",
    ),
    ZOMBIE_WALK(
        buttonId = 34,
        animation = Animations.ZOMBIE_WALK_EMOTE_3544,
        lockedMessage = "This emote can be unlocked during the Gravedigger random event.",
    ),
    ZOMBIE_DANCE(
        buttonId = 35,
        animation = Animations.ZOMBIE_DANCE_3543,
        lockedMessage = "This emote can be unlocked during the Gravedigger random event.",
    ),
    ZOMBIE_HAND(
        buttonId = 36,
        animation = Animations.HW07_ARM_FROM_THE_GROUND_EMOTE_7272,
        graphics = shared.consts.Graphics.HW07_ARM_FROM_THE_GROUND_SPOTANIM_2_1244,
        lockedMessage = "This emote can be unlocked during the Gravedigger random event.",
    ),
    SCARED(
        buttonId = 37,
        animation = Animations.TERRIFIED_EMOTE_2836,
        lockedMessage = "This emote can be unlocked by playing a Halloween holiday event.",
    ),
    BUNNY_HOP(
        buttonId = 38,
        animation = Animations.RABBIT_EMOTE_6111,
        lockedMessage = "This emote can be unlocked by playing an Easter holiday event.",
    ),
    SKILLCAPE(
        buttonId = 39
    ) {
        override fun play(player: Player) {
            val cape = player.equipment[EquipmentContainer.SLOT_CAPE]
            if (cape == null) {
                sendMessage(player, "You need to be wearing a skillcape in order to perform this emote.")
                return
            }
            val capeId = cape.id
            SKILLCAPE_INFO.firstOrNull { capeId == it[0] || capeId == it[1] }?.let { info ->
                visualize(player, info[3], info[2])
                player.locks.lock("emote", Animation.create(info[3]).duration)
            } ?: sendMessage(player, "You need to be wearing a skillcape in order to perform this emote.")
        }
    },
    SNOWMAN_DANCE(
        buttonId = 40,
        animation = Animations.XMAS_07_PLAYER_EMOTE_7531,
        lockedMessage = "This emote can be unlocked by playing a Christmas holiday event.",
    ),
    AIR_GUITAR(
        buttonId = 41,
        animation = Animations.MUSIC_EMOTE_2414,
        graphics = 1537,
        lockedMessage = "This emote can be accessed by unlocking 200 pieces of music.",
    ) {
        override fun play(player: Player) {
            playJingle(player, 302)
            super.play(player)
        }
    },
    SAFETY_FIRST(
        buttonId = 42,
        animation = Animations.SOPS_SECURITY_EMOTE_PLAYER_8770,
        graphics = shared.consts.Graphics.SOPS_SECURITY_EMOTE_1553,
        lockedMessage = "You can't use this emote yet. Visit the Stronghold of Player safety to unlock it.",
    ),
    EXPLORE(
        buttonId = 43,
        animation = Animations.ATLUM_PLAYER_NAVIGATION_EMOTE_9990,
        graphics = shared.consts.Graphics.ATLUM_NAVIGATION_EMOTE_SPOT_1734,
        lockedMessage = "You can't use this emote yet. You must complete all the Lumbridge and Draynor beginner tasks to unlock it.",
    ),
    TRICK(
        buttonId = 44,
        animation = Animations.SWEPT_PLAYER_SCARE_EMOTE_10530,
        graphics = shared.consts.Graphics.SPELLS_UPDATE_PLAYER_WATER_LAUNCH_1863,
        lockedMessage = "This emote can be unlocked by playing a Halloween holiday event.",
    ),
    FREEZE(
        buttonId = 45,
        animation = Animations.XMAS08_PLAYER_FREEZE_11044,
        graphics = shared.consts.Graphics.XMAS08_PLAYER_FREEZE_SPOT_1973,
        lockedMessage = "This emote can be unlocked by playing a Christmas holiday event.",
    ),
    GIVE_THANKS(
        buttonId = 46,
        lockedMessage = "This emote can be unlocked by playing a Thanksgiving holiday event.",
    ) {
        override fun play(player: Player) {
            queueScript(player, 1, QueueStrength.NORMAL) { stage ->
                when (stage) {
                    0 -> {
                        forceEmote(player, Animation(Animations.TG08_TURKEY_EMOTE_DANCE_PLAYER_TRANSMOG_10994), Graphics(shared.consts.Graphics.SMOKEPUFF_2_86))
                        return@queueScript delayScript(player, 2)
                    }
                    1 -> {
                        player.appearance.transformNPC(NPCs.TG08_TURKEY_DANCE_EMOTE_8499)
                        forceEmote(player, Animation(Animations.TG08_TURKEY_DANCE_EMOTE_10996), Graphics(-1))
                        return@queueScript delayScript(player, 10)
                    }
                    2 -> {
                        player.appearance.transformNPC(NPCs.TG08_ATTACKABLE_TURKEY_8501)
                        return@queueScript delayScript(player, 3)
                    }
                    3 -> {
                        player.appearance.transformNPC(-1)
                        forceEmote(player, Animation(Animations.TG08_TURKEY_EMOTE_DANCE_PLAYER_REVERT_10995), Graphics(shared.consts.Graphics.SMOKEPUFF_2_86))
                        return@queueScript stopExecuting(player)
                    }
                    else -> return@queueScript stopExecuting(player)
                }
            }
        }
    };

    /**
     * Plays the emote.
     *
     * @param player the player.
     */
    open fun play(player: Player) {
        forceEmote(player, Animation(animation!!), Graphics(graphics!!))
    }

    constructor(buttonId: Int, animation: Int?, lockedMessage: String?) : this(
        buttonId = buttonId,
        animation = animation,
        graphics = -1,
        lockedMessage = lockedMessage,
    )

    constructor(buttonId: Int, lockedMessage: String?) : this(
        buttonId = buttonId,
        animation = -1,
        graphics = -1,
        lockedMessage = lockedMessage,
    )

    companion object {
        private val SKILLCAPE_INFO = arrayOf(
            intArrayOf(
                Items.ATTACK_CAPE_9747,
                Items.ATTACK_CAPE_T_9748,
                shared.consts.Graphics.SKILL_CAPE_ATTACK_SPOTANIM_823,
                Animations.SKILL_CAPE_ATTACK_4959,
            ),
            intArrayOf(
                Items.STRENGTH_CAPE_9750,
                Items.STRENGTH_CAPE_T_9751,
                shared.consts.Graphics.SKILLCAPES_SPOT_STRENGTH_828,
                Animations.SKILLCAPES_HUMAN_STRENGTH_4981,
            ),
            intArrayOf(
                Items.DEFENCE_CAPE_9753,
                Items.DEFENCE_CAPE_T_9754,
                shared.consts.Graphics.SKILLCAPE_DEFEND_SPOTANIM_824,
                Animations.SKILL_CAPE_DEFEND_4961,
            ),
            intArrayOf(
                Items.RANGING_CAPE_9756,
                Items.RANGING_CAPE_T_9757,
                shared.consts.Graphics.SKILLCAPES_SPOT_RANGE_832,
                Animations.SKILLCAPES_HUMAN_RANGE_4973,
            ),
            intArrayOf(
                Items.PRAYER_CAPE_9759,
                Items.PRAYER_CAPE_T_9760,
                shared.consts.Graphics.SKILLCAPES_SPOT_PRAYER_829,
                Animations.SKILLCAPES_HUMAN_PRAYER_4979,
            ),
            intArrayOf(
                Items.MAGIC_CAPE_9762,
                Items.MAGIC_CAPE_T_9763,
                shared.consts.Graphics.CLAN_SKILL_CRAFTING_TIER_1_3_SPOOL_TOWER_2C_813,
                Animations.SKILLCAPES_PLAYER_MAGIC_4939,
            ),
            intArrayOf(
                Items.RUNECRAFT_CAPE_9765,
                Items.RUNECRAFT_CAPE_T_9766,
                shared.consts.Graphics.SKILLCAPES_RUNECRAFTING_SPOTANIM_817,
                Animations.SKILLCAPES_PLAYER_RUNECRAFTING_4947,
            ),
            intArrayOf(
                Items.HITPOINTS_CAPE_9768,
                Items.HITPOINTS_CAPE_T_9769,
                shared.consts.Graphics.DRAGON_UPDATE_09_ELVARG_RANGED_FIRE_ATTACK_1_833,
                Animations.DRAGON_UPDATE_09_GHOST_DRAGON_RANGED_ATTACK_4971,
            ),
            intArrayOf(
                Items.AGILITY_CAPE_9771,
                Items.AGILITY_CAPE_T_9772,
                shared.consts.Graphics.SKILLCAPES_SPOT_AGILITY_830,
                Animations.SKILLCAPES_HUMAN_AGILITY_4977,
            ),
            intArrayOf(
                Items.HERBLORE_CAPE_9774,
                Items.HERBLORE_CAPE_T_9775,
                shared.consts.Graphics.SKILLCAPES_SPOT_HERBLORE_835,
                Animations.SKILLCAPES_HUMAN_HERBLORE_4969,
            ),
            intArrayOf(
                Items.THIEVING_CAPE_9777,
                Items.THIEVING_CAPE_T_9778,
                shared.consts.Graphics.SKILL_CAPE_THIEVING_SPOTANIM_826,
                Animations.SKILL_CAPE_THIEVING_4965,
            ),
            intArrayOf(
                Items.CRAFTING_CAPE_9780,
                Items.CRAFTING_CAPE_T_9781,
                shared.consts.Graphics.SKILLCAPES_CRAFTING_SPOTANIM_818,
                Animations.SKILLCAPES_CRAFTING_PLAYER_ANIM_4949,
            ),
            intArrayOf(
                Items.FLETCHING_CAPE_9783,
                Items.FLETCHING_CAPE_T_9784,
                shared.consts.Graphics.SKILLCAPES_FLETCHING_BOW_SPOTANIM_812,
                Animations.SKILLCAPES_PLAYER_FLETCHING_BOW_4937,
            ),
            intArrayOf(
                Items.SLAYER_CAPE_9786,
                Items.SLAYER_CAPE_T_9787,
                shared.consts.Graphics.SKILLCAPE_SLAYER_TUROTH_SPOTANIM_1656,
                Animations.SKILL_CAPE_SLAYER_4967,
            ),
            intArrayOf(
                Items.CONSTRUCT_CAPE_9789,
                Items.CONSTRUCT_CAPE_T_9790,
                shared.consts.Graphics.SKILLCAPES_CONSTRUCTION_SPOTANIM_820,
                Animations.SKILLCAPES_CONSTRUCTION_PLAYER_ANIM_4953,
            ),
            intArrayOf(
                Items.MINING_CAPE_9792,
                Items.MINING_CAPE_T_9793,
                shared.consts.Graphics.SKILLCAPES_MINING_SPOTANIM_814,
                Animations.SKILLCAPES_PLAYER_MINING_4941,
            ),
            intArrayOf(
                Items.SMITHING_CAPE_9795,
                Items.SMITHING_CAPE_T_9796,
                shared.consts.Graphics.SKILLCAPES_SMITHING_SPOTANIM_815,
                Animations.SKILLCAPES_PLAYER_SMITHING_4943,
            ),
            intArrayOf(
                Items.FISHING_CAPE_9798,
                Items.FISHING_CAPE_T_9799,
                shared.consts.Graphics.SKILLCAPES_FISHING_SPOTANIM_819,
                Animations.SKILLCAPES_FISHING_PLAYER_ANIM_4951,
            ),
            intArrayOf(
                Items.COOKING_CAPE_9801,
                Items.COOKING_CAPE_T_9802,
                shared.consts.Graphics.SKILLCAPES_COOKING_SPOTANIM_821,
                Animations.SKILLCAPES_COOKING_PLAYER_ANIM_4955,
            ),
            intArrayOf(
                Items.FIREMAKING_CAPE_9804,
                Items.FIREMAKING_CAPE_T_9805,
                shared.consts.Graphics.SKILLCAPES_SPOT_FIREMAKING_831,
                Animations.SKILLCAPES_HUMAN_FIREMAKING_4975,
            ),
            intArrayOf(
                Items.WOODCUTTING_CAPE_9807,
                Items.WOODCUT_CAPE_T_9808,
                shared.consts.Graphics.SKILLCAPES_WOODCUTTING_SPOTANIM_822,
                Animations.SKILLCAPES_WOODCUTTING_PLAYER_ANIM_4957,
            ),
            intArrayOf(
                Items.FARMING_CAPE_9810,
                Items.FARMING_CAPE_T_9811,
                shared.consts.Graphics.SKILLCAPE_FARMING_SPOTANIM_825,
                Animations.SKILLCAPE_FARMING_4963,
            ),
            intArrayOf(
                Items.SUMMONING_CAPE_12169,
                Items.SUMMONING_CAPE_T_12170,
                shared.consts.Graphics.CLAN_FLYTHROUGH_PLAYER_SITTING_D_1515,
                Animations.SKILL_CAPE_SUMMONING_8525,
            ),
            intArrayOf(
                Items.QUEST_POINT_CAPE_9813,
                -1,
                shared.consts.Graphics.SKILLCAPES_QUEST_CAPE_SPOTANIM_816,
                Animations.SKILLCAPES_PLAYER_QUEST_CAPE_4945),
            intArrayOf(
                Items.HUNTER_CAPE_9948,
                Items.HUNTER_CAPE_T_9949,
                shared.consts.Graphics.SKILLCAPES_SPOT_HUNTING_907,
                Animations.SKILLCAPES_HUMAN_HUNTING_5158,
            ),
        )

        /**
         * Handles the emote button interaction by the player.
         *
         * @param player The player performing the emote.
         * @param buttonId The button id corresponding to the emote clicked.
         */
        fun handle(
            player: Player,
            buttonId: Int,
        ) {
            if (player.locks.isLocked("emote")) {
                sendMessage(player, "You're already doing an emote!")
                return
            }
            if (player.properties.combatPulse.isAttacking || player.inCombat()) {
                sendMessage(player, "You can't perform an emote while being in combat.")
                return
            }
            val emote = forId(buttonId) ?: run {
                player.debug("Unhandled emote for button id - $buttonId")
                return
            }
            if (!hasEmote(player, emote)) {
                if (player.rights == Rights.ADMINISTRATOR) {
                    unlockEmote(player, emote)
                    player.pulseManager.clear()
                    emote.play(player)
                    return
                }
                sendDialogue(player, emote.lockedMessage ?: "You can't use this emote.")
                return
            }
            if (!player.achievementDiaryManager.getDiary(DiaryType.VARROCK)!!.isComplete(1, 6) && (buttonId in 30..33)) {
                if (!player.getAttribute("emote-$buttonId", false)) {
                    setAttribute(player, "emote-$buttonId", true)
                }
                val good = (30..33).all { player.getAttribute("emote-$it", false) }
                player.achievementDiaryManager.getDiary(DiaryType.VARROCK)!!.updateTask(player, 1, 6, good)
            }
            player.pulseManager.clear()
            emote.play(player)
        }

        /**
         * Forces the player to perform an emote animation and optionally a graphic.
         * Locks the emote action for the duration of the animation.
         *
         * @param player The player performing the emote.
         * @param animation The animation to perform.
         * @param graphic The graphic to display with the animation.
         */
        private fun forceEmote(player: Player, animation: Animation?, graphic: Graphics) {
            if (animation != null) {
                player.animator.animate(animation, graphic)
                player.locks.lock("emote", animation.duration)
            }
        }

        /**
         * Retrieves the [Emotes] instance associated with the given button id.
         *
         * @param buttonId The button id clicked by the player.
         * @return The matching [Emotes] instance, or `null` if none found.
         */
        @JvmStatic
        fun forId(buttonId: Int): Emotes? = values().find { it.buttonId == buttonId }

        /**
         * Checks if the player is wearing the full chicken outfit.
         *
         * @param player The player to check.
         * @return `True` if the full chicken set is equipped, `false` otherwise.
         */
        private fun isWearingChickenOutfit(player: Player): Boolean {
            val head = player.equipment[EquipmentContainer.SLOT_HAT]
            val wings = player.equipment[EquipmentContainer.SLOT_CHEST]
            val legs = player.equipment[EquipmentContainer.SLOT_LEGS]
            val feet = player.equipment[EquipmentContainer.SLOT_FEET]
            return head?.id == Items.CHICKEN_HEAD_11021 && wings?.id == Items.CHICKEN_WINGS_11020 && legs?.id == Items.CHICKEN_LEGS_11022 && feet?.id == Items.CHICKEN_FEET_11019
        }

        /**
         * Checks if the player is eligible to complete the falador task.
         *
         * @param player The player to check.
         * @return `True` if the player meets the requirements.
         */
        private fun isEligibleForSalute(player: Player): Boolean =
            !player.achievementDiaryManager.hasCompletedTask(DiaryType.FALADOR, 1, 8) && player.location == Location(
                2997, 3374, 0
            ) && player.direction == Direction.SOUTH && anyInEquipment(
                player,
                Items.INITIATE_SALLET_5574,
                Items.INITIATE_HAUBERK_5575,
                Items.INITIATE_CUISSE_5576,
            )

        /**
         * Checks if the player is eligible for the goblin bow requirement.
         *
         * @param player The player to check.
         * @return True if the player can use it.
         */
        private fun isEligibleForGoblinBow(player: Player): Boolean =
            player.location.regionId == 13206 && !player.getAttribute("mistag-greeted", false)
    }
}
