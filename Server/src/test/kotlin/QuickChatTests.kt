import core.net.packet.QuickChat
import content.global.skill.slayer.Tasks
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class QuickChatTests {
    @Test
    fun staticPhraseUsesUnsignedId() {
        assertArrayEquals(byteArrayOf(0x80.toByte(), 1), QuickChat.encodeDefinition(32769, byteArrayOf(1, 65, 0, 0), byteArrayOf()))
    }

    @Test
    fun itemParameterUsesTwoBytes() {
        val definition = byteArrayOf(1, 60, 0, 3, 1, 0, 1, 0)
        assertArrayEquals(byteArrayOf(0, 42, 1, 47), QuickChat.encodeDefinition(42, definition, byteArrayOf(1, 47)))
        assertNull(QuickChat.encodeDefinition(42, definition, byteArrayOf(1)))
    }

    @Test
    fun serverDerivedParameterIsNotInvented() {
        assertNull(QuickChat.encodeDefinition(42, byteArrayOf(1, 60, 0, 3, 1, 0, 3, 0), byteArrayOf()))
    }
    @Test
    fun baseLevelCannotBeForgedByClient() {
        val definition = byteArrayOf(1, 60, 0, 3, 1, 0, 4, 0, 16, 0)
        assertArrayEquals(byteArrayOf(0, 1, 42), QuickChat.encodeDefinition(1, definition, byteArrayOf(99)) { command, params ->
            if (command == 4 && params.contentEquals(intArrayOf(16))) 42L else null
        })
    }

    @Test
    fun serverQuestPointsUseFourBytes() {
        val definition = byteArrayOf(1, 60, 0, 3, 1, 0, 8, 0, 101, 0)
        assertArrayEquals(byteArrayOf(2, 35, 0, 0, 1, 44), QuickChat.encodeDefinition(547, definition, byteArrayOf()) { _, _ -> 300L })
    }

    @Test
    fun mixedClientAndServerParametersKeepWireOrder() {
        val definition = byteArrayOf(1, 60, 60, 60, 0, 3, 3, 0, 4, 0, 16, 0, 11, 3, 40, 0, 16, 0, 11, 8, 92, 0, 16, 0)
        assertArrayEquals(byteArrayOf(2, 103, 42, 42, 42), QuickChat.encodeDefinition(615, definition, byteArrayOf(99)) { _, _ -> 42L })
    }

    @Test
    fun rejectsOutOfRangeAndMalformedDefinitions() {
        val definition = byteArrayOf(1, 60, 0, 3, 1, 0, 15, 0)
        assertNull(QuickChat.encodeDefinition(952, definition, byteArrayOf()) { _, _ -> 256L })
        assertNull(QuickChat.encodeDefinition(952, definition, byteArrayOf(1)) { _, _ -> 42L })
        assertNull(QuickChat.encodeDefinition(952, byteArrayOf(1, 60, 0, 0), byteArrayOf()))
    }
    @Test
    fun slayerKeysAreNotServerOrdinals() {
        assertEquals(41, QuickChat.slayerKey(Tasks.ABERRANT_SPECTRES))
        assertEquals(101, QuickChat.slayerKey(Tasks.TZHAAR))
        assertEquals(0, QuickChat.slayerKey(null))
        assertNull(QuickChat.slayerKey(Tasks.AVIANSIES))
    }

    @Test
    fun slayerAssignmentUsesFourBytes() {
        val definition = byteArrayOf(1, 60, 0, 3, 1, 0, 6, 6, 27, 1, 0x8b.toByte(), 0)
        assertArrayEquals(byteArrayOf(0, 42, 0, 0, 0, 41), QuickChat.encodeDefinition(42, definition, byteArrayOf()) { command, params ->
            if (command == 6 && params.contentEquals(intArrayOf(1563, 395))) 41L else null
        })
    }
    @Test
    fun fogRatingUsesFourBytes() {
        val definition = byteArrayOf(1, 60, 0, 3, 1, 0, 8, 5, 125, 0)
        assertArrayEquals(byteArrayOf(1, 100, 0, 0, 4, 0), QuickChat.encodeDefinition(356, definition, byteArrayOf()) { command, params ->
            if (command == 8 && params.contentEquals(intArrayOf(1405))) 1024L else null
        })
    }

    @Test
    fun championCountUsesFourBytes() {
        val definition = byteArrayOf(1, 60, 0, 3, 1, 0, 9, 26, 108, 0)
        assertArrayEquals(byteArrayOf(3, 189.toByte(), 0, 0, 0, 11), QuickChat.encodeDefinition(957, definition, byteArrayOf()) { command, params ->
            if (command == 9 && params.contentEquals(intArrayOf(6764))) 11L else null
        })
    }
}
