package core.net.event

import core.auth.AuthResponse
import core.net.EventProducer
import core.net.IoSession
import core.net.IoWriteEvent
import core.net.producer.GameEventProducer
import core.net.packet.out.LoginWorldEncoder
import java.nio.ByteBuffer

/**
 * Handles login writing events.
 *
 * @author Emperor.
 */
class LoginWriteEvent(
    session: IoSession,
    context: Any
) : IoWriteEvent(session, context) {

    override fun write(session: IoSession, context: Any) {
        val response = context as AuthResponse
        if (response == AuthResponse.Success) {
            val player = session.player
            val location = core.game.world.map.Location.create(player.location)
            val firstWorld = LoginWorldEncoder.response(player.index, location, player.details.rights.ordinal,
                session.reconnecting, session.isaacPair.output)
            if (!session.reconnecting) session.initialScene = location
            session.queue(firstWorld)
            session.producer = GAME_PRODUCER
            session.playerInfo = core.game.world.update.PlayerInfo(player.index, location.x, location.y, location.z)
            return
        }
        val buffer = ByteBuffer.allocate(500)

        buffer.put(response.ordinal.toByte())

        when (response.ordinal) {
            21 -> { // Moving world
                buffer.put(session.serverKey.toByte())
            }
        }

        buffer.flip()
        session.queue(buffer)
    }

    companion object {
        private val GAME_PRODUCER: EventProducer = GameEventProducer()

    }
}
