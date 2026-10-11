package core.net.packet.context

import core.game.node.entity.player.Player
import core.net.packet.Context

class InterfaceSetModelRotationContext(private val player: Player, val id: Int, val componentId: Int, val pitch: Int, val yaw: Int) : Context {
    override fun getPlayer(): Player = player
}