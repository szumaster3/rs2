package core.net.event

import core.auth.AuthResponse
import core.game.node.entity.player.info.ClientInfo
import core.game.node.entity.player.info.PlayerDetails
import core.game.world.GameWorld
import core.net.IoReadEvent
import core.net.IoSession
import core.net.packet.`in`.Login
import java.nio.ByteBuffer

/**
 * Handles login reading events.
 * @author Ceikry
 */
class LoginReadEvent(session: IoSession?, buffer: ByteBuffer?) : IoReadEvent(session, buffer) {
    override fun read(session: IoSession, buffer: ByteBuffer) {
        try {
            if (buffer.remaining() < 3 || buffer.remaining() - 3 < (buffer.getShort(buffer.position() + 1).toInt() and 0xffff)) {
                queueBuffer()
                return
            }
            if (!session.loginSubmitted.compareAndSet(false, true)) return
            val (response, info) = Login.decodeFromBuffer(buffer, session.serverKey)
            if (response != AuthResponse.Success || info == null) {
                session.write(response)
                return
            }
            val reconnect = info.opcode == 18
            val auth = GameWorld.authenticator
            val (authResponse, accountInfo) = if (reconnect) auth.checkReconnect(info.username, info.password)
                else auth.checkLogin(info.username, info.password)

            if (authResponse != AuthResponse.Success || accountInfo == null) {
                session.write(authResponse)
                return
            }
            val details = PlayerDetails(info.username)
            details.accountInfo = accountInfo
            details.communication.parse(accountInfo)
            session.clientInfo = ClientInfo(info.displayMode, info.windowMode, info.screenWidth, info.screenHeight)
            session.isaacPair = info.isaacPair
            session.reconnecting = reconnect
            session.associatedUsername = info.username
            Login.proceedWith(session, details, info.opcode)
        } catch (e: Exception) {
            e.printStackTrace()
            session.write(AuthResponse.UnexpectedError)
        }
    }
}
