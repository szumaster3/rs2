package core.net.packet

import core.game.node.entity.player.Player
import core.game.node.entity.player.info.Rights
import core.game.node.entity.player.info.PlayerMonitor
import core.game.system.communication.CommunicationInfo
import core.game.world.repository.Repository
import core.net.amsc.WorldCommunicator
import core.tools.StringUtils.stringToLong
import java.util.concurrent.atomic.AtomicInteger

/**
 * Native delivery for local-world Quick Chat.
 */
object QuickChatDelivery {
    private val sequence = AtomicInteger()

    /** The 578 client combines the high short using an int shift (32 becomes 0). */
    fun putMessageId(buffer: IoBuffer): IoBuffer {
        val id = sequence.updateAndGet { if (it == -1) 1 else it + 1 }
        return buffer.putShort((id ushr 24) shl 8).putTri(id and 0xffffff)
    }

    private fun incoming(opcode: Int, sender: Player, clanName: String? = null): IoBuffer {
        val buffer = IoBuffer(opcode, PacketHeader.BYTE).put(0).putString(sender.name)
        if (clanName != null) buffer.putLong(stringToLong(clanName))
        return putMessageId(buffer).put(Rights.getChatIcon(sender))
    }

    fun privateMessage(sender: Player, recipient: String?, payload: ByteArray) {
        if (sender.details.isMuted) return
        val target = Repository.getPlayerByName(recipient)
        if (target == null && WorldCommunicator.isEnabled()) {
            sender.sendMessage("Cross-world Quick Chat is not yet supported.")
            return
        }
        if (target == null || !CommunicationInfo.showActive(sender, target)) {
            sender.sendMessage("That player is not available.")
            return
        }
        if (payload.size !in 2..255 || target.name.contains('\u0000') || sender.name.contains('\u0000') ||
            target.name.length + 1 + payload.size > 255 || sender.name.length + 8 + payload.size > 255) return
        sender.session.write(IoBuffer(43, PacketHeader.BYTE).putString(target.name).putBytes(payload, 0, payload.size))
        target.session.write(incoming(105, sender).putBytes(payload, 0, payload.size))
        target.setAttribute("replyTo", sender.name)
        PlayerMonitor.logPrivateChat(sender, target.name, "Quick Chat phrase ${phraseId(payload)}")
    }

    fun clan(sender: Player, payload: ByteArray) {
        if (sender.details.isMuted) return
        if (WorldCommunicator.isEnabled()) {
            sender.sendMessage("Cross-world Quick Chat is not yet supported.")
            return
        }
        val clan = sender.communication.clan ?: return
        if (sender.locks.isLocked("cc_message") || clan.isBanned(sender.name)) return
        if (sender.name != clan.owner && sender.details.rights != Rights.ADMINISTRATOR &&
            clan.getRank(sender).ordinal < clan.messageRequirement.ordinal) {
            sender.sendMessage("You do not have a high enough rank to talk in this clan channel.:clan:")
            return
        }
        if (payload.size !in 2..255 || sender.name.contains('\u0000') || sender.name.length + 16 + payload.size > 255) return
        sender.locks.lock("cc_message", 1)
        PlayerMonitor.logChat(sender, "clan", "Quick Chat phrase ${phraseId(payload)}")
        for (entry in clan.players) {
            val target = entry.player ?: continue
            if (target.communication.isIgnoring(sender.name)) continue
            target.session.write(incoming(51, sender, clan.name).putBytes(payload, 0, payload.size))
        }
    }

    private fun phraseId(payload: ByteArray): Int = ((payload[0].toInt() and 255) shl 8) or (payload[1].toInt() and 255)
}
