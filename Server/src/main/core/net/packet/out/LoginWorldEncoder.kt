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
    // Static353.anIntArray449[0] = 104 tiles (13 x 13 chunks).
    private const val SCENE_RADIUS = 6
    private const val SCENE_CHUNKS = 13
    private const val REGION_HEADER_SIZE = 6
    private const val XTEA_SIZE = 16

    private fun validateLocation(location: Location) {
        require(location.x in 0..16383 && location.y in 0..16383 && location.z in 0..3) {
            "Invalid scene location: $location"
        }
    }

    private fun keys(id: Int): IntArray = XteaParser.getRegionXTEA(id).also {
        require(it.size == 4) { "Invalid XTEA key count for map square $id" }
    }

    private fun normalMapSquares(location: Location): List<Int> {
        validateLocation(location)
        val result = ArrayList<Int>(9)
        // Match Static181.rebuildMap(false), including integer division at world edges.
        for (x in (location.regionX - SCENE_RADIUS) / 8..(location.regionX + SCENE_RADIUS) / 8) {
            for (z in (location.regionY - SCENE_RADIUS) / 8..(location.regionY + SCENE_RADIUS) / 8) {
                require(x in 0..255 && z in 0..255) { "Map square outside packed region range: $x,$z" }
                result.add((x shl 8) or z)
            }
        }
        return result
    }

    private fun requirePayloadLength(length: Int) {
        require(length in 0..ServerProt.MAX_PAYLOAD_SIZE) {
            "Scene payload requires $length bytes; client capacity is ${ServerProt.MAX_PAYLOAD_SIZE}"
        }
    }

    fun response(index: Int, location: Location, rights: Int, reconnect: Boolean, cipher: ISAACCipher): ByteBuffer {
        require(index in 1..2047)
        validateLocation(location)
        val payload = IoBuffer(-1, PacketHeader.SHORT, ByteBuffer.allocate(ServerProt.MAX_PAYLOAD_SIZE))
        payload.setBitAccess()
        payload.putBits(30, (location.z shl 28) or ((location.x and 0x3fff) shl 14) or (location.y and 0x3fff))
        for (slot in 1..2047) if (slot != index) payload.putBits(18, 0)
        payload.setByteAccess()
        if (!reconnect) appendRegion(payload.toByteBuffer(), location)
        requirePayloadLength(payload.toByteBuffer().position())
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
        val mapKeys = normalMapSquares(location).map(::keys)
        val regionLength = REGION_HEADER_SIZE + XTEA_SIZE * mapKeys.size
        requirePayloadLength(buffer.position() + regionLength)
        require(buffer.remaining() >= regionLength) { "Insufficient normal scene buffer capacity" }
        buffer.order(ByteOrder.LITTLE_ENDIAN).putShort(location.regionX.toShort()).order(ByteOrder.BIG_ENDIAN)
        buffer.putShort(location.regionY.toShort()).put(0).put((-1).toByte())
        for (key in mapKeys) key.forEach(buffer::putInt)
    }

    fun dynamicRegion(location: Location, chunks: Array<Array<Array<RegionChunk?>>>): IoBuffer {
        validateLocation(location)
        require(chunks.size == 4 && chunks.all { plane ->
            plane.size == SCENE_CHUNKS && plane.all { it.size == SCENE_CHUNKS }
        })
        // Static181.rebuildMap(true) reads all 676 presence bits, each present
        // 26-bit template, then one key per distinct map square in encounter order.
        // Plan the complete frame first: no chunk can be dropped to make it fit.
        val templates = IntArray(4 * SCENE_CHUNKS * SCENE_CHUNKS) { -1 }
        val mapSquares = linkedSetOf<Int>()
        var index = 0
        var present = 0
        for (plane in chunks) for (column in plane) for (chunk in column) {
            val slot = index++
            if (chunk == null) continue
            val base = Location.create(chunk.base)
            if (base.x < 0 || base.y < 0) continue
            val x = base.regionX
            val z = base.regionY
            val rotation = chunk.rotation
            require(x in 0..1023 && z in 0..2047 && base.z in 0..3 && rotation in 0..3) {
                "Dynamic chunk cannot be represented by revision 578: $x,$z"
            }
            templates[slot] = (base.z shl 24) or (x shl 14) or (z shl 3) or (rotation shl 1)
            mapSquares.add(((x shr 3) shl 8) or (z shr 3))
            present++
        }
        val length = REGION_HEADER_SIZE + (templates.size + 26 * present + 7) / 8 + XTEA_SIZE * mapSquares.size
        requirePayloadLength(length)
        val mapKeys = mapSquares.map(::keys)
        val out = IoBuffer(ServerProt.REBUILD_REGION, PacketHeader.SHORT, ByteBuffer.allocate(length))
        out.putShort(location.regionX).putLEShort(location.regionY).putC(0).putC(1)
        out.setBitAccess()
        for (template in templates) {
            if (template == -1) out.putBits(1, 0)
            else out.putBits(1, 1).putBits(26, template)
        }
        out.setByteAccess()
        for (key in mapKeys) key.forEach(out::putInt)
        check(out.toByteBuffer().position() == length) { "Dynamic scene length mismatch" }
        return out
    }
}
