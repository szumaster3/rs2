package content.region.kandarin.plugin

import core.game.node.entity.Entity
import core.game.node.entity.npc.AbstractNPC
import core.game.node.entity.npc.NPC
import core.game.world.map.Location
import core.game.world.map.RegionManager.getLocalNpcs
import core.plugin.Initializable
import core.tools.RandomFunction
import shared.consts.NPCs

/**
 * Handles Battlefield NPC behavior.
 */
@Initializable
class KhazardBattlefield(id: Int = 0, location: Location? = null) : AbstractNPC(id, location) {

    private var nextActionTime = 0L
    private var isGnome = false

    override fun construct(id: Int, location: Location, vararg objects: Any): AbstractNPC =
        KhazardBattlefield(id, location)

    override fun init() {
        super.init()
        isGnome = id in GNOMES
    }

    override fun tick() {
        super.tick()
        val currentTime = System.currentTimeMillis()
        if (currentTime < nextActionTime) return

        val victim = properties.combatPulse.getVictim()

        if (victim == null) {
            findAndAttackEnemy()
        } else {
            if (RandomFunction.random(9) == 0) {
                chatDuringCombat()
            }
        }

        nextActionTime = currentTime + RandomFunction.random(3000, 5000)
    }

    private fun findAndAttackEnemy() {
        val target = getNearbyEnemies().firstOrNull() ?: return
        target.lock(5)
        properties.combatPulse.attack(target)
    }

    private fun chatDuringCombat() {
        val enemy = properties.combatPulse.getVictim() ?: return
        if (enemy.location.getDistance(location) > 4) return

        val shouldChat = (enemy as? NPC)?.id?.let { getOpposingFaction().contains(it) } ?: true
        if (shouldChat) {
            sendChat(getRandomChat())
        }
    }

    private fun getNearbyEnemies(): List<NPC> {
        val opposingFaction = getOpposingFaction()
        return getLocalNpcs(this, 8).filter { npc ->
            npc != this && npc.id in opposingFaction && !npc.properties.combatPulse.isAttacking
        }
    }

    private fun getOpposingFaction(): IntArray = if (isGnome) TROOPERS else GNOMES

    private fun getRandomChat(): String =
        if (isGnome) GNOMES_FORCE_CHAT.random() else TROOPERS_FORCE_CHAT.random()

    override fun finalizeDeath(killer: Entity) {
        super.finalizeDeath(killer)
    }

    override fun getIds(): IntArray = ID

    companion object {
        private val ID =
            intArrayOf(
                NPCs.BATTLEFIELD_KHAZARD_TROOP_2245,
                NPCs.BATTLEFIELD_KHAZARD_TROOP_SPEARMEN_2246,
                NPCs.BROWCLOTHEDGNOME_67,
                NPCs.TERRORBIRD_MOUNTED_1752,
                NPCs.BATTLEFIELD_GNOMEKNIGHT_2247,
                NPCs.BATTLEFIELD_GNOME_TROOP_2249,
                NPCs.BATTLEFIELD_DARKSKINNED_GNOME_ARCHER_2250,
                NPCs.BATTLEFIELD_GNOME_ARCHER_2251,
                NPCs.BATTLEFIELD_TERRORBIRD_MOUNTEDSPEAR_6109,
                NPCs.BATTLEFIELD_TERRORBIRD_MOUNTEDSPEAR2_6110,
                NPCs.BATTLEFIELD_TERRORBIRD_MOUNTEDSPEAR3_6111
            )
        private val GNOMES =
            intArrayOf(
                NPCs.BROWCLOTHEDGNOME_67,
                NPCs.TERRORBIRD_MOUNTED_1752,
                NPCs.BATTLEFIELD_GNOMEKNIGHT_2247,
                NPCs.BATTLEFIELD_GNOME_TROOP_2249,
                NPCs.BATTLEFIELD_DARKSKINNED_GNOME_ARCHER_2250,
                NPCs.BATTLEFIELD_GNOME_ARCHER_2251,
                NPCs.BATTLE_TORTOISE_WAR2_3808,
                NPCs.BATTLEFIELD_TERRORBIRD_MOUNTEDSPEAR_6109
            )
        private val TROOPERS = intArrayOf(NPCs.BATTLEFIELD_KHAZARD_TROOP_2245, NPCs.BATTLEFIELD_KHAZARD_TROOP_SPEARMEN_2246)
        private val TROOPERS_FORCE_CHAT =
            arrayOf(
                "Get off of me, ya little pest!",
                "Die, you filthy goblin!",
                "Victory will be ours!",
                "We will crush you!"
            )
        private val GNOMES_FORCE_CHAT =
            arrayOf(
                "Stop right there, Khazard scum!",
                "Tally Ho!",
                "We are the true force of the battlefield!",
                "Feel the wrath of the goblins!"
            )
    }
}
