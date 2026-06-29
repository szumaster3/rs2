package content.global.skill.construction

import content.data.GameAttributes
import core.api.*
import core.game.node.entity.Entity
import core.game.node.entity.player.Player
import core.game.system.task.Pulse
import core.game.world.map.RegionManager.forId
import core.game.world.map.RegionManager.removeRegion
import core.game.world.map.zone.MapZone
import core.game.world.map.zone.ZoneRestriction
import core.game.world.map.zone.ZoneType
import shared.consts.Items

class HouseZone(private val house: HouseManager) :
    MapZone("poh-zone$house", true, ZoneRestriction.RANDOM_EVENTS) {
    private var previousRegion = -1
    private var previousDungeon = -1

    override fun configure() {
        unregisterOldRegions()
        setZoneType(ZoneType.P_O_H.id)
        registerRegion(house.houseRegion.id)

        house.dungeonRegion?.let {
            registerRegion(it.id)
        }
    }

    private fun unregisterOldRegions() {
        if (previousRegion != -1) {
            unregisterRegion(previousRegion)
        }
        if (previousDungeon != -1) {
            unregisterRegion(previousDungeon)
        }
    }

    override fun enter(e: Entity): Boolean {
        if (e is Player) {

            if (house == e.houseManager) {
                previousRegion = house.houseRegion.id
                previousDungeon = house.dungeonRegion?.id ?: -1
            }
            registerLogoutListener(e, "houselogout") { p ->
                p.location = house.location.exitLocation
            }
        }
        return super.enter(e)
    }

    override fun death(e: Entity, killer: Entity): Boolean {
        if (e is Player) {
            HouseManager.leave(e)
            return true
        }

        return super.death(e, killer)
    }

    override fun leave(e: Entity, logout: Boolean): Boolean {
        if (e !is Player) {
            return true
        }

        val player = e

        if(getAttribute(player, GameAttributes.CON_GAZE_INTO, false)) return false

        if (!logout) {
            val dest = player.properties.teleportLocation

            val currentRegion = player.location.regionId
            val houseRegion = house.houseRegion.id
            val dungeonRegion = house.dungeonRegion?.id ?: -1

            val currentlyInHouse =
                currentRegion == houseRegion || currentRegion == dungeonRegion

            val destinationRegion =
                dest?.regionId ?: -1

            val movingToHouse =
                destinationRegion == houseRegion || destinationRegion == dungeonRegion

            if (currentlyInHouse || movingToHouse) {
                return true
            }
        }

        removeItems(player)

        if (house == player.houseManager) {

            house.expelGuests(player)

            val removeHouse = previousRegion
            val removeDungeon = previousDungeon

            submitWorldPulse(object : Pulse(2) {
                override fun pulse(): Boolean {

                    val region = forId(removeHouse)
                    val dungeon = if (removeDungeon != -1)
                        forId(removeDungeon)
                    else null

                    removeRegion(removeHouse)
                    unregisterRegion(removeHouse)
                    region.flagInactive()

                    if (removeDungeon != -1) {
                        removeRegion(removeDungeon)
                        unregisterRegion(removeDungeon)
                        dungeon?.flagInactive()
                    }

                    return true
                }
            })
        }

        clearLogoutListener(player, "houselogout")
        return true
    }

    private fun removeItems(player: Player) {
        for (item in Items.KETTLE_7688..Items.CHEFS_DELIGHT_7755) {
            removeAll(player, item, Container.INVENTORY)
            removeAll(player, item, Container.BoB)
        }
    }
}