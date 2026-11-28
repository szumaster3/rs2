package core.cache.def.impl

import core.api.log
import core.cache.Cache
import core.tools.Log
import java.nio.ByteBuffer

class BasType {
    @JvmField var walkBackAnimationId: Int = -1
    @JvmField var opcode37Value: Int = -1
    @JvmField var walkTurnLeftAnimationId: Int = -1
    @JvmField var walkLeftAnimationId: Int = -1
    @JvmField var idleTurnRightAnimationId: Int = -1
    /** BAS opcode 44, read by the client overhead timer bar renderer. */
    var timerBarSpriteId: Int = -1
    @JvmField var opcode33Value: Int = 0
    @JvmField var runRightAnimationId: Int = -1
    @JvmField var idleTurnLeftAnimationId: Int = -1
    @JvmField var standAnimationWeights: IntArray? = null
    @JvmField var runBackAnimationId: Int = -1
    @JvmField var opcode32Value: Int = 0
    @JvmField var opcode51Value: Int = -1
    @JvmField var walkAnimationId: Int = -1
    @JvmField var crawlTurnLeftAnimationId: Int = -1
    @JvmField var opcode36Value: Int = 0
    @JvmField var runTurnLeftAnimationId: Int = -1
    @JvmField var standAnimationIds: IntArray? = null
    @JvmField var opcode35Value: Int = 0
    @JvmField var equipmentSlotMapping: IntArray? = null
    @JvmField var standAnimationId: Int = -1
    @JvmField var terrainAlignmentWidth: Int = 0
    @JvmField var crawlAnimationId: Int = -1
    @JvmField var terrainAlignmentLength: Int = 0
    @JvmField var runAnimationId: Int = -1
    @JvmField var crawlRightAnimationId: Int = -1
    @JvmField var characterShadowEnabled: Boolean = true
    @JvmField var equipmentTransforms: Array<IntArray?>? = null
    @JvmField var opcode30Value: Int = 0
    @JvmField var walkRightAnimationId: Int = -1
    @JvmField var crawlTurnRightAnimationId: Int = -1
    @JvmField var runLeftAnimationId: Int = -1
    @JvmField var crawlLeftAnimationId: Int = -1
    @JvmField var crawlBackAnimationId: Int = -1
    @JvmField var opcode31Value: Int = 0
    @JvmField var runTurnRightAnimationId: Int = -1
    @JvmField var walkTurnRightAnimationId: Int = -1
    @JvmField var opcode29Value: Int = 0
    @JvmField var opcode34Value: Int = 0
    @JvmField var totalStandAnimationWeight: Int = 0
    /** BAS opcode 43: retained even though its effect is not established in this client. */
    @JvmField var opcode43Value: Int = -1

    internal fun parse(buffer: ByteBuffer) {
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
            2 -> crawlAnimationId = buffer.short.toInt() and 0xffff
            3 -> crawlBackAnimationId = buffer.short.toInt() and 0xffff
            4 -> crawlLeftAnimationId = buffer.short.toInt() and 0xffff
            5 -> crawlRightAnimationId = buffer.short.toInt() and 0xffff
            6 -> runAnimationId = buffer.short.toInt() and 0xffff
            7 -> runBackAnimationId = buffer.short.toInt() and 0xffff
            8 -> runLeftAnimationId = buffer.short.toInt() and 0xffff
            9 -> runRightAnimationId = buffer.short.toInt() and 0xffff
            26 -> {
                terrainAlignmentWidth = 4 * (buffer.get().toInt() and 0xff)
                terrainAlignmentLength = 4 * (buffer.get().toInt() and 0xff)
            }
            27 -> {
                val transforms = equipmentTransforms ?: arrayOfNulls<IntArray>(12).also { equipmentTransforms = it }
                val index = buffer.get().toInt() and 0xff
                transforms[index] = IntArray(6) { buffer.short.toInt() }
            }
            28 -> equipmentSlotMapping = IntArray(12) {
                (buffer.get().toInt() and 0xff).let { if (it == 255) -1 else it }
            }
            29 -> opcode29Value = buffer.get().toInt() and 0xff
            31 -> opcode31Value = buffer.get().toInt() and 0xff
            34 -> opcode34Value = buffer.get().toInt() and 0xff
            37 -> opcode37Value = buffer.get().toInt() and 0xff
            30 -> opcode30Value = buffer.short.toInt() and 0xffff
            32 -> opcode32Value = buffer.short.toInt() and 0xffff
            35 -> opcode35Value = buffer.short.toInt() and 0xffff
            38 -> idleTurnLeftAnimationId = buffer.short.toInt() and 0xffff
            39 -> idleTurnRightAnimationId = buffer.short.toInt() and 0xffff
            40 -> walkBackAnimationId = buffer.short.toInt() and 0xffff
            41 -> walkLeftAnimationId = buffer.short.toInt() and 0xffff
            42 -> walkRightAnimationId = buffer.short.toInt() and 0xffff
            44 -> timerBarSpriteId = buffer.short.toInt() and 0xffff
            45 -> crawlTurnLeftAnimationId = buffer.short.toInt() and 0xffff
            46 -> crawlTurnRightAnimationId = buffer.short.toInt() and 0xffff
            47 -> runTurnLeftAnimationId = buffer.short.toInt() and 0xffff
            48 -> runTurnRightAnimationId = buffer.short.toInt() and 0xffff
            49 -> walkTurnLeftAnimationId = buffer.short.toInt() and 0xffff
            50 -> walkTurnRightAnimationId = buffer.short.toInt() and 0xffff
            51 -> opcode51Value = buffer.short.toInt() and 0xffff
            33 -> opcode33Value = buffer.short.toInt()
            36 -> opcode36Value = buffer.short.toInt()
            43 -> opcode43Value = buffer.short.toInt() and 0xffff
            52 -> {
                val count = buffer.get().toInt() and 0xff
                val weights = IntArray(count)
                val animations = IntArray(count)
                repeat(count) { index ->
                    animations[index] = buffer.short.toInt() and 0xffff
                    weights[index] = buffer.get().toInt() and 0xff
                    totalStandAnimationWeight += weights[index]
                }
                standAnimationWeights = weights
                standAnimationIds = animations
            }
            53 -> characterShadowEnabled = false
            else -> error("Unknown BAS opcode $opcode at ${buffer.position() - 1}")
        }
    }

    companion object {

        @JvmStatic
        fun forId(animId: Int): BasType? {
            if (animId == -1) {
                return null
            }
            val data = Cache.getIndexes()[2]!!.getFileData(32, animId, null)
            val defs = BasType()
            if (data != null) {
                val buffer = ByteBuffer.wrap(data)
                defs.parse(buffer)
                check(!buffer.hasRemaining()) { "Trailing bytes in BAS $animId" }
            } else {
                log(BasType::class.java, Log.ERR, "No definitions found for render animation " + animId + ", size=" + Cache.getIndexes()[2]!!.getFilesSize(32) + "!")
            }
            return defs
        }
    }
}
