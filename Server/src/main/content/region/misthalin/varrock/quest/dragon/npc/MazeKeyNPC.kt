package content.region.misthalin.varrock.quest.dragon.npc

import core.api.produceGroundItem
import core.game.node.entity.Entity
import core.game.node.entity.npc.AbstractNPC
import core.game.node.entity.player.Player
import core.game.world.map.Location
import core.tools.RandomFunction
import shared.consts.Items
import shared.consts.NPCs

class MazeKeyNPC : AbstractNPC {

    constructor() : super(0, null)
    private constructor(id: Int, location: Location) : super(id, location)

    override fun construct(id: Int, location: Location, vararg objects: Any): AbstractNPC =
        MazeKeyNPC(id, location)

    override fun finalizeDeath(killer: Entity) {
        super.finalizeDeath(killer)

        if (killer !is Player) return

        val config = CONFIGS[id] ?: return

        if (!killer.location.withinDistance(config.location)) return

        if (RandomFunction.random(config.chance) == 2) {
            produceGroundItem(killer, config.item, 1, getLocation())
        }
    }

    override fun getIds(): IntArray = CONFIGS.keys.toIntArray()

    companion object {

        data class DropConfig(
            val item: Int,
            val chance: Int,
            val location: Location
        )

        private val CONFIGS = mapOf(
            NPCs.LESSER_DEMON_82 to DropConfig(
                Items.KEY_1548,
                5,
                Location.create(2936, 9652, 0)
            ),

            NPCs.GHOST_103 to DropConfig(
                Items.KEY_1544,
                5,
                Location.create(2926, 3253, 1)
            ),

            NPCs.SKELETON_UNARMED_90 to DropConfig(
                Items.KEY_1545,
                6,
                Location.create(2927, 3253, 2)
            ),

            NPCs.ZOMBIE_ARMED_75 to DropConfig(
                Items.KEY_1546,
                4,
                Location.create(2933, 9641, 0)
            )
        )
    }
}