package core.cache.def.impl

import core.api.getVarbit
import core.cache.Cache
import core.game.node.entity.player.Player
import java.nio.ByteBuffer

class VarbitDefinition(val id: Int) {
    var varpId = 0
        private set
    var startBit = 0
        private set
    var endBit = 0
        private set
    constructor(varpId: Int, id: Int, startBit: Int, endBit: Int) : this(id) {
        this.varpId = varpId
        this.startBit = startBit
        this.endBit = endBit
    }
    internal fun decode(buffer: ByteBuffer) {
        while (true) {
            when (val opcode = buffer.get().toInt() and 0xff) {
                0 -> return
                1 -> {
                    varpId = buffer.short.toInt() and 0xffff
                    startBit = buffer.get().toInt() and 0xff
                    endBit = buffer.get().toInt() and 0xff
                }
                else -> error("Varbit $id: unknown opcode $opcode at ${buffer.position() - 1}")
            }
        }
    }

    val mask: Int get() = (startBit..endBit).fold(0) { value, bit -> value or (1 shl (bit - startBit)) }

    fun getValue(player: Player): Int = getVarbit(player, id)

    override fun toString(): String =
    "ConfigFileDefinition [id=$id, configId=$varpId, bitShift=$startBit, bitSize=$endBit]"

    companion object {
        private val definitions = HashMap<Int, VarbitDefinition>()

        @JvmStatic
        fun exists(id: Int): Boolean = id >= 0 && Cache.getIndexes().getOrNull(22)?.validFile(id ushr 10, id and 0x3FF) == true

        @JvmStatic fun forSceneryId(id: Int): VarbitDefinition = forId(id)
        @JvmStatic fun forNPCId(id: Int): VarbitDefinition = forId(id)
        @JvmStatic fun forItemId(id: Int): VarbitDefinition = forId(id)
        @JvmStatic
        fun forId(id: Int): VarbitDefinition = definitions.getOrPut(id) {
            VarbitDefinition(id).apply {
                val data = Cache.getIndexes()[22]!!.getFileData(id ushr 10, id and 0x3FF)
                if (data != null) {
                    decode(ByteBuffer.wrap(data))
                }
            }
        }

        @JvmStatic
        fun create(varpId: Int, varbitId: Int, startBit: Int, endBit: Int) {
            definitions[varbitId] = VarbitDefinition(varpId, varbitId, startBit, endBit)
        }

        @JvmStatic fun getMapping(): MutableMap<Int, VarbitDefinition> = definitions
    }
}
