package core.game.world.update.flag

import core.ServerConstants
import core.game.node.entity.Entity
import core.game.node.entity.player.Player
import core.game.world.GameWorld
import core.game.world.map.Location
import core.game.world.update.flag.context.*
import core.net.packet.IoBuffer
import core.tools.StringUtils
import java.nio.charset.Charset
import kotlin.math.max
import kotlin.reflect.typeOf

/**
 * Player update flags for syncing player state.
 */
sealed class PlayerFlags(
    p: Int,
    o: Int,
    f: EntityFlag,
) : EFlagProvider(ServerConstants.REVISION, EFlagType.Player, p, o, f) {
    companion object {
        private val cp1252 = Charset.forName("windows-1252")
    }
    /** Persistent client movement type: crawl=0, walk=1, run=2. */
    class MovementType : PlayerFlags(0x20, 0, EntityFlag.MovementType) {
        override fun writeTo(buffer: IoBuffer, context: Any?) {
            buffer.p1add(context as Int)
        }
    }

    /** Overhead progress bar, distinct from the hitpoint bar. */
    class Progress : PlayerFlags(0x1000, 7, EntityFlag.ProgressBar) {
        override fun writeTo(buffer: IoBuffer, context: Any?) {
            val progress = context as ProgressBar
            buffer.p2(progress.packed).p1add(progress.hold).p1sub(progress.step)
        }
    }

    /**
     * Sends a chat message update.
     */
    class Chat : PlayerFlags(0x8, 1, EntityFlag.Chat) {
        override fun writeTo(
            buffer: IoBuffer,
            context: Any?,
        ) {
            if (context !is ChatMessage) {
                logInvalidType(context, typeOf<ChatMessage>())
                return
            }
            val quick = context.nativeQuickChat
            if (quick != null) {
                buffer.p2add((context.effects and 0x7fff) or 0x8000)
                buffer.p1neg(context.chatIcon)
                buffer.p1(quick.size)
                buffer.putReverseA(quick, 0, quick.size - 1)
                return
            }
            buffer.p2add(context.effects and 0x7fff)
            buffer.p1neg(context.chatIcon)
            val text = context.text.toByteArray(cp1252)
            val compressed = ByteArray(1024)
            var length = minOf(text.size, 255)
            var size: Int
            do {
                val prefix = if (length < 128) 1 else 2
                if (prefix == 2) compressed[0] = ((length shr 8) + 128).toByte()
                compressed[prefix - 1] = length.toByte()
                size = prefix + StringUtils.encryptPlayerChat(compressed, 0, prefix, length, text)
                length--
            } while (size > 255)
            buffer.p1(size)
            buffer.putReverseA(compressed, 0, size - 1)
        }
    }

    /**
     * Sends primary hit damage info.
     */
    class PrimaryHit : PlayerFlags(0x40, 11, EntityFlag.PrimaryHit) {
        override fun writeTo(
            buffer: IoBuffer,
            context: Any?,
        ) {
            if (context !is HitMark) {
                logInvalidType(context, typeOf<HitMark>())
                return
            }
            buffer.psmarts(context.damage)
            buffer.p1sub(context.type)

            var ratio = 255
            val e = context.entity
            val max = e.skills.maximumLifepoints
            if (max > e.skills.lifepoints) {
                ratio = e.skills.lifepoints * 255 / max
            }
            buffer.p1neg(ratio)
        }
    }

    /**
     * Sends animation update.
     */
    class Animate : PlayerFlags(0x4, 5, EntityFlag.Animate) {
        override fun writeTo(
            buffer: IoBuffer,
            context: Any?,
        ) {
            if (context !is Animation) {
                logInvalidType(context, typeOf<Animation>())
                return
            }
            buffer.ip2add(context.id)
            buffer.p1add(context.delay)
        }
    }

    /**
     * Sends player appearance data.
     */
    class Appearance : PlayerFlags(0x1, 3, EntityFlag.Appearance) {
        override fun writeTo(buffer: IoBuffer, context: Any?) {
            if (context !is Player) {
                logInvalidType(context, typeOf<Player>())
                return
            }
            val appearance = context.appearance
            appearance.prepareBodyData(context)
            var settings = appearance.gender.toByte().toInt()
            val nonPvp = context.skullManager.isWilderness && context.skullManager.isWildernessDisabled
            if (context.size() > 1) {
                settings += (context.size() - 1) shl 3
            }
            if (nonPvp) {
                settings += 0x4 // flag the player as non-PvP
            }

            val output = buffer
            val buffer = IoBuffer()

            buffer.p1(settings)
            buffer.p1(-1)
            buffer.p1(appearance.skullIcon)
            buffer.p1(appearance.headIcon)
            val npcId = appearance.npcId
            if (npcId == -1) {
                val parts = appearance.bodyParts
                for (i in 0 until 12) {
                    if (parts[i] == 0) {
                        buffer.p1(0)
                    } else {
                        buffer.p2(parts[i])
                    }
                }
            } else {
                buffer.p2(-1)
                buffer.p2(npcId)
                buffer.p1(255)
            }
            arrayOf(appearance.hair, appearance.torso, appearance.legs, appearance.feet, appearance.skin).forEach { part ->
                buffer.p1(part.color)
            }
            buffer.p2(appearance.renderAnimation)
            buffer.putString(context.name)
            if (nonPvp) {
                buffer.p1(context.properties.currentCombatLevel) // with summoning
                buffer.p2(context.skills.totalLevel)
            } else {
                // combat level calculations
                if ((GameWorld.settings!!.isPvp || (GameWorld.settings!!.wild_pvp_enabled && context.skullManager.isWilderness)) && !context.familiarManager.isUsingSummoning) {
                    buffer.p1(context.properties.combatLevel)
                    buffer.p1(context.properties.combatLevel + context.familiarManager.summoningCombatLevel)
                } else {
                    buffer.p1(context.properties.currentCombatLevel)
                    buffer.p1(context.properties.currentCombatLevel)
                }
                if (GameWorld.settings!!.isPvp) {
                    buffer.p1(context.skullManager.level)
                } else {
                    buffer.p1(-1) // combat range
                }
            }
            buffer.p1(0) // sound radius
            val size = buffer.toByteBuffer().position()
            output.p1sub(size)
            output.putReverse(buffer.toByteBuffer().array(), 0, size - 1)
        }
    }

    /**
     * Sends the entity the player is facing.
     */
    class FaceEntity : PlayerFlags(0x10, 4, EntityFlag.FaceEntity) {
        override fun writeTo(
            buffer: IoBuffer,
            context: Any?,
        ) {
            if (context !is Entity?) {
                logInvalidType(context, typeOf<Entity>())
                return
            }
            if (context == null) {
                buffer.p2add(-1)
            } else {
                buffer.p2add(context.clientIndex)
            }
        }
    }

    /**
     * Sends forced movement data for the player.
     */
    class ForceMove : PlayerFlags(0x100, 6, EntityFlag.ForceMove) {
        override fun writeToDynamic(
            buffer: IoBuffer,
            context: Any?,
            e: Entity,
        ) {
            if (context !is ForceMoveCtx) {
                logInvalidType(context, typeOf<ForceMoveCtx>())
                return
            }
            if (e !is Player) {
                logInvalidType(context, typeOf<Player>())
                return
            }
            val l = e.location
            buffer.p1add(context.start.x - l.x)
            buffer.p1add(context.start.y - l.y)
            buffer.p1sub(context.dest.x - l.x)
            buffer.p1(context.dest.y - l.y)
            buffer.p2add(context.startArrive)
            buffer.p2add(max(context.startArrive + 1, context.startArrive + context.destArrive))
            buffer.p1neg(context.direction.toInteger())
        }
    }

    /**
     * Sends forced chat text for the player.
     */
    class ForceChat : PlayerFlags(0x200, 9, EntityFlag.ForceChat) {
        override fun writeTo(
            buffer: IoBuffer,
            context: Any?,
        ) {
            if (context !is String) {
                logInvalidType(context, typeOf<String>())
                return
            }
            buffer.putString(context.ifEmpty { " " }.replace('\u0000', ' '))
        }
    }

    /**
     * Sends secondary hit damage info.
     */
    class SecondaryHit : PlayerFlags(0x800, 12, EntityFlag.SecondaryHit) {
        override fun writeTo(
            buffer: IoBuffer,
            context: Any?,
        ) {
            if (context !is HitMark) {
                logInvalidType(context, typeOf<HitMark>())
                return
            }
            buffer.psmarts(context.damage)
            buffer.p1sub(context.type)
        }
    }

    /**
     * Sends animation sequence update.
     */
    class AnimationSequence : PlayerFlags (0x400, 2, EntityFlag.AnimSeq) {
        override fun writeTo(buffer: IoBuffer, context: Any?) {
            if (context is Animation) {
                buffer.p1sub(1).ip2(context.id).p1neg(context.delay).ip2(0xffff)
            } else {
                buffer.p1sub(0)
            }
        }
    }

    /**
     * Sends spot animation data.
     */
    class SpotAnim : PlayerFlags(0x2000, 10, EntityFlag.SpotAnim) {
        override fun writeTo(
            buffer: IoBuffer,
            context: Any?,
        ) {
            if (context !is Graphics) {
                logInvalidType(context, typeOf<Graphics>())
                return
            }
            buffer.ip2add(context.id)
            buffer.p4((context.height shl 16) + context.delay)
        }
    }

    /**
     * Sends location the player is facing.
     */
    class FaceLocation : PlayerFlags(0x80, 8, EntityFlag.FaceLocation) {
        override fun writeToDynamic(
            buffer: IoBuffer,
            context: Any?,
            e: Entity,
        ) {
            if (context !is Location) {
                logInvalidType(context, typeOf<Location>())
                return
            }
            val angle = kotlin.math.atan2((e.location.x - context.x).toDouble(), (e.location.y - context.y).toDouble())
            buffer.p2((angle * (8192 / Math.PI)).toInt() and 0x3fff)
        }
    }
}
