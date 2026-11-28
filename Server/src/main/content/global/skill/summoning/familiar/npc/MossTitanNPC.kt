package content.global.skill.summoning.familiar.npc

import content.global.skill.summoning.familiar.Familiar
import core.game.node.entity.combat.equipment.WeaponInterface
import core.game.node.entity.player.Player
import core.plugin.Initializable
import shared.consts.Items
import shared.consts.NPCs

@Initializable
class MossTitanNPC
@JvmOverloads constructor(owner: Player? = null, id: Int = NPCs.LORE_MOSS_TITAN_7357) :
    ElementalTitanNPC(owner, id, 5800, Items.MOSS_TITAN_POUCH_12804, 20, WeaponInterface.STYLE_AGGRESSIVE) {
    override fun construct(owner: Player, id: Int): Familiar {
        return MossTitanNPC(owner, id)
    }

    override fun getIds(): IntArray {
        return intArrayOf(NPCs.LORE_MOSS_TITAN_7357, NPCs.LORE_MOSS_TITAN_COMBAT_7358)
    }
}
