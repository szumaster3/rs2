package content.region.misthalin.draynor.quest.swept

import content.data.GameAttributes
import content.region.misthalin.draynor.quest.swept.plugin.SweptUtils
import core.api.*
import core.game.node.entity.Entity
import core.game.node.entity.player.Player
import core.game.world.map.zone.ZoneBorders
import core.game.world.map.zone.ZoneRestriction
import shared.consts.Items

/**
 * Represents the Basement (Betty & Hetty) area for Swept Away quest.
 */
class Basement : MapArea {
    override fun defineAreaBorders(): Array<ZoneBorders> = arrayOf(bettyBasement, hettyBasement)

    override fun getRestrictions(): Array<ZoneRestriction> = arrayOf(ZoneRestriction.CANNON, ZoneRestriction.RANDOM_EVENTS)

    override fun areaEnter(entity: Entity) {
        super.areaEnter(entity)
        if (entity !is Player) return

        val p = entity.asPlayer()
        if (inBorders(p, bettyBasement)) {
            setAttribute(p, WHICH_BASEMENT, BETTY)
            SweptUtils.spawnBettyBasementNPCs()
        } else {
            setAttribute(p, WHICH_BASEMENT, HETTY)
        }

        registerLogoutListener(p, GameAttributes.QUEST_SWEPT_AWAY) { player ->
            SweptUtils.removeBettyNPCs()
            for (pen in SweptUtils.Pen.values()) {
                if (pen.itemId != -1 && inInventory(player, pen.itemId)) {
                    removeAll(player, pen.itemId)
                }
            }

            return@registerLogoutListener
        }

        setAttribute(p, GameAttributes.QUEST_SWEPT_AWAY_CREATURE_INTER, 0)
    }

    override fun areaLeave(entity: Entity, logout: Boolean) {
        super.areaLeave(entity, logout)
        if (entity !is Player) return

        val p = entity.asPlayer()
        /*
         * By the time this is called the player is already outside of the zone,
         * so the basement they left is remembered when they enter it.
         */
        val left = getAttribute(p, WHICH_BASEMENT, 0)
        removeAttribute(p, WHICH_BASEMENT)

        if (left == HETTY) {
            var removed = false
            for (label in intArrayOf(Items.NEWT_LABEL_14065, Items.TOAD_LABEL_14066, Items.NEWTS_AND_TOADS_LABEL_14067)) {
                if (inInventory(p, label)) {
                    removeAll(p, label)
                    removed = true
                }
            }
            if (removed) sendMessage(p, "As you leave Hetty's basement, your labels mysteriously disappear.")
        }

        if (left == BETTY) {
            var removed = false
            if (inInventory(p, Items.MAGIC_SLATE_14069)) {
                removeItem(p, Items.MAGIC_SLATE_14069)
                removed = true
            }
            for (pen in SweptUtils.Pen.values()) {
                if (pen.itemId != -1 && inInventory(p, pen.itemId)) {
                    removeAll(p, pen.itemId)
                }
            }

            SweptUtils.removeBettyNPCs()
            clearLogoutListener(p, GameAttributes.QUEST_SWEPT_AWAY)
            removeAttribute(p, GameAttributes.QUEST_SWEPT_AWAY_BETTY_WAND)
            setVarbit(p, SweptUtils.VARBIT_LOTTIE_CHEST, 0)
            if (removed) sendMessage(p, "As you leave Betty's basement, your magic slate mysteriously disappears.")
        }
    }

    companion object {
        val bettyBasement = ZoneBorders(3217, 4500, 3244, 4525)
        val hettyBasement = ZoneBorders(3158, 4510, 3176, 4526)

        private const val WHICH_BASEMENT = "swept-away:basement"
        private const val BETTY = 1
        private const val HETTY = 2
    }
}
