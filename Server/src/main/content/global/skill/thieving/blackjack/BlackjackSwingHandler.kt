package content.global.skill.thieving.blackjack

import core.api.stun
import core.game.container.impl.EquipmentContainer
import core.game.node.entity.Entity
import core.game.node.entity.combat.BattleState
import core.game.node.entity.combat.CombatStyle
import core.game.node.entity.combat.MeleeSwingHandler
import core.game.node.entity.combat.equipment.Weapon
import core.game.node.entity.player.Player
import core.plugin.Initializable
import core.plugin.Plugin
import core.tools.RandomFunction

@Initializable
class BlackjackSwingHandler : MeleeSwingHandler(), Plugin<Any> {

    override fun newInstance(arg: Any?): Plugin<Any> {
        BlackjackType.values()
            .flatMap { it.itemIds.asList() }
            .forEach { CombatStyle.MELEE.swingHandler.register(it, this) }
        return this
    }

    override fun fireEvent(identifier: String, vararg args: Any): Any = Unit

    override fun swing(entity: Entity?, victim: Entity?, state: BattleState?): Int {
        val player = entity as? Player ?: return -1
        val target = victim ?: return -1
        val combatState = state ?: return -1
        val weaponItem = player.equipment[EquipmentContainer.SLOT_WEAPON] ?: return -1

        combatState.weapon = Weapon(weaponItem)
        combatState.style = CombatStyle.MELEE

        val max = calculateHit(player, target, 1.0)
        combatState.maximumHit = max

        val hit = if (isAccurateImpact(player, target, CombatStyle.MELEE)) RandomFunction.random(max + 1) else 0
        combatState.estimatedHit = hit

        if (hit > 0 && !target.isPlayer) {
            val lifepoints = target.skills.lifepoints
            val nearDeath = lifepoints <= hit
            if (nearDeath || RandomFunction.random(hit + 1) > RandomFunction.random(lifepoints + 1)) {
                stun(target, BlackjackType.stunTicks(weaponItem.id), false)
            }
        }

        return 1
    }
}
