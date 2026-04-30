package core.game.system.command.rottenpotato

import core.game.interaction.InteractionListener
import core.game.node.Node
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.game.node.scenery.Scenery
import shared.consts.Items

class RottenPotatoPlugin : InteractionListener {

    companion object {
        const val ROTTEN_POTATO = Items.ROTTEN_POTATO_5733
    }

    override fun defineListeners() {

        onUseAnyWith(ITEM, ROTTEN_POTATO) { player, used, with ->
            handle(player, used, with)
        }

        onUseWithWildcard(SCENERY, { usedId, _ ->
            usedId == ROTTEN_POTATO
        }) { player, _, with ->
            player.dialogueInterpreter.open(RPUseWithSceneryDialogue().ID, with as Scenery)
            true
        }

        onUseWithPlayer(ROTTEN_POTATO) { player, used, with ->
            handle(player, used, with)
        }
    }

    private fun handle(player: Player, used: Node, with: Node): Boolean {
        if (used !is Item || used.id != ROTTEN_POTATO) {
            return false
        }

        handleTarget(with, player)
        return true
    }

    private fun handleTarget(node: Node, player: Player) {
        when (node) {
            is Scenery -> {
                val go = node.asScenery()
                player.dialogueInterpreter.open(RPUseWithSceneryDialogue().ID, go)
            }

            is NPC -> {
                val npc = node.asNpc()
                player.dialogueInterpreter.open(RPUseWithNPCDialogue().ID, npc)
            }

            is Item -> {
                val item = node.asItem()
                // TODO
            }

            is Player -> {
                val p = node.asPlayer()
                player.dialogueInterpreter.open(RPUseWithPlayerDialogue().ID, p)
            }
        }
    }
}