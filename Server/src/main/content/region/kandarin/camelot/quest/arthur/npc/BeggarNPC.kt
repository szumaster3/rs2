package content.region.kandarin.camelot.quest.arthur.npc

import content.data.GameAttributes
import core.api.removeAttribute
import core.game.node.entity.npc.AbstractNPC
import core.game.node.entity.player.Player
import core.game.world.map.Location
import shared.consts.NPCs

/**
 * Represents the Beggar NPC (Draynor Village).
 */
class BeggarNPC(
    id: Int = 0,
    location: Location? = null,
) : AbstractNPC(id, location) {
    var spawnedTicks = 0
    var player: Player? = null
    var finalized = false

    override fun construct(
        id: Int,
        location: Location?,
        vararg objects: Any?,
    ): AbstractNPC = BeggarNPC(id, location)

    override fun getIds(): IntArray = intArrayOf(NPCs.LAKE_BEGGAR_252)

    override fun tick() {
        super.tick()

        if (!finalized) {
            if (spawnedTicks++ == 100) {
                clear()

                player ?: return
                removeAttribute(player!!, GameAttributes.TEMP_ATTR_BEGGAR)
            }
        }
    }
}
