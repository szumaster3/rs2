package content.global.plugins.inter.with_scenery

import core.api.*
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.QueueStrength
import core.game.node.Node
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.game.system.task.Pulse
import core.game.world.map.RegionManager
import shared.consts.Animations
import shared.consts.Items
import shared.consts.Scenery
import shared.consts.Sounds

/**
 * Handles the logic for filling containers (buckets and sandbags) with sand from sandpits or sand piles.
 */
class SandSourcePlugin : InteractionListener {

    companion object {
        private val SANDPITS = intArrayOf(Scenery.SAND_PIT_2645, Scenery.SAND_PIT_4373, Scenery.SANDPIT_10814)
        private val SAND_PILES = intArrayOf(Scenery.SAND_2977, Scenery.SAND_2978, Scenery.SAND_2979)
        private val CONTAINER_IDS = intArrayOf(Items.BUCKET_1925, Items.EMPTY_SACK_5418)
        private const val SOUND_EFFECT = Sounds.SAND_BUCKET_2584

        /*
         * Sand pile depletion.
         */

        private const val PILE_DEPLETE_CHANCE = 8 // 1-in-8 chance per fill
        private const val PILE_RESPAWN_TICKS = 75
    }

    override fun defineListeners() {

        /*
         * Handles using containers on sandpits & sand piles.
         */

        onUseWith(IntType.SCENERY, CONTAINER_IDS, *SANDPITS, *SAND_PILES) { player, used, with ->
            val isSandPile = with.id in SAND_PILES
            fillSand(player, used.id, with, depletable = isSandPile)
            return@onUseWith true
        }

        /*
         * Handles examine sand piles.
         */

        on(SAND_PILES, IntType.SCENERY, "look") { player, node ->
            val examine = sceneryDefinition(node.id).examine.toString().lowercase()
            sendDialogue(player, "This looks like $examine")
            return@on true
        }
    }

    /**
     * Fills empty containers of the given type in the player inventory with sand.
     *
     * @param player The player.
     * @param containerId The item id.
     * @param source The scenery id.
     * @param depletable True if is not infinite src.
     */
    private fun fillSand(player: Player, containerId: Int, source: Node, depletable: Boolean) {
        var remaining = amountInInventory(player, containerId)
        if (remaining <= 0) return

        val (animation, filledItem, itemName) = when (containerId) {
            Items.BUCKET_1925 -> Triple(Animations.FILL_BUCKET_SAND_895, Items.BUCKET_OF_SAND_1783, "bucket")
            Items.EMPTY_SACK_5418 -> Triple(Animations.ENLIGHTENED_SANDBAG_5155, Items.SANDBAG_9943, "bag")
            else -> return
        }

        var ticks = 0

        queueScript(player, 2, QueueStrength.WEAK) {
            if (remaining <= 0) return@queueScript stopExecuting(player)

            if (!inInventory(player, containerId)) {
                return@queueScript stopExecuting(player)
            }

            if (depletable && RegionManager.getObject(source.location) == null) {
                return@queueScript stopExecuting(player)
            }

            if (!removeItem(player, Item(containerId, 1), Container.INVENTORY)) {
                return@queueScript stopExecuting(player)
            }

            if (ticks % 2 == 0) {
                animate(player, animation)
                playAudio(player, SOUND_EFFECT)
            }
            ticks++

            if (addItem(player, filledItem)) {
                sendMessage(player, "You fill the $itemName with sand.")
            } else {
                sendMessage(player, "You do not have enough inventory space.")
                return@queueScript stopExecuting(player)
            }

            if (depletable && (1..PILE_DEPLETE_CHANCE).random() == 1) {
                handleSandPile(source)
            }

            remaining--

            if (remaining <= 0) return@queueScript stopExecuting(player)

            setCurrentScriptState(player, 0)
            delayScript(player, 2)
        }
    }

    /**
     * Handles sand pile state change after collecting sand.
     */
    private fun handleSandPile(with: Node) {
        val nextId = when (with.id) {
            Scenery.SAND_2977 -> Scenery.SAND_2978
            Scenery.SAND_2978 -> Scenery.SAND_2979
            else -> null
        }

        if (nextId != null) {
            replaceScenery(with.asScenery(), nextId, -1)
        } else {
            removeScenery(with.asScenery())
            submitWorldPulse(object : Pulse(PILE_RESPAWN_TICKS) {
                override fun pulse(): Boolean {
                    addScenery(Scenery.SAND_2977, with.location, with.direction.ordinal)
                    return true
                }
            })
        }
        with.isActive = false
    }
}