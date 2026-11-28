package content.global.skill.hunter.tracking

import core.cache.def.impl.LocType
import core.game.node.item.Item
import core.game.world.map.Location
import core.game.world.update.flag.context.Animation
import core.plugin.Initializable
import core.plugin.Plugin
import shared.consts.Animations
import shared.consts.Items
import shared.consts.Scenery

/**
 * Polar Kebbit Hunter plugin, configuring trails, rewards and interactions.
 */
@Initializable
class PolarKebbitHunting : HunterTracking() {

    init {
        catchingKebbitAnimation = Animation(Animations.CATCH_POLAR_KEBBIT_NOOSE_WAND_5256)
        trailLimit = 3
        attribute = "hunter:tracking:polartrail"
        indexAttribute = "hunter:tracking:polarindex"
        rewards = arrayOf(
            Item(Items.RAW_BEAST_MEAT_9986),
            Item(Items.POLAR_KEBBIT_FUR_10117),
            Item(Items.BONES_526)
        )
        tunnelEntrances = arrayOf(
            Location.create(2711, 3819, 1),
            Location.create(2714, 3821, 1),
            Location.create(2718, 3829, 1),
            Location.create(2721, 3827, 1),
            Location.create(2718, 3832, 1),
            Location.create(2715, 3820, 1)
        )
        initialMap = hashMapOf(
            19640 to arrayListOf(
                TrailDefinition(3061, TrailType.TUNNEL, false, Location.create(2712, 3831, 1), Location.create(2718, 3832, 1)),
                TrailDefinition(3060, TrailType.LINKING, true, Location.create(2712, 3831, 1), Location.create(2716, 3827, 1), Location.create(2713, 3827, 1)),
                TrailDefinition(3057, TrailType.LINKING, false, Location.create(2712, 3831, 1), Location.create(2708, 3819, 1), Location.create(2708, 3825, 1))
            ),
            19641 to arrayListOf(
                TrailDefinition(3053, TrailType.LINKING, true, Location.create(2718, 3820, 1), Location.create(2708, 3819, 1), Location.create(2712, 3815, 1)),
                TrailDefinition(3055, TrailType.TUNNEL, false, Location.create(2718, 3820, 1), Location.create(2715, 3820, 1)),
                TrailDefinition(3056, TrailType.TUNNEL, false, Location.create(2718, 3820, 1), Location.create(2721, 3827, 1))
            )
        )
        linkingTrails = arrayListOf(
            TrailDefinition(3058, TrailType.LINKING, true, Location.create(2714, 3821, 1), Location.create(2716, 3827, 1)),
            TrailDefinition(3059, TrailType.TUNNEL, true, Location.create(2716, 3827, 1), Location.create(2718, 3829, 1)),
            TrailDefinition(3054, TrailType.TUNNEL, false, Location.create(2708, 3819, 1), Location.create(2711, 3819, 1))
        )
        experience = 30.0
        varp = 926
        requiredLevel = 1
    }

    override fun newInstance(arg: Any?): Plugin<Any> {
        addExtraTrails()

        val sceneryIds = listOf(
            Scenery.HOLE_19640, Scenery.HOLE_19641,
            Scenery.HOLLOW_LOG_36688, Scenery.HOLLOW_LOG_36689, Scenery.HOLLOW_LOG_36690,
            Scenery.TUNNEL_19419, Scenery.TUNNEL_19420, Scenery.TUNNEL_19421, Scenery.TUNNEL_19423, Scenery.TUNNEL_19424, Scenery.TUNNEL_19426,
            Scenery.SNOW_DRIFT_19435
        )

        sceneryIds.forEach { id ->
            val def = LocType.forId(id)
            def.handlers["option:inspect"] = this
            def.handlers["option:search"] = this
            def.handlers["option:attack"] = this
        }

        return this
    }
}