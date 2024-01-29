package bytebuffer

import bytebuffer.ByteOrder

interface PositionBuffer {
    val byteOrder: ByteOrder
    fun setLimit(limit: Int)
    fun limit(): Int
    fun position(): Int
    fun position(newPosition: Int)
    fun remaining() = limit() - position()
    fun hasRemaining() = position() < limit()
}
