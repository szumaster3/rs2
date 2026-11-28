package core.game.world.update.flag

import core.game.world.update.flag.context.AnimationSequenceUpdate
import core.game.world.update.flag.context.LayeredSequence
import core.game.world.update.flag.context.ProgressBar
import core.net.packet.IoBuffer
import core.net.packet.PacketHeader
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer

class NativeMask578Tests {
    private fun serialize(provider: EFlagProvider, context: Any): ByteArray {
        val data = ByteBuffer.allocate(2048)
        provider.writeTo(IoBuffer(-1, PacketHeader.NORMAL, data), context)
        return data.array().copyOf(data.position())
    }

    @Test
    fun persistentMovementTypeUsesByteAdd() {
        assertArrayEquals(byteArrayOf(128.toByte()), serialize(PlayerFlags.MovementType(), 0))
        assertArrayEquals(byteArrayOf(129.toByte()), serialize(PlayerFlags.MovementType(), 1))
        assertArrayEquals(byteArrayOf(130.toByte()), serialize(PlayerFlags.MovementType(), 2))
    }

    @Test
    fun timerBarsUseDifferentPlayerAndNpcTransforms() {
        val progress = ProgressBar(90, 30, 1, true)
        assertEquals(0x805a, progress.packed)
        assertArrayEquals(byteArrayOf(-128, 90, -98, 127), serialize(PlayerFlags.Progress(), progress))
        assertArrayEquals(byteArrayOf(-128, -38, 98, 127), serialize(NPCFlags.Progress(), progress))
    }

    @Test
    fun layeredSequencesPreserveMultipleEntriesAndClearSentinel() {
        val update = AnimationSequenceUpdate(listOf(LayeredSequence(0x1234, 3, 1), LayeredSequence(-1, 0, 0xfff)))
        assertArrayEquals(byteArrayOf(126, 0x34, 0x12, -3, 1, 0, -1, -1, 0, -1, 15),
            serialize(PlayerFlags.AnimationSequence(), update))
        assertArrayEquals(byteArrayOf(126, 0x12, -76, -3, 0, 1, -1, 127, 0, 15, -1),
            serialize(NPCFlags.AnimationSequence(), update))
    }

    @Test
    fun invalidMaskContextsAreRejectedBeforeSerialization() {
        assertThrows(IllegalArgumentException::class.java) { ProgressBar(0, 0, 0) }
        assertThrows(IllegalArgumentException::class.java) { ProgressBar(32768, 0, 0) }
        assertThrows(IllegalArgumentException::class.java) { LayeredSequence(65535, 0, 1) }
        assertThrows(IllegalArgumentException::class.java) { LayeredSequence(1, 0, 0) }
        assertThrows(IllegalArgumentException::class.java) { AnimationSequenceUpdate(emptyList()) }
    }
}
