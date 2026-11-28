package core.net

import core.game.node.item.Item
import core.net.packet.ServerProt
import core.net.packet.out.ContainerUpdate
import core.net.packet.out.ContainerUpdate.Entry
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ContainerUpdateTests {
    private fun bytes(vararg values: Int) = ByteArray(values.size) { values[it].toByte() }

    @Test
    fun fullContainerEncodesEmptySlotAndExtendedAmount() {
        val buffer = ContainerUpdate.encode(93, false,
            listOf(null, Entry(0, 254), Entry(995, 0x01020304)), 3)!!
        assertEquals(ServerProt.UPDATE_INV_FULL, buffer.opcode())
        assertArrayEquals(bytes(0, 93, 0, 0, 3, 0, 0, 0, 1, 0, 254,
            0xe4, 0x03, 255, 0x02, 0x01, 0x04, 0x03),
            buffer.toByteBuffer().array().copyOf(buffer.toByteBuffer().position()))
    }

    @Test
    fun partialContainerUsesSmartSlotsBigEndianIdAndNoAmountForEmpty() {
        val entries = MutableList<Entry?>(129) { null }
        entries[128] = Entry(995, 255)
        val buffer = ContainerUpdate.encode(90, true, entries, entries.size, intArrayOf(127, 128))!!
        assertEquals(ServerProt.UPDATE_INV_PARTIAL, buffer.opcode())
        assertArrayEquals(bytes(0, 90, 1, 127, 0, 0, 0x80, 0x80, 0x03, 0xe4, 255, 0, 0, 0, 255),
            buffer.toByteBuffer().array().copyOf(buffer.toByteBuffer().position()))
    }

    @Test
    fun emptyItemUsesUnsignedSentinelWithoutRejectingFullPacket() {
        assertNull(ContainerUpdate.snapshot(Item()))
        val buffer = ContainerUpdate.encode(93, false, null, 2, ids = intArrayOf(-1, 65535))!!
        assertArrayEquals(bytes(0, 93, 0, 0, 2, 0, 0, 0, 0, 0, 0),
            buffer.toByteBuffer().array().copyOf(buffer.toByteBuffer().position()))
    }

    @Test
    fun rejectsInvalidPayloadBeforeFrameAllocation() {
        assertNull(ContainerUpdate.encode(-1, false, emptyList(), 0))
        assertNull(ContainerUpdate.encode(93, false, listOf(Entry(65535, 1)), 1))
        assertNull(ContainerUpdate.encode(93, false, listOf(Entry(0, -1)), 1))
        assertNull(ContainerUpdate.encode(93, false, emptyList(), 65535))
        assertNull(ContainerUpdate.encode(93, false, listOf(null), 1, intArrayOf(1)))
        assertNull(ContainerUpdate.encode(93, false, listOf(null), 1, intArrayOf()))
    }
}
