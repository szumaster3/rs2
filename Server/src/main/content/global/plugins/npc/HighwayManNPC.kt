package content.global.plugins.npc

import core.api.finishDiaryTask
import core.api.hasDiaryTaskComplete
import core.api.setVarbit
import core.game.node.entity.Entity
import core.game.node.entity.npc.AbstractNPC
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.diary.DiaryType
import core.game.world.map.Location
import core.plugin.Initializable
import shared.consts.NPCs

@Initializable
class HighwayManNPC(id: Int = 0, location: Location? = null) : AbstractNPC(id, location) {

    override fun construct(id: Int, location: Location, vararg objects: Any): AbstractNPC = HighwayManNPC(id, location)

    override fun finalizeDeath(killer: Entity) {
        super.finalizeDeath(killer)
        if (id == NPCs.HIGHWAYMAN_180) {
            if (killer is Player) {
                val player = killer.asPlayer()
                if (!hasDiaryTaskComplete(player, DiaryType.FALADOR, 0, 10)) {
                    finishDiaryTask(player, DiaryType.FALADOR, 0, 10)
                    setVarbit(player, 5701,1, true)
                }
            }
        }
    }

    override fun onAttack(target: Entity) {
        sendChat("Stand and deliver!")
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.HIGHWAYMAN_180, NPCs.HIGHWAYMAN2_2677)
}
