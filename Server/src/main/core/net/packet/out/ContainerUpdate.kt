package core.net.packet.out

import core.game.container.ContainerEvent
import core.game.node.item.Item
import core.net.packet.IoBuffer
import core.net.packet.PacketHeader
import core.net.packet.ServerProt

/** Protocol.java PACKET_69 / PACKET_100, Static62.method2391. */
internal object ContainerUpdate {
    data class Entry(val id: Int, val amount: Int)

    // Item() exposes 65535 via the unsigned getId(), despite its documented -1 ID.
    fun snapshot(item: Item?): Entry? = if (item == null || item.id == 65535 || item === ContainerEvent.NULL_ITEM) null
        else Entry(item.id, item.amount)

    fun encode(key: Int, split: Boolean, entries: List<Entry?>?, count: Int,
               slots: IntArray? = null, ids: IntArray? = null): IoBuffer? {
        if (key !in 0..65535) return null
        fun fullEntry(slot: Int): Entry? = if (ids != null) {
            val id = ids.getOrNull(slot) ?: -1
            if (id == -1 || id == 65535) null else Entry(id, 1)
        } else entries?.getOrNull(slot)
        fun valid(entry: Entry?) = entry == null || (entry.id in 0..65534 && entry.amount >= 0)
        fun amountSize(entry: Entry?) = if ((entry?.amount ?: 0) >= 255) 5 else 1
        var length = if (slots == null) 5 else 3
        if (slots == null) {
            if (count !in 0..65535) return null
            for (slot in 0 until count) {
                val entry = fullEntry(slot)
                if (!valid(entry)) return null
                length += 2 + amountSize(entry)
                if (length > 65535) return null
            }
        } else {
            if (entries == null || slots.isEmpty()) return null
            for (slot in slots) {
                if (slot !in 0..32767 || slot !in entries.indices) return null
                val entry = entries[slot]
                if (!valid(entry)) return null
                length += (if (slot < 128) 1 else 2) + 2 + (if (entry == null) 0 else amountSize(entry))
                if (length > 65535) return null
            }
        }
        val buffer = IoBuffer(if (slots == null) ServerProt.UPDATE_INV_FULL else ServerProt.UPDATE_INV_PARTIAL, PacketHeader.SHORT)
        buffer.putShort(key).put(if (split) 1 else 0)
        if (slots == null) {
            buffer.putShort(count)
            for (slot in 0 until count) {
                val entry = fullEntry(slot)
                buffer.putLEShort((entry?.id ?: -1) + 1)
                val amount = entry?.amount ?: 0
                if (amount >= 255) buffer.put(255).putIntB(amount) else buffer.put(amount)
            }
        } else {
            for (slot in slots) {
                val entry = entries!![slot]
                buffer.putSmart(slot).putShort((entry?.id ?: -1) + 1)
                if (entry != null) {
                    if (entry.amount >= 255) buffer.put(255).putInt(entry.amount) else buffer.put(entry.amount)
                }
            }
        }
        return buffer
    }
}
