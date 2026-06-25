package core.net.packet.context

import core.game.node.entity.player.Player

data class SceneryUpdateContext(
    private val player: Player,
    val objectId: Int,
    val type: Int,
    val rotation: Int,
    val x: Int,
    val y: Int,
    val int0: Int,
    val int1: Int,
    val int2: Int,
    val int3: Int,
    val int4: Int,
    val int5: Int,
    val int6: Int
) {
    override fun getPlayer(): Player = player
}