package core.game.world.update.flag.context

/** Additional sequences applied to the client's 12 animation slots, not the primary animation. */
data class LayeredSequence(val sequenceId: Int, val delay: Int, val slotMask: Int) {
    init {
        require(sequenceId in -1..65534 && delay in 0..255 && slotMask in 1..0xfff)
    }
}

class AnimationSequenceUpdate(entries: List<LayeredSequence>) {
    val entries: List<LayeredSequence> = entries.toList()
    init { require(this.entries.size in 1..255) }
}
