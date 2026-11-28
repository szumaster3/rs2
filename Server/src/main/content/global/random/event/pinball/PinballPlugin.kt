package content.global.random.event.pinball

import content.data.GameAttributes
import core.api.*
import core.api.utils.PlayerCamera
import core.game.component.Component
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.Entity
import core.game.node.entity.player.Player
import core.game.world.map.Location
import core.game.world.map.zone.ZoneBorders
import core.game.world.map.zone.ZoneRestriction
import core.tools.BLUE
import shared.consts.Animations
import shared.consts.Components
import shared.consts.NPCs
import shared.consts.Scenery

/**
 * Handles pinball random event interactions.
 */
class PinballPlugin : InteractionListener, MapArea {

    override fun defineListeners() {

        /*
         * Handles pillar tagging.
         */

        on(PinballUtils.PILLAR_OBJECTS, IntType.SCENERY, "tag") { player, node ->
            val points = getVarbit(player, PinballUtils.VARBIT_PINBALL_SCORE)
            if(points >= 10) return@on false

            lock(player, 2)
            animate(player, Animations.HUMAN_PICKUPTABLE_832)


            sendString(player, "Score: $points", Components.PINBALL_INTERFACE_263, 1)

            val targetIndex = getAttribute(player, GameAttributes.RE_PINBALL_OBJ, -1)
            val targetPillar = PinballUtils.PINBALL_PILLARS.getOrNull(targetIndex)

            if (node.id != targetPillar?.id) {
                PinballUtils.resetScore(player)
                Component.setUnclosable(
                    player,
                    player.dialogueInterpreter!!.sendPlainMessage(
                        true,
                        "",
                        "Wrong post! Your score has been reset.",
                        "Tag the post with the ${BLUE}flashing rings</col>."
                    )
                )
                PinballUtils.getTag(player)
                return@on true
            }

            PinballUtils.incrementScore(player)

            if (!PinballUtils.isComplete(player)) {
                PinballUtils.getTag(player)
                Component.setUnclosable(player, player.dialogueInterpreter!!.sendPlainMessage(true, "", "Well done! Now tag the next post."))
            } else {
                val lastIndex = getAttribute(player, GameAttributes.RE_PINBALL_INTER, -1)
                PinballUtils.PINBALL_PILLARS.getOrNull(lastIndex)?.let {
                    animateScenery(it, Animations.PINBALL_POST_INACTIVE_4006)
                }
                PlayerCamera(player).reset()
                sendPlainDialogue(player, true, "", "Congratulations - you can now leave the arena.")
            }
            return@on true
        }

        /*
         * Handles exit from the event.
         */

        on(Scenery.CAVE_EXIT_15010, IntType.SCENERY, "exit") { player, _ ->
            if (PinballUtils.isComplete(player)) {
                PinballUtils.cleanup(player)
                PinballUtils.reward(player)
                return@on true
            }

            openDialogue(player, PinballGuardDialogue())
            return@on true
        }

        /*
         * Handles interaction with guards.
         */

        on(intArrayOf(NPCs.FLIPPA_3912, NPCs.TILT_3913), IntType.NPC, "Talk-to") { player, npc ->
            openDialogue(player, PinballGuardDialogue(), npc)
            return@on true
        }
    }

    override fun defineAreaBorders(): Array<ZoneBorders> =
        arrayOf(PinballUtils.PINBALL_EVENT_ZONE_BORDERS)

    override fun entityStep(entity: Entity, location: Location, lastLocation: Location) {
        if (entity !is Player) return
        if (PinballUtils.isComplete(entity)) {
            sendPlainDialogue(entity, true, "", "Congratulations - you can now leave the arena.")
        }
    }

    override fun getRestrictions(): Array<ZoneRestriction> = arrayOf(
        ZoneRestriction.CANNON,
        ZoneRestriction.FOLLOWERS,
        ZoneRestriction.FIRES
    )

    override fun areaLeave(entity: Entity, logout: Boolean) {
        if (entity is Player) PinballUtils.cleanup(entity)
    }
}
