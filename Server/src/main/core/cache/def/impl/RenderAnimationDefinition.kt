package core.cache.def.impl

import core.api.log
import core.cache.Cache
import core.tools.Log
import java.nio.ByteBuffer

class RenderAnimationDefinition {
    @JvmField var turn180Animation: Int = -1
    @JvmField var anInt951: Int = -1
    @JvmField var anInt952: Int = -1
    @JvmField var turnCWAnimation: Int = -1
    @JvmField var anInt954: Int = -1
    @JvmField var anInt955: Int = -1
    @JvmField var anInt956: Int = 0
    @JvmField var anInt957: Int = -1
    @JvmField var anInt958: Int = -1
    @JvmField var anIntArray959: IntArray? = null
    @JvmField var anInt960: Int = -1
    @JvmField var anInt961: Int = 0
    @JvmField var anInt962: Int = -1
    @JvmField var walkAnimationId: Int = -1
    @JvmField var anInt964: Int = -1
    @JvmField var anInt965: Int = 0
    @JvmField var anInt966: Int = -1
    @JvmField var standAnimationIds: IntArray? = null
    @JvmField var anInt969: Int = 0
    @JvmField var anIntArray971: IntArray? = null
    @JvmField var standAnimationId: Int = -1
    @JvmField var anInt973: Int = 0
    @JvmField var anInt974: Int = -1
    @JvmField var anInt975: Int = 0
    @JvmField var runAnimationId: Int = -1
    @JvmField var anInt977: Int = -1
    @JvmField var aBoolean978: Boolean = true
    @JvmField var anIntArrayArray979: Array<IntArray?>? = null
    @JvmField var anInt980: Int = 0
    @JvmField var turnCCWAnimation: Int = -1
    @JvmField var anInt983: Int = -1
    @JvmField var anInt985: Int = -1
    @JvmField var anInt986: Int = -1
    @JvmField var anInt987: Int = -1
    @JvmField var anInt988: Int = 0
    @JvmField var anInt989: Int = -1
    @JvmField var anInt990: Int = -1
    @JvmField var anInt992: Int = 0
    @JvmField var anInt993: Int = 0
    @JvmField var anInt994: Int = 0
    private fun parse(buffer: ByteBuffer) {
        while (true) {
            val opcode = buffer.get().toInt() and 0xff
            if (opcode == 0) return
            parseOpcode(buffer, opcode)
        }
    }

    private fun parseOpcode(buffer: ByteBuffer, opcode: Int) {
        when (opcode) {
            1 -> {
                standAnimationId = (buffer.short.toInt() and 0xffff).let { if (it == 65535) -1 else it }
                walkAnimationId = (buffer.short.toInt() and 0xffff).let { if (it == 65535) -1 else it }
            }
            2 -> anInt974 = buffer.short.toInt() and 0xffff
            3 -> anInt987 = buffer.short.toInt() and 0xffff
            4 -> anInt986 = buffer.short.toInt() and 0xffff
            5 -> anInt977 = buffer.short.toInt() and 0xffff
            6 -> runAnimationId = buffer.short.toInt() and 0xffff
            7 -> anInt960 = buffer.short.toInt() and 0xffff
            8 -> anInt985 = buffer.short.toInt() and 0xffff
            9 -> anInt957 = buffer.short.toInt() and 0xffff
            26 -> {
                anInt973 = (4 * buffer.get().toInt()) and 0xff
                anInt975 = buffer.get().toInt() and (0xff * 4)
            }
            27 -> {
                val transforms = anIntArrayArray979 ?: arrayOfNulls<IntArray>(12).also { anIntArrayArray979 = it }
                val index = buffer.get().toInt() and 0xff
                transforms[index] = IntArray(6) { buffer.short.toInt() }
            }
            28 -> anIntArray971 = IntArray(12) {
                (buffer.get().toInt() and 0xff).let { if (it == 255) -1 else it }
            }
            29 -> anInt992 = buffer.get().toInt() and 0xff
            31 -> anInt988 = buffer.get().toInt() and 0xff
            34 -> anInt993 = buffer.get().toInt() and 0xff
            37 -> anInt951 = buffer.get().toInt() and 0xff
            30 -> anInt980 = buffer.short.toInt() and 0xffff
            32 -> anInt961 = buffer.short.toInt() and 0xffff
            35 -> anInt969 = buffer.short.toInt() and 0xffff
            38 -> anInt958 = buffer.short.toInt() and 0xffff
            39 -> anInt954 = buffer.short.toInt() and 0xffff
            40 -> turn180Animation = buffer.short.toInt() and 0xffff
            41 -> turnCWAnimation = buffer.short.toInt() and 0xffff
            42 -> turnCCWAnimation = buffer.short.toInt() and 0xffff
            44 -> anInt955 = buffer.short.toInt() and 0xffff
            45 -> anInt964 = buffer.short.toInt() and 0xffff
            46 -> anInt983 = buffer.short.toInt() and 0xffff
            47 -> anInt966 = buffer.short.toInt() and 0xffff
            48 -> anInt989 = buffer.short.toInt() and 0xffff
            49 -> anInt952 = buffer.short.toInt() and 0xffff
            50 -> anInt990 = buffer.short.toInt() and 0xffff
            51 -> anInt962 = buffer.short.toInt() and 0xffff
            33 -> anInt956 = buffer.short.toInt()
            36 -> anInt965 = buffer.short.toInt()
            43 -> buffer.short
            52 -> {
                val count = buffer.get().toInt() and 0xff
                val weights = IntArray(count)
                val animations = IntArray(count)
                repeat(count) { index ->
                    animations[index] = buffer.short.toInt() and 0xffff
                    weights[index] = buffer.get().toInt() and 0xff
                    anInt994 += weights[index]
                }
                anIntArray959 = weights
                standAnimationIds = animations
            }
            53 -> aBoolean978 = false
            54 -> repeat(2) { buffer.get() }
            55 -> { buffer.get(); buffer.short }
            56 -> { buffer.get(); repeat(3) { buffer.short } }
        }
    }

    companion object {

        @JvmStatic
        fun forId(animId: Int): RenderAnimationDefinition? {
            if (animId == -1) {
                return null
            }
            val data = Cache.getIndexes()[2]!!.getFileData(32, animId, null)
            val defs = RenderAnimationDefinition()
            if (data != null) {
                defs.parse(ByteBuffer.wrap(data))
            } else {
                log(RenderAnimationDefinition::class.java, Log.ERR, "No definitions found for render animation " + animId + ", size=" + Cache.getIndexes()[2]!!.getFilesSize(32) + "!")
            }
            return defs
        }
    }
}
