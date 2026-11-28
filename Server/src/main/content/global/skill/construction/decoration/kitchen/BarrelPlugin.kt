package content.global.skill.construction.decoration.kitchen

import core.api.addItem
import core.api.animate
import core.api.removeItem
import core.api.sendMessage
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.item.Item
import core.game.world.update.flag.context.Animation
import shared.consts.Animations
import shared.consts.Items

class BarrelPlugin : InteractionListener {

    override fun defineListeners() {
        onUseWith(IntType.SCENERY, BARRELS) { player, used, _ ->
            val scenery = used.asScenery() ?: return@onUseWith false

            if (!removeItem(player, Item(BEER_GLASS))) return@onUseWith false
            val animationId = Animations.POH_BARREL_USE2_BEER_3661 + (scenery.id - 13569)

            animate(player, Animation.create(animationId))

            val drinkName = scenery.name.lowercase().replace("barrel", "").trim()
            sendMessage(player, "You fill up your glass with $drinkName.")
            addItem(player, getReward(scenery.id), 1)
            return@onUseWith true
        }
    }

    private fun getReward(barrelId: Int): Int = REWARDS[barrelId - 13568] ?: DEFAULT_REWARD

    companion object {
        private val BARRELS = intArrayOf(
            shared.consts.Scenery.BEER_BARREL_13568,
            shared.consts.Scenery.CIDER_BARREL_13569,
            shared.consts.Scenery.ASGARNIAN_ALE_13570,
            shared.consts.Scenery.GREENMANS_ALE_13571,
            shared.consts.Scenery.DRAGON_BITTER_13572,
            shared.consts.Scenery.CHEFS_DELIGHT_13573
        )
        private val REWARDS = mapOf(
            shared.consts.Scenery.BEER_BARREL_13568     to Items.BEER_1917,
            shared.consts.Scenery.CIDER_BARREL_13569    to Items.CIDER_5763,
            shared.consts.Scenery.ASGARNIAN_ALE_13570   to Items.ASGARNIAN_ALE_1905,
            shared.consts.Scenery.GREENMANS_ALE_13571  to Items.GREENMANS_ALE_1909,
            shared.consts.Scenery.DRAGON_BITTER_13572   to Items.DRAGON_BITTER_1911,
            shared.consts.Scenery.CHEFS_DELIGHT_13573  to Items.CHEFS_DELIGHT_5755
        )
        private const val DEFAULT_REWARD = Items.BEER_1917
        private const val BEER_GLASS = Items.BEER_GLASS_1919
    }
}
