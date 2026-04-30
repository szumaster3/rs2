package content.region.kandarin.camelot.ctr

import core.api.*
import core.game.interaction.QueueStrength
import core.game.node.entity.npc.AbstractNPC
import core.game.node.entity.player.Player
import core.game.world.map.Location
import core.plugin.Initializable
import shared.consts.NPCs

@Initializable
class MerlinNPC(
    id: Int = 0,
    location: Location? = null,
    val session: CamelotSession? = null
) : AbstractNPC(id, location) {

    private var cleanTime = 0

    private val player: Player?
        get() = session?.player

    init {
        isWalks = false
        isAggressive = false
        isRespawn = false
    }

    override fun construct(id: Int, location: Location, vararg objects: Any): AbstractNPC {
        val session = objects.firstOrNull() as? CamelotSession
        return MerlinNPC(id, location, session)
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.MERLIN_249)

    override fun handleTickActions() {
        super.handleTickActions()

        val p = player ?: return

        if (!p.isActive) {
            clear()
            return
        }

        if (cleanTime++ > 100) {
            poofClear(this)
        }
    }

    companion object {

        private const val SAFE_X = 2750
        private const val SAFE_Y = 3507
        private const val SAFE_Z = 2

        fun spawn(player: Player, session: CamelotSession) {

            val merlin = MerlinNPC(
                NPCs.MERLIN_249,
                Location.create(SAFE_X, SAFE_Y, SAFE_Z),
                session
            )

            merlin.init()

            queueScript(player, 1, QueueStrength.SOFT) {
                val npc = findLocalNPC(player, NPCs.MERLIN_249) ?: return@queueScript true
                face(npc, player, 3)
                face(player, npc)
                player.dialogueInterpreter.open(NPCs.MERLIN_249, npc)
                return@queueScript stopExecuting(player)
            }
        }
    }
}