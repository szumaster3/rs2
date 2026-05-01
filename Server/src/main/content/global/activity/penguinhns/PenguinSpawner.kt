package content.global.activity.penguinhns

import core.game.node.entity.npc.NPC

/**
 * Represents the spawning penguin NPCs in the world.
 */
class PenguinSpawner {

    /**
     * Spawns penguins randomly.
     */
    fun spawnPenguins(amount: Int): ArrayList<Int> {
        val selectedPenguins = PenguinLocation.values()
            .toList()
            .shuffled()
            .take(amount.coerceAtMost(PenguinLocation.values().size))

        val spawnedOrdinals = ArrayList<Int>(selectedPenguins.size)

        for (penguin in selectedPenguins) {
            val location = penguin.locations.random()
            spawnedOrdinals.add(penguin.ordinal)

            NPC(penguin.npcId, location).also {
                it.isNeverWalks = true
                it.isWalks = false
                PenguinManager.npcs.add(it)
            }.init()
        }
        return spawnedOrdinals
    }

    /**
     * Spawns penguins by their ordinals.
     */
    fun spawnPenguins(ordinals: List<Int>) {
        ordinals.distinct().forEach { ordinal ->
            val penguin = PenguinLocation.values()[ordinal]
            val location = penguin.locations.random()

            NPC(penguin.npcId, location).also {
                it.isNeverWalks = true
                it.isWalks = false
                PenguinManager.npcs.add(it)
            }.init()
        }
    }
}