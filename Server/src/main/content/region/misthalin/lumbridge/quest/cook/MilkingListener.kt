package content.region.misthalin.lumbridge.quest.cook

import core.api.*
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.QueueStrength
import core.game.node.entity.player.Player
import core.game.node.scenery.Scenery
import core.game.world.update.flag.context.Animation
import shared.consts.Animations
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Sounds

/**
 * Listener handling cow milking interactions.
 */
class MilkingListener : InteractionListener {

    companion object {
        private val ANIMATION = Animation(Animations.MILKIT_2305)

        private const val DAIRY_COW_0 = shared.consts.Scenery.DAIRY_COW_8689
        private const val DAIRY_COW_1 = shared.consts.Scenery.DAIRY_COW_12111

        private const val EMPTY_BUCKET = Items.BUCKET_1925
        private const val BUCKET_OF_MILK = Items.BUCKET_OF_MILK_1927

        private val COW_IDS = intArrayOf(DAIRY_COW_0, DAIRY_COW_1)
    }

    override fun defineListeners() {

        /*
         * Handles milk option on cow.
         */
        on(COW_IDS, IntType.SCENERY, "milk") { player, node ->
            milk(player, node.asScenery())
            return@on true
        }

        /*
         * Handles using a bucket on a cow.
         */

        onUseWith(IntType.SCENERY, EMPTY_BUCKET, *COW_IDS) { player, _, with ->
            milk(player, with.asScenery())
            return@onUseWith true
        }

        /*
         * Handles stealing cowbell (Cold War quest).
         */

        on(IntType.SCENERY, "steal-cowbell") { player, _ ->
            sendDialogue(player, "You need to have started the Cold War quest to attempt this.")
            return@on true
        }
    }

    /**
     * Handles logic for milking a cow.
     */
    private fun milk(player: Player, cow: Scenery): Boolean {
        if (!inInventory(player, EMPTY_BUCKET)) {
            // Near Lumbridge cows.
            if (inBorders(player, 3250, 3271, 3256, 3277)) {
                openDialogue(player, NPCs.GILLIE_THE_MILKMAID_3807, true, true)
            } else {
                sendDialogue(player, "You'll need an empty bucket for the milk itself.")
            }
            return true
        }

        lock(player, 8)
        player.animate(ANIMATION)
        playAudio(player, Sounds.MILK_COW_372)
        queueScript(player, 8, QueueStrength.NORMAL) {
            if (!finishedMoving(player))
                return@queueScript delayScript(player, 3)

            if(!removeItem(player, EMPTY_BUCKET)) {
                return@queueScript stopExecuting(player)
            }
            addItem(player, BUCKET_OF_MILK, 1)
            sendMessage(player, "You milk the cow.")
            return@queueScript stopExecuting(player)
        }

        return true
    }
}
