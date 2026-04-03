package content.global.travel.balloon

import core.game.node.entity.player.Player

/**
 * Represents a balloon position on interface grid.
 *
 * @property top top model child id
 * @property bottom bottom model child id
 */
data class BalloonPosition(val top: Int, val bottom: Int)

/**
 * Represents a single stage in a balloon route.
 *
 * @property sequence button/action sequence for this stage
 * @property overlay models for drawing overlay
 * @property position base balloon model position for this stage
 */
data class RouteStage(val sequence: List<Int>, val overlay: (Player, Int) -> Unit, val position: BalloonPosition)

/**
 * Container for all stages in a balloon route.
 *
 * @property stages list of route stages
 */
data class RouteData(val stages: List<RouteStage>)

/**
 * Registry holding all balloon routes data.
 */
object BalloonRouteConfiguration {

    /**
     * Flight interface button ids.
     *
     * Logs     - 9
     * Sandbag  - 4
     * Relax    - 5
     * Rope     - 6
     * Red rope - 10
     */

    private val TAVERLEY =
        RouteData(
            stages = listOf(
                RouteStage(
                    sequence = listOf(4,9,5,5,5,5,5,5,5,5,5,10,5,5,6,5,5,5,5,5),
                    overlay = BalloonRouteDefinition::taverley_0,
                    position = BalloonPosition(118, 99)
                ),
                RouteStage(
                    sequence = listOf(9,5,9,5,5,5,5,5,5,5,5,5,5,9,5,5,5,5,5),
                    overlay = BalloonRouteDefinition::taverley_1,
                    position = BalloonPosition(119, 98)
                ),
                RouteStage(
                    sequence = listOf(5,5,5,5,5,5,5,10,6,5,5,5,9,5,5,5,5,6),
                    overlay = BalloonRouteDefinition::taverley_2,
                    position = BalloonPosition(179, 159)
                )
            )
        )

    private val CRAFTING_GUILD =
        RouteData(
            stages = listOf(
                RouteStage(
                    sequence = listOf(5,5,5,9,9,5,5,10,5,5,5,5,9,10,5,5,6,5,9,5),
                    overlay = BalloonRouteDefinition::crafting_guild_0,
                    position = BalloonPosition(178,158)
                ),
                RouteStage(
                    sequence = listOf(5,6,5,9,5,5,5,5,6,5,5,5,9,5,5,5,5,5,5),
                    overlay = BalloonRouteDefinition::crafting_guild_1,
                    position = BalloonPosition(159,139)
                ),
                RouteStage(
                    sequence = listOf(5,5,4,5,5,5,5,5,5,10,5,5,5,6,5,5,5,6),
                    overlay = BalloonRouteDefinition::crafting_guild_2,
                    position = BalloonPosition(160,140)
                )
            )
        )

    private val VARROCK =
        RouteData(
            stages = listOf(
                RouteStage(
                    sequence = listOf(9,9,9,9,5,5,6,6,6,5,5,6,9,5,5,6,9,9,5,5),
                    overlay = BalloonRouteDefinition::varrock_0,
                    position = BalloonPosition(138,118)
                ),
                RouteStage(
                    sequence = listOf(5,5,5,5,5,5,9,9,6,6,6,6,5,5,4,5,5,5,5),
                    overlay = BalloonRouteDefinition::varrock_1,
                    position = BalloonPosition(0, 0)
                ),
                RouteStage(
                    sequence = listOf(5,5,5,4,5,6,6,4,5,6,6,4,5,10,5,10,5,6),
                    overlay = BalloonRouteDefinition::varrock_2,
                    position = BalloonPosition(0, 0)
                )
            )
        )

    private val CASTLE_WARS =
        RouteData(
            stages = listOf(
                RouteStage(
                    sequence = listOf(4,4,9,5,5,5,5,5,10,9,5,5,10,5,5,9,9,5,5,5),
                    overlay = BalloonRouteDefinition::castle_wars_0,
                    BalloonPosition(0, 0)
                ),
                RouteStage(
                    sequence = listOf(9,5,5,10,6,5,5,5,5,5,5,5,4,5,6,5,5,6,5),
                    overlay = BalloonRouteDefinition::castle_wars_1,
                    BalloonPosition(0, 0)
                ),
                RouteStage(
                    sequence = listOf(9,5,5,6,5,5,5,9,10,5,5,5,5,4,10,5,5,6),
                    overlay = BalloonRouteDefinition::castle_wars_2,
                    BalloonPosition(0, 0)
                )
            )
        )

    private val GRAND_TREE =
        RouteData(
            stages = listOf(
                RouteStage(
                    sequence = listOf(5,5,5,5,5,5,5,6,5,5,5,5,5,5,5,5,5,5,5,5),
                    { _, _ -> },
                    BalloonPosition(0, 0)
                ),
                RouteStage(
                    sequence = listOf(9,6,9,6,9,5,5,9,6,5,4,5,5,10,5,6,5,5,5),
                    { _, _ -> },
                    BalloonPosition(0, 0)
                ),
                RouteStage(
                    sequence = listOf(9,9,9,5,5,6,9,5,5,5,5,5,10,5,10,6,5,5,6),
                    { _, _ -> },
                    BalloonPosition(0, 0)
                )
            )
        )

    val ROUTES: Map<Int, RouteData> =
        mapOf(
            1 to TAVERLEY,
            2 to CRAFTING_GUILD,
            3 to VARROCK,
            4 to CASTLE_WARS,
            5 to GRAND_TREE
        )
}