package core.net.packet.context

import core.game.node.entity.player.Player

class InterfaceAnimateRotateContext(private val player: Player, val id: Int, val componentId: Int, val pitch: Int, val yaw: Int) {
    override fun getPlayer(): Player = player
}