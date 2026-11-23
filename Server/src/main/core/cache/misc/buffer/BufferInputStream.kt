package core.cache.misc.buffer

import java.io.InputStream
import java.nio.ByteBuffer

class BufferInputStream(val buffer: ByteBuffer) : InputStream() {

    override fun read(): Int = if (buffer.hasRemaining()) buffer.get().toInt() and 0xFF else -1
}
