package content.global.skill.summoning.familiar.npc

import content.global.skill.summoning.familiar.Familiar
import content.global.skill.summoning.familiar.FamiliarSpecial
import core.game.node.entity.combat.equipment.WeaponInterface
import core.game.node.entity.player.Player
import core.plugin.Initializable
import shared.consts.Items
import shared.consts.NPCs

@Initializable
class PrayingMantisNPC @JvmOverloads constructor(owner: Player? = null, id: Int = NPCs.LORE_PRAYING_MANTIS_6798) :
    Familiar(owner, id, 6900, Items.PRAYING_MANTIS_POUCH_12011, 6, WeaponInterface.STYLE_ACCURATE) {
    override fun construct(owner: Player, id: Int): Familiar {
        return PrayingMantisNPC(owner, id)
    }

    override fun specialMove(special: FamiliarSpecial): Boolean {
        return false
    }

    override fun getIds(): IntArray {
        return intArrayOf(NPCs.LORE_PRAYING_MANTIS_6798, NPCs.LORE_PRAYING_MANTIS_COMBAT_6799)
    }
}
