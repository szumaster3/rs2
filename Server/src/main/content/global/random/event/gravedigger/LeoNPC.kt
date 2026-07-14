package content.global.random.event.gravedigger

import content.data.RandomEvent
import content.global.random.RandomEventNPC
import content.global.random.event.gravedigger.GravediggerPlugin.Companion.removeCoffins
import core.api.*
import core.api.utils.WeightBasedTable
import core.game.interaction.QueueStrength
import core.game.node.entity.npc.NPC
import core.game.node.entity.player.link.TeleportManager
import core.game.system.timer.impl.AntiMacro
import core.tools.RandomFunction
import shared.consts.NPCs

/**
 * Represents the Leo NPC for graveyard random event.
 * @author szu
 */
class LeoNPC(override var loot: WeightBasedTable? = null) : RandomEventNPC(NPCs.LEO_3508) {

    override fun init() {
        super.init()
        sendChat("Can I borrow you for a minute, ${player.username}?")
        lock(player, 5)
        val region = GravediggerPlugin.createRegion(player)
        region.add(player)
        val leo = create(NPCs.LEO_3508, region.baseLocation.transform(8, 10, 0))
        leo.apply {
            isWalks = false
            isNeverWalks = true
            isRespawn = false
            init()
        }
        teleport(player, region.baseLocation.transform(8, 9, 0), TeleportManager.TeleportType.NORMAL)
        leo.properties.teleportLocation = region.baseLocation.transform(8, 10, 0)
        setAttribute(player, RandomEvent.save(), player.location)
        registerLogoutListener(player, RandomEvent.logout()) { p ->
            p.location = getAttribute(p, RandomEvent.save(), player.location)
            removeCoffins(player)
        }
        queueScript(player, 5, QueueStrength.SOFT) {
            setMinimapState(player, 2)
            faceLocation(player, leo.location)
            player.dialogueInterpreter.open(LeoDialogue(), NPCs.LEO_3508)
            AntiMacro.terminateEventNpc(player)
            return@queueScript stopExecuting(player)
        }
    }

    override fun tick() {
        super.tick()
        if (RandomFunction.random(1, 10) == 5) {
            sendChat("Can I borrow you for a minute, ${player.username}?")
        }
    }

    override fun talkTo(npc: NPC) {
        player.dialogueInterpreter.open(LeoDialogue(), npc)
    }
}
