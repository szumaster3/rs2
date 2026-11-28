package content.global.plugins.inter.with_item

import core.api.*
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.Node
import core.game.node.entity.player.Player
import core.tools.RandomFunction
import shared.consts.Animations
import shared.consts.Items
import shared.consts.Sounds

class MachetePlugin : InteractionListener {

    private val macheteIDs = intArrayOf(
        Items.MACHETE_975,
        Items.OPAL_MACHETE_6313,
        Items.JADE_MACHETE_6315,
        Items.RED_TOPAZ_MACHETE_6317
    )

    private val skewerStick = Items.SKEWER_STICK_6305

    private val thatchMap = mapOf(
        Items.THATCH_SPAR_LIGHT_6281 to "thatch spar light",
        Items.THATCH_SPAR_MED_6283 to "thatch spar medium",
        Items.THATCH_SPAR_DENSE_6285 to "thatch spar dense"
    )

    override fun defineListeners() {
        for ((thatchId, name) in thatchMap) {
            onUseWith(IntType.ITEM, macheteIDs, thatchId) { player, used, _ ->
                handleThatch(player, used, thatchId, name)
                return@onUseWith true
            }
        }
    }

    private fun handleThatch(player: Player, machete: Node, thatchId: Int, typeName: String) {
        animate(player, getAnimation(machete))
        playAudio(player, Sounds.TBCU_PREPARE_WOOD_1274)
        if (!removeItem(player, thatchId, Container.INVENTORY)) return
        addItem(player, skewerStick, RandomFunction.random(3, 6))
        sendMessage(player, "You slice the $typeName into skewer sticks")
    }

    private fun getAnimation(machete: Node): Int {
        return when (machete.asItem().id) {
            Items.MACHETE_975 -> Animations.TBW_CLEANUP_PLAYER_PREPARE_WOOD_2389
            Items.OPAL_MACHETE_6313 -> Animations.TBW_CLEANUP_PLAYER_PREPARE_WOOD_OPAL_2429
            Items.JADE_MACHETE_6315 -> 6430
            Items.RED_TOPAZ_MACHETE_6317 -> Animations.TBW_CLEANUP_PLAYER_PREPARE_WOOD_REDTOPAZ_2431
            else -> Animations.TBW_CLEANUP_PLAYER_PREPARE_WOOD_2389
        }
    }
}