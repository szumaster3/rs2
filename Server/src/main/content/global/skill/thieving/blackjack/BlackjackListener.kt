package content.global.skill.thieving.blackjack

import core.api.*
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.player.Player
import core.game.node.entity.player.info.Rights
import shared.consts.Quests

class BlackjackListener : InteractionListener {

    private val npcs = (BlackjackNPC.values().flatMap { it.npcIds.asList() } + BlackjackNPC.WRAPPER_IDS.asList()).toIntArray()

    override fun defineListeners() {
        on(npcs, IntType.NPC, "lure") { player, node ->
            if (canBlackjack(player)) BlackjackUtils.lure(player, node.asNpc())
            return@on true
        }

        on(npcs, IntType.NPC, "knock-out", "talk-to") { player, node ->
            val npc = node.asNpc()

            if (BlackjackUtils.isUnconscious(npc)) {
                BlackjackUtils.notify(player, npc)
                return@on true
            }

            if (getUsedOption(player) != "knock-out") return@on false

            if (canBlackjack(player)) BlackjackUtils.knockOut(player, npc)
            return@on true
        }
    }

    private fun canBlackjack(player: Player): Boolean =
        player.rights == Rights.ADMINISTRATOR || hasRequirement(player, Quests.THE_FEUD)
}
