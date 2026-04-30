package content.region.kandarin.camelot.ctr

import content.data.GameAttributes
import core.api.*
import core.game.node.entity.player.Player
import core.game.system.timer.impl.SkillRestore
import core.game.world.map.Location
import shared.consts.NPCs

class CamelotSession(val player: Player) {

    private var npc = CamelotTrainingRoomNPC(
        player.getAttribute(GameAttributes.KW_TIER, NPCs.SIR_BEDIVERE_6177),
        Location.create(2758, 3508, 2),
        this
    )

    init {
        player.removeExtension(CamelotSession::class.java)
        player.addExtension(CamelotSession::class.java, this)

        setAttribute(player, GameAttributes.PRAYER_LOCK, true)
        player.hook(Event.PrayerDeactivated, SkillRestore.PrayerDeactivatedHook)
        player.hook(Event.PrayerActivated, SkillRestore.PrayerActivatedHook)
    }

    fun start() {
        npc.init()
        npc.properties.combatPulse.attack(player)
        player.unlock()
    }

    fun respawn(nextId: Int) {
        npc.clear()

        npc = CamelotTrainingRoomNPC(
            nextId,
            location(2758, 3508, 2),
            this
        )

        npc.init()
        npc.properties.combatPulse.attack(player)
    }

    fun death() {
        teleport(player, Location.create(2750, 3507, 2))
    }

    fun close() {
        npc.clear()

        player.unhook(SkillRestore.PrayerDeactivatedHook)
        player.unhook(SkillRestore.PrayerActivatedHook)

        removeAttributes(
            player,
            GameAttributes.PRAYER_LOCK,
            GameAttributes.KW_SPAWN,
            GameAttributes.KW_TIER,
            GameAttributes.KW_BEGIN
        )

        player.removeExtension(CamelotSession::class.java)
    }

    companion object {
        fun create(player: Player): CamelotSession = CamelotSession(player)

        fun getSession(player: Player): CamelotSession? =
            player.getExtension(CamelotSession::class.java)
    }
}