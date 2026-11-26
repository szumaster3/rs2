package core.net

import core.auth.AuthResponse
import core.net.packet.`in`.Login
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import java.util.Base64

class LoginTests {
    // LoginManager.java:151-219, serialized with the bundled client's Packet and RSA parameters.
    private val fixture = Base64.getDecoder().decode(
        "EADjAAACQgACBQAC0AEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMBAgMAMQAAAAAAAAABAAAAAgAAAAMAAAAEAAAABQAAAAYAAAAHAAAACAAAAAkAAAAKAAAACwAAAAwAAAANAAAADgAAAA8AAAAQAAAAEQAAABIAAAATAAAAFAAAABUAAAAWAAAAFwAAABgAAAAZAAAAGgAAABsAAAAcQBC0YOMYQiNM8ldSHhvlCVVZgM5F4Z+CKzGcHbQ6928I0zS4vcZZD/Dkvd+S1cZvgbm+/nHRhfEbtkKhknDu5go="
    )
    private val challenge = (3L shl 32) or 4

    @Test fun readsClientLoginAndReconnect() {
        for (opcode in listOf(16, 18)) {
            fixture[0] = opcode.toByte()
            val (result, info) = Login.decodeFromBuffer(ByteBuffer.wrap(fixture), challenge)
            assertEquals(AuthResponse.Success, result)
            assertEquals("fixture", info!!.username)
            assertEquals("fixtureonly", info.password)
            assertEquals(1280, info.screenWidth)
            assertEquals(720, info.screenHeight)
            assertEquals(49, info.currentPacketCount)
            assertArrayEquals(IntArray(29) { it }, info.crcSums)
        }
    }

    @Test fun rejectsIncorrectChallenge() {
        assertEquals(AuthResponse.BadSessionID, Login.decodeFromBuffer(ByteBuffer.wrap(fixture), 0).first)
    }

    @Test fun rejectsDamagedEncryptedBlock() {
        fixture.fill(0, fixture.size - 64)
        assertEquals(AuthResponse.UnexpectedError, Login.decodeFromBuffer(ByteBuffer.wrap(fixture), challenge).first)
    }

    @Test fun rejectsInvalidOpcode() {
        fixture[0] = 2
        assertEquals(AuthResponse.InvalidLoginServer, Login.decodeFromBuffer(ByteBuffer.wrap(fixture), challenge).first)
    }
}
