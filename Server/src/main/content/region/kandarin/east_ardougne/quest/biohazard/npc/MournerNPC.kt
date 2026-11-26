package content.region.kandarin.east_ardougne.quest.biohazard.npc

import content.data.Quests

import core.api.*
import core.game.node.entity.Entity
import core.game.node.entity.npc.AbstractNPC
import core.game.world.map.Location
import core.plugin.Initializable
import shared.consts.Items
import shared.consts.NPCs

@Initializable
class MournerNPC(id: Int = 0, location: Location? = null) : AbstractNPC(id, location) {

    override fun construct(id: Int, location: Location, vararg objects: Any): AbstractNPC = MournerNPC(id, location)

    override fun getIds(): IntArray = intArrayOf(NPCs.MOURNER_370)

    override fun finalizeDeath(killer: Entity?) {
        val player = killer?.asPlayer() ?: return super.finalizeDeath(killer)

        val questStage = getQuestStage(player, Quests.BIOHAZARD.questName)
        if (questStage < 6) {
            return super.finalizeDeath(killer)
        }

        sendMessage(player, "You search the mourner...")

        if (inInventory(player, Items.KEY_2832)) {
            sendMessage(player, "...but find nothing.")
        } else {
            addItemOrDrop(player, Items.KEY_2832)
            sendMessage(player, "and find a key.", 1)
            if (questStage < 8) {
                setQuestStage(player, Quests.BIOHAZARD.questName, 8)
            }
        }

        super.finalizeDeath(killer)
    }
}
