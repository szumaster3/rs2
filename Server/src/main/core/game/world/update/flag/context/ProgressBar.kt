package core.game.world.update.flag.context

/** Client overhead progress bar; duration and hold are client cycles, not game ticks. */
data class ProgressBar(val duration: Int, val hold: Int, val step: Int, val alternate: Boolean = false) {
    init {
        require(duration in 1..0x7fff && hold in 0..255 && step in 0..255)
    }
    val packed: Int get() = duration or (if (alternate) 0x8000 else 0)
}
