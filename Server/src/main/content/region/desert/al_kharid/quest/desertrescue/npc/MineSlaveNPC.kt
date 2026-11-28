package content.region.desert.al_kharid.quest.desertrescue.npc

import core.game.node.entity.npc.AbstractNPC
import core.game.world.GameWorld.ticks
import core.game.world.map.Location
import core.tools.RandomFunction
import shared.consts.NPCs

class MineSlaveNPC : AbstractNPC {
    private var delay = 0

    constructor() : super(0, null)

    constructor(id: Int, location: Location?) : super(id, location) {
        delay = ticks + RandomFunction.random(20, 100)
    }

    override fun construct(id: Int, location: Location, vararg objects: Any): AbstractNPC = MineSlaveNPC(id, location)

    override fun tick() {
        if (delay < ticks) {
            sendChat(CHATS[RandomFunction.random(CHATS.size)])
            delay = ticks + RandomFunction.random(20, 100)
        }
        super.tick()
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.TOURTRAP_QIP_SLAVE_MALE_1_4975, NPCs.TOURTRAP_QIP_MINING_SLAVE_MALE_2_4976, NPCs.TOURTRAP_QIP_MINING_SLAVE_FEMALE_1_4977, NPCs.TOURTRAP_QIP_MINING_SLAVE_FEMALE_2_4978)

    companion object {
        private val CHATS =
            arrayOf(
                "I'm sick of this place.",
                "What I wouldn't give for a good nights rest.",
                "I feel so weak I could faint.",
                "I didn't want to be a miner anyway.",
                "Ooh my back.",
                "I'm rich in experience, poor in wealth.",
                "I can' think straight, i'm so tired.",
            )
    }
}
