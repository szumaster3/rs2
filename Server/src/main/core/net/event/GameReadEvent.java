package core.net.event;

import core.net.packet.in.Decoders578;

import core.game.node.entity.player.Player;
import core.net.IoReadEvent;
import core.net.IoSession;
import core.net.packet.PacketProcessor;
import core.net.packet.in.Packet;
import java.nio.ByteBuffer;

public final class GameReadEvent extends IoReadEvent {
    public GameReadEvent(IoSession session, ByteBuffer buffer) {
        super(session, buffer);
    }

    @Override
    public void read(IoSession session, ByteBuffer buffer) {
        Player player = session.getPlayer();
        if (player == null || session.getIsaacPair() == null) {
            session.disconnect();
            return;
        }
        if (player.getSession() != session || !session.isActive()) return;
        session.gameInput.read(buffer, session.getIsaacPair().getInput(), (opcode, payload) -> {
            if (player.getSession() != session || !session.isActive()) return;
            session.setLastPing(System.currentTimeMillis());
            if (player.opCounts[opcode] >= 10) return;
            player.opCounts[opcode]++;
            Packet packet;
            try {
                packet = decode(player, opcode, payload);
            } catch (java.nio.BufferUnderflowException | IllegalArgumentException malformed) {
                return;
            }
            if (packet instanceof Packet.TransmitVerifyAck) {
                // Never resolve the session again through player: a reconnect can replace it.
                if (session.isActive() && player.getSession() == session) {
                    session.recordTransmitVerifyId(((Packet.TransmitVerifyAck) packet).getId());
                }
                return;
            }
            if (player.<Boolean>getAttribute("logging_in", false) && !(packet instanceof Packet.CloseIface)
                    && !(packet instanceof Packet.TrackingDisplayUpdate)) return;
            if (packet != null) PacketProcessor.enqueue(player, session, packet);
        });
    }

    static Packet decode(Player player, int opcode, ByteBuffer payload) {
        return Decoders578.decode(player, opcode, payload);
    }
}
