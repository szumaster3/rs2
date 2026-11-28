package core.net.packet.out

import core.net.packet.ServerProt

import core.cache.crypto.ISAACCipher
import core.game.system.config.XteaParser
import core.game.world.map.Location
import core.game.world.map.RegionChunk
import core.net.packet.IoBuffer
import core.net.packet.PacketHeader
import java.nio.ByteBuffer
import java.nio.ByteOrder

object LoginWorldEncoder {
    fun response(index: Int, location: Location, rights: Int, reconnect: Boolean, cipher: ISAACCipher): ByteBuffer {
        require(index in 1..2047)
        val payload = IoBuffer(-1, PacketHeader.SHORT)
        payload.setBitAccess()
        payload.putBits(30, (location.z shl 28) or ((location.x and 0x3fff) shl 14) or (location.y and 0x3fff))
        for (slot in 1..2047) if (slot != index) payload.putBits(18, 0)
        payload.setByteAccess()
        if (!reconnect) appendRegion(payload.toByteBuffer(), location)
        val data = payload.toByteBuffer().flip()
        val frame = ByteBuffer.allocate(data.remaining() + if (reconnect) 3 else 14)
        if (reconnect) {
            frame.put(15)
        } else {
            frame.put(2).put(rights.toByte()).put(0)
            frame.put(0).put(0).put(1).put(0)
            frame.putShort(index.toShort()).put(1).put(1)
            val isaac = cipher.nextValue
            val encryptedOpcode = (ServerProt.REBUILD_NORMAL + isaac) and 0xFF
            frame.put(encryptedOpcode.toByte())
        }
        return frame.putShort(data.remaining().toShort()).put(data).flip()
    }

    fun appendRegion(buffer: ByteBuffer, location: Location) {
        val zoneX = location.regionX
        val zoneY = location.regionY
        buffer.order(ByteOrder.LITTLE_ENDIAN).putShort(zoneX.toShort()).order(ByteOrder.BIG_ENDIAN)
        buffer.putShort(zoneY.toShort()).put(0).put((-1).toByte())
        for (x in (zoneX - 6) / 8..(zoneX + 6) / 8) {
            for (y in (zoneY - 6) / 8..(zoneY + 6) / 8) {
                XteaParser.getRegionXTEA((x shl 8) or y).forEach(buffer::putInt)
            }
        }
    }

    fun dynamicRegion(location: Location, chunks: Array<Array<Array<RegionChunk?>>>): IoBuffer {
        require(chunks.size == 4 && chunks.all { plane -> plane.size == 13 && plane.all { it.size == 13 } })
        val out = IoBuffer(ServerProt.REBUILD_REGION, PacketHeader.SHORT)
        out.putShort(location.regionX).putLEShort(location.regionY).putC(0).putC(1)
        val mapSquares = linkedSetOf<Int>()
        out.setBitAccess()
        for (plane in chunks) for (column in plane) for (chunk in column) {
            if (chunk == null || chunk.base.x < 0 || chunk.base.y < 0) {
                out.putBits(1, 0)
                continue
            }
            val x = chunk.base.regionX
            val z = chunk.base.regionY
            require(x in 0..1023 && z in 0..2047 && chunk.base.z in 0..3 && chunk.rotation in 0..3) {
                "Dynamic chunk cannot be represented by revision 578: $x,$z"
            }
            out.putBits(1, 1).putBits(2, chunk.base.z).putBits(10, x)
                .putBits(11, z).putBits(2, chunk.rotation).putBits(1, 0)
            mapSquares.add(((x shr 3) shl 8) or (z shr 3))
        }
        out.setByteAccess()
        for (id in mapSquares) XteaParser.getRegionXTEA(id).forEach(out::putInt)
        return out
    }
}
