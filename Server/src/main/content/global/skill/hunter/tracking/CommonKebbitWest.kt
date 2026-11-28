/*package content.global.skill.hunter.tracking

import core.cache.def.impl.LocType
import core.game.node.item.Item
import core.game.world.map.Location
import core.game.world.update.flag.context.Animation
import core.plugin.Initializable
import core.plugin.Plugin
import shared.consts.Animations
import shared.consts.Items
import shared.consts.Scenery

@Initializable
class CommonKebbitWest : HunterTracking() {
    init {
        initialMap = hashMapOf(
            19443 to arrayListOf(
                TrailDefinition(
                    varbit = 2992,
                    type = TrailType.LINKING,
                    inverted = false,
                    startLocation = Location.create(2300, 3570, 0),
                    endLocation = Location.create(2305, 3575, 0),
                    triggerObjectLocation = Location.create(2303, 3572, 0)
                ),
                TrailDefinition(
                    varbit = 2993,
                    type = TrailType.LINKING,
                    inverted = false,
                    startLocation = Location.create(2300, 3570, 0),
                    endLocation = Location.create(2302, 3573, 0),
                    triggerObjectLocation = Location.create(2301, 3572, 0)
                )
            ),
            19444 to arrayListOf(
                TrailDefinition(
                    varbit = 2994,
                    type = TrailType.LINKING,
                    inverted = true,
                    startLocation = Location.create(2305, 3575, 0),
                    endLocation = Location.create(2300, 3570, 0),
                    triggerObjectLocation = Location.create(2303, 3572, 0)
                )
            )
        )

        linkingTrails = arrayListOf(
            TrailDefinition(
                varbit = 2995,
                type = TrailType.LINKING,
                inverted = false,
                startLocation = Location.create(2302, 3573, 0),
                endLocation = Location.create(2305, 3575, 0),
                triggerObjectLocation = Location.create(2303, 3574, 0)
            ),
            TrailDefinition(
                varbit = 2996,
                type = TrailType.LINKING,
                inverted = false,
                startLocation = Location.create(2300, 3570, 0),
                endLocation = Location.create(2302, 3573, 0),
                triggerObjectLocation = Location.create(2301, 3572, 0)
            )
        )

        experience = 36.0
        varp = 921
        trailLimit = 3
        attribute = "hunter:tracking:commontrailwest"
        indexAttribute = "hunter:tracking:commonIndexWest"
        rewards = arrayOf(
            Item(Items.COMMON_KEBBIT_FUR_10121),
            Item(Items.BONES_526),
            Item(Items.RAW_BEAST_MEAT_9986)
        )
        catchingKebbitAnimation = Animation(Animations.CATCH_KEBBIT_NOOSE_WAND_5259)
    }

    override fun newInstance(arg: Any?): Plugin<Any> {
        if (!linkingTrails.contains(initialMap.values.random()[0])) {
            addExtraTrails()
        }

        val plantIds = arrayOf(
            Scenery.PLANT_19381, Scenery.PLANT_19382, Scenery.PLANT_19383,
            Scenery.PLANT_19384, Scenery.PLANT_19385, Scenery.PLANT_19386,
            Scenery.PLANT_19387
        )
        for (id in plantIds) {
            LocType.forId(id).handlers["option:inspect"] = this
        }

        val burrowIds = arrayOf(19443, 19444)
        for (id in burrowIds) {
            LocType.forId(id).handlers["option:inspect"] = this
        }

        LocType.forId(19429).handlers["option:attack"] = this
        LocType.forId(19429).handlers["option:search"] = this

        return this
    }
}
*/
