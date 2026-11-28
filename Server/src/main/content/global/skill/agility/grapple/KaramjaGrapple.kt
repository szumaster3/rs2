package content.global.skill.agility.grapple

import content.global.skill.agility.AgilityHandler
import core.api.*
import core.cache.def.impl.LocType
import core.game.interaction.OptionHandler
import core.game.node.Node
import core.game.node.entity.impl.ForceMovement
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.diary.DiaryType
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
class KaramjaGrapple : OptionHandler() {

    companion object {
        private val REQUIREMENTS: Map<Int, Int> = mapOf(
            Skills.AGILITY to 53,
            Skills.RANGE to 42,
            Skills.STRENGTH to 21
        )

        private val requirementsString: String =
            REQUIREMENTS.entries.joinToString(", ") { "${it.value} ${Skills.SKILL_NAME[it.key]}" }
                .replaceAfterLast(",", " and${REQUIREMENTS.entries.last().let { " ${it.value} ${Skills.SKILL_NAME[it.key]}" }}")
                .let { "You need at least $it to use this shortcut." }

        private val ROPES: MutableList<Scenery> = mutableListOf()
        private val RUN_ANIMATION: Animation = Animation(1995)

        private val CROSSBOWS: IntArray = intArrayOf(
            Items.DORGESHUUN_CBOW_8880,
            Items.MITH_CROSSBOW_9181,
            Items.ADAMANT_CROSSBOW_9183,
            Items.RUNE_CROSSBOW_9185,
            Items.KARILS_CROSSBOW_4734,
            Items.HUNTERS_CROSSBOW_10156
        )

        private const val MITHRIL_GRAPPLE: Int = Items.MITH_GRAPPLE_9419
    }

    override fun newInstance(arg: Any?): Plugin<Any> {
        LocType.forId(17074).handlers["option:grapple"] = this
        return this
    }

    private fun handleObjects(add: Boolean, player: Player) {
        if (add) {
            val yRange = if (player.location.y > 3134) 3137..3141 else 3128..3133
            ROPES.addAll(
                yRange.map { y -> Scenery(1998, Location.create(2874, y, 0), 10, 0) }
            )
            ROPES.forEach(SceneryBuilder::add)
        } else {
            ROPES.forEach(SceneryBuilder::remove)
            ROPES.clear()
        }
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

        val goingNorth = player.location.y <= 3134
        val (startTree, endTree, direction) = if (goingNorth) {
            Triple(
                getObject(Location.create(2873, 3125, 0)),
                getObject(Location.create(2874, 3144, 0)),
                Direction.NORTH
            )
        } else {
            Triple(
                getObject(Location.create(2874, 3144, 0)),
                getObject(Location.create(2873, 3125, 0)),
                Direction.SOUTH
            )
        }

        val islandTree = getObject(Location.create(2873, 3134, 0))
        player.lock()
        val start = player.location
        player.logoutListeners["karamja-grapple"] = { p: Player ->
            p.location = start
        }
        Pulser.submit(object : Pulse(1, player) {
            private var step = 0

            override fun pulse(): Boolean {
                when (step++) {
                    1 -> player.faceLocation(player.location.transform(direction))
                        .also { player.animate(Animation(Animations.FIRE_CROSSBOW_4230)) }
                    3 -> player.packetDispatch.sendPositionedGraphic(67, 10, 0, player.location.transform(direction, 5))
                    4 -> {
                        startTree?.let { SceneryBuilder.replace(it, it.transform(it.id + 1), 10) }
                        islandTree?.let { SceneryBuilder.replace(it, it.transform(it.id + 2), 10) }
                    }
                    5 -> visualize(player, Animations.PULL_SELF_THROUGH_WATER_4466, Graphics.create(shared.consts.Graphics.WATER_SPLASH_68))
                    6 -> handleWalk(player, if (goingNorth) Location.create(2874, 3133) else Location.create(2874, 3136))
                        .also { handleObjects(true, player) }
                    15 -> handleWalk(player, if (goingNorth) Location.create(2875, 3133) else Location.create(2875, 3136))
                        .also { handleObjects(false, player) }
                    17 -> handleRun(player, if (goingNorth) Location.create(2875, 3136) else Location.create(2875, 3133))
                    18 -> handleWalk(player, if (goingNorth) Location.create(2874, 3136) else Location.create(2874, 3133))
                    22 -> player.faceLocation(player.location.transform(direction))
                        .also { animate(player, Animation(Animations.FIRE_CROSSBOW_4230)) }
                    23 -> player.packetDispatch.sendPositionedGraphic(67, 10, 0, player.location.transform(direction, 5))
                    24 -> {
                        islandTree?.let { SceneryBuilder.replace(it, it.transform(it.id + 1), 10) }
                        endTree?.let { SceneryBuilder.replace(it, it.transform(it.id + 1), 10) }
                        startTree?.let { SceneryBuilder.replace(it, it.transform(it.id + 2), 10) }
                    }
                    25 -> visualize(player, Animations.PULL_SELF_THROUGH_WATER_4466, Graphics.create(68))
                    26 -> handleWalk(player, if (goingNorth) Location.create(2874, 3142) else Location.create(2874, 3127))
                        .also { handleObjects(true, player) }
                    34 -> {
                        player.unlock()
                        finishDiaryTask(player, DiaryType.KARAMJA, 2, 6)
                        setVarbit(player, 3606,1, true)
                        handleObjects(false, player)
                        AgilityHandler.checkGrappleBreak(player)
                        player.logoutListeners.remove("karamja-grapple")
                        return true
                    }
                }
                return false
            }
        })

        return true
    }

    private fun handleWalk(player: Player, location: Location) {
        ForceMovement.run(player, player.location, location, null, ForceMovement.WALKING_SPEED)
    }

    private fun handleRun(player: Player, location: Location) {
        ForceMovement.run(player, player.location, location, RUN_ANIMATION, ForceMovement.RUNNING_SPEED)
    }

    override fun getDestination(moving: Node, destination: Node): Location =
        if (moving.location.y > 3134) Location.create(2874, 3142, 0)
        else Location.create(2874, 3127, 0)
}
