package content.region.misthalin.lumbridge.plugin.gnomecopter

import core.api.MapArea
import core.game.world.map.zone.ZoneBorders
import core.game.world.map.zone.ZoneRestriction

class GnomeCopter : MapArea {
    override fun defineAreaBorders(): Array<ZoneBorders> {
        return arrayOf(
            ZoneBorders.forRegion(8020),    //CASTLE_WARS_8020
            ZoneBorders.forRegion(10068),   //HUNTER_AREA_10068
            ZoneBorders.forRegion(9556),    //BURTHORPE_9556
            ZoneBorders.forRegion(11093),   //LLETYA_11093
            ZoneBorders.forRegion(9044),    //PEST_CONTROL_9044
            ZoneBorders.forRegion(8532),    //BURGH_DE_ROTT_8532
        )
    }

    override fun getRestrictions(): Array<ZoneRestriction> {
        return arrayOf(
            ZoneRestriction.RANDOM_EVENTS,
            ZoneRestriction.CANNON,
            ZoneRestriction.FOLLOWERS
        )
    }
}