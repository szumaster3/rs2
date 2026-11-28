package content.global.skill.summoning.familiar.dialogue.spirit

import content.global.skill.summoning.familiar.Familiar
import core.api.*
import core.cache.def.impl.NpcType
import core.cache.def.impl.LocType
import core.game.interaction.OptionHandler
import core.game.node.Node
import core.game.node.entity.player.Player
import core.game.node.entity.player.link.TeleportManager
import core.game.node.item.Item
import core.game.node.scenery.Scenery
import core.game.node.scenery.SceneryBuilder
import core.game.world.map.Location
import core.game.world.map.zone.impl.WildernessZone
import core.game.world.update.flag.context.Animation
import core.plugin.Initializable
import core.plugin.Plugin
import shared.consts.Animations
import shared.consts.Items
import shared.consts.NPCs
import shared.consts.Scenery as Objects

/**
 * Handles Spirit Kyatt familiar interactions.
 */
@Initializable
class SpiritKyattOptionPlugin : OptionHandler() {

    companion object {
        private const val BRONZE_AXE_ITEM = Items.BRONZE_HATCHET_1351
        private const val BRONZE_PICKAXE_ITEM = Items.BRONZE_PICKAXE_1265
        private const val BRONZE_AXE_SCENERY = Objects.BRONZE_HATCHET_14912
        private const val BRONZE_PICKAXE_SCENERY = Objects.BRONZE_PICKAXE_14910
        private const val AXE_TRANSFORM = 14908
    }

    override fun newInstance(arg: Any?): Plugin<Any> {
        NPCs.LORE_KYATT_7365.let { NpcType.forId(it).handlers["option:interact"] = this }
        NPCs.LORE_KYATT_COMBAT_7366.let { NpcType.forId(it).handlers["option:interact"] = this }
        Objects.TRAPDOOR_28741.let { LocType.forId(it).handlers["option:open"] = this }
        Objects.LADDER_28743.let { LocType.forId(it).handlers["option:climb-up"] = this }
        BRONZE_AXE_SCENERY.let { LocType.forId(it).handlers["option:take"] = this }
        BRONZE_PICKAXE_SCENERY.let { LocType.forId(it).handlers["option:take"] = this }
        return this
    }

    override fun handle(player: Player, node: Node, option: String): Boolean {
        when (node.id) {
            NPCs.LORE_KYATT_7365, NPCs.LORE_KYATT_COMBAT_7366 -> {
                val f = node as Familiar
                if (f.owner != player) {
                    sendMessage(player, "This is not your follower.")
                    return true
                }
                sendOptions(player, "Select an Option", "Chat", "Teleport")
                addDialogueAction(player) { p, button ->
                    when (button) {
                        2 -> {
                            openDialogue(p, SpiritKyattDialogue())
                            return@addDialogueAction
                        }
                        3 -> {
                            if (!WildernessZone.checkTeleport(p, 20)) {
                                closeDialogue(p)
                            } else {
                                closeDialogue(p)
                                teleport(p, Location(2326, 3634, 0), TeleportManager.TeleportType.NORMAL)
                            }
                            return@addDialogueAction
                        }
                        else -> {
                            closeDialogue(p)
                            return@addDialogueAction
                        }
                    }
                }
            }

            Objects.TRAPDOOR_28741 -> {
                player.animate(Animation(Animations.HUMAN_PICKUPFLOOR_827))
                teleport(player, Location(2333, 10015), TeleportManager.TeleportType.INSTANT, 1)
            }

            Objects.LADDER_28743 -> {
                player.animate(Animation(Animations.HUMAN_REACHFORLADDER_828))
                teleport(player, Location(2328, 3646), TeleportManager.TeleportType.INSTANT, 1)
            }

            BRONZE_AXE_SCENERY -> takeItemFromScenery(player, node as Scenery, BRONZE_AXE_ITEM, AXE_TRANSFORM)
            BRONZE_PICKAXE_SCENERY -> takeItemFromScenery(player, node as Scenery, BRONZE_PICKAXE_ITEM, AXE_TRANSFORM)
        }
        return true
    }

    private fun takeItemFromScenery(player: Player, scenery: Scenery, itemId: Int, transformId: Int) {
        if (!player.inventory.add(Item(itemId, 1))) {
            sendMessage(player, "You don't have enough inventory space.")
            return
        }
        SceneryBuilder.replace(scenery, scenery.transform(transformId), 500)
    }
}