package content.region.misthalin.lumbridge.plugin.gnomecopter.sign

import content.region.misthalin.lumbridge.plugin.gnomecopter.sign.impl.*
import core.game.node.entity.player.Player
import core.game.world.map.Location

object InformationSignManager {

    private val SIGNS: Map<Location, InformationSign> = buildMap {

        // Burgh
        BurghDeRottInformationSign.values().forEach {
            put(it.location, it)
        }

        // Castle Wars
        CastleWarsInformationSign.values().forEach {
            put(it.location, it)
        }

        // Pest Control
        PestControlInformationSign.values().forEach {
            put(it.location, it)
        }

        // Trollweiss
        TrollweissAndRellekkaInformationSign.values().forEach {
            put(it.location, it)
        }

        // Burthorpe
        BurthorpeInformationSign.values().forEach {
            put(it.location, it)
        }

        // Lletya
        LletyaInformationSign.values().forEach {
            put(it.location, it)
        }
    }

    fun handle(player: Player, location: Location): Boolean {
        val sign = SIGNS[location] ?: return false
        sign.read(player)
        return true
    }
}