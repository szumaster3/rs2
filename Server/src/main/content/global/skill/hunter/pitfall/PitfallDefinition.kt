package content.global.skill.hunter.pitfall

import core.game.world.map.Direction
import core.game.world.map.Location
import shared.consts.NPCs

/**
 * Defines the Pitfall traps for Hunter skill, including NPC IDs, requirements,
 * pit locations, varbits, jump spots, and helper methods for handling pitfalls.
 */
object PitfallDefinition {

    /**
     * NPC ids for Spined Larupia.
     */
    val LARUPIA_IDS = intArrayOf(NPCs.SPINED_LARUPIA_5104)

    /**
     * NPC ids for Horned Graahk.
     */
    val GRAAHK_IDS = intArrayOf(
        NPCs.HORNED_GRAAHK_5105,
        NPCs.MAN_5106,
        NPCs.MAN_5107,
        NPCs.MAN_5108
    )

    /**
     * NPC IDs for Sabre-Toothed Kyatt.
     */
    val KYATT_IDS = intArrayOf(NPCs.SABRE_TOOTHED_KYATT_5103, NPCs.MOM2_STONE_GOLEM_7497)

    /**
     * Represents all beasts caught in pitfalls.
     */
    val BEAST_IDS = intArrayOf(*LARUPIA_IDS, *GRAAHK_IDS, *KYATT_IDS)

    /**
     * Required Hunter levels for each type of beast.
     */
    val HUNTER_REQS = hashMapOf(
        "Spined larupia"        to 31,
        "Horned graahk"         to 41,
        "Sabre-toothed kyatt"   to 55
    )

    /**
     * Mapping of pitfall varp offsets.
     */
    val pitVarpOffsets = hashMapOf(
        19264 to 3,
        19265 to 6,
        19266 to 9,
        19267 to 12,
        19268 to 15
    )

    /**
     * Represents a single pit.
     *
     * @property varbitId the varbit id associated with the pit
     * @property horizontal whether the pit is horizontal (true) or vertical (false)
     */
    data class Pit(val varbitId: Int, val horizontal: Boolean)

    /**
     * Mapping of pit locations to their definitions.
     */
    val pitVarps = hashMapOf(
        Location.create(2565, 2888) to Pit(2967, true),
        Location.create(2573, 2885) to Pit(2968, false),
        Location.create(2556, 2893) to Pit(2964, false),
        Location.create(2552, 2904) to Pit(2966, true),
        Location.create(2543, 2908) to Pit(2965, false),
        Location.create(2538, 2899) to Pit(2966, true),
        Location.create(2700, 3795) to Pit(2958, true),
        Location.create(2700, 3785) to Pit(2959, false),
        Location.create(2706, 3789) to Pit(2960, false),
        Location.create(2730, 3791) to Pit(2961, true),
        Location.create(2737, 3784) to Pit(2962, true),
        Location.create(2730, 3780) to Pit(2963, false),
        Location.create(2766, 3010) to Pit(2969, false),
        Location.create(2762, 3005) to Pit(2970, false),
        Location.create(2771, 3004) to Pit(2971, true),
        Location.create(2777, 3001) to Pit(2972, false),
        Location.create(2784, 3001) to Pit(2973, true)
    )

    /**
     * Predefined jump spots near pits for easier navigation.
     *
     * The first map key is the pit location, and the inner map links surrounding
     * locations to the direction the player should jump.
     */
    val pitJumpSpots = hashMapOf(
        Location.create(2766, 3010) to hashMapOf(
            Location.create(2766, 3009) to Direction.NORTH,
            Location.create(2767, 3009) to Direction.NORTH,
            Location.create(2766, 3012) to Direction.SOUTH,
            Location.create(2767, 3012) to Direction.SOUTH
        ),
        Location.create(2762, 3005) to hashMapOf(
            Location.create(2762, 3004) to Direction.NORTH,
            Location.create(2763, 3004) to Direction.NORTH,
            Location.create(2762, 3007) to Direction.SOUTH,
            Location.create(2763, 3007) to Direction.SOUTH
        ),
        Location.create(2771, 3004) to hashMapOf(
            Location.create(2770, 3004) to Direction.EAST,
            Location.create(2770, 3005) to Direction.EAST,
            Location.create(2773, 3004) to Direction.WEST,
            Location.create(2773, 3005) to Direction.WEST
        ),
        Location.create(2777, 3001) to hashMapOf(
            Location.create(2777, 3000) to Direction.NORTH,
            Location.create(2778, 3000) to Direction.NORTH,
            Location.create(2777, 3003) to Direction.SOUTH,
            Location.create(2778, 3003) to Direction.SOUTH
        ),
        Location.create(2784, 3001) to hashMapOf(
            Location.create(2783, 3002) to Direction.EAST,
            Location.create(2783, 3001) to Direction.EAST,
            Location.create(2786, 3002) to Direction.WEST,
            Location.create(2786, 3001) to Direction.WEST
        )
    )

    /**
     * Returns possible jump spots for a given pit location.
     *
     * @param loc the pit location
     * @return a map of surrounding locations and the direction to jump,
     *         or null if the location is not a pit
     */
    fun pitJumpSpots(loc: Location): HashMap<Location, Direction>? {
        val pit = pitVarps[loc] ?: return null
        return if (pit.horizontal) {
            hashMapOf(
                loc.transform(-1, 0, 0) to Direction.EAST,
                loc.transform(-1, 1, 0) to Direction.EAST,
                loc.transform(2, 0, 0) to Direction.WEST,
                loc.transform(2, 1, 0) to Direction.WEST
            )
        } else {
            hashMapOf(
                loc.transform(0, -1, 0) to Direction.NORTH,
                loc.transform(1, -1, 0) to Direction.NORTH,
                loc.transform(0, 2, 0) to Direction.SOUTH,
                loc.transform(1, 2, 0) to Direction.SOUTH
            )
        }
    }
}
