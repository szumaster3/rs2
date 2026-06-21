package content.region.kandarin.camelot.quest.kr

import core.api.*
import core.game.interaction.IntType
import core.game.interaction.InteractionListener
import core.game.node.entity.Entity
import core.game.node.entity.player.Player
import core.game.node.item.Item
import core.game.world.map.Location
import core.game.world.map.zone.ZoneBorders
import shared.consts.Items
import shared.consts.Quests
import shared.consts.Scenery

class KingsRansomPlugin : InteractionListener, MapArea {
    private data class LadderExit(
        val key: String,
        val destination: Location
    )

    private val LADDER_EXITS: Map<Int, LadderExit> = mapOf(
        6722 to LadderExit(
            key = "king-ransom",
            destination = Location.create(1696, 4263, 0)
        ),
        11061 to LadderExit(
            key = "merlin",
            destination = Location.create(2770, 3408, 0)
        )
    )

    override fun defineListeners() {
        on(Items.ADDRESS_FORM_11680, IntType.ITEM, "read") { player, _ ->
            openInterface(player, 586)
            return@on true
        }

        on(Items.SCRAP_PAPER_11681, IntType.ITEM, "read") { player, _ ->
            openInterface(player, 587)
            return@on true
        }

        /*
         * Handles ladder to Keep Le Faye basement location.
         */

        on(Scenery.LADDER_25662, IntType.SCENERY, "climb-down") { player, _ ->
            val exit = LADDER_EXITS[player.location.regionId] ?: LADDER_EXITS[0]!!
            setAttribute(player, "/save:ladder-exit", exit.key)
            player.properties.teleportLocation = Location.create(1888, 4269, 0)
            return@on true
        }

        on(Scenery.LADDER_25663, IntType.SCENERY, "climb-up") { player, _ ->
            val key = getAttribute(player, "ladder-exit", null) as? String
            val exit = LADDER_EXITS.values.find { it.key == key } ?: LADDER_EXITS[0]!!
            player.properties.teleportLocation = exit.destination
            removeAttribute(player, "ladder-exit")
            return@on true
        }

        /*
         * Handles the destroy dialogue for the Holy Grail.
         */

        on(Items.HOLY_GRAIL_19, IntType.ITEM, "destroy") { player, node ->
            val item = node as Item

            val destroyMessage = when {
                // After Holy Grail, during King's Ransom.
                isQuestComplete(player, Quests.HOLY_GRAIL) && !isQuestComplete(player, Quests.KINGS_RANSOM) ->
                    "If you destroy the Holy Grail, you will need to return to the Fisher King's realm to obtain another."

                // Complete King's Ransom.
                isQuestComplete(player, Quests.KINGS_RANSOM) ->
                    "If you destroy the Holy Grail, you can obtain another from Keep Le Faye or reclaim one from the quest storage chest."

                // During Holy Grail.
                else ->
                    "If you destroy the Holy Grail, you will need to return to the Fisher King's realm to obtain another."
            }

            sendDestroyItemDialogue(player, Items.HOLY_GRAIL_19, destroyMessage)

            addDialogueAction(player) { p, button ->
                if (button == 3) {
                    closeDialogue(p)
                    removeItem(p, item)
                }
            }

            return@on true
        }

    }

    override fun defineAreaBorders(): Array<ZoneBorders> {
        return arrayOf(ZoneBorders.forRegion(7490))
    }

    override fun areaLeave(entity: Entity, logout: Boolean) {
        super.areaLeave(entity, logout)

        if (entity is Player && !logout) {
            removeAttribute(entity, "ladder-exit")
        }
    }
}