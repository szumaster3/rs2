package content.global.skill.summoning.familiar.npc

import content.data.consumables.Consumables
import content.global.skill.summoning.familiar.Familiar
import content.global.skill.summoning.familiar.FamiliarSpecial
import core.game.node.entity.combat.equipment.WeaponInterface
import core.game.node.entity.player.Player
import core.game.world.update.flag.context.Graphics
import core.plugin.Initializable
import shared.consts.Items
import shared.consts.NPCs

@Initializable
class RavenousLocustNPC @JvmOverloads constructor(owner: Player? = null, id: Int = NPCs.LORE_LOCUST_7372) :
    Familiar(owner, id, 2400, Items.RAVENOUS_LOCUST_POUCH_12820, 12, WeaponInterface.STYLE_ACCURATE) {

    override fun construct(owner: Player, id: Int): Familiar {
        return RavenousLocustNPC(owner, id)
    }

    override fun specialMove(special: FamiliarSpecial): Boolean {
        val target = special.target
        if (!canCombatSpecial(target)) {
            return false
        }
        animate(properties.attackAnimation)
        graphics(Graphics.create(1346))
        target.graphics(Graphics.create(1347))
        if (target is Player) {
            val p = target.asPlayer()
            for (item in p.inventory.toArray()) {
                if (item == null) {
                    continue
                }
                val consumable = Consumables.getConsumableById(item.id)?.consumable
                if (consumable != null) {
                    p.inventory.remove(item)
                    break
                }
            }
        }
        return true
    }

    override fun getIds(): IntArray {
        return intArrayOf(NPCs.LORE_LOCUST_7372, NPCs.LORE_LOCUST_COMBAT_7373)
    }
}
