package content.global.random.event.rivertroll

import content.global.random.RandomEventNPC
import core.api.utils.WeightBasedTable
import core.game.node.entity.Entity
import core.game.node.entity.npc.NPC
import shared.consts.NPCs
import java.lang.Integer.max

/**
 * Handles the river troll npc.
 * @author Vexia
 */
class RiverTrollNPC(
    override var loot: WeightBasedTable? = null,
) : RandomEventNPC(NPCs.FROG_391) {
    val ids = (NPCs.FROG_391..NPCs.FROG_396).toList()

    override fun init() {
        super.init()
        val index = max(0, (player.properties.currentCombatLevel / 20) - 1)
        val id = ids.toList()[index]
        this.transform(id)
        this.attack(player)
        sendChat("Fishies be mine, leave dem fishies!")
        this.isRespawn = false
    }

    override fun finalizeDeath(killer: Entity?) {
        super.finalizeDeath(killer)
    }

    override fun tick() {
        super.tick()
        if (!this.inCombat())
            this.attack(player)
    }

    override fun talkTo(npc: NPC) {
        // Empty
    }
}
