package content.global.skill.agility.grapple

import content.global.skill.agility.AgilityHandler
import core.api.*
import core.cache.def.impl.LocType
import core.game.interaction.OptionHandler
import core.game.node.Node
import core.game.node.entity.impl.ForceMovement
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.game.node.scenery.Scenery
import core.game.node.scenery.SceneryBuilder
import core.game.system.task.Pulse
import core.game.world.GameWorld.Pulser
import core.game.world.map.Direction
import core.game.world.map.Location
import core.game.world.map.RegionManager.getObject
import core.game.world.update.flag.context.Animation
import core.game.world.update.flag.context.Graphics
import core.plugin.Initializable
import core.plugin.Plugin
import shared.consts.Animations
import shared.consts.Items

@Initializable
class RiverLumGrapple : OptionHandler() {

    companion object {
        private val REQUIREMENTS: Map<Int, Int> = mapOf(
            Skills.AGILITY to 8,
            Skills.RANGE to 37,
            Skills.STRENGTH to 17
        )

        private val requirementsString: String =
            "You need at least ${REQUIREMENTS[Skills.AGILITY]} ${Skills.SKILL_NAME[Skills.AGILITY]}, " +
                    "${REQUIREMENTS[Skills.RANGE]} ${Skills.SKILL_NAME[Skills.RANGE]}, and " +
                    "${REQUIREMENTS[Skills.STRENGTH]} ${Skills.SKILL_NAME[Skills.STRENGTH]} to use this shortcut."

        private val CROSSBOWS: IntArray = intArrayOf(
            Items.DORGESHUUN_CBOW_8880,
            Items.MITH_CROSSBOW_9181,
            Items.ADAMANT_CROSSBOW_9183,
            Items.RUNE_CROSSBOW_9185,
            Items.KARILS_CROSSBOW_4734,
            Items.HUNTERS_CROSSBOW_10156,
        )

        private const val MITHRIL_GRAPPLE: Int = Items.MITH_GRAPPLE_9419
        private val ROPES: MutableList<Scenery> = mutableListOf()
    }

    private fun handleRopeScenery(add: Boolean, player: Player) {
        if (add) {
            val ropesToAdd: List<Scenery> = when {
                player.location.x > 3258 || player.location.x == 3253 -> listOf(
                    Scenery(1998, Location.create(3257, 3179, 0), 10, 1),
                    Scenery(1998, Location.create(3256, 3179, 0), 10, 1),
                    Scenery(1998, Location.create(3255, 3179, 0), 10, 1),
                    Scenery(1998, Location.create(3254, 3179, 0), 10, 1),
                )
                else -> listOf(
                    Scenery(1998, Location.create(3251, 3179, 0), 10, 1),
                    Scenery(1998, Location.create(3250, 3179, 0), 10, 1),
                    Scenery(1998, Location.create(3249, 3179, 0), 10, 1),
                )
            }
            ROPES.addAll(ropesToAdd)
            ROPES.forEach(SceneryBuilder::add)
        } else {
            ROPES.forEach(SceneryBuilder::remove)
            ROPES.clear()
        }
    }

    override fun newInstance(arg: Any?): Plugin<Any> {
        LocType.forId(17068).handlers["option:grapple"] = this
        return this
    }

    override fun handle(player: Player, node: Node, option: String): Boolean {
        if (option != "grapple") return true

        REQUIREMENTS.forEach { (skill, level) ->
            if (player.skills.getLevel(skill) < level) {
                sendDialogue(player, requirementsString)
                return true
            }
        }

        if (!anyInEquipment(player, *CROSSBOWS) || !inEquipment(player, MITHRIL_GRAPPLE)) {
            sendMessage(player, "You need a mithril grapple tipped bolt with a rope to do that.")
            return true
        }

        val (startTree, endTree, direction) = if (player.location == Location.create(3259, 3179, 0)) {
            Triple(getObject(Location.create(3260, 3178, 0)), getObject(Location.create(3244, 3179, 0)), Direction.WEST)
        } else {
            Triple(getObject(Location.create(3244, 3179, 0)), getObject(Location.create(3260, 3178, 0)), Direction.EAST)
        }

        player.lock()
        val start = player.location
        player.logoutListeners["riverlum-grapple"] = { p: Player ->
            p.location = start
        }
        Pulser.submit(object : Pulse(1, player) {
            private var step: Int = 0

            override fun pulse(): Boolean {
                when (step++) {
                    1 -> {
                        player.faceLocation(player.location.transform(direction))
                        player.animate(Animation(Animations.XBOWS_HUMAN_FIRE_AND_RELOAD_4230))
                        handleRopeScenery(true, player)
                    }
                    2 -> {
                        startTree?.let { SceneryBuilder.replace(it, it.transform(it.id + 1), 10) }
                        sendMessage(player, "You successfully grapple the raft and tie the rope to a tree.")
                    }
                    4 -> {
                        visualize(player, -1, Graphics.create(shared.consts.Graphics.WATERSPLASH_1_68))
                        val target = if (player.location.x > 3258) Location.create(3253, 3179, 0)
                        else Location.create(3251, 3179, 0)

                        ForceMovement.run(
                            player, player.location, target,
                            Animation.create(Animations.XBOWS_KARAMJA_NORTH_SWIMMING_ANIM_4464),
                            ForceMovement.WALKING_SPEED
                        )
                    }
                    12 -> handleRopeScenery(false, player)
                    13 -> {
                        val target = if (player.location.x > 3258) Location.create(3252, 3180, 0)
                        else Location.create(3253, 3179, 0)

                        ForceMovement.run(
                            player, player.location, target,
                            ForceMovement.WALK_ANIMATION, ForceMovement.WALKING_SPEED
                        )
                    }
                    16 -> {
                        player.faceLocation(player.location.transform(direction))
                        animate(player, Animation(Animations.XBOWS_HUMAN_FIRE_AND_RELOAD_4230))
                        sendMessage(
                            player,
                            if (player.location.x <= 3253)
                                "You successfully grapple the tree on the opposite bank."
                            else
                                "You successfully grapple the tree."
                        )
                    }
                    18 -> {
                        endTree?.let { SceneryBuilder.replace(it, it.transform(it.id + 2), 10) }
                        startTree?.let { SceneryBuilder.replace(it, it.transform(it.id + 1), 10) }
                        handleRopeScenery(true, player)
                    }
                    19 -> visualize(player, -1, Graphics.create(shared.consts.Graphics.WATERSPLASH_1_68))
                    20 -> {
                        val target = if (player.location.x > 3258) Location.create(3246, 3179, 0)
                        else Location.create(3259, 3179, 0)

                        ForceMovement.run(
                            player, player.location, target,
                            Animation.create(Animations.XBOWS_LUMBRIDGE_EAST_SWIMMING_ANIM_4466),
                            ForceMovement.WALKING_SPEED
                        )
                    }
                    26 -> {
                        player.unlock()
                        handleRopeScenery(false, player)
                        AgilityHandler.checkGrappleBreak(player)
                        player.logoutListeners.remove("riverlum-grapple")
                        return true
                    }
                }
                return false
            }
        })

        return true
    }

    override fun getDestination(moving: Node, destination: Node): Location =
        if (moving.location.x > 3258) Location.create(3259, 3179, 0)
        else Location.create(3246, 3179, 0)
}
