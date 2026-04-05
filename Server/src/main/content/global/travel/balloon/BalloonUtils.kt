package content.global.travel.balloon

import content.data.GameAttributes
import content.region.other.entrana.quest.zep.dialogue.AugusteFirstTalkAfterQuestDialogue
import core.api.*
import core.cache.def.impl.ItemDefinition
import core.game.interaction.QueueStrength
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.TeleportManager
import core.game.node.entity.player.link.diary.DiaryType
import core.game.node.entity.skill.Skills
import core.game.node.item.Item
import core.game.world.update.flag.context.Animation
import core.tools.colorize
import shared.consts.Components
import shared.consts.Quests
import shared.consts.Regions
import shared.consts.Sounds

/**
 * Utility responsible for rendering and updating balloon flight interface state.
 *
 * # Handles
 * - balloon init
 * - movement rendering per input
 * - stage progression
 * - interface reset and cleanup
 * - destination unlocking
 * - flight validation
 * - teleport sequence handling
 * - route payment and access control
 * - log consumption
 * - charge storage (varbit-based)
 * - bulk log conversion into charges
 *
 * # Relations
 * - [BalloonFlightInterface]
 * - [BalloonFlightHandler]
 * - [AssistantDialogue]
 */
object BalloonUtils {

    private const val SANDBAG_VARBIT = 2880
    private const val LOGS_VARBIT = 2881
    private const val STORAGE_CAPACITY = 4000

    /**
     * Draws the initial balloon position for a given route stage.
     *
     * @param player target player
     * @param routeId route identifier
     * @param step current stage index (1-based)
     */
    fun drawBaseBalloon(player: Player, routeId: Int, step: Int) {
        val stage = BalloonRouteConfiguration.ROUTES[routeId]!!.stages[step - 1]
        val base = stage.position

        setAttribute(player, keyTop(routeId, step), base.top)
        setAttribute(player, keyBottom(routeId, step), base.bottom)

        sendModelOnInterface(player, Components.ZEP_INTERFACE_470, base.top, 19517)
        sendModelOnInterface(player, Components.ZEP_INTERFACE_470, base.bottom, 19518)
    }

    /**
     * Applies movement input and redraw balloon position.
     *
     * @param player target player
     * @param move movement type
     * @param routeId route identifier
     * @param step current stage index
     */
    fun drawBalloon(player: Player, move: BalloonMove, routeId: Int, step: Int) {
        val stage = BalloonRouteConfiguration.ROUTES[routeId]!!.stages[step - 1]
        val base = stage.position

        var top = getAttribute(player, keyTop(routeId, step), base.top)
        var bottom = getAttribute(player, keyBottom(routeId, step), base.bottom)

        sendModelOnInterface(player, Components.ZEP_INTERFACE_470, top, -1)
        sendModelOnInterface(player, Components.ZEP_INTERFACE_470, bottom, -1)

        if (bottom == 98 || bottom == 113) bottom += 2

        if (move == BalloonMove.EMERGENCY_TUG) {// Diagonal correction.
            top = moveEast(top)
            bottom = moveEast(bottom)

            top = moveSouth(top)
            bottom = moveSouth(bottom)
        } else {
            repeat(move.dx) {
                top = moveEast(top)
                bottom = moveEast(bottom)
            }

            repeat(kotlin.math.abs(move.dy)) {
                if (move.dy < 0) {
                    top = moveNorth(top)
                    bottom = moveNorth(bottom)
                } else {
                    top = moveSouth(top)
                    bottom = moveSouth(bottom)
                }
            }
        }

        sendModelOnInterface(player, Components.ZEP_INTERFACE_470, top, 19517)
        sendModelOnInterface(player, Components.ZEP_INTERFACE_470, bottom, 19518)

        setAttribute(player, keyTop(routeId, step), top)
        setAttribute(player, keyBottom(routeId, step), bottom)
    }

    /**
     * Advances the balloon interface to the next stage or completes the route.
     *
     * @param player target player
     * @param routeId route identifier
     * @param step current stage index
     * @param routeData route configuration
     */
    fun updateScreen(player: Player, routeId: Int, step: Int, routeData: RouteData) {

        val nextStep = step + 1

        if (nextStep <= routeData.stages.size) {
            setAttribute(player, "zep_current_step_$routeId", nextStep)

            val nextStage = routeData.stages[nextStep - 1]

            nextStage.overlay(player, Components.ZEP_INTERFACE_470)
            drawBaseBalloon(player, routeId, nextStep)
            return
        }

        val balloonDestination =
            when (routeId) {
                1 -> BalloonTravelDefinition.TAVERLEY
                2 -> BalloonTravelDefinition.CRAFT_GUILD
                3 -> BalloonTravelDefinition.VARROCK
                4 -> BalloonTravelDefinition.CASTLE_WARS
                5 -> BalloonTravelDefinition.GRAND_TREE
                else -> BalloonTravelDefinition.TAVERLEY
            }

        removeAttribute(player, "zep_current_step_$routeId")
        teleport(player, balloonDestination.destination)
        unlockDestination(player, balloonDestination)

        closeOverlay(player)
        closeInterface(player)

        if (!isQuestComplete(player, Quests.ENLIGHTENED_JOURNEY))
            openDialogue(player, AugusteFirstTalkAfterQuestDialogue())
    }

    /**
     * Moves model one tile east in interface grid space.
     */
    private fun moveEast(child: Int) = child + 1

    /**
     * Moves model north with row-wrap correction.
     */
    private fun moveNorth(child: Int) = child + if (child >= 118) 20 else 19

    /**
     * Moves model one tile south in interface grid space.
     */
    private fun moveSouth(child: Int) = child - 19

    /**
     * Represents available balloon movement actions.
     */
    enum class BalloonMove(val dx: Int, val dy: Int) {
        SANDBAG(1, -2),
        LOGS(1, -1),
        RELAX(1, 0),
        TUG(0, 1),
        EMERGENCY_TUG(0, 2)
    }

    /**
     * Plays sound associated with balloon control button.
     */
    fun getSoundForButton(player: Player, buttonID: Int) {
        val sound =
            mapOf(
                4 to Sounds.ZEP_DROP_BALLAST_3249,
                9 to Sounds.ZEP_USE_LOGS_3251,
                5 to Sounds.ZEP_BREEZE_3247,
                6 to Sounds.ZEP_HAMMERING_1_3250,
                10 to Sounds.ZEP_CONSTRUCT_3248
            )[buttonID]

        sound?.let { playAudio(player, it) }
    }

    /**
     * Converts button id into debug-friendly name.
     */
    private fun getButtonName(buttonID: Int): String = when (buttonID) {
        9 -> "Logs"
        4 -> "Sandbag"
        5 -> "Relax"
        6 -> "Rope"
        10 -> "Red rope"
        else -> "unknown [$buttonID]"
    }

    /**
     * Prints next expected move in current sequence (debug only).
     */
    fun debugNextMove(player: Player, sequence: List<Int>, stage: Int, index: Int) {
        if (sequence.isEmpty()) {
            player.debug("Stage $stage sequence not implemented.")
            return
        }

        val next = sequence.getOrNull(index)
        if (next != null) {
            player.debug("Stage $stage | Step ${index + 1}/${sequence.size} -> Next: ${getButtonName(next)}")
        } else {
            player.debug("Stage $stage completed")
        }
    }

    /**
     * Clears all balloon-related attributes for current stage.
     */
    fun clearBalloonState(player: Player, routeId: Int, step: Int) {
        removeAttributes(
            player,
            "zep_current_route",
            "zep_current_step_$routeId",
            "zep_sequence_progress_${routeId}_$step",
            keyTop(routeId, step),
            keyBottom(routeId, step)
        )
    }

    private fun keyTop(routeId: Int, step: Int) = "zep_balloon_top_${routeId}_$step"
    private fun keyBottom(routeId: Int, step: Int) = "zep_balloon_bottom_${routeId}_$step"

    private val allChildren = (78..230).toSet()

    /**
     * Resets all interface models for balloon screen.
     */
    fun reset(player: Player, component: Int) {
        allChildren.forEach { sendModelOnInterface(player, component, it, -1) }
    }

    /**
     * Checks whether destination is unlocked via varbit.
     */
    private fun isUnlocked(player: Player, destination: BalloonTravelDefinition): Boolean {
        return getVarbit(player, destination.varbitId) == 1
    }

    /**
     * Opens balloon world map interface and sets origin context.
     */
    fun openFlightOverlay(player: Player, location: BalloonTravelDefinition) {
        player.setAttribute(GameAttributes.BALLOON_ORIGIN, location)
        openInterface(player, Components.ZEP_BALLOON_MAP_469)
        setComponentVisibility(player, Components.ZEP_BALLOON_MAP_469, location.componentId, false)
    }

    /**
     * Handles payment validation for balloon travel.
     * Supports both charge-based and log-based payment systems.
     */
    fun payForFlight(
        player: Player,
        origin: BalloonTravelDefinition?,
        destination: BalloonTravelDefinition,
        onSuccess: () -> Unit
    ) {
        val unlocked = isUnlocked(player, destination)

        if (unlocked) {
            if (consumeCharges(player, destination)) {
                onSuccess()
                return
            }

            if (consumeLogs(player, destination)) {
                onSuccess()
                return
            }

            sendMessage(player, "You don't have enough charges or logs.")
            return
        }

        if (consumeLogs(player, destination)) {
            onSuccess()
        } else {
            sendMessage(player, "You don't have the required logs.")
        }
    }

    /**
     * Starts balloon flight sequence, handles animation, teleport and cleanup.
     */
    fun startFlight(player: Player, destination: BalloonTravelDefinition) {
        val origin = player.getAttribute<BalloonTravelDefinition>(GameAttributes.BALLOON_ORIGIN)
        if (origin == null) {
            player.debug("null location.")
            return
        }

        val animationId = BalloonTravelDefinition.getAnimationId(origin, destination)
        val anim = Animation(animationId)
        val animationDelay = animationDuration(anim)

        registerLogoutListener(player, "balloon-flight") { p -> p.location = player.location }

        lock(player, animationDelay)
        hideMinimap(player)
        playJingle(player, 118)
        openOverlay(player, Components.BLACK_OVERLAY_333)
        openInterface(player, Components.ZEP_BALLOON_MAP_469)
        setComponentVisibility(player, Components.ZEP_BALLOON_MAP_469, 12, false)
        animateInterface(player, Components.ZEP_BALLOON_MAP_469, 12, animationId)
        sendMessage(player, "You board the balloon and fly to ${destination.destName}.")
        teleport(player, destination.destination, TeleportManager.TeleportType.INSTANT)

        queueScript(player, animationDelay, QueueStrength.SOFT) {
            unlock(player)
            closeInterface(player)
            showMinimap(player)
            openOverlay(player, Components.FADE_FROM_BLACK_170)
            removeAttribute(player, GameAttributes.BALLOON_ORIGIN)
            sendDialogue(player, "You arrive safely ${destination.destName}.")
            when (destination) {
                BalloonTravelDefinition.VARROCK -> finishDiaryTask(player, DiaryType.VARROCK, 1, 17)
                BalloonTravelDefinition.CASTLE_WARS -> finishDiaryTask(player, DiaryType.ARDOUGNE, 1, 7)
                else -> {}
            }
            return@queueScript stopExecuting(player)
        }
    }

    /**
     * Unlocks destination for player and grants reward XP if applicable.
     */
    fun unlockDestination(player: Player, destination: BalloonTravelDefinition) {
        if (getVarbit(player, destination.varbitId) != 1) {
            setVarbit(player, destination.varbitId, 1, true)

            val xp = 2000
            if (destination != BalloonTravelDefinition.ENTRANA) {
                rewardXP(player, Skills.FIREMAKING, xp.toDouble())
            }

            sendMessage(
                player,
                colorize("%RYou have unlocked the balloon route to ${destination.destName}!")
            )
        } else {
            sendDialogue(player, "You can open new locations from Entrana.")
        }
    }

    /**
     * Validates whether player is allowed to start flight.
     */
    fun checkRequirements(
        player: Player,
        origin: BalloonTravelDefinition?,
        destination: BalloonTravelDefinition
    ): Boolean {

        if (!hasLevelStat(player, Skills.FIREMAKING, destination.requiredLevel)) {
            sendDialogue(
                player,
                "You require a Firemaking level of ${destination.requiredLevel} to travel to ${destination.destName}."
            )
            return false
        }

        if (origin == destination) {
            sendDialogue(player, "You can't fly to the same location.")
            return false
        }

        if (player.familiarManager.hasFamiliar() || player.familiarManager.hasPet()) {
            sendMessage(player, "You can't take a follower or pet on a ride.")
            return false
        }

        if (player.settings.weight > 40.0) {
            sendDialogue(player, "You're carrying too much weight to fly. Try reducing your weight below 40 kg.")
            return false
        }

        if (destination == BalloonTravelDefinition.ENTRANA) {
            if (!ItemDefinition.canEnterEntrana(player)) {
                sendDialogue(player, "You can't take flight with weapons and armour to Entrana.")
                return false
            }
            sendMessage(player, "You are quickly searched.")
        }

        return true
    }

    /**
     * Handles unlocking new location.
     */
    fun unlockNewLocation(
        player: Player,
        destination: BalloonTravelDefinition
    ): Boolean {
        if (isUnlocked(player, destination) ||
            destination == BalloonTravelDefinition.ENTRANA ||
            destination == BalloonTravelDefinition.TAVERLEY
        ) {
            return false
        }

        if (!inBorders(player, getRegionBorders(Regions.ENTRANA_11060))) {
            sendDialogue(player, "You can open new locations from Entrana.")
            return true
        }

        if (!consumeLogs(player, destination)) {
            val logName = getItemName(destination.logId)
                .lowercase()
                .removeSuffix("s")
                .trim()

            sendDialogue(
                player,
                "You need ${destination.logAmount} $logName to start."
            )
            return true
        }

        closeInterface(player)
        setAttribute(player, "zep_current_route", destination.ordinal)
        setAttribute(player, "zep_current_step_${destination.ordinal}", 1)
        openInterface(player, Components.ZEP_INTERFACE_470)

        return true
    }

    /**
     * Consumes required logs from player inventory for destination travel.
     */
    fun consumeLogs(
        player: Player,
        destination: BalloonTravelDefinition
    ): Boolean {
        return removeItem(
            player,
            Item(destination.logId, destination.logAmount)
        )
    }

    /**
     * Consumes balloon charges stored in varbit.
     */
    fun consumeCharges(
        player: Player,
        destination: BalloonTravelDefinition
    ): Boolean {
        val cost = destination.chargeCost
        val current = getCharges(player)

        if (current < cost) return false

        setVarbit(player, LOGS_VARBIT, current - cost, true)
        return true
    }

    /**
     * Returns current stored balloon charges.
     */
    fun getCharges(player: Player): Int =
        getVarbit(player, LOGS_VARBIT)

    /**
     * Adds charges to player storage.
     */
    fun addCharges(player: Player, amount: Int) {
        if (amount <= 0) return
        setVarbit(player, LOGS_VARBIT, getCharges(player) + amount, true)
    }

    /**
     * Removes all balloon logs from inventory.
     * Returns total amount removed.
     */
    private fun removeAllLogs(player: Player): Int {
        var total = 0

        BalloonTravelDefinition.values().forEach { def ->
            val amount = amountInInventory(player, def.logId)
            val removeItem = Item(def.logId, amount)
            if (amount > 0) {
                removeItem(player, removeItem)
                total += amount
            }
        }

        return total
    }

    /**
     * Converts all inventory logs into stored balloon charges.
     */
    fun handOverLogs(player: Player): Int {
        val removed = removeAllLogs(player)

        if (removed > 0) {
            addCharges(player, removed)
        }

        return removed
    }
}