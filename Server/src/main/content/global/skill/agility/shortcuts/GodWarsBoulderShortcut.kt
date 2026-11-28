package content.global.skill.agility.shortcuts

import content.data.Quests

import content.data.GameAttributes
import content.global.skill.agility.AgilityHandler
import content.global.skill.agility.AgilityShortcut
import core.api.*
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.TeleportManager
import core.game.node.entity.skill.Skills
import core.game.node.scenery.Scenery
import core.game.system.task.Pulse
import core.game.world.GameWorld
import core.game.world.map.Location
import core.game.world.map.build.DynamicRegion
import core.game.world.update.flag.context.Animation
import core.plugin.Initializable
import shared.consts.Animations
import shared.consts.Components

/**
 * Handles the GWD shortcut.
 */
@Initializable
class GodWarsBoulderShortcut : AgilityShortcut(intArrayOf(26338), 0, 0.0, "move") {

    private var boulder: Scenery? = null

    override fun run(player: Player, scenery: Scenery, option: String, failed: Boolean) {
        if (!isQuestComplete(player, Quests.TROLL_STRONGHOLD.questName)) {
            sendMessage(player, "You need to complete the Troll Stronghold quest to move this boulder.")
            return
        }

        if (getStatLevel(player, Skills.STRENGTH) < 60) {
            sendMessage(player, "You need a Strength level of 60 to move this boulder.")
            return
        }

        val region = DynamicRegion.create(11578)
        region.add(player)
        region.flagActive()
        region.setRegionTimeOut(16)

        val isMovingNorth = player.location.y < 3716
        val startLocationY = if (isMovingNorth) 3 else 7
        val targetLocation = region.baseLocation.transform(18, startLocationY, 0)

        teleport(player, targetLocation)
        boulder = Scenery(scenery.id, region.baseLocation.transform(18, 4, 0), 10, 0)

        registerLogoutListener(player, GameAttributes.LOGOUT) { p: Player ->
            p.location = getAttribute(p, GameAttributes.ORIGINAL_LOCATION, player.location)
        }

        val boulderInstance = boulder!!.getChild(player)
        val moveOffsetY = if (isMovingNorth) 4 else -4

        lock(player, 14)
        GameWorld.Pulser.submit(object : Pulse(1, player) {
            var counter = 0
            override fun pulse(): Boolean {
                when (counter++) {
                    0 -> {
                        openOverlay(player, Components.SNOW_OVERLAY_370)
                        animateScenery(boulderInstance, Animations.GODWARS_EA_BOULDER_MOVE_UP_6980)
                        AgilityHandler.forceWalk(
                            player,
                            -1,
                            player.location,
                            player.location.transform(0, moveOffsetY, 0),
                            Animation(if (isMovingNorth) Animations.GODWARS_EA_PLAYER_MOVE_BOULDER_6978 else Animations.GODWARS_EA_PLAYER_MOVE_BOULDER_SOUTH_6979),
                            3,
                            0.0,
                            null
                        )
                    }

                    12 -> {
                        animateScenery(boulderInstance, Animations.GODWARS_EA_BOULDER_MOVE_DOWN_6981)
                    }

                    14 -> {
                        val finalY = 3715 + if (isMovingNorth) 4 else 0
                        teleport(player, Location.create(2898, finalY, 0), TeleportManager.TeleportType.INSTANT)
                        removeAttributes(player, GameAttributes.LOGOUT, GameAttributes.ORIGINAL_LOCATION)
                        clearLogoutListener(player, GameAttributes.LOGOUT)
                        unlock(player)
                        return true
                    }
                }
                return false
            }
        })
    }
}
