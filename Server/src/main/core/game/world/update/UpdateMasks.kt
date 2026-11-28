package core.game.world.update

import core.api.*
import core.game.node.entity.Entity
import core.game.node.entity.combat.ImpactHandler
import core.game.node.entity.player.Player
import core.game.world.map.Location
import core.game.world.update.flag.context.ChatMessage
import core.game.world.update.flag.context.ForceMoveCtx
import core.game.world.update.flag.*
import core.game.world.update.flag.context.HitMark
import core.game.world.update.flag.context.ProgressBar
import core.game.world.update.flag.context.AnimationSequenceUpdate
import core.game.world.update.flag.context.Animation
import core.game.world.update.flag.context.Graphics
import core.game.world.update.flag.context.LayeredSequence
import core.net.packet.IoBuffer
import core.net.packet.PacketHeader
import core.net.packet.ServerProt
import java.nio.ByteBuffer
import core.tools.*
import java.util.concurrent.atomic.AtomicBoolean

class UpdateMasks(val owner: Entity) {
    var appearanceStamp: Long = 0
    private val type = EFlagType.of(owner)
    private val updating = AtomicBoolean()
    private var presenceFlags = 0
    private var syncedPresenceFlags = 0
    private val elements = arrayOfNulls<MaskElement?>(SIZE)
    private val syncedElements = arrayOfNulls<MaskElement?>(SIZE)

    private data class MaskElement(val encoder: EFlagProvider, val context: Any?)

    @JvmOverloads
    fun register(flag: EntityFlag, context: Any?, sync: Boolean = false): Boolean {
        // A provider must never skip its body after the presence bit has been published.
        val valid = when (flag) {
            EntityFlag.Chat -> context is ChatMessage && context.player === owner &&
                (context.nativeQuickChat?.size?.let { it in 1..255 } ?: true)
            EntityFlag.Appearance -> context is Player && context === owner
            EntityFlag.PrimaryHit, EntityFlag.SecondaryHit -> context is HitMark &&
                context.entity === owner && context.damage in 0..32767 && context.type in 0..255
            EntityFlag.FaceEntity -> context == null || context is Entity
            EntityFlag.FaceLocation -> context is Location
            EntityFlag.ForceMove -> context is ForceMoveCtx && !sync
            EntityFlag.TypeSwap -> context is Int && context in 0..16383
            else -> true
        }
        if (!valid) return false
        if (flag == EntityFlag.MovementType && (context !is Int || context !in 0..2)) return false
        if (flag == EntityFlag.ProgressBar && (context !is ProgressBar || sync)) return false
        if (flag == EntityFlag.AnimSeq && (sync || (context !is AnimationSequenceUpdate && context !is Animation))) return false
        if (flag == EntityFlag.AnimSeq && context is Animation && (context.id !in -1..65534 || context.delay !in 0..255)) return false
        if (flag == EntityFlag.Animate && (context !is Animation || context.id !in -1..65534 || context.delay !in 0..255)) return false
        if (flag == EntityFlag.SpotAnim && (context !is Graphics || context.id !in -1..65534 || context.height !in -32768..32767 || context.delay !in 0..65535)) return false
        if (flag == EntityFlag.ForceChat && context !is String) return false
        var synced = sync || flag == EntityFlag.MovementType
        var provider = EntityFlags.getFlagFor(type, flag)
        if (provider == null) {
            logWithStack(
                this::class.java,
                Log.ERR,
                "Tried to use flag ${flag.name} which is not available for ${type.name} in this revision."
            )
            return false
        }
        if (updating.get())
            return false
        if (flag == EntityFlag.Appearance) {
            appearanceStamp = System.currentTimeMillis()
            synced = true
        }
        // Preserve order: client processes each layered sequence separately with its own replay/priority rules.
        // Do not collapse overlapping slots: that would change those rules.
        val queuedContext = if (flag == EntityFlag.AnimSeq) {
            fun entries(value: Any?): List<LayeredSequence> = when (value) {
                is AnimationSequenceUpdate -> value.entries
                is Animation -> listOf(LayeredSequence(value.id, value.delay, 0xfff))
                else -> emptyList()
            }
            val combined = entries(elements[provider.ordinal]?.context) + entries(context)
            if (combined.size > 255) return false
            AnimationSequenceUpdate(combined)
        } else if (flag == EntityFlag.Animate) (context as Animation).copy()
        else if (flag == EntityFlag.ForceMove) (context as ForceMoveCtx).snapshot()
        else context
        if (synced) {
            syncedElements[provider.ordinal] = MaskElement(provider, queuedContext)
            syncedPresenceFlags = syncedPresenceFlags or provider.presenceFlag
        }
        elements[provider.ordinal] = MaskElement(provider, queuedContext)
        presenceFlags = presenceFlags or provider.presenceFlag
        return true
    }

    /**
     * Unregisters a synced update mask.
     * @param maskData The mask data.
     * @return `True` if the mask got removed.
     */
    fun hasSynced(ordinal: Int): Boolean = ordinal in syncedElements.indices && syncedElements[ordinal] != null

    fun unregisterSynced(ordinal: Int): Boolean {
        if (ordinal !in syncedElements.indices) return false
        if (syncedElements[ordinal] != null) {
            syncedPresenceFlags = syncedPresenceFlags and syncedElements[ordinal]!!.encoder.presenceFlag.inv()
            syncedElements[ordinal] = null
            return true
        }
        return false
    }

    /**
     * Writes the flags.
     * @param p The player.
     * @param e The entity to update.
     * @param buffer The buffer to write on.
     */
    fun write(p: Player?, e: Entity?, buffer: IoBuffer) {
        var maskData = presenceFlags
        if (maskData >= 0x100) {
            maskData = maskData or if (e is Player) 0x2 else 0x40
            buffer.put(maskData).put(maskData shr 8)
        } else {
            buffer.put(maskData)
        }
        for (i in elements.indices) {
            val element = elements[i]
            element?.encoder?.writeToDynamic(buffer, element.context, owner)
        }
    }

    /**
     * Writes the update masks on synchronization.
     * @param p The player.
     * @param e The entity to update.
     * @param buffer The buffer to write on.
     * @param appearance If the appearance mask should be written.
     */
    fun writeSynced(p: Player?, e: Entity?, buffer: IoBuffer, appearance: Boolean) {
        var maskData = presenceFlags
        var synced = syncedPresenceFlags
        var appearanceFlag = EntityFlags.getPresenceFlag(type, EntityFlag.Appearance)
        if (!appearance && synced and appearanceFlag != 0) {
            synced = synced and appearanceFlag.inv()
        }
        maskData = maskData or synced
        if (maskData >= 0x100) {
            maskData = maskData or if (e is Player) 0x2 else 0x40
            buffer.put(maskData).put(maskData shr 8)
        } else {
            buffer.put(maskData)
        }
        for (i in elements.indices) {
            var element = elements[i]
            if (element == null) {
                element = syncedElements[i]
                if (!appearance && element != null && element.encoder.flag == EntityFlag.Appearance) {
                    continue
                }
            }
            element?.encoder?.writeToDynamic(buffer, element.context, owner)
        }
    }

    /**
     * First sight includes persistent flags and the current appearance.
     */
    fun playerBlock(player: Player, firstSight: Boolean, traversal: Int?): ByteArray? {
        var bits = presenceFlags
        if (firstSight) bits = bits or syncedPresenceFlags or 1 or 0x20
        if (traversal != null) bits = bits or 0x4000
        // Recheck at serialization in case a pulse changed the player's location
        // after prepare(). Omit the bit AND its body for this frame, never just the body.
        val forceOrdinal = EntityFlags.getOrdinal(type, EntityFlag.ForceMove)
        val force = elements.getOrNull(forceOrdinal)
        val skipForce = force != null && (force.context as? ForceMoveCtx)?.isEncodableFrom(player.location) != true
        if (skipForce) bits = bits and EntityFlags.getPresenceFlag(type, EntityFlag.ForceMove).inv()
        if (bits == 0) return null
        val out = IoBuffer(-1, PacketHeader.NORMAL, ByteBuffer.allocate(ServerProt.MAX_PAYLOAD_SIZE))
        if (bits >= 256) out.put(bits or 2).put(bits shr 8) else out.put(bits)
        for (i in elements.indices) {
            if (skipForce && i == forceOrdinal) continue
            val element = elements[i] ?: if (firstSight) syncedElements[i] else null
            if (element != null) element.encoder.writeToDynamic(out, element.context, owner)
            else if (firstSight && i == 0) PlayerFlags.MovementType().writeTo(out, 1)
            else if (firstSight && i == 3) PlayerFlags.Appearance().writeTo(out, player)
        }
        if (traversal != null) out.p1sub(traversal)
        val bytes = out.toByteBuffer()
        return bytes.array().copyOf(bytes.position())
    }

    /**
     * Adds the dynamic update flags.
     * @param entity The entity.
     */
    fun prepare(entity: Entity) {
        // Persist the actual traversal mode before rendering, without changing run settings.
        if (entity is Player) {
            // Movement has already been processed by Entity.tick(). Validate against
            // the position which will be sent in GPI, before publishing the mask bit.
            val ordinal = EntityFlags.getOrdinal(type, EntityFlag.ForceMove)
            if (ordinal in elements.indices) {
                val element = elements[ordinal]
                val movement = element?.context as? ForceMoveCtx
                if (element != null && (movement == null || !movement.isEncodableFrom(entity.location))) {
                    presenceFlags = presenceFlags and element.encoder.presenceFlag.inv()
                    elements[ordinal] = null
                    log(this::class.java, Log.WARN, "Unrepresentable force movement mask at ${entity.location}: $movement")
                }
            }
            val queue = entity.walkingQueue
            val movement = when {
                queue.runDir != -1 -> 2
                queue.walkDir != -1 -> 1
                else -> null
            }
            if (movement != null && syncedElements[0]?.context != movement) {
                register(EntityFlag.MovementType, movement)
            }
        }
        val handler = entity.impactHandler
        for (i in 0..1) {
            if (handler.impactQueue.peek() == null) {
                break
            }
            val impact = handler.impactQueue.poll()
            registerHitUpdate(entity, impact, i == 1)
        }
        updating.set(true)
    }

    /**
     * Registers the hit update for the given [ImpactHandler.Impact].
     * @param e The entity.
     * @param impact The impact to update.
     * @param secondary If the hit update is secondary.
     */
    private fun registerHitUpdate(e: Entity, impact: ImpactHandler.Impact, secondary: Boolean): HitMark {
        val mark = HitMark(impact.amount, impact.type.ordinal, e)
        register(if (secondary) EntityFlag.SecondaryHit else EntityFlag.PrimaryHit, mark)
        return mark
    }

    /**
     * Resets the update masks.
     */
    fun reset() {
        for (i in elements.indices) {
            elements[i] = null
        }
        presenceFlags = 0
        updating.set(false)
    }

    fun isUpdating(): Boolean {
        return updating.get()
    }

    /**
     * Checks if an update is required.
     * @return `True` if so.
     */
    val isUpdateRequired: Boolean
        get() = presenceFlags != 0

    /**
     * Checks if synced update masks have been registered.
     * @return `True` if so.
     */
    fun hasSynced(): Boolean {
        return syncedPresenceFlags != 0
    }

    companion object {
        /**
         * The amount of update masks.
         */
        const val SIZE = 13
    }
}
