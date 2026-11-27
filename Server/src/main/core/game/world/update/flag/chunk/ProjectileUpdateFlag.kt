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
        val p = context ?: return
        val start = p.sourceLocation ?: return
        val target = p.victim
        val end = (if (p.isLocationBased) p.endLocation else target?.location) ?: return
        val dx = end.x - start.x
        val dy = end.y - start.y
        if (start.z != end.z || dx !in -128..127 || dy !in -128..127) return
        if (target != null && target.index !in (if (target is Player) 1..2047 else 1..32766)) return
        val targetId = if (target == null) 0 else if (target is Player) -(target.index + 1) else target.index + 1
        if (p.projectileId !in 0..65534 || p.startHeight !in 0..255 || p.endHeight !in 0..255 ||
            p.startDelay !in 0..65535 || p.speed !in 0..65535 || p.angle !in -1..255 || p.distance !in 0..255) return
        ZoneProt.MAP_PROJANIM.start(buffer)
            .put((start.chunkOffsetX shl 4) or (start.chunkOffsetY and 0x7))
            .put(dx)
            .put(dy)
            .putShort(targetId)
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
