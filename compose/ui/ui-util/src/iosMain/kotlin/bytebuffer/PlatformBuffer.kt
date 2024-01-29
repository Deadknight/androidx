package bytebuffer

import com.ditchoom.buffer.SuspendCloseable

interface PlatformBuffer : ReadBuffer, WriteBuffer, SuspendCloseable {
    val capacity: Int

    companion object
}
