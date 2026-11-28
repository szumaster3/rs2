package content.global.activity.godwars

import core.api.*
import core.game.dialogue.FaceAnim
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.interaction.QueueStrength
import core.game.node.entity.player.link.TeleportManager
import core.game.node.entity.skill.Skills
import core.game.world.map.Location
import shared.consts.*

class GodwarsEntrancePlugin : InteractionListener {
    override fun defineListeners() {

        /*
         * Handles tie rope on gwd entrance.
         */

        on(Scenery.HOLE_26340, IntType.SCENERY, "tie-rope") { player, _ ->
            if (!removeItem(player, Items.ROPE_954)) {
                sendMessage(player, "You don't have a rope to tie around the pillar.")
                return@on true
            }
            setVarbit(player, Vars.VARBIT_GWD_ROPE_ON_HOLE_3932, 1, true)
            return@on true
        }

        /*
         * Handles climb down the gwd entrance.
         */

        on(Scenery.HOLE_26341, IntType.SCENERY, "climb-down") { player, _ ->
            if (getStatLevel(player, Skills.AGILITY) < 15) {
                sendMessage(player, "You need an agility level of 15 to enter this.")
                return@on true
            }
            if (getVarbit(player, 3936) == 0) {
                sendNPCDialogue(player, NPCs.KNIGHT_6201, "Cough... Hey, over here.", FaceAnim.HALF_GUILTY)
                return@on true
            }
            val endLoc = Location(2882, 5311, 2)
            lock(player, 2)
            sendMessage(player, "You climb down the rope.")
            animate(player, Animations.HUMAN_REACHFORLADDER_828)
            teleport(player, endLoc, TeleportManager.TeleportType.INSTANT)
            return@on true
        }

        /*
         * Handles gwd shortcut.
         */

        on(Scenery.LITTLE_CRACK_26305, IntType.SCENERY, "crawl-through") { player, node ->
            val requiredLevel = 60

            if (getStatLevel(player, Skills.AGILITY) < requiredLevel) {
                sendMessage(player, "You need an agility level of $requiredLevel to squeeze through the crack.")
                return@on true
            }

            val startLoc = node.location

            val endLoc = when (startLoc) {
                Location(2900, 3713, 0) -> Location(2904, 3720, 0)
                else -> Location(2899, 3713, 0)
            }

            lock(player, 6)
            openOverlay(player, Components.FADE_TO_BLACK_115)
            queueScript(player, 5, QueueStrength.SOFT) {
                teleport(player, endLoc, TeleportManager.TeleportType.INSTANT)
                openOverlay(player, Components.FADE_FROM_BLACK_170)
                return@queueScript stopExecuting(player)
            }

            return@on true
        }

        /*
         * Handles climb the wall.
         */

        on(Scenery.CLIMBING_WALL_41068, IntType.SCENERY, "climb-down") { player, _ ->
            val endLoc = Location(2941, 3822, 0)
            teleport(player, endLoc)
            return@on true
        }
    }
}
