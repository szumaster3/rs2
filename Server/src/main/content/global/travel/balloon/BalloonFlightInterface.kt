package content.global.travel.balloon

import core.api.*
import core.game.interaction.InterfaceListener
import core.game.node.entity.player.Player
import core.game.system.command.Privilege
import shared.consts.Components
import shared.consts.Quests

class BalloonFlightInterface : InterfaceListener, Commands {

    override fun defineInterfaceListeners() {

        onClose(Components.ZEP_INTERFACE_470) { player, _ ->
            closeSingleTab(player)
            showMinimap(player)
            return@onClose true
        }

        onOpen(Components.ZEP_INTERFACE_470) { player, _ ->
            openSingleTab(player, Components.ZEP_INTERFACE_SIDE_471)
            hideMinimap(player)

            val routeId = getAttribute(player, "zep_current_route", 1)
            setAttribute(player, "zep_current_route", routeId)

            val stepAttr = "zep_current_step_$routeId"
            val step = getAttribute(player, stepAttr, 1)
            setAttribute(player, stepAttr, step)

            BalloonRouteConfiguration.ROUTES[routeId]?.let {
                refresh(player, routeId, it)
            }

            return@onOpen true
        }

        on(Components.ZEP_INTERFACE_SIDE_471) { player: Player, _, _, buttonID: Int, _, _ ->

            val routeId = getAttribute(player, "zep_current_route", -1)
            if (routeId == -1) return@on true

            val routeData = BalloonRouteConfiguration.ROUTES[routeId] ?: return@on true
            val stepAttr = "zep_current_step_$routeId"
            val step = getAttribute(player, stepAttr, 1)

            val stage = routeData.stages.getOrNull(step - 1) ?: return@on true

            val progressAttr = "zep_sequence_progress_${routeId}_$step"
            val index = getAttribute(player, progressAttr, 0)

            registerLogoutListener(player, "balloon-control-panel") {
                BalloonUtils.clearBalloonState(player, routeId, step)
            }

            val sequence = stage.sequence

            val move = when (buttonID) {
                4 ->  BalloonUtils.BalloonMove.SANDBAG
                9 ->  BalloonUtils.BalloonMove.LOGS
                5 ->  BalloonUtils.BalloonMove.RELAX
                6 ->  BalloonUtils.BalloonMove.TUG
                10 -> BalloonUtils.BalloonMove.EMERGENCY_TUG
                else -> null
            }

            if (buttonID == 8 || buttonID != sequence.getOrNull(index) || move == null) {
                BalloonUtils.clearBalloonState(player, routeId, step)
                closeInterface(player)
                closeSingleTab(player)
                return@on true
            }

            BalloonUtils.getSoundForButton(player, buttonID)
            BalloonUtils.drawBalloon(player, move, routeId, step)

            val newIndex = index + 1
            setAttribute(player, progressAttr, newIndex)
            BalloonUtils.debugNextMove(player, sequence, step, newIndex)
            if (newIndex >= sequence.size) {
                BalloonUtils.reset(player, Components.ZEP_INTERFACE_470)
                removeAttribute(player, progressAttr)

                BalloonUtils.updateScreen(player, routeId, step, routeData)
                refresh(player, routeId, routeData)
            }

            return@on true
        }
    }

    override fun defineCommands() {

        define(
            name = "balloon",
            privilege = Privilege.ADMIN,
            usage = "::balloon <routeId>",
            description = "Opens balloon interface for tests."
        ) { player, args ->

            if (args.size < 2) {
                reject(player, "Syntax: ::balloon <routeId>")
                return@define
            }

            val routeId = args[1].toIntOrNull()
            if (routeId == null || !BalloonRouteConfiguration.ROUTES.containsKey(routeId)) {
                reject(player, "Invalid routeId. Available: ${BalloonRouteConfiguration.ROUTES.keys}")
                return@define
            }

            val route = BalloonRouteConfiguration.ROUTES[routeId]!!

            setAttribute(player, "zep_current_route", routeId)
            setAttribute(player, "zep_current_stage", 1)
            setAttribute(player, "zep_current_step_$routeId", 1)

            openInterface(player, Components.ZEP_INTERFACE_470)

            refresh(player, routeId, route)

            player.debug("Opened balloon route: $routeId")
        }

        define(
            name = "finbal",
            privilege = Privilege.ADMIN,
            usage = "::bfinbal",
            description = "Enable balloon traveling."
        ) { player, _ ->
            finishQuest(player, Quests.ENLIGHTENED_JOURNEY)
        }
    }

    private fun refresh(player: Player, routeId: Int, routeData: RouteData)
    {
        val step = getAttribute(player, "zep_current_step_$routeId", 1)
        val stage = routeData.stages.getOrNull(step - 1) ?: return

        BalloonUtils.drawBaseBalloon(player, routeId, step)

        stage.overlay.invoke(player, Components.ZEP_INTERFACE_470)

        BalloonUtils.debugNextMove(player, stage.sequence, step, 0)
    }
}