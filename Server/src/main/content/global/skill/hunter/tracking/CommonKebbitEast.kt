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
 * Common Kebbit East Hunter plugin.
 */
@Initializable
class CommonKebbitEast : HunterTracking() {

    init {
        trailLimit = 3
        attribute = "hunter:tracking:commontrail"
        indexAttribute = "hunter:tracking:commonIndex"
        rewards = arrayOf(
            Item(Items.COMMON_KEBBIT_FUR_10121),
            Item(Items.BONES_526),
            Item(Items.RAW_BEAST_MEAT_9986)
        )
        catchingKebbitAnimation = Animation(Animations.CATCH_KEBBIT_NOOSE_WAND_5259)
        experience = 36.0
        varp = 919

        initialMap = hashMapOf(
            19439 to arrayListOf(
                TrailDefinition(2974, TrailType.LINKING, false, Location.create(2354, 3595, 0), Location.create(2360, 3602, 0)),
                TrailDefinition(2975, TrailType.LINKING, false, Location.create(2354, 3595, 0), Location.create(2355, 3601, 0)),
                TrailDefinition(2976, TrailType.LINKING, false, Location.create(2354, 3594, 0), Location.create(2349, 3604, 0))
            ),
            19440 to arrayListOf(
                TrailDefinition(2980, TrailType.LINKING, true, Location.create(2361, 3611, 0), Location.create(2360, 3602, 0)),
                TrailDefinition(2981, TrailType.LINKING, true, Location.create(2360, 3612, 0), Location.create(2357, 3607, 0))
            )
        )

        linkingTrails = arrayListOf(
            TrailDefinition(2982, TrailType.LINKING, false, Location.create(2357, 3607, 0), Location.create(2354, 3609, 0), Location.create(2355, 3608, 0)),
            TrailDefinition(2983, TrailType.LINKING, false, Location.create(2354, 3609, 0), Location.create(2349, 3604, 0), Location.create(2351, 3608, 0)),
            TrailDefinition(2977, TrailType.LINKING, false, Location.create(2360, 3602, 0), Location.create(2355, 3601, 0), Location.create(2358, 3599, 0)),
            TrailDefinition(2978, TrailType.LINKING, false, Location.create(2355, 3601, 0), Location.create(2349, 3604, 0), Location.create(2352, 3603, 0)),
            TrailDefinition(2979, TrailType.LINKING, false, Location.create(2360, 3602, 0), Location.create(2357, 3607, 0), Location.create(2358, 3603, 0))
        )
    }

    override fun newInstance(arg: Any?): Plugin<Any> {
        if (!linkingTrails.contains(initialMap.values.random()[0])) addExtraTrails()

        val inspectScenery = listOf(
            Scenery.PLANT_19356, Scenery.PLANT_19357, Scenery.PLANT_19358, Scenery.PLANT_19359, Scenery.PLANT_19360,
            Scenery.PLANT_19361, Scenery.PLANT_19362, Scenery.PLANT_19363, Scenery.PLANT_19364, Scenery.PLANT_19365,
            Scenery.PLANT_19372, Scenery.PLANT_19373, Scenery.PLANT_19374, Scenery.PLANT_19375, Scenery.PLANT_19376,
            Scenery.PLANT_19377, Scenery.PLANT_19378, Scenery.PLANT_19379, Scenery.PLANT_19380,
            Scenery.BURROW_19439, Scenery.BURROW_19440
        )

        inspectScenery.forEach { id -> LocType.forId(id).handlers["option:inspect"] = this }

        val actionScenery = listOf(
            Scenery.BUSH_19428
        )

        actionScenery.forEach {
            val def = LocType.forId(it)
            def.handlers["option:attack"] = this
            def.handlers["option:search"] = this
        }

        return this
    }
}