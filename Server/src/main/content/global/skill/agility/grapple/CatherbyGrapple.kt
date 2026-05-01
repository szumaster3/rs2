package content.global.skill.agility.grapple

import content.global.skill.agility.AgilityHandler
import core.api.*
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.QueueStrength
import core.game.node.entity.player.Player
import core.game.node.entity.skill.Skills
import core.game.system.task.Pulse
import core.game.world.map.Location
import core.game.world.update.flag.context.Animation
import shared.consts.Animations
import shared.consts.Items
import shared.consts.Scenery

class CatherbyGrapple : InteractionListener {
    companion object {
        private val END_LOCATION: Location = Location.create(2869, 3430, 0)

        private val REQUIREMENTS =
            hashMapOf(
                Skills.AGILITY to 32,
                Skills.RANGE to 35,
                Skills.STRENGTH to 35,
            )

        private val crossbowIds =
            intArrayOf(
                Items.DORGESHUUN_CBOW_8880,
                Items.MITH_CROSSBOW_9181,
                Items.ADAMANT_CROSSBOW_9183,
                Items.RUNE_CROSSBOW_9185,
                Items.KARILS_CROSSBOW_4734,
                Items.HUNTERS_CROSSBOW_10156,
            )
        private const val MITHRIL_GRAPPLE = Items.MITH_GRAPPLE_9419
    }

    private var rocks = getScenery(Location.create(2869, 3429, 0))

    override fun defineListeners() {

        on(Scenery.ROCKS_17042, IntType.SCENERY, "grapple") { player, _ ->
            if (!inEquipment(player, MITHRIL_GRAPPLE) || !anyInEquipment(player, *crossbowIds)) {
                sendMessage(player, "You need a mithril grapple tipped bolt with a rope to do that.")
                return@on true
            }

            if (!hasRequirements(player)) {
                sendDialogueLines(
                    player,
                    "You need at least " +
                        REQUIREMENTS[Skills.AGILITY] + " " + Skills.SKILL_NAME[Skills.AGILITY] + ", " +
                        REQUIREMENTS[Skills.RANGE] + " " + Skills.SKILL_NAME[Skills.RANGE] + ", ",
                    "and " +
                        REQUIREMENTS[Skills.STRENGTH] + " " + Skills.SKILL_NAME[Skills.STRENGTH] +
                        " to use this shortcut.",
                )
                return@on true
            }
            lock(player, 15)
            val start = player.location
            player.logoutListeners["yanille-grapple"] = { p: Player ->
                p.location = start
            }
            queueScript(player,2,QueueStrength.SOFT) { stage ->
                when (stage) {
                    0 -> {
                        face(player, END_LOCATION)
                        animate(player, Animation(Animations.FIRE_CROSSBOW_TO_CLIMB_WALL_4455))
                        return@queueScript delayScript(player, 4)
                    }
                    1 -> {
                        replaceScenery(rocks!!, rocks!!.id + 1, 10)
                        return@queueScript delayScript(player, 10)
                    }
                    2 -> {
                        teleport(player, END_LOCATION)
                        return@queueScript delayScript(player, 2)
                    }
                    3 -> {
                        sendMessage(player, "You successfully grapple the rock and climb the cliffside.")
                        AgilityHandler.checkGrappleBreak(player)
                        player.logoutListeners.remove("catherby-grapple")
                        return@queueScript stopExecuting(player)
                    }
                    else -> return@queueScript stopExecuting(player)
                }
            }
            return@on true
        }
    }

    private fun hasRequirements(player: Player): Boolean {
        for ((skill, requiredLevel) in REQUIREMENTS) {
            if (!hasLevelDyn(player, skill, requiredLevel)) {
                return false
            }
        }
        return true
    }

    override fun defineDestinationOverrides() {
        setDest(IntType.SCENERY, Scenery.ROCKS_17042) { _, _ ->
            return@setDest Location(2866, 3429, 0)
        }
    }
}
