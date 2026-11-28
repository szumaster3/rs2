package content.region.karamja.quest.roots.npc

import content.data.Quests

import content.data.GameAttributes
import content.data.skill.SkillingTool
import content.region.karamja.quest.roots.BackToMyRootsPlugin
import core.api.*
import core.game.node.entity.Entity
import core.game.node.entity.combat.BattleState
import core.game.node.entity.combat.CombatStyle
import core.game.node.entity.npc.AbstractNPC
import core.game.node.entity.player.Player
import core.game.world.map.Location
import shared.consts.NPCs

class WildJadeVineNPC(
    id: Int = 0,
    location: Location? = null
) : AbstractNPC(id, location) {

    var target: Player? = null

    init {
        isWalks = false
        isRespawn = false
        isNeverWalks = true
        isAggressive = true
        isRenderable = true
    }

    override fun construct(id: Int, location: Location, vararg objects: Any): AbstractNPC =
        WildJadeVineNPC(id, location)

    override fun getIds(): IntArray = WILD_JADE_NPC

    override fun handleTickActions() {
        super.handleTickActions()

        val vine = this.asNpc()
        val hp = vine.skills.lifepoints

        val stageIndex = WILD_JADE_NPC.indexOf(vine.id)
        if (stageIndex in 0 until WILD_JADE_NPC.lastIndex) {
            val nextId = WILD_JADE_NPC[stageIndex + 1]
            val transformation = listOf(145, 65, 35).getOrNull(stageIndex) ?: Int.MIN_VALUE

            if (hp < transformation) {
                val curHP = vine.skills.lifepoints
                transform(nextId)
                vine.skills.lifepoints = curHP
                attack(target)
            }
        }
    }

    override fun checkImpact(state: BattleState) {
        super.checkImpact(state)
        val player = state.attacker as? Player ?: return
        val tool = SkillingTool.getAxe(player)?.id ?: return
        val hasAnAxe = anyInEquipment(player, tool)

        when {
            state.style == CombatStyle.MELEE && hasAnAxe -> {
                state.neutralizeHits()
                state.estimatedHit = state.maximumHit
            }

            state.style != CombatStyle.MELEE && !hasAnAxe -> {
                if (state.estimatedHit > -1) state.estimatedHit = 0
                else if (state.secondaryHit > -1) state.secondaryHit = 0
            }
        }
    }

    override fun finalizeDeath(killer: Entity) {
        super.finalizeDeath(killer)
        val player = killer as? Player ?: return

        if (getQuestStage(player, Quests.BACK_TO_MY_ROOTS.questName) == 7) {
            setQuestStage(player, Quests.BACK_TO_MY_ROOTS.questName, 8)

            val back = getAttribute(player, GameAttributes.VINE_FIGHT, player.location)
            clearLogoutListener(player, GameAttributes.VINE_FIGHT)

            runTask(player, 3) {
                teleport(player, back)
                unlock(player)
            }
            BackToMyRootsPlugin.region.flagInactive()
        }
    }

    companion object {
        val WILD_JADE_NPC = intArrayOf(
            NPCs.ROOTS_GIANT_VINE_3409,
            NPCs.ROOTS_GIANT_VINE02_3410,
            NPCs.ROOTS_GIANT_VINE03_3411,
            NPCs.ROOTS_GIANT_VINE04_3412
        )
    }
}
