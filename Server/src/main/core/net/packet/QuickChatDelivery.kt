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

    private fun incoming(opcode: Int, sender: Player, clanName: String? = null): IoBuffer {
        val id = sequence.incrementAndGet()
        // Client combines the high short with the low medium using an int shift.
        val buffer = IoBuffer(opcode, PacketHeader.BYTE).put(0).putString(sender.name)
        if (clanName != null) buffer.putLong(stringToLong(clanName))
        return buffer.putShort((id ushr 24) shl 8).putTri(id and 0xffffff).put(Rights.getChatIcon(sender))
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
        if (target.name.length + 1 + payload.size > 255 || sender.name.length + 8 + payload.size > 255) return
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
        if (sender.name.length + 16 + payload.size > 255) return
        sender.locks.lock("cc_message", 1)
        PlayerMonitor.logChat(sender, "clan", "Quick Chat phrase ${phraseId(payload)}")
        for (entry in clan.players) {
            val target = entry.player ?: continue
            if (target.communication.blocked.contains(sender.name)) continue
            target.session.write(incoming(51, sender, clan.name).putBytes(payload, 0, payload.size))
        }
    }

    private fun phraseId(payload: ByteArray): Int = ((payload[0].toInt() and 255) shl 8) or (payload[1].toInt() and 255)
}
