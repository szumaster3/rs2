package content.region.karamja.tbw.quest.junglepotion.plugin

import core.api.*
import core.game.interaction.Clocks
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.QueueStrength
import core.game.node.entity.player.Player
import core.game.world.map.Location
import core.game.world.update.flag.context.Animation
import core.tools.RandomFunction
import shared.consts.Animations
import shared.consts.Items
import shared.consts.Quests
import shared.consts.Scenery
import core.game.node.scenery.Scenery as SceneryNode

/**
 * Handles the Jungle Potion quest interactions.
 */
class JunglePotionListener : InteractionListener {

    /**
     * Data on objects to be searched in the quest.
     */
    enum class Herb(
        val objectId: Int,
        val herbId: Int,
        val productId: Int,
        val stage: Int,
        val clue: String,
        val animationId: Int = Animations.HUMAN_SEARCH_BUSHES_800,
    ) {
        JUNGLE_VINE(
            Scenery.MARSHY_JUNGLE_VINE_2575, Items.GRIMY_SNAKE_WEED_1525, Items.CLEAN_SNAKE_WEED_1526, 10,
            "It grows near vines in an area to the south west where the ground turns soft and the water kisses your feet.",
            Animations.SEARCH_FOR_SNAKEWEED_JUNGLE_POTION_2094
        ),
        PALM_TREE(
            Scenery.PALM_TREE_2577, Items.GRIMY_ARDRIGAL_1527, Items.CLEAN_ARDRIGAL_1528, 20,
            "You are looking for Ardrigal. It is related to the palm and grows in its brothers shady profusion."
        ),
        SITO_FOIL(
            Scenery.SCORCHED_EARTH_2579, Items.GRIMY_SITO_FOIL_1529, Items.CLEAN_SITO_FOIL_1530, 30,
            "You are looking for Sito Foil, and it grows best where the ground has been blackened by the living flame."
        ),
        VOLENCIA_MOSS(
            Scenery.ROCK_2581, Items.GRIMY_VOLENCIA_MOSS_1531, Items.CLEAN_VOLENCIA_MOSS_1532, 40,
            "You are looking for Volencia Moss. It clings to rocks for its existence. It is difficult to see, so you must search for it well."
        ),
        ROGUES_PURSE(
            Scenery.FUNGUS_COVERED_CAVERN_WALL_32106, Items.GRIMY_ROGUES_PURSE_1533, Items.CLEAN_ROGUES_PURSE_1534, 50,
            "It inhabits the darkness of the underground, and grows in the caverns to the north. A secret entrance to the caverns is set into the northern cliffs, be careful Bwana.",
            Animations.SEARCH_WALL_JUNGLE_POTION_2097
        );

        /**
         * The higher the value, the harder it is to find the herb.
         */
        val successChance: Int get() = if (this == ROGUES_PURSE) 4 else 3

        companion object {
            private val BY_ID = values().associateBy { it.objectId }
            private val BY_STAGE = values().associateBy { it.stage }

            fun forId(id: Int): Herb? = BY_ID[id]
            fun forStage(stage: Int): Herb? = BY_STAGE[stage]
        }
    }

    override fun defineListeners() {

        /*
         * Handles searching the jungle scenery for herbs.
         */

        on(HERB_OBJECTS, IntType.SCENERY, "search") { player, node ->
            val herb = Herb.forId(node.id) ?: return@on true
            val scenery = node.asScenery()

            if (getQuestStage(player, Quests.JUNGLE_POTION) < herb.stage) {
                sendMessage(player, "Unfortunately, you find nothing of interest.", 1)
                return@on true
            }

            searchForHerb(player, scenery, herb)
            return@on true
        }

        /*
         * Handles searching the rocks.
         */

        on(Scenery.ROCKS_2584, IntType.SCENERY, "search") { player, _ ->
            openDialogue(player, "jogre_dialogue")
            return@on true
        }

        /*
         * Handles climbing the hand holds.
         */

        on(Scenery.HAND_HOLDS_2585, IntType.SCENERY, "climb") { player, _ ->
            openDialogue(player, "jogre_dialogue", true, true)
            return@on true
        }
    }

    override fun defineDestinationOverrides() {
        setDest(IntType.SCENERY, Scenery.HAND_HOLDS_2585) { _, _ ->
            Location.create(2830, 9521, 0)
        }
    }

    /**
     * Repeats the search attempt.
     */
    private fun searchForHerb(player: Player, scenery: SceneryNode, herb: Herb) {
        val animation = Animation.create(herb.animationId)

        queueScript(player, 0, QueueStrength.WEAK) {
            if (!clockReady(player, Clocks.SKILLING)) return@queueScript false

            if (freeSlots(player) < 1) {
                sendMessage(player, "You don't have enough inventory space.")
                return@queueScript false
            }

            player.animate(animation)
            sendMessage(player, "You search the area...")

            if (RandomFunction.random(herb.successChance) == 1) {
                if (scenery.isActive) {
                    replaceScenery(scenery.asScenery(), scenery.id + 1, 80)
                }
                addItem(player, herb.herbId)
                sendItemDialogue(player, herb.herbId, "You find a grimy herb.")
                return@queueScript stopExecuting(player)
            }

            return@queueScript delayClock(player, Clocks.SKILLING, 2, true)
        }
    }

    companion object {
        private val HERB_OBJECTS = Herb.values().map { it.objectId }.toIntArray()
    }
}