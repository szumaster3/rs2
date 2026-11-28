package content.region.kandarin.port_khazard.quest.arena.npc

import content.data.Quests

import content.data.GameAttributes
import content.region.kandarin.port_khazard.quest.arena.dialogue.JeremyServilDialogueFile
import content.region.kandarin.port_khazard.quest.arena.plugin.FightArenaPlugin.Companion.General
import core.api.getQuestStage
import core.api.openDialogue
import core.api.setAttribute
import core.api.setQuestStage
import core.game.node.entity.Entity
import core.game.node.entity.npc.AbstractNPC
import core.game.node.entity.player.Player
import core.game.world.map.Location
import core.plugin.Initializable
import shared.consts.NPCs

@Initializable
class GeneralKhazardNPC(id: Int = 0, location: Location? = null) : AbstractNPC(id, location) {

    override fun construct(id: Int, location: Location, vararg objects: Any): AbstractNPC = GeneralKhazardNPC(id, location)

    override fun getIds(): IntArray = intArrayOf(NPCs.LADY_SERVIL_258)

    override fun handleTickActions() {
        super.handleTickActions()
        General.asNpc().isRespawn = true
        General.respawnTick = 10
    }

    override fun finalizeDeath(killer: Entity?) {
        if (killer is Player) {
            if (getQuestStage(killer, Quests.FIGHT_ARENA.questName) == 97) {
                setQuestStage(killer, Quests.FIGHT_ARENA.questName, 98)
                setAttribute(killer, GameAttributes.FIGHT_ARENA_OPTIONAL_KILL, true)
            }
            openDialogue(killer.asPlayer(), JeremyServilDialogueFile())
        }
        super.finalizeDeath(killer)
    }
}
