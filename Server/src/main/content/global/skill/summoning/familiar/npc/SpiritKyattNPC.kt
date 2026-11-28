package content.global.skill.summoning.familiar.npc

import content.global.skill.summoning.familiar.Familiar
import content.global.skill.summoning.familiar.FamiliarSpecial
import core.game.node.entity.combat.equipment.WeaponInterface
import core.game.node.entity.player.Player
import core.game.node.entity.skill.SkillBonus
import core.game.node.entity.skill.Skills
import core.plugin.Initializable
import shared.consts.Items
import shared.consts.NPCs

@Initializable
class SpiritKyattNPC @JvmOverloads constructor(owner: Player? = null, id: Int = NPCs.LORE_KYATT_COMBAT_7366) :
    Familiar(owner, id, 4900, Items.SPIRIT_KYATT_POUCH_12812, 3, WeaponInterface.STYLE_ACCURATE) {

    init {
        boosts.add(SkillBonus(Skills.HUNTER, 5.0))
    }

    override fun construct(owner: Player, id: Int): Familiar {
        return SpiritKyattNPC(owner, id)
    }

    override fun specialMove(special: FamiliarSpecial): Boolean {
        if (!super.isOwnerAttackable()) {
            return false
        }
        call()
        return true
    }

    override fun getIds(): IntArray {
        return intArrayOf(NPCs.LORE_KYATT_7365, NPCs.LORE_KYATT_COMBAT_7366)
    }
}
