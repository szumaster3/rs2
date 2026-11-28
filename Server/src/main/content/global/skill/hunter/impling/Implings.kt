package content.global.skill.hunter.impling

import core.api.utils.WeightedTable
import core.game.world.map.Location
import core.game.world.map.RegionManager
import core.game.world.map.build.RegionFlags
import core.game.world.map.path.ClipMaskSupplier
import shared.consts.NPCs

/**
 * Represents the different types of implings.
 *
 * @property npcId the NPC id of the impling in normal areas
 * @property puroId the NPC id of the impling in the Puro-Puro area
 */
enum class Implings(val npcId: Int, val puroId: Int) {
    BABY(NPCs.II_IMPLING_TYPE_1_1028, NPCs.II_IMPLING_TYPE_1_MAZE_6055),
    YOUNG(NPCs.II_IMPLING_TYPE_2_1029, NPCs.II_IMPLING_TYPE_2_MAZE_6056),
    GOURMET(NPCs.II_IMPLING_TYPE_3_1030, NPCs.II_IMPLING_TYPE_3_MAZE_6057),
    EARTH(NPCs.II_IMPLING_TYPE_4_1031, NPCs.II_IMPLING_TYPE_4_MAZE_6058),
    ESSENCE(NPCs.II_IMPLING_TYPE_5_1032, NPCs.II_IMPLING_TYPE_5_MAZE_6059),
    ECLECTIC(NPCs.II_IMPLING_TYPE_6_1033, NPCs.II_IMPLING_TYPE_6_MAZE_6060),
    NINJA(NPCs.II_IMPLING_TYPE_9_6053, NPCs.II_IMPLING_TYPE_9_MAZE_6063),
    NATURE(NPCs.II_IMPLING_TYPE_7_1034, NPCs.II_IMPLING_TYPE_7_MAZE_6061),
    MAGPIE(NPCs.II_IMPLING_TYPE_8_1035, NPCs.II_IMPLING_TYPE_8_MAZE_6062),
    PIRATE(NPCs.II_IMPLING_TYPE_11_7845, NPCs.II_IMPLING_TYPE_11_MAZE_7846),
    DRAGON(NPCs.II_IMPLING_TYPE_10_6054, NPCs.II_IMPLING_TYPE_10_MAZE_6064),
    ;

    companion object {
        /**
         * Returns an array containing all NPC ids (both normal and Puro-Puro) for all implings.
         */
        fun getIds(): IntArray {
            val list = ArrayList<Int>()
            for (imp in values()) {
                list.add(imp.npcId)
                list.add(imp.puroId)
            }
            return list.toIntArray()
        }
    }
}

/**
 * Represents different impling spawners and the weighted chance for each impling to spawn.
 *
 * @property npcId the NPC id of the spawner
 * @property table the weighted table of implings that this spawner can generate
 */
enum class ImplingSpawner(val npcId: Int, val table: WeightedTable<Implings>) {
    LOW_TIER(
        npcId = NPCs.VAMPIRE_1024,
        WeightedTable.create(
            Pair(Implings.BABY, 20.0),
            Pair(Implings.YOUNG, 20.0),
            Pair(Implings.GOURMET, 20.0),
            Pair(Implings.EARTH, 20.0),
            Pair(Implings.ESSENCE, 10.0),
            Pair(Implings.ECLECTIC, 10.0),
        ),
    ),
    MID_TIER(
        npcId = NPCs.VAMPIRE_1025,
        WeightedTable.create(
            Pair(Implings.GOURMET, 10.0),
            Pair(Implings.EARTH, 10.0),
            Pair(Implings.ESSENCE, 20.0),
            Pair(Implings.ECLECTIC, 37.0),
            Pair(Implings.NATURE, 20.0),
            Pair(Implings.MAGPIE, 2.0),
            Pair(Implings.NINJA, 1.0),
        ),
    ),
    HIGH_TIER(
        npcId = NPCs.VAMPIRE_1026,
        WeightedTable.create(
            Pair(Implings.NATURE, 10.0),
            Pair(Implings.MAGPIE, 40.0),
            Pair(Implings.NINJA, 30.0),
            Pair(Implings.PIRATE, 10.0),
            Pair(Implings.DRAGON, 10.0),
        ),
    ),
    PURO_LOW_TIER(
        npcId = NPCs.DRAGON_IMPLING_6065,
        WeightedTable.create(
            Pair(Implings.BABY, 20.0),
            Pair(Implings.YOUNG, 20.0),
            Pair(Implings.GOURMET, 20.0),
            Pair(Implings.EARTH, 20.0),
            Pair(Implings.ESSENCE, 10.0),
            Pair(Implings.ECLECTIC, 10.0),
        ),
    ),
    PURO_MID_TIER(
        npcId = NPCs.DRAGON_IMPLING_6066,
        WeightedTable.create(
            Pair(Implings.GOURMET, 10.0),
            Pair(Implings.EARTH, 10.0),
            Pair(Implings.ESSENCE, 20.0),
            Pair(Implings.ECLECTIC, 37.0),
            Pair(Implings.NATURE, 20.0),
            Pair(Implings.MAGPIE, 2.0),
            Pair(Implings.NINJA, 1.0),
        ),
    ),
    PURO_HIGH_TIER(
        npcId = NPCs.DRAGON_IMPLING_6067,
        WeightedTable.create(
            Pair(Implings.NATURE, 150.0),
            Pair(Implings.MAGPIE, 114.0),
            Pair(Implings.NINJA, 37.0),
            Pair(Implings.PIRATE, 13.0),
            Pair(Implings.DRAGON, 10.0),
        ),
    ),
    NULL(npcId = -1, WeightedTable());
    ;

    companion object {
        private val idMap = values().map { it.npcId to it }.toMap()

        /**
         * Returns the spawner associated with the given NPC id.
         *
         * @param id the NPC id
         * @return the [ImplingSpawner], or null if not found
         */
        fun forId(id: Int): ImplingSpawner? = idMap[id]

        /**
         * Returns an array of all spawner NPC IDs.
         */
        fun getIds(): IntArray = idMap.keys.toIntArray()
    }

}

/**
 * Defines weighted spawn types for impling spawners.
 *
 * @property table the weighted table of [ImplingSpawner]s for this spawn type
 * @property spawnRolls the number of spawn rolls per location
 */
enum class ImplingSpawnTypes(val table: WeightedTable<ImplingSpawner>, val spawnRolls: Int) {
    DEFAULT_ONLY(
        WeightedTable.create(
            Pair(ImplingSpawner.LOW_TIER, 14.0),
            Pair(ImplingSpawner.MID_TIER, 7.0),
            Pair(ImplingSpawner.HIGH_TIER, 4.0),
            Pair(ImplingSpawner.NULL, 75.0)
        ),
        3,
    ),
    LOW_TIER_ONLY(WeightedTable.create(Pair(ImplingSpawner.LOW_TIER, 100.0)), 1),
    MID_TIER_ONLY(WeightedTable.create(Pair(ImplingSpawner.MID_TIER, 100.0)), 1),
    HIGH_TIER_ONLY(WeightedTable.create(Pair(ImplingSpawner.HIGH_TIER, 100.0)), 1),
    PURO_HIGH_TIER_ONLY(WeightedTable.create(Pair(ImplingSpawner.PURO_HIGH_TIER, 100.0)), 1),
}

/**
 * Represents specific spawn locations for implings.
 *
 * @property type the spawn type controlling which spawners appear here
 * @property locations the exact [Location]s where implings can spawn
 */
enum class ImplingSpawnLocations(val type: ImplingSpawnTypes, vararg val locations: Location) {
    DEFAULT_SPAWN(
        ImplingSpawnTypes.DEFAULT_ONLY,
        Location.create(2204, 3232, 0),
        Location.create(2582, 2974, 0),
        Location.create(2522, 3105, 0),
        Location.create(2470, 3221, 0),
        Location.create(2593, 3251, 0),
        Location.create(2735, 3354, 0),
        Location.create(2646, 3424, 0),
        Location.create(2462, 3429, 0),
        Location.create(2386, 3513, 0),
        Location.create(2335, 3649, 0),
        Location.create(2740, 3536, 0),
        Location.create(2654, 3609, 0),
        Location.create(2724, 3769, 0),
        Location.create(2817, 3513, 0),
        Location.create(2844, 3154, 0),
        Location.create(2844, 3033, 0),
        Location.create(2841, 2926, 0),
        Location.create(2907, 3491, 0),
        Location.create(3020, 3525, 0),
        Location.create(3021, 3424, 0),
        Location.create(2981, 3276, 0),
        Location.create(3135, 3377, 0),
        Location.create(3149, 3233, 0),
        Location.create(3170, 3004, 0),
        Location.create(3239, 3289, 0),
        Location.create(3287, 3271, 0),
        Location.create(3418, 3124, 0),
        Location.create(3356, 3010, 0),
        Location.create(3550, 3529, 0),
        Location.create(3449, 3488, 0),
        Location.create(3441, 3352, 0),
    ),
    LOW_TIER_SPAWN(
        ImplingSpawnTypes.LOW_TIER_ONLY,
        Location.create(2348, 3610, 0),
        Location.create(2277, 3186, 0),
        Location.create(2459, 3085, 0),
        Location.create(2564, 3393, 0),
        Location.create(2780, 3463, 0),
        Location.create(2966, 3411, 0),
        Location.create(3094, 3237, 0),
        Location.create(3281, 3427, 0),
        Location.create(3278, 3160, 0),
    ),
}

/**
 * Supplies clipping flags for impling spawns, ignoring solid tiles and objects.
 */
object ImplingClipper : ClipMaskSupplier {
    override fun getClippingFlag(z: Int, x: Int, y: Int): Int {
        var flag = RegionManager.getClippingFlag(z, x, y)
        return flag and (RegionFlags.SOLID_TILE.inv()) and (RegionFlags.TILE_OBJECT.inv())
    }
}
