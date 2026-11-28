package content.region.kandarin.camelot.plugin

import core.api.*
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.world.update.flag.context.Animation
import shared.consts.Animations
import shared.consts.Items
import shared.consts.Scenery
import shared.consts.Sounds

class CamelotPlugin : InteractionListener {

    companion object {
        private val petFishIDs = intArrayOf(
            Items.FISHBOWL_6670,
            Items.FISHBOWL_6671,
            Items.FISHBOWL_6672
        )
        private val feedAnimation = Animation(Animations.HUMAN_FEED_PET_FISH_BLUE_2781)
    }

    override fun defineListeners() {

        /*
         * Using fishbowl on aquarium.
         */

        onUseWith(IntType.SCENERY, petFishIDs, Scenery.AQUARIUM_10091) { player, used, _ ->
            if (removeItem(player, used.asItem(), Container.INVENTORY)) {
                addItem(player, Items.FISHBOWL_6667, 1)
            }
            return@onUseWith true
        }

        /*
         * Handles use any item on pet fish.
         */

        onUseAnyWith(IntType.ITEM, *petFishIDs) { player, used, _ ->

            val item = used.asItem()

            when (used.id) {

                Items.FISH_FOOD_272 -> {
                    if (removeItem(player, item)) {
                        lock(player, feedAnimation.duration)
                        addItem(player, Items.AN_EMPTY_BOX_6675, 1)
                        animate(player, feedAnimation)
                        playAudio(player, Sounds.FILL_STONE_BOWL_1537)
                        sendMessage(player, "You feed your fish.")
                    }
                }

                Items.POISONED_FISH_FOOD_274 -> {
                    sendMessage(player, "You can't poison your own pet!")
                }

                else -> {
                    sendMessage(player, "Your fish looks at you strangely. You get the feeling this will not work.")
                }
            }

            return@onUseAnyWith true
        }
    }
}