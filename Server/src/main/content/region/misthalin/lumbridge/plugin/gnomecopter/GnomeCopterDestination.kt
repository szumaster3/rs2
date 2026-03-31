package content.region.misthalin.lumbridge.plugin.gnomecopter

import core.game.world.map.Location

enum class GnomeCopterDestination(
    val id: Int,
    val displayName: String,
    val landingLocation: Location,
    val region : Int,
    val autoPilot: Array<Location> = emptyArray()
) {

    CASTLE_WARS(
        0,
        "Castle Wars",
        Location.create(2440, 3089, 0),
        8020,
    ),

    LLETYA(
        1,
        "Lletya",
        Location.create(2778, 5468, 0),
        11093

    ),

    TROLLWEISS_RELLEKKA_HUNTER(
        2,
        "Trollweiss & Rellekka Hunter",
        Location.create(2523, 5393, 0), // Location.create(2541, 5418, 1)
        10068,
    ),

    BURTHORPE_GAMES_ROOM(
        3,
        "Burthorpe Games Room",
        Location.create(2399, 5407, 0),
        9556,
    ),

    BURGH_DE_ROTT(
        4,
        "Burgh de Rott",
        Location.create(2132, 5411, 0),
        8532,
    ),

    PEST_CONTROL(
        5,
        "Pest Control",
        Location.create(2274, 5421, 0),
        9044,
    );

    companion object {

        fun default() = CASTLE_WARS

        fun forId(id: Int): GnomeCopterDestination {
            return values().firstOrNull { it.id == id } ?: default()
        }
    }
}