package content.global.activity.tog

import content.data.GameAttributes
import core.api.*
import core.game.component.Component
import core.game.event.EventHook
import core.game.event.TickEvent
import core.game.interaction.InteractionListener
import core.game.interaction.QueueStrength
import core.game.node.entity.Entity
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import core.game.world.map.Location
import core.game.world.map.zone.ZoneBorders
import core.game.world.map.zone.ZoneRestriction
import core.game.world.update.flag.context.Animation
import shared.consts.*

/**
 * Tears of Guthix Activity
 * ```
 * ::setvarbit 454 // (varp 449 7-11) 0-10 where its just time bar length
 * ::setvarbit 455 // (varp 449 12-20) X where x is number of points he gets
 * ```
 * @author ovenbreado
 */
class TearsOfGuthixActivity : MapArea, InteractionListener, EventHook<TickEvent> {

    companion object {
        const val TEARS_SWITCH = Vars.VARBIT_TOG_COLLECTING_TEARS_SWITCH_453
        const val QP_REQUIREMENTS = Vars.VARBIT_TEARS_OF_GUTHIX_QUEST_POINT_REQUIREMENT_456
        const val VARBIT_TOG_TIME_BAR = Vars.VARBIT_TEARS_OF_GUTHIX_TIME_BAR_454
        const val VARBIT_TOG_SCORE = Vars.VARBIT_TEARS_OF_GUTHIX_POINTS_455

        private val junaScenery = getScenery(Location(3252, 9516, 2))
        private val rewardArray = arrayOf(Skills.COOKING, Skills.CRAFTING, Skills.FIREMAKING, Skills.FISHING, Skills.MAGIC, Skills.MINING, Skills.PRAYER, Skills.RANGE, Skills.RUNECRAFTING, Skills.SMITHING, Skills.WOODCUTTING, Skills.AGILITY, Skills.HERBLORE, Skills.FLETCHING, Skills.THIEVING, Skills.SLAYER, Skills.ATTACK, Skills.DEFENCE, Skills.STRENGTH, Skills.HITPOINTS, Skills.FARMING, Skills.CONSTRUCTION, Skills.HUNTER)

        private val rewardText =
            arrayOf(
                "You have a brief urge to cook some food.",
                "Your fingers feel nimble and suited to delicate work.",
                "You have a brief urge to set light to something.",
                "You gain a deep understanding of the creatures of the sea.",
                "You feel the power of the runes surging through you.",
                "You gain a deep understanding of the stones of the earth.",
                "You suddenly feel very close to the gods.",
                "Your aim improves.",
                "You gain a deep understanding of runes.",
                "You gain a deep understanding of all types of metal.",
                "You gain a deep understanding of the trees in the wood.",
                "You feel very nimble.",
                "You gain a deep understanding of all kinds of strange plants.",
                "You gain a deep understanding of wooden sticks.",
                "You feel your respect for others' property slipping away.",
                "You gain a deep understanding of many strange creatures.",
                "You feel a brief surge of aggression.",
                "You feel more able to defend yourself.",
                "Your muscles bulge.",
                "You feel very healthy.",
                "You gain a deep understanding of the cycles of nature.",
                "You feel homesick.",
                "You briefly experience the joy of the hunt.",
                "You feel at one with nature.",
            )

        private fun rewardTears(player: Player) {
            val lowestSkill =
                rewardArray.reduce { acc, curr ->

                    if (curr == Skills.CONSTRUCTION && !hasHouse(player)) {
                        acc
                    } else if (curr == Skills.HERBLORE && !isQuestComplete(player, Quests.DRUIDIC_RITUAL)) {
                        acc
                    } else if (curr == Skills.RUNECRAFTING && !isQuestComplete(player, Quests.RUNE_MYSTERIES)) {
                        acc
                    } else if (player.skills.getExperience(acc) <= player.skills.getExperience(curr)) {
                        acc
                    } else {
                        curr
                    }
                }

            var perTearXP = 60.0
            if (getStatLevel(player, lowestSkill) < 30) {
                perTearXP = (getStatLevel(player, lowestSkill) - 1) * 1.724137
                perTearXP += 10
            }

            sendMessage(player, rewardText[rewardArray.indexOf(lowestSkill)])

            val tearsCollected = getAttribute(player, GameAttributes.TOG_TEARS_TTL, 0)
            rewardXP(player, lowestSkill, perTearXP * tearsCollected)
        }

        fun startGame(player: Player) {
            player.interfaceManager.openSingleTab(Component(Components.TOG_WATER_BOWL_4))
            setAttribute(player, GameAttributes.TOG_TIMER, getQuestPoints(player) + 15)
            setAttribute(player, GameAttributes.TOG_TEARS_TTL, 0)

            setVarbit(player, VARBIT_TOG_TIME_BAR, 10)
            setVarbit(player, VARBIT_TOG_SCORE, 0)

            player.equipment.replace(Item(Items.STONE_BOWL_4704), 3)
            player.appearance.setAnimations(Animation(Animations.TOG_BOWL_RENDER_357))
            player.appearance.sync()

            playAudio(player, Sounds.JUNA_HISS_1797)
            junaScenery?.let { animateScenery(it, Animations.JUNA_TAIL_LIFT_2055) }
            registerLogoutListener(player, "tog-bowl") {
                removeItem(player, Items.STONE_BOWL_4704, Container.EQUIPMENT)
            }

            queueScript(player, 1, QueueStrength.SOFT) { stage ->
                when (stage) {
                    0 -> {
                        val dest = Location(3251, 9516, 2)
                        player.walkingQueue.reset()
                        player.walkingQueue.addPath(dest.x, dest.y)
                        return@queueScript delayScript(player, 1)
                    }
                    1 -> {
                        val dest = Location(3253, 9516, 2)
                        player.walkingQueue.reset()
                        player.walkingQueue.addPath(dest.x, dest.y)
                        return@queueScript delayScript(player, 1)
                    }
                    2 -> {
                        val dest = Location(3257, 9517, 2)
                        player.walkingQueue.reset()
                        player.walkingQueue.addPath(dest.x, dest.y)
                        return@queueScript stopExecuting(player)
                    }

                    else -> return@queueScript stopExecuting(player)
                }
            }
        }

        fun endGame(player: Player) {
            sendMessage(player, "Your time in the cave is up.")

            playAudio(player, Sounds.JUNA_HISS_1797)
            junaScenery?.let { animateScenery(it, Animations.JUNA_TAIL_LIFT_2055) }

            queueScript(player, 1, QueueStrength.SOFT) { stage ->
                when (stage) {
                    0 -> {
                        val dest = Location(3253, 9517, 2)
                        player.walkingQueue.reset()
                        player.walkingQueue.addPath(dest.x, dest.y)
                        return@queueScript delayScript(player, 1)
                    }

                    1 -> {
                        val dest = Location(3253, 9516, 2)
                        player.walkingQueue.reset()
                        player.walkingQueue.addPath(dest.x, dest.y)
                        return@queueScript delayScript(player, 1)
                    }

                    2 -> {
                        val dest = Location(3251, 9516, 2)
                        player.walkingQueue.reset()
                        player.walkingQueue.addPath(dest.x, dest.y)
                        return@queueScript delayScript(player, 1)
                    }

                    3 -> {
                        playAudio(player, Sounds.DRINK_AND_MAGIC_1796)
                        animate(player, Animations.DRINK_BOWL_2045)
                        sendMessage(player, "You drink the liquid...")
                        return@queueScript delayScript(player, 1)
                    }

                    4 -> {
                        rewardTears(player)
                        setAttribute(player, GameAttributes.QUEST_TOG_LAST_DATE, System.currentTimeMillis())
                        setAttribute(player, GameAttributes.QUEST_TOG_LAST_XP_AMOUNT, player.skills.totalXp)
                        setAttribute(player, GameAttributes.QUEST_TOG_LAST_QP, getQuestPoints(player))
                        removeAttribute(player, GameAttributes.TOG_TEARS_TTL)
                        if (player.interfaceManager.singleTab?.id == 4) {
                            player.interfaceManager.closeSingleTab()
                        }
                        player.interfaceManager.restoreTabs()
                        removeItem(player, Items.STONE_BOWL_4704, Container.EQUIPMENT)
                        clearLogoutListener(player, "tog-bowl")
                        return@queueScript stopExecuting(player)
                    }

                    else -> return@queueScript stopExecuting(player)
                }
            }
        }
    }

    override fun defineListeners() {
        on(Scenery.WEEPING_WALL_6660, SCENERY, "collect-from") { player, node ->
            playAudio(player, Sounds.COLLECT_TEAR_1795)
            animate(player, Animations.FILL_TOG_BOWL_2043)
            val index = TearsOfGuthixListener.allWalls.indexOf(node.location)
            setAttribute(player, GameAttributes.TOG_ACTIVITY, index)
            return@on true
        }
    }

    override fun process(entity: Entity, event: TickEvent) {
        if (entity !is Player) return

        val timer = getAttribute(entity, GameAttributes.TOG_TIMER, -1)
        if (timer > 0) {
            val newTimer = timer - 1
            setAttribute(entity, GameAttributes.TOG_TIMER, newTimer)

            // Timer bar.
            val questPoints = getQuestPoints(entity)
            setVarbit(entity, VARBIT_TOG_TIME_BAR, (newTimer * 10 / questPoints), false)

            // Activity.
            val activityIndex = getAttribute(entity, GameAttributes.TOG_ACTIVITY, 0)
            if (activityIndex != 0) {
                val tearState = TearsOfGuthixListener.globalWallState[activityIndex]
                var tears = getAttribute(entity, GameAttributes.TOG_TEARS_TTL, 0)

                when (tearState) {
                    1 -> {
                        playAudio(entity, Sounds.COLLECT_GOOD_TEAR_1794)
                        tears++
                    }
                    2 -> {
                        if (tears > 0) {
                            playAudio(entity, Sounds.COLLECT_BAD_TEAR_1793)
                            tears--
                        }
                    }
                }

                setAttribute(entity, GameAttributes.TOG_TEARS_TTL, tears)
                setVarbit(entity, VARBIT_TOG_SCORE, tears)
            }

        } else if (timer == 0) {
            removeAttribute(entity, GameAttributes.TOG_TIMER)
            endGame(entity)
        }
    }

    override fun defineAreaBorders(): Array<ZoneBorders> = arrayOf(ZoneBorders(3253, 9513, 3262, 9522, 2))

    override fun getRestrictions(): Array<ZoneRestriction> = arrayOf(ZoneRestriction.RANDOM_EVENTS, ZoneRestriction.CANNON, ZoneRestriction.FOLLOWERS, ZoneRestriction.TELEPORT)

    override fun areaEnter(entity: Entity) {
        if (entity is Player) {
            if (getAttribute(entity, GameAttributes.TOG_TIMER, 0) <= 0) {
                removeItem(entity, Items.STONE_BOWL_4704, Container.EQUIPMENT)
                teleport(entity, Location(3251, 9516, 2))
            } else {
                entity.hook(Event.Tick, this)
            }
        }
    }

    override fun areaLeave(entity: Entity, logout: Boolean) {
        if (entity is Player) {
            entity.unhook(this)
            if (logout) {
                removeItem(entity, Items.STONE_BOWL_4704, Container.EQUIPMENT)
                removeAttribute(entity, GameAttributes.TOG_TEARS_TTL)
                removeAttribute(entity, GameAttributes.TOG_TIMER)
                teleport(entity, Location(3251, 9516, 2))
            }
        }
    }

    override fun entityStep(entity: Entity, location: Location, lastLocation: Location) {
        if (entity is Player) {
            entity.hook(Event.Tick, this)
            setAttribute(entity, GameAttributes.TOG_ACTIVITY, 0)
        }
    }
}
