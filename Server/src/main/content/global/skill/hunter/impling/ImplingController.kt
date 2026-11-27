package content.global.skill.hunter.impling

import core.api.*
import core.game.node.entity.player.link.quest.QuestJournal
import core.game.node.entity.npc.NPC
import core.game.system.command.Privilege
import core.game.world.map.Location
import core.tools.secondsToTicks
import shared.consts.Components
import kotlin.math.ceil
import kotlin.math.min

class ImplingController : TickListener, Commands {

    override fun tick() {
        if (--nextCycle > getTicksBeforeNextCycleToDespawn()) {
            return
        }
        if (activeImplings.size > 0) {
            clearSomeImplings(min(activeImplings.size, implingsClearedPerTick))
            return
        }
        generateSpawners()
        nextCycle = secondsToTicks(60 * 30)
    }

    override fun defineCommands() {
        define(
            name = "implings",
            privilege = Privilege.ADMIN,
            usage = "::implings",
            description = "Lists the currently active implings/spawners",
        ) { player, _ ->
            openInterface(player, Components.QUESTJOURNAL_SCROLL_275)
            QuestJournal.clear(player, "Implings")
            for ((index, impling) in activeImplings.withIndex()) {
                var text = "This shouldn't be here -> ${impling.id}"
                if (impling.id < 1028) {
                    val table = ImplingSpawner.forId(impling.id)
                    if (table != null) {
                        text = table.name
                    }
                } else {
                    text = impling.name
                }
                QuestJournal.writeRow(player, index, "$text -> ${impling.location}")
            }
            QuestJournal.finish(player)
        }
    }

    companion object {

        /**
         * Number of implings to clear per server tick.
         */
        val implingsClearedPerTick = 5

        /**
         * Ticks until the next despawn cycle.
         */
        var nextCycle = 0

        /**
         * List of currently active impling NPCs in the world.
         */
        var activeImplings = ArrayList<NPC>()

        /**
         * Clears a specific number of active implings from the world.
         *
         * @param amount the number of implings to clear
         */
        fun clearSomeImplings(amount: Int) {
            for (i in 0 until amount) {
                val impling = activeImplings.removeAt(0)
                poofClear(impling)
            }
        }

        /**
         * Generates spawner NPCs for all impling spawn locations.
         */
        fun generateSpawners() {
            val typeLocations = ImplingSpawnLocations.values()
            for (set in typeLocations) {
                val locations = set.locations
                val type = set.type
                locations.forEach { generateSpawnersAt(it, type) }
            }
        }

        /**
         * Generates impling spawners at a specific location.
         *
         * @param location the location to spawn implings
         * @param type the type of impling to spawn
         */
        fun generateSpawnersAt(location: Location, type: ImplingSpawnTypes) {
            for (i in 0 until type.spawnRolls) {
                val spawner = type.table.roll() ?: continue
                if (spawner == ImplingSpawner.NULL) continue
                val npc = NPC.create(spawner.npcId, location)
                npc.init()
                activeImplings.add(npc)
            }
        }

        /**
         * Calculates how many ticks are remaining before the next despawn cycle.
         *
         * @return number of ticks until the next cycle
         */
        fun getTicksBeforeNextCycleToDespawn(): Int =
            ceil(activeImplings.size / implingsClearedPerTick.toDouble()).toInt()

        /**
         * Deregisters an impling from the active list and optionally despawns it.
         *
         * @param impling the NPC to deregister
         * @param graceful if true, perform a special poof despawn effect; otherwise, remove normally
         * @return always returns true after deregistration
         */
        fun deregister(impling: NPC, graceful: Boolean = false): Boolean {
            activeImplings.remove(impling)
            if (graceful) {
                poofClear(impling)
            } else {
                impling.clear()
            }
            return true
        }
    }

}
