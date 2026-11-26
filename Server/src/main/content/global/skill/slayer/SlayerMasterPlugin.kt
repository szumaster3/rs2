package content.global.skill.slayer

import content.data.Quests

import core.api.hasRequirement
import core.game.component.Component
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.Node
import core.game.node.entity.player.Player
import shared.consts.Components

class SlayerMasterPlugin : InteractionListener {

    override fun defineListeners() {
        for (master in SlayerMaster.values()) {
            on(master.npc, IntType.NPC, "rewards") { player, node ->
                handleRewardOption(player, node)
                return@on true
            }
        }
    }

    private fun handleRewardOption(player: Player, node: Node) {
        if (!hasRequirement(player, Quests.SMOKING_KILLS.questName)) return
        player.interfaceManager.open(Component(Components.SMKI_BUY_164))
        val opened = player.interfaceManager.opened
        if (opened != null) {
            player.interfaceManager.open(opened)
        }
    }
}