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
class LavaTitanNPC @JvmOverloads constructor(owner: Player? = null, id: Int = NPCs.LORE_LAVA_TITAN_7341) :
    Familiar(owner, id, 6100, Items.LAVA_TITAN_POUCH_12788, 4, WeaponInterface.STYLE_AGGRESSIVE) {

    init {
        boosts.add(SkillBonus(Skills.MINING, 10.0))
        boosts.add(SkillBonus(Skills.FIREMAKING, 10.0))
    }

    override fun construct(owner: Player, id: Int): Familiar {
        return LavaTitanNPC(owner, id)
    }

    override fun specialMove(special: FamiliarSpecial): Boolean {
        return false
    }

    override fun getIds(): IntArray {
        return intArrayOf(NPCs.LORE_LAVA_TITAN_7341, NPCs.LORE_LAVA_TITAN_COMBAT_7342)
    }
}
