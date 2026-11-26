package core.game.world.update.flag.chunk

import core.game.node.entity.impl.Projectile
import core.game.node.entity.player.Player
import core.game.world.update.flag.UpdateFlag
import core.net.packet.IoBuffer
import core.net.packet.out.ZoneProt

/**
 * Handles the projectile updating.
 * @author Emperor
 */
class ProjectileUpdateFlag(projectile: Projectile?) : UpdateFlag<Projectile?>(projectile) {

    override fun write(buffer: IoBuffer) {
        val p = context!!
        val start = p.sourceLocation
        val target = p.victim
        val end = if (p.isLocationBased) p.endLocation else target!!.location
        ZoneProt.MAP_PROJANIM.start(buffer)
            .put((start.chunkOffsetX shl 4) or (start.chunkOffsetY and 0x7))
            .put(end.x - start.x)
            .put(end.y - start.y)
            .putShort(if (target != null) (if (target is Player) -(target.getIndex() + 1) else (target.index + 1)) else 0)
            .putShort(p.projectileId)
            .put(p.startHeight)
            .put(p.endHeight)
            .putShort(p.startDelay)
            .putShort(p.speed)
            .put(p.angle)
            .put(p.distance)
    }

    override fun data(): Int {
        return 0
    }

    override fun ordinal(): Int {
        return 2
    }
}
