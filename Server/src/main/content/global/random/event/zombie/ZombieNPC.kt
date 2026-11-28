package content.global.random.event.zombie

import content.global.random.RandomEventNPC
import core.api.utils.WeightBasedTable
import core.api.withinDistance
import core.game.node.entity.Entity
import core.game.node.entity.npc.NPC
import shared.consts.NPCs
import kotlin.math.max
import kotlin.math.min

/**
 * Handles the zombie npc.
 * @author Vexia
 */
class ZombieNPC(
    override var loot: WeightBasedTable? = null,
) : RandomEventNPC(NPCs.EVILTREE_LEPRECHAUN_RUN_419) {
    val ids = (NPCs.EVILTREE_LEPRECHAUN_RUN_419..NPCs.CAPTAIN_UNDAK_424).toList()

    override fun init() {
        super.init()
        val index = max(0, min(ids.size - 1, (player.properties.currentCombatLevel / 20) - 1))
        val id = ids[index]
        this.transform(id)
        this.setAttribute("no-spawn-return", true)
        this.attack(player)
        sendChat("Braaaainssss!")
    }

    override fun finalizeDeath(killer: Entity?) {
        super.finalizeDeath(killer)
    }

    override fun tick() {
        super.tick()
        if (!this.inCombat())
            this.attack(player)
    }

    override fun talkTo(npc: NPC) {}
}
