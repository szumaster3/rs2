package content.minigame.mage_training.plugin

import content.global.skill.magic.spells.modern.EnchantSpell
import content.global.skill.magic.spells.modern.TelekineticGrabSpell
import content.minigame.mage_training.plugin.rooms.TelekineticTheatrePlugin
import core.api.*
import core.cache.def.impl.ObjType
import core.cache.def.impl.NpcType
import core.cache.def.impl.LocType
import core.game.interaction.OptionHandler
import core.game.node.Node
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.game.node.item.GroundItem
import core.game.world.map.Direction
import core.game.world.map.Location
import core.game.world.map.zone.ZoneBuilder
import core.plugin.ClassScanner.definePlugins
import core.plugin.Initializable
import core.plugin.Plugin
import shared.consts.*

/**
 * Handles interactions and initialization related to the Magic Training Arena (MTA).
 */
@Initializable
class MageTrainingArena : OptionHandler() {

    override fun newInstance(arg: Any?): Plugin<Any> {
        LocType.forId(Scenery.DOORWAY_10721).handlers["option:enter"] = this
        NpcType.forId(NPCs.MAGICTRAINING_GUARD_REWARDS_3103).handlers["option:trade-with"] = this
        for (type in MTARoomType.values()) {
            if (type.mtaZone != null) {
                ZoneBuilder.configure(type.mtaZone)
            }
            LocType.forId(type.sceneryId).handlers["option:enter"] = this
        }
        ObjType.forId(TelekineticTheatrePlugin.STATUE).handlers["option:observe"] = this
        ObjType.forId(TelekineticTheatrePlugin.STATUE).handlers["option:reset"] = this
        NpcType.forId(NPCs.MAGICTRAINING_GUARD_MAZE_3102).handlers["option:talk-to"] = this
        definePlugins(EnchantSpell(), TelekineticGrabSpell())
        return this
    }

    override fun handle(player: Player, node: Node, option: String): Boolean {
        when (node.id) {
            Scenery.DOORWAY_10721 -> {
                if (!hasLevelStat(player, Skills.MAGIC, 7)) {
                    sendDialogue(player, "You need a Magic level of at least 7 to enter the guild.")
                    return false
                }
                playAudio(player, Sounds.MAGICAL_BARRIER_1657)
                val destination = if (player.location.y < 3300) Direction.NORTH else Direction.SOUTH
                forceMove(player, player.location, player.location.transform(destination, 2), 30, 90, dir = destination, Animations.SC_WALKING_THROUGH_FORCEFIELD_10584)
                return true
            }

            NPCs.MAGICTRAINING_GUARD_REWARDS_3103 -> {
                if (!player.getSavedData().activityData.startMageTrainingArena) {
                    openDialogue(player, NPCs.MAGICTRAINING_GUARD_REWARDS_3103, this, true, true)
                } else {
                    openInterface(player, Components.MAGICTRAINING_SHOP_197)
                }
                return true
            }

            NPCs.MAGICTRAINING_GUARD_MAZE_3102 -> {
                openDialogue(player, node.id, node)
                return true
            }
        }

        when (option) {
            "enter" -> {
                val type = MTARoomType.forId(node.id)
                if (type == null) {
                    player.debug("Can't find a suitable area to enter.")
                    return true
                }
                type.enter(player)
                return true
            }

            "reset", "observe" -> {
                val zone: TelekineticTheatrePlugin = TelekineticTheatrePlugin.getZone(player)
                if (option == "reset") {
                    zone.reset(player)
                } else {
                    zone.observe(player)
                }
                return true
            }

            else -> return false
        }
    }

    override fun isWalk(player: Player, n: Node): Boolean {
        if (n !is GroundItem) {
            return true
        }
        return n.getId() != TelekineticTheatrePlugin.STATUE
    }

    override fun getDestination(node: Node, n: Node): Location? =
        if (n.id == NPCs.MAGICTRAINING_GUARD_MAZE_3102) {
            n.location.transform(Direction.getDirection(node.location, n.location), -1)
        } else {
            null
        }

    override fun isWalk(): Boolean = false
}
