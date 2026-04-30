package content.region.kandarin.camelot.ctr

import core.api.*
import core.game.node.entity.Entity
import core.game.node.entity.combat.BattleState
import core.game.node.entity.combat.CombatStyle
import core.game.node.entity.npc.AbstractNPC
import core.game.node.entity.player.Player
import core.game.system.task.Pulse
import core.game.world.GameWorld.Pulser
import core.game.world.map.Direction
import core.game.world.map.Location
import core.game.world.map.RegionManager.getLocalPlayers
import shared.consts.NPCs

private const val MAX_TICKS_BEFORE_CLEAR = 5000

/**
 * Represents a Knight NPC participating in the wave battles.
 */
class CamelotTrainingRoomNPC(id: Int, location: Location?, val session: CamelotSession) : AbstractNPC(id, location) {

    private val player: Player = session.player
    private var type: WaveTier? = WaveTier.forId(id)

    private var timer = 0

    init {
        isWalks = true
        isRespawn = false
        isInvisible = false
    }

    override fun init() {
        super.init()
        properties.combatPulse.attack(player)
    }

    override fun handleTickActions() {
        super.handleTickActions()

        if (!player.isActive || !getLocalPlayers(this).contains(player)) {
            session.close()
            clear()
            return
        }

        if (!properties.combatPulse.isAttacking) {
            properties.combatPulse.attack(player)
        }

        if (timer++ > MAX_TICKS_BEFORE_CLEAR) {
            session.close()
            clear()
        }
    }

    override fun finalizeDeath(killer: Entity?) {
        super.finalizeDeath(killer)

        val wave = type ?: return

        Pulser.submit(object : Pulse(2) {
            override fun pulse(): Boolean {

                val next = wave.next()

                if (next == WaveTier.IX) {
                    teleport(player, Location.create(2750, 3507, 2).transform(Direction.SOUTH))
                    MerlinNPC.spawn(player, session)
                    session.close()
                    return true
                }

                if (next != null) {
                    session.respawn(next.id)
                }

                return true
            }
        })

        clear()
    }

    override fun isAttackable(entity: Entity, style: CombatStyle, message: Boolean): Boolean {
        return session.player == entity
    }

    override fun canSelectTarget(target: Entity): Boolean {
        if (target is Player) {
            if (target != session.player) {
                return false
            }
        }
        return true
    }

    override fun checkImpact(state: BattleState) {
        super.checkImpact(state)
        if (state.attacker !is Player) return
        if (state.style != CombatStyle.MELEE) {
            state.neutralizeHits()
            return
        }
    }

    override fun construct(id: Int, location: Location, vararg objects: Any): AbstractNPC {
        val session = objects.firstOrNull() as? CamelotSession
            ?: throw IllegalStateException("CamelotTrainingRoomNPC requires CamelotSession")

        return CamelotTrainingRoomNPC(id, location, session)
    }

    override fun getIds(): IntArray = intArrayOf(
        NPCs.SIR_BEDIVERE_6177,
        NPCs.SIR_PELLEAS_6176,
        NPCs.SIR_TRISTRAM_6175,
        NPCs.SIR_PALOMEDES_1883,
        NPCs.SIR_LUCAN_6173,
        NPCs.SIR_GAWAIN_6172,
        NPCs.SIR_KAY_6171,
        NPCs.SIR_LANCELOT_6170,
    )
}