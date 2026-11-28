package core.game.system.command.sets

import core.api.refreshAppearance
import core.api.sendEntityTimerBar
import core.api.sendLayeredSequences
import core.game.node.entity.Entity
import core.game.world.map.RegionManager
import core.game.world.update.flag.context.LayeredSequence
import core.game.system.command.CommandPlugin.Companion.toInteger
import core.game.system.command.Privilege
import core.game.system.task.Pulse
import core.game.world.GameWorld
import core.game.world.update.flag.context.Animation
import core.plugin.Initializable

@Initializable
class AnimationCommandSet : CommandSet(Privilege.ADMIN) {

    override fun defineCommands() {
        define("timerbar", usage = "::timerbar duration hold step [npcIndex] [alternate]",
            description = "Tests the native overhead timer bar in client cycles.") { player, args ->
            val duration = args.getOrNull(1)?.toIntOrNull()
            val hold = args.getOrNull(2)?.toIntOrNull()
            val step = args.getOrNull(3)?.toIntOrNull()
            val target: Entity? = if (args.size > 4) {
                val index = args[4].toIntOrNull()
                RegionManager.getLocalNpcs(player).firstOrNull { it.index == index && it.isActive }
            } else player
            val alternate = if (args.size > 5) args[5].toIntOrNull() else 0
            if (duration == null || hold == null || step == null || target == null || alternate !in listOf(0, 1) ||
                !sendEntityTimerBar(target, duration, hold, step, alternate == 1)) {
                reject(player, "Invalid timer bar arguments, target or update phase.")
            }
        }
        define("layeranim", usage = "::layeranim sequence delay slotMask [npcIndex]",
            description = "Tests additional animation slots (decimal mask 1-4095, sequence -1 clears).") { player, args ->
            val sequence = args.getOrNull(1)?.toIntOrNull()
            val delay = args.getOrNull(2)?.toIntOrNull()
            val mask = args.getOrNull(3)?.toIntOrNull()
            val target: Entity? = if (args.size > 4) {
                val index = args[4].toIntOrNull()
                RegionManager.getLocalNpcs(player).firstOrNull { it.index == index && it.isActive }
            } else player
            if (sequence == null || sequence !in -1..65534 || delay == null || delay !in 0..255 ||
                mask == null || mask !in 1..4095 || target == null ||
                !sendLayeredSequences(target, listOf(LayeredSequence(sequence, delay, mask)))) {
                reject(player, "Invalid layered animation arguments, cache sequence, target or update phase.")
            }
        }

        /*
         * Command for playing animation on the player.nie
         */

        define(
            name = "anim",
            privilege = Privilege.ADMIN,
            usage = "::anim <lt>Animation ID<gt>",
            description = "Plays the animation with the given ID.",
        ) { player, args ->
            if (args.size < 2) {
                reject(player, "Syntax error: ::anim <Animation ID>")
            }
            val animation = Animation(args[1].toInt())
            player.animate(animation)
        }

        /*
         * Command for loopanim: plays a range of animations
         * sequentially on the player with a configurable delay.
         */

        define(
            name = "loopanim",
            privilege = Privilege.ADMIN,
            usage = "::loopanim <lt>startID<gt> <lt>endID<gt> <lt>delay<gt>",
            description = "Plays the animation with the given ID range.",
        ) { player, args ->
            if (args.size < 2) {
                reject(player, "Syntax error: ::loopanim <start ID> <end ID> <delay>")
                return@define
            }

            val start = toInteger(args[1])
            val end = if (args.size > 2) toInteger(args[2]) else 11154
            val delay = if (args.size > 3) toInteger(args[3]) else 3

            GameWorld.Pulser.submit(
                object : Pulse(delay, player) {
                var animId = start

                override fun pulse(): Boolean {
                    if (delay == 1) {
                        refreshAppearance(player)
                    }

                    player.animate(Animation.create(animId))
                    player.debug("Animation id: $animId")

                    animId++
                    return animId >= end
                }
            })
        }

        /*
         * Command for setting the player render (walk/idle) animation.
         */

        define(
            name = "ranim",
            privilege = Privilege.ADMIN,
            usage = "::ranim <lt>Render Anim ID<gt>",
            description = "Sets the player's render (walk/idle) animation.",
        ) { player, args ->
            if (args.size < 2) {
                reject(player, "Syntax error: ::ranim <Render Animation ID>")
            }
            if (args.size > 2) {
                GameWorld.Pulser.submit(
                    object : Pulse(3, player) {
                        var id = args[1].toInt()

                        override fun pulse(): Boolean {
                            player.appearance.setAnimations(Animation.create(id))
                            refreshAppearance(player)
                            player.sendChat("Current: $id")
                            return ++id >= args[2].toInt()
                        }
                    },
                )
            } else {
                try {
                    player.appearance.setAnimations(Animation.create(args[1].toInt()))
                    refreshAppearance(player)
                } catch (e: NumberFormatException) {
                    reject(player, "Syntax error: ::ranim <Render Animation ID>")
                }
            }
        }

        /*
         * Command for reset the player render
         * (walk/idle) animation to default.
         */

        define(
            name = "resetanim",
            privilege = Privilege.ADMIN,
            usage = "::resetanim",
            description = "Resets the player's render (walk/idle) animation to default.",
        ) { player, _ ->
            player.appearance.prepareBodyData(player)
            player.appearance.setDefaultAnimations()
            player.appearance.setAnimations()
            refreshAppearance(player)
        }
    }
}
