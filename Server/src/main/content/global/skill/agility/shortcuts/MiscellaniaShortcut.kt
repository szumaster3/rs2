package content.global.skill.agility.shortcuts

import core.api.animationCycles
import core.api.anyInEquipment
import core.api.forceMove
import core.api.sendMessage
import core.cache.def.impl.LocType
import core.game.interaction.OptionHandler
import core.game.node.Node
import core.game.node.entity.player.Player
import core.game.world.map.Location
import core.plugin.Initializable
import core.plugin.Plugin
import shared.consts.Animations
import shared.consts.Items
import shared.consts.Scenery

/**
 * Handles the broken pier shortcut.
 */
@Initializable
class MiscellaniaShortcut : OptionHandler() {

    override fun newInstance(arg: Any?): Plugin<Any> {
        LocType.forId(Scenery.BROKEN_PIER_41531).handlers["option:step"] = this
        return this
    }

    override fun handle(player: Player, node: Node, option: String): Boolean {
        if (!player.location.withinDistance(node.location, 2)) {
            sendMessage(player, "You cannot do that from here.")
            return true
        }

        if (!anyInEquipment(player, Items.FREMENNIK_SEA_BOOTS_1_14571, Items.FREMENNIK_SEA_BOOTS_2_14572, Items.FREMENNIK_SEA_BOOTS_3_14573)) {
            sendMessage(player, "You need to claim the reward for the medium Fremennik diary to use this shortcut.")
            return true
        }

        forceMove(player, player.location, Location.create(2573, 3862, 0), 0, 60, null, Animations.HUMAN_STEPPINGSTONEJUMP_769)
        forceMove(player, Location.create(2573, 3862, 0), Location.create(2576, 3862, 0), 90, animationCycles(Animations.WILD_DITCH_JUMP_6132), null, Animations.WILD_DITCH_JUMP_6132)
        return true
    }

    override fun isWalk(): Boolean = false
}