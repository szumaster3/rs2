package core.api

import core.cache.Cache
import core.game.node.entity.Entity
import core.game.world.update.flag.EntityFlag
import core.game.world.update.flag.context.AnimationSequenceUpdate
import core.game.world.update.flag.context.LayeredSequence
import core.game.world.update.flag.context.ProgressBar

/** Client cycles (30 per server tick); returns false if the entity's masks are frozen for rendering. */
fun sendEntityTimerBar(entity: Entity, durationCycles: Int, holdCycles: Int = 0,
                       stepCycles: Int = 1, alternate: Boolean = false): Boolean {
    if (durationCycles !in 1..0x7fff || holdCycles !in 0..255 || stepCycles !in 0..255) return false
    return entity.updateMasks.register(EntityFlag.ProgressBar, ProgressBar(durationCycles, holdCycles, stepCycles, alternate))
}

/** Send additional animation slots without replacing the server's primary animation. */
fun sendLayeredSequences(entity: Entity, entries: List<LayeredSequence>): Boolean {
    if (entries.size !in 1..255) return false
    // SeqTypeList uses index 20, group=id>>7, file=id&127.
    val index = Cache.getIndexes().getOrNull(20) ?: return false
    if (entries.any { it.sequenceId != -1 && !index.validFile(it.sequenceId ushr 7, it.sequenceId and 127) }) return false
    return entity.updateMasks.register(EntityFlag.AnimSeq, AnimationSequenceUpdate(entries))
}
