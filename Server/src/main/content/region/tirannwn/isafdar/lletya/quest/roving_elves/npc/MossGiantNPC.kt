package content.region.tirannwn.isafdar.lletya.quest.roving_elves.npc

import content.region.tirannwn.isafdar.lletya.quest.roving_elves.RovingElves
import core.api.getQuestStage
import core.api.produceGroundItem
import core.api.sendMessage
import core.cache.def.impl.NPCDefinition
import core.game.interaction.OptionHandler
import core.game.node.Node
import core.game.node.entity.Entity
import core.game.node.entity.npc.AbstractNPC
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.Player
import core.game.node.item.GroundItemManager
import core.game.node.item.Item
import core.game.world.map.Location
import core.plugin.ClassScanner.definePlugin
import core.plugin.Plugin
import shared.consts.NPCs
import shared.consts.Quests

/**
 * Represents the Moss giant NPC.
 */
class MossGiantNPC : AbstractNPC {
    constructor() : super(0, null)

    constructor(id: Int, location: Location?) : super(id, location) {
        this.isAggressive = true
    }

    override fun construct(id: Int, location: Location, vararg objects: Any): AbstractNPC = MossGiantNPC(id, location)

    override fun finalizeDeath(killer: Entity) {
        super.finalizeDeath(killer)

        if (killer is Player) {
            if (getQuestStage(killer, Quests.ROVING_ELVES) == 15 &&
                !killer.inventory.contains(RovingElves.CONSECRATION_SEED, 1)
            ) {
                sendMessage(killer,"A small grey seed drops on the ground.")
                produceGroundItem(killer, RovingElves.CONSECRATION_SEED, 1, getLocation())
            }
        }
    }

    override fun newInstance(arg: Any?): Plugin<Any> {
        definePlugin(
            object : OptionHandler() {
                override fun newInstance(arg: Any?): Plugin<Any> {
                    NPCDefinition.forId(ids[0]).handlers["option:attack"] = this
                    return this
                }

                override fun handle(player: Player, node: Node, option: String): Boolean {
                    (node as NPC).attack(player)
                    return true
                }
            },
        )
        return super.newInstance(arg)
    }

    override fun getIds(): IntArray = intArrayOf(NPCs.MOSS_GIANT_1681)
}
