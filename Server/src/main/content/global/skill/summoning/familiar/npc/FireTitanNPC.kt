package content.global.skill.summoning.familiar.npc

import content.global.skill.summoning.familiar.Familiar
import core.game.node.entity.combat.equipment.WeaponInterface
import core.game.node.entity.player.Player
import core.plugin.Initializable
import shared.consts.Items
import shared.consts.NPCs

@Initializable
class FireTitanNPC @JvmOverloads constructor(owner: Player? = null, id: Int = NPCs.LORE_FIRE_TITAN_7355) :
    ElementalTitanNPC(owner, id, 6200, Items.FIRE_TITAN_POUCH_12802, 20, WeaponInterface.STYLE_CAST) {
    override fun construct(owner: Player, id: Int): Familiar {
        return FireTitanNPC(owner, id)
    }

    override fun getIds(): IntArray {
        return intArrayOf(NPCs.LORE_FIRE_TITAN_7355, NPCs.LORE_FIRE_TITAN_COMBAT_7356)
    }
}
