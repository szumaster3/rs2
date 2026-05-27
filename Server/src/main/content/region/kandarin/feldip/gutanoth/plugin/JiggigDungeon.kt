package content.region.kandarin.feldip.gutanoth.plugin

import content.region.kandarin.feldip.quest.zogre.plugin.ZogreUtils
import core.api.*
import core.api.utils.PlayerCamera
import core.game.interaction.QueueStrength
import core.game.node.entity.Entity
import core.game.node.entity.player.Player
import core.game.world.map.Location
import core.game.world.map.zone.ZoneBorders

class JiggigDungeon : MapArea {

    override fun defineAreaBorders(): Array<ZoneBorders> =
        arrayOf(CHARRED_AREA)

    override fun entityStep(entity: Entity, location: Location, lastLocation: Location) {
        super.entityStep(entity, location, lastLocation)

        if (entity !is Player) return
        val player = entity

        if (!inBorders(player, CHARRED_AREA)) return
        if (getAttribute(player, ZogreUtils.CHARRED_AREA, false)) return

        stopWalk(player)
        lock(player, 4)
        playCharredAreaSequence(player)
    }

    private fun playCharredAreaSequence(player: Player) {
        queueScript(player,1,QueueStrength.SOFT) { stage ->
            when (stage) {
                0 -> {
                    val message = "You enter this blackened, charred area — it looks like some sort of explosion has taken place."
                    player.dialogueInterpreter.sendPlainMessage(true, message)
                    sendMessage(player, message)
                    return@queueScript delayScript(player, 1)
                }
                1 -> {
                    closeDialogue(player)
                    PlayerCamera(player).apply {
                        setPosition(2447, 9457, 400)
                        panTo(2441, 9459, 400, 100)
                    }
                    return@queueScript delayScript(player, 1)
                }
                2 -> {
                    PlayerCamera(player).rotateTo(2441, 9459, 300, 10)
                    return@queueScript delayScript(player, 1)
                }
                3 -> {
                    PlayerCamera(player).reset()
                    setAttribute(player, ZogreUtils.CHARRED_AREA, true)
                    unlock(player)
                    return@queueScript stopExecuting(player)
                }
                else -> return@queueScript stopExecuting(player)
            }
        }
    }

    companion object {
        private val CHARRED_AREA = ZoneBorders(2445, 9458, 2447, 9467)
    }
}
