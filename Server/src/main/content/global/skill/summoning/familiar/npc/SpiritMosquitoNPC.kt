package content.global.skill.summoning.familiar.npc

import content.global.skill.summoning.familiar.Familiar
import content.global.skill.summoning.familiar.FamiliarSpecial
import core.game.node.entity.Entity
import core.game.node.entity.combat.equipment.WeaponInterface
import core.game.node.entity.player.Player
import core.game.world.update.flag.context.Animation
import core.game.world.update.flag.context.Graphics
import core.plugin.Initializable
import shared.consts.Items
import shared.consts.NPCs

@Initializable
class SpiritMosquitoNPC @JvmOverloads constructor(owner: Player? = null, id: Int = NPCs.LORE_MOSQUITO_7331) :
    Familiar(owner, id, 1200, Items.SPIRIT_MOSQUITO_POUCH_12778, 3, WeaponInterface.STYLE_ACCURATE) {
    override fun construct(owner: Player, id: Int): Familiar {
        return SpiritMosquitoNPC(owner, id)
    }

    override fun specialMove(special: FamiliarSpecial): Boolean {
        val target = special.node as Entity
        if (!canAttack(target)) {
            return false
        }
        visualize(Animation.create(8032), Graphics.create(1442))
        properties.combatPulse.attack(target)
        return true
    }

    override fun isPoisonImmune(): Boolean {
        return true
    }

    override fun getIds(): IntArray {
        return intArrayOf(NPCs.LORE_MOSQUITO_7331, NPCs.LORE_MOSQUITO_COMBAT_7332)
    }
}
