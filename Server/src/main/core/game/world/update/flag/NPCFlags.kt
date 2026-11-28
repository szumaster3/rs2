package core.game.world.update.flag

import core.ServerConstants
import core.game.node.entity.Entity
import core.game.world.map.Location
import core.game.world.update.flag.context.Animation
import core.game.world.update.flag.context.Graphics
import core.game.world.update.flag.context.HitMark
import core.game.world.update.flag.context.ProgressBar
import core.game.world.update.flag.context.AnimationSequenceUpdate
import core.game.world.update.flag.context.LayeredSequence
import core.net.packet.IoBuffer
import kotlin.reflect.typeOf

/**
 * Flags specific to NPC entities for update packets.
 *
 * @param p Presence flag bitmask
 * @param o Ordinal for ordering flags
 * @param f Corresponding entity flag type
 */
sealed class NPCFlags(
    p: Int,
    o: Int,
    f: EntityFlag,
) : EFlagProvider(ServerConstants.REVISION, EFlagType.NPC, p, o, f) {

    /** Overhead progress bar, distinct from the hitpoint bar. */
    class Progress : NPCFlags(0x400, 0, EntityFlag.ProgressBar) {
        override fun writeTo(buffer: IoBuffer, context: Any?) {
            val progress = context as ProgressBar
            buffer.p2add(progress.packed).p1sub(progress.hold).p1sub(progress.step)
        }
    }

    /**
     * Represents the primary hit update flag.
     * Expects [HitMark] context containing damage and hit type.
     */
    class PrimaryHit : NPCFlags(0x2, 1, EntityFlag.PrimaryHit) {
        override fun writeTo(
            buffer: IoBuffer,
            context: Any?,
        ) {
            if (context !is HitMark) {
                logInvalidType(context, typeOf<HitMark>())
                return
            }
            buffer.psmarts(context.damage)
            buffer.p1neg(context.type)

            val e = context.entity
            var ratio = 255
            val max = e.skills.maximumLifepoints
            if (e.skills.lifepoints < max) {
                ratio = e.skills.lifepoints * 255 / max
            }
            buffer.p1(ratio)
        }
    }

    /**
     * Represents the secondary hit update flag.
     * Expects [HitMark] context.
     */
    class SecondaryHit : NPCFlags(0x1, 6, EntityFlag.SecondaryHit) {
        override fun writeTo(
            buffer: IoBuffer,
            context: Any?,
        ) {
            if (context !is HitMark) {
                logInvalidType(context, typeOf<HitMark>())
                return
            }
            buffer.psmarts(context.damage)
            buffer.p1neg(context.type)
        }
    }

    /**
     * Represents animation update flag.
     * Expects [Animation] context.
     */
    class Animate : NPCFlags(0x8, 2, EntityFlag.Animate) {
        override fun writeTo(
            buffer: IoBuffer,
            context: Any?,
        ) {
            if (context !is Animation) {
                logInvalidType(context, typeOf<Animation>())
                return
            }
            buffer.p2(context.id)
            buffer.p1sub(context.delay)
        }
    }

    /**
     * Flag for facing another entity.
     * Context can be [Entity] or null.
     */
    class FaceEntity : NPCFlags(0x20, 5, EntityFlag.FaceEntity) {
        override fun writeTo(
            buffer: IoBuffer,
            context: Any?,
        ) {
            if (context !is Entity?) {
                logInvalidType(context, typeOf<Entity>())
                return
            }
            if (context == null) {
                buffer.p2(-1)
            } else {
                buffer.p2(context.clientIndex)
            }
        }
    }

    /**
     * Spot animation flag.
     * Expects [Graphics] context.
     */
    class SpotAnim : NPCFlags(0x4, 4, EntityFlag.SpotAnim) {
        override fun writeTo(
            buffer: IoBuffer,
            context: Any?,
        ) {
            if (context !is Graphics) {
                logInvalidType(context, typeOf<Graphics>())
                return
            }
            buffer.ip2add(context.id)
            buffer.ip4((context.height shl 16) + context.delay)
        }
    }

    /**
     * Type swap flag.
     * Expects an [Int] context representing new NPC type ID.
     */
    class TypeSwap : NPCFlags(0x10, 9, EntityFlag.TypeSwap) {
        override fun writeTo(
            buffer: IoBuffer,
            context: Any?,
        ) {
            if (context !is Int) {
                logInvalidType(context, typeOf<Int>())
                return
            }
            buffer.ip2(context)
        }
    }

    /**
     * Force chat update flag.
     * Expects a [String] context containing the forced chat text.
     */
    class ForceChat : NPCFlags(0x80, 3, EntityFlag.ForceChat) {
        override fun writeTo(
            buffer: IoBuffer,
            context: Any?,
        ) {
            if (context !is String) {
                logInvalidType(context, typeOf<String>())
                return
            }
            buffer.putString(context.replace('\u0000', ' '))
        }
    }

    /**
     * Animation sequence flag.
     * Expects [Animation] context.
     */
    class AnimationSequence : NPCFlags(0x100, 7, EntityFlag.AnimSeq) {
        override fun writeTo(buffer: IoBuffer, context: Any?) {
            val entries = when (context) {
                is AnimationSequenceUpdate -> context.entries
                is Animation -> listOf(LayeredSequence(context.id, context.delay, 0xfff))
                else -> emptyList()
            }
            buffer.p1sub(entries.size)
            for (entry in entries) {
                buffer.p2add(entry.sequenceId).p1neg(entry.delay).p2(entry.slotMask)
            }
        }
    }

    /**
     * Face location flag.
     * Expects [Location] context.
     */
    class FaceLocation : NPCFlags(0x200, 8, EntityFlag.FaceLocation) {
        override fun writeTo(
            buffer: IoBuffer,
            context: Any?,
        ) {
            if (context !is Location) {
                logInvalidType(context, typeOf<Location>())
                return
            }
            buffer.p2((context.x shl 1) + 1)
            buffer.ip2((context.y shl 1) + 1)
        }
    }
}
