package content.region.asgarnia.trollheim.plugin

import core.api.*
import core.cache.def.impl.SceneryDefinition
import core.game.activity.ActivityManager
import core.game.activity.ActivityPlugin
import core.game.activity.CutscenePlugin
import core.game.interaction.OptionHandler
import core.game.node.Node
import core.game.node.entity.Entity
import core.game.node.entity.impl.Projectile
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.warning.WarningManager
import core.game.node.entity.player.link.warning.WarningType
import core.game.node.entity.skill.Skills
import core.game.system.task.Pulse
import core.game.world.GameWorld.Pulser
import core.game.world.map.Location
import core.game.world.map.zone.MapZone
import core.game.world.map.zone.ZoneBorders
import core.game.world.map.zone.ZoneBuilder
import core.game.world.repository.Repository.findNPC
import core.game.world.update.flag.context.Animation
import core.net.packet.PacketRepository
import core.net.packet.context.Context.Camera.
import core.net.packet.out.CameraViewPacket
import core.plugin.ClassScanner.definePlugin
import core.plugin.Initializable
import core.plugin.Plugin
import shared.consts.*

@Initializable
class TrollheimRocksPlugin : OptionHandler() {

    override fun newInstance(arg: Any?): Plugin<Any?> {
        val rockLoc = listOf(
            Scenery.ROCKS_3722, Scenery.ROCKS_3748, Scenery.ROCKS_3790, Scenery.ROCKS_3791, Scenery.ROCKS_3803,
            Scenery.ROCKS_3804, Scenery.ROCKS_9303, Scenery.ROCKS_9304, Scenery.ROCKS_9305, Scenery.ROCKS_9306,
            Scenery.ROCKS_9327
        )

        rockLoc.forEach { id ->
            SceneryDefinition.forId(id).handlers["option:climb"] = this
        }

        definePlugin(WarningZone())
        ActivityManager.register(WarningCutscene())
        return this
    }

    override fun handle(player: Player, node: Node, option: String): Boolean {
        if (option != "climb") return true
        val scenery = node as? core.game.node.scenery.Scenery ?: return true
        val id = scenery.id
        val loc = scenery.location

        if (!inEquipment(player, CLIMBING_BOOTS)) {
            sendMessage(player, "You need Climbing boots to negotiate these rocks.")
            return true
        }

        fun hasRequirements(req: Int): Boolean {
            val level = getStatLevel(player, Skills.AGILITY)
            if (level < req) {
                sendMessage(player, "You need an agility level of $req to climb here.")
                return false
            }
            return true
        }

        player.faceLocation(loc)

        when (id) {
            Scenery.ROCKS_3722 -> runClimb(player, Location(2880, 3592, 0), CLIMB_DOWN)
            Scenery.ROCKS_3723 -> runClimb(player, Location(2881, 3596, 0), CLIMB_UP)
            Scenery.ROCKS_3790, Scenery.ROCKS_3791 -> {
                val anim = if (player.location.x > 2877) CLIMB_DOWN else CLIMB_UP
                val offset = if (player.location.x < loc.x) 2 else -2
                runClimb(player, loc.transform(offset, 0, 0), anim)
            }

            Scenery.ROCKS_3748 -> handleRockShortcuts(player, scenery, loc, ::hasRequirements)
            Scenery.ROCKS_3803, Scenery.ROCKS_3804 -> handleRocksCircleShortcuts(player, ::hasRequirements)
            in listOf(Scenery.ROCKS_9303, Scenery.ROCKS_9304, Scenery.ROCKS_9305, Scenery.ROCKS_9306, Scenery.ROCKS_9327) -> handleRocksShortcuts(player, scenery, ::hasRequirements)
        }
        return true
    }

    private fun handleRockShortcuts(player: Player, scenery: Node, loc: Location, hasAgility: (Int) -> Boolean) {
        when (loc) {
            Location(2821, 3635, 0) -> {
                val step = if (player.location.x > loc.x) -1 else 1
                runClimb(player, loc.transform(step, 0, 0), JUMP_ANIMATION)
            }

            Location(2910, 3686, 0), Location(2910, 3687, 0) -> {
                if (!hasAgility(43)) return
                val target = when (player.location) {
                    Location(2911, 3687, 0) -> Location(2909, 3687, 0)
                    Location(2909, 3687, 0) -> Location(2911, 3687, 0)
                    Location(2911, 3686, 0) -> Location(2909, 3686, 0)
                    else -> Location(2911, 3686, 0)
                }
                runClimb(player, target, JUMP_ANIMATION)
            }

            else -> {
                val deltaY = if (player.location.y < scenery.location.y) 2 else -2
                runClimb(player, player.location.transform(0, deltaY, 0), JUMP_ANIMATION)
            }
        }
    }

    private fun handleRocksCircleShortcuts(player: Player, hasAgility: (Int) -> Boolean) {
        if (!hasAgility(43)) return
        val target = when (player.location) {
            Location(2884, 3684, 0) -> Location(2886, 3684, 0)
            Location(2884, 3683, 0) -> Location(2886, 3683, 0)
            Location(2886, 3683, 0) -> Location(2884, 3683, 0)
            Location(2888, 3660, 0), Location(2887, 3660, 0) -> player.location.transform(0, 2, 0)
            Location(2888, 3662, 0), Location(2887, 3662, 0) -> player.location.transform(0, -2, 0)
            else -> Location(2884, 3684, 0)
        }
        val anim = if (target.y > player.location.y) CLIMB_UP else CLIMB_DOWN
        runClimb(player, target, anim)
    }

    private fun handleRocksShortcuts(player: Player, scenery: Node, hasAgilityLevelRequirements: (Int) -> Boolean) {
        val id = scenery.id
        val loc = scenery.location

        val requiredLevelForLoc = when (id) {
            Scenery.ROCKS_9303 -> 41
            Scenery.ROCKS_9304,
            Scenery.ROCKS_9306 -> 47

            Scenery.ROCKS_9305 -> 44
            Scenery.ROCKS_9327 -> 64
            else -> 0
        }

        if (!hasAgilityLevelRequirements(requiredLevelForLoc)) return

        val target = when (id) {
            Scenery.ROCKS_9303 -> if (player.location.x > loc.x) loc.transform(-2, 0, 0) else loc.transform(2, 0, 0)
            Scenery.ROCKS_9304 -> if (player.location == Location(2878, 3665, 0)) Location(2878, 3668, 0) else Location(
                2878,
                3665,
                0
            )

            Scenery.ROCKS_9305 -> if (player.location == Location(2909, 3684, 0)) Location(2907, 3682, 0) else Location(
                2909,
                3684,
                0
            )

            Scenery.ROCKS_9306 -> if (player.location == Location(2903, 3680, 0)) Location(2900, 3680, 0) else Location(
                2903,
                3680,
                0
            )

            else -> handleShortcut(player, scenery)
        }

        val anim = if (target.y > player.location.y) CLIMB_UP else CLIMB_DOWN
        runClimb(player, target, anim)
    }

    private fun handleShortcut(player: Player, scenery: Node): Location = when (scenery.location) {
        Location(2916, 3672, 0) -> Location(2918, 3672, 0)
        Location(2917, 3672, 0) -> Location(2915, 3672, 0)
        Location(2923, 3673, 0) -> Location(2921, 3672, 0)
        Location(2922, 3672, 0) -> Location(2924, 3673, 0)
        Location(2947, 3678, 0) -> Location(2950, 3681, 0)
        Location(2949, 3680, 0) -> Location(2946, 3678, 0)
        else -> player.location
    }

    override fun getDestination(node: Node, n: Node): Location? {
        if (n !is core.game.node.scenery.Scenery) return null

        return when (n.id) {
            3782 -> if (node.location.x >= 2897) Location(2897, 3618, 0) else null
            3804 -> if (n.location == Location(2885, 3684, 0) && node.location.x >= 2885)
                n.location.transform(1, 0, 0) else null

            9306 -> if (node.location.x >= 2902) Location(2903, 3680, 0) else null
            9327 -> if (node.asPlayer().location.y >= 3680) Location(2950, 3681, 0) else null
            else -> null
        }
    }

    companion object {
        private const val CLIMBING_BOOTS = Items.CLIMBING_BOOTS_3105
        private const val CLIMB_DOWN = Animations.WALK_BACKWARDS_CLIMB_1148
        private const val CLIMB_UP = Animations.CLIMB_DOWN_B_740
        private const val JUMP_ANIMATION = Animations.CLIMB_OBJECT_839
    }

    private fun runClimb(player: Player, to: Location, anim: Int) {
        sendMessage(player, "You climb onto the rock...")
        forceMove(player, player.location, to, 30, 90, null, anim)
        {
            sendMessage(player, "...and step down the other side.")
            resetAnimator(player)
        }
    }

    /**
     * Represents Warning zone for Death Plateau quest.
     */
    class WarningZone : MapZone("trollheim-warning", true), Plugin<Any?> {
        override fun enter(entity: Entity): Boolean {
            if (entity is Player) {
                val player = entity.asPlayer()

                if (player.walkingQueue.footPrint.y < 3592) {
                    WarningManager.trigger(player, WarningType.DEATH_PLATEAU) {
                        player.walkingQueue.reset()
                        player.pulseManager.clear()
                    }
                    return false
                }
            }

            return super.enter(entity)
        }

        override fun configure() {
            register(ZoneBorders(2837, 3592, 2838, 3593))
        }

        override fun newInstance(arg: Any?): Plugin<Any?> {
            ZoneBuilder.configure(this)
            return this
        }

        override fun fireEvent(identifier: String, vararg args: Any?): Any? = null
    }

    /**
     * Represents Warning cutscene for entering Trollheim from burthorpe destination.
     */
    class WarningCutscene : CutscenePlugin {
        constructor() : super("trollheim-warning")
        constructor(p: Player?) : super("trollheim-warning", false) {
            this.player = p
        }

        override fun newInstance(p: Player): ActivityPlugin = WarningCutscene(p)

        private fun sendProjectile(npc: NPC) {
            val projectile = Projectile.create(npc, player, 276)
            projectile.speed = 50
            projectile.startHeight = 26
            projectile.endHeight = 1
            projectile.send()
            playAudio(player, Sounds.TROLL_THROW_ROCK_870)
        }

        override fun open() {
            val npc = findNPC(TROLL_LOCATION)
            val loc = Location.create(2849, 3597, 0)
            PacketRepository.send(
                CameraViewPacket::class.java,
                Context.Camera.(player, Context.CameraCameraType.POSITION, loc.x - 2, loc.y, 1300, 1, 30)
            )
            PacketRepository.send(
                CameraViewPacket::class.java,
                Context.Camera.(player, Context.CameraCameraType.ROTATION, loc.x + 22, loc.y + 10, 1300, 1, 30)
            )
            Pulser.submit(
                object : Pulse(1, player) {
                    var count: Int = 0

                    override fun pulse(): Boolean {
                        when (count++) {
                            4 -> if (npc != null) {
                                npc.faceTemporary(player, 3)
                                npc.animate(THROW)
                                sendProjectile(npc)
                            }

                            6 -> {
                                this@WarningCutscene.stop(false)
                                PacketRepository.send(
                                    CameraViewPacket::class.java,
                                    Context.Camera.(player, Context.CameraCameraType.RESET, 0, 0, 1300, 1, 30),
                                )
                                return true
                            }
                        }
                        return false
                    }
                },
            )
        }

        override fun getMapState(): Int = 0

        override fun getSpawnLocation(): Location? = null

        override fun configure() {
            ActivityManager.register(this)
        }

        companion object {
            private val THROW = Animation(Animations.IDLE_1142)
            private val TROLL_LOCATION = Location(2851, 3598, 0)
        }
    }
}
