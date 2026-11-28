package content.global.plugins.inter.with_scenery

import content.global.skill.gather.woodcutting.WoodcuttingNode
import content.global.skill.gather.woodcutting.WoodcuttingPulse
import core.api.submitIndividualPulse
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import shared.consts.Items

class AxeOnTreePlugin : InteractionListener {

    private val AXE_IDS = intArrayOf(Items.BRONZE_HATCHET_1351, Items.MITHRIL_HATCHET_1355, Items.IRON_HATCHET_1349, Items.BLACK_HATCHET_1361, Items.STEEL_HATCHET_1353, Items.ADAMANT_HATCHET_1357, Items.RUNE_HATCHET_1359, Items.DRAGON_HATCHET_6739, Items.INFERNO_ADZE_13661, Items.HATCHET_CLASS_1_14132, Items.HATCHET_CLASS_2_14134, Items.HATCHET_CLASS_3_14136, Items.HATCHET_CLASS_4_14138, Items.HATCHET_CLASS_5_14140)
    private val TREE_IDS = WoodcuttingNode.values().map { it.id }.toIntArray()

    override fun defineListeners() {

        /*
         * Handles use axe on tree.
         */

        onUseWith(IntType.SCENERY, AXE_IDS, *TREE_IDS) { player, _, with ->
            submitIndividualPulse(player, WoodcuttingPulse(player, with.asScenery()))
            return@onUseWith true
        }
    }
}
