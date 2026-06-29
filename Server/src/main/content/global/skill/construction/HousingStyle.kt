package content.global.skill.construction

import core.api.getStatLevel
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills

/**
 * Represents the house style.
 */
enum class HousingStyle(
    val levelRequirement: Int,
    val cost: Int,
    val regionId: Int,
    val plane: Int,
    val doorId: Int,
    val secondDoorId: Int,
    val wallId: Int,
    val window: Decoration
) {
    BASIC_WOOD(1, 5000, 7503, 0, 13100, 13101, 13098, Decoration.BASIC_WOOD_WINDOW),
    BASIC_STONE(10, 5000, 7503, 1, 13094, 13096, 1902, Decoration.BASIC_STONE_WINDOW),
    WHITEWASHED_STONE(20, 7500, 7503, 2, 13006, 13007, 1415, Decoration.WHITEWASHED_STONE_WINDOW),
    FREMENNIK_STYLE_WOOD(30, 10000, 7503, 3, 13109, 13107, 13111, Decoration.FREMENNIK_WINDOW),
    TROPICAL_WOOD(40, 15000, 7759, 0, 13016, 13015, 13011, Decoration.TROPICAL_WOOD_WINDOW),
    FANCY_STONE(50, 25000, 7759, 1, 13119, 13118, 13116, Decoration.FANCY_STONE_WINDOW);


    fun hasLevel(player: Player): Boolean =
        getStatLevel(player, Skills.CONSTRUCTION) >= levelRequirement


    companion object {

        /**
         * Array of all Dungeon Wall IDs.
         * From Region 7503, Location(1898, 5084, 0)
         */
        private val DUNGEON_WALL_IDS = setOf(
            13019, 13020, 13021, 13022, 13023, 13024, 13025, 13026,
            13027, 13028, 13029, 13030, 13031, 13032, 13033, 13034,
            13035, 13036, 13037, 13046, 13048, 13049, 13050, 13051,
            13055, 13056, 13058, 13059, 13060, 13061, 13062, 13063,
            13065, 13066, 13067, 13068, 13069, 13070, 13072, 13073,
            13074, 13075, 13076, 13077, 13079, 13080, 13081, 13082,
            13083, 13084, 13086, 13087, 13088, 13089
        )

        /**
         * Checks if the provided ID is a dungeon wall.
         */
        @JvmStatic
        fun isDungeonWall(id: Int): Boolean {
            return id in DUNGEON_WALL_IDS
        }
    }
}