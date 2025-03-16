package world.gregs.voidps.buffer

import io.ktor.utils.io.*
import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.remaining
import io.ktor.utils.io.writeShort
import kotlinx.io.Source
import kotlinx.io.readUByte
import org.darkan.core.currentTimeTicks
import org.darkan.core.hashToShort
import world.gregs.voidps.buffer.write.BufferWriter
import kotlin.text.toByteArray

suspend fun ByteWriteChannel.writeBoolean(value: Boolean) = writeByte(if (value) 1 else 0)

suspend fun ByteWriteChannel.writeBooleanAdd(value: Boolean) = writeByteAdd(if (value) 1 else 0)

suspend fun ByteWriteChannel.writeBooleanSubtract(value: Boolean) = writeByteSubtract(if (value) 1 else 0)

suspend fun ByteWriteChannel.writeBooleanInverse(value: Boolean) = writeByteInverse(if (value) 1 else 0)

suspend fun ByteWriteChannel.writeByte(value: Int) = writeByte(value.toByte())

suspend fun ByteWriteChannel.writeShort(value: Int) = writeShort(value.toShort())

suspend fun ByteWriteChannel.writeByteAdd(value: Boolean) = writeByteAdd(if (value) 1 else 0)

suspend fun ByteWriteChannel.writeByteInverse(value: Boolean) = writeByteInverse(if (value) 1 else 0)

suspend fun ByteWriteChannel.writeByteAdd(value: Int) = writeByte(value + 128)

suspend fun ByteWriteChannel.writeByteInverse(value: Int) = writeByte(-value)

suspend fun ByteWriteChannel.writeByteSubtract(value: Int) = writeByte(-value + 128)

suspend fun ByteWriteChannel.writeBytes(value: ByteArray) = writeFully(value)

suspend fun ByteWriteChannel.writeShortAdd(value: Int) {
    writeByte(value shr 8)
    writeByteAdd(value)
}

suspend fun ByteWriteChannel.writeShortLittle(value: Int) = writeShort(value.toShort().reverseByteOrder())

suspend fun ByteWriteChannel.writeShortAddLittle(value: Int) {
    writeByteAdd(value)
    writeByte(value shr 8)
}

suspend fun ByteWriteChannel.writeIntMiddle(value: Int) {
    writeByte(value shr 8)
    writeByte(value)
    writeByte(value shr 24)
    writeByte(value shr 16)
}

suspend fun ByteWriteChannel.writeIntInverse(value: Int) {
    writeByte(value shr 8)
    writeByte(value shr 24)
    writeByte(value shr 16)
    writeByteInverse(value)
}

suspend fun ByteWriteChannel.writeIntLittle(value: Int) = writeInt(value.reverseByteOrder())

suspend fun ByteWriteChannel.writeIntInverseMiddle(value: Int) {
    writeByte(value shr 16)
    writeByte(value shr 24)
    writeByte(value)
    writeByte(value shr 8)
}

suspend fun ByteWriteChannel.writeMedium(value: Int) {
    writeByte(value shr 16)
    writeByte(value shr 8)
    writeByte(value)
}

suspend fun ByteWriteChannel.writeMediumReverseEnd(value: Int) {
    writeByte(value shr 16)
    writeByte(value)
    writeByte(value shr 8)
}

suspend fun ByteWriteChannel.writeSmart(value: Int) {
    if (value >= 128) {
        writeShort(value + 32768)
    } else {
        writeByte(value)
    }
}

suspend fun ByteWriteChannel.writeFlags(flags: Int) {
    var flags = flags
    while ((flags and 0x7F.inv()) != 0) {
        writeByte((flags and 0x7F) or 0x80)
        flags = flags ushr 7
    }
    writeByte(flags and 0x7F)
}

suspend fun ByteWriteChannel.writeString(value: String?) {
    if (value != null)
        writeFully(value.toByteArray())
    writeByte(0)
}

suspend fun ByteWriteChannel.writeJagString(value: String?) {
    writeByte(0)
    writeString(value)
}

/**
 * TODO
 * absolutely zero idea what this function actually should do
 * if anyone knows, please let me know
 */
suspend fun ByteWriteChannel.writeHashedMessageTimestamp(message: String) {
    writeShort(message.hashToShort())
    writeMedium((currentTimeTicks and 0xFFFFFF).toInt())
}

suspend fun ByteWriteChannel.writeHashedQCMessageTimestamp(messageId: Int) {
    writeShort(messageId.toShort())
    writeMedium((currentTimeTicks and 0xFFFFFF).toInt())
}

class BitAccessor {
    private var bitIndex = 0
    private val data = ByteArray(4096 * 2)

    fun writeBit(value: Boolean) = writeBits(1, if (value) 1 else 0)

    fun writeBits(count: Int, value: Int) {
        var numBits = count

        var byteIndex = bitIndex shr 3
        var bitOffset = 8 - (bitIndex and 7)
        bitIndex += numBits

        var tmp: Int
        var max: Int
        while (numBits > bitOffset) {
            tmp = data[byteIndex].toInt()
            max = BufferWriter.BIT_MASKS[bitOffset]
            tmp = tmp and max.inv() or (value shr numBits - bitOffset and max)
            data[byteIndex++] = tmp.toByte()
            numBits -= bitOffset
            bitOffset = 8
        }

        tmp = data[byteIndex].toInt()
        max = BufferWriter.BIT_MASKS[numBits]
        if (numBits == bitOffset) {
            tmp = tmp and max.inv() or (value and max)
        } else {
            tmp = tmp and (max shl bitOffset - numBits).inv()
            tmp = tmp or (value and max shl bitOffset - numBits)
        }
        data[byteIndex] = tmp.toByte()
    }

    suspend fun write(channel: ByteWriteChannel) {
        channel.writeFully(data, 0, (bitIndex + 7) / 8)
    }
}

suspend fun ByteWriteChannel.bitAccess(block: BitAccessor.() -> Unit) {
    val accessor = BitAccessor()
    block.invoke(accessor)
    accessor.write(this)
}

suspend fun ByteWriteChannel.respond(value: Int) {
    writeByte(value)
}

suspend fun ByteWriteChannel.finish(value: Int) {
    respond(value)
}

fun Source.readRSString(): String {
    val sb = StringBuilder()
    var b: Int
    while (remaining > 0) {
        b = readByte().toInt()
        if (b == 0) {
            break
        }
        sb.append(b.toChar())
    }
    return sb.toString()
}

fun Source.readJagString(): String {
    readByte()
    var s = ""
    var b: Int
    while ((readByte().toInt().also { b = it }) != 0)
        s += b.toChar()
    return s
}

fun Source.readFlags(): Int {
    var result = 0
    var shift = 0
    var currValue: Int
    do {
        currValue = readByte().toInt() and 0xFF
        result = result or ((currValue and 0x7F) shl shift)
        shift += 7
        if (shift >= 32 && (currValue and 0x80) != 0)
            error("Variable length quantity is too long")
    } while ((currValue and 0x80) != 0)
    return result
}

fun Source.readUByte(): Int = readByte().toInt() and 0xff

fun Source.readUShort(): Int = (readUByte().toInt() shl 8) or readUByte().toInt()

fun Source.readMedium() = (readByte().toInt() shl 16) + (readByte().toInt() shl 8) + readByte().toInt()

fun Source.readUMedium() = (readUByte().toInt() shl 16) + (readUByte().toInt() shl 8) + readUByte().toInt()

fun Source.read40BitLong() = (readByte().toLong() shl 32) + (readByte().toLong() shl 24) + (readByte().toLong() shl 16) + (readByte().toLong() shl 8) + readByte().toLong()

fun Source.read40BitULong() = (readUByte().toLong() shl 32) + (readUByte().toLong() shl 24) + (readUByte().toLong() shl 16) + (readUByte().toLong() shl 8) + readUByte().toLong()

fun Source.readBoolean(): Boolean = readByte().toInt() == 1

fun Source.readBooleanInverse() = readByteInverse() == 1

fun Source.readBooleanSubtract() = readByteSubtract() == 1

fun Source.readBooleanAdd() = readByteAdd() == 1

fun Source.readByteAdd(): Int = (readByte() - 128).toByte().toInt()

fun Source.readByteInverse(): Int = -readByte()

fun Source.readByteSubtract(): Int = (readByteInverse() + 128).toByte().toInt()

fun Source.readShortAdd(): Int = (readByte().toInt() shl 8) or readByteAdd()

fun Source.readShortAddLittle(): Int = ((readByte().toInt() - 128) and 0xff) or ((readByte().toInt() shl 8) and 0xff00)

fun Source.readUShortAdd(): Int = (readByte().toInt() shl 8) or ((readByte() - 128) and 0xff)

fun Source.readUShortLittle(): Int = readUByte().toInt() or (readUByte().toInt() shl 8)

fun Source.readShortLittle(): Int {
    val value = readUByte().toInt() or (readUByte().toInt() shl 8)
    if (value > 0x7FFF)
        return value - 0x10000
    return value
}

fun Source.readUShortAddLittle(): Int = (readByte() - 128 and 0xff) + (readByte().toInt() shl 8 and 0xff00)

fun Source.readUIntMiddle(): Int = (readUByte().toInt() shl 8) or readUByte().toInt() or (readUByte().toInt() shl 24) or (readUByte().toInt() shl 16)

fun Source.readIntInverseMiddle(): Int = (readByte().toInt() shl 16) or (readByte().toInt() shl 24) or readUByte().toInt() or (readByte().toInt() shl 8)

fun Source.readUIntInverseMiddle(): Int = (readUByte().toInt() shl 16) or (readUByte().toInt() shl 24) or readUByte().toInt() or (readUByte().toInt() shl 8)

fun Source.readUIntLittle(): Int = (readUByte().toInt()) or (readUByte().toInt() shl 8) or (readUByte().toInt() shl 16) or (readUByte().toInt() shl 24)

fun Source.readSmart(): Int {
    val peek = readUByte().toInt()
    return if (peek < 128) {
        peek and 0xFF
    } else {
        (peek shl 8 or readUByte().toInt()) - 32768
    }
}

suspend fun ByteReadChannel.readRSString(): String {
    val sb = StringBuilder()
    var b: Int
    while (true) {
        b = readByte().toInt()
        if (b == 0)
            break
        sb.append(b.toChar())
    }
    return sb.toString()
}

suspend fun ByteReadChannel.readJagString(): String {
    readByte()
    return readRSString()
}

suspend fun ByteReadChannel.readFlags(): Int {
    var result = 0
    var shift = 0
    var currValue: Int
    do {
        currValue = readByte().toInt() and 0xFF
        result = result or ((currValue and 0x7F) shl shift)
        shift += 7
        if (shift >= 32 && (currValue and 0x80) != 0)
            error("Variable length quantity is too long")
    } while ((currValue and 0x80) != 0)
    return result
}

suspend fun ByteReadChannel.readUByte(): Int = readByte().toInt() and 0xff

suspend fun ByteReadChannel.readUShort(): Int = (readUByte().toInt() shl 8) or readUByte().toInt()

suspend fun ByteReadChannel.readMedium() = (readByte().toInt() shl 16) + (readByte().toInt() shl 8) + readByte().toInt()

suspend fun ByteReadChannel.readUMedium() = (readUByte().toInt() shl 16) + (readUByte().toInt() shl 8) + readUByte().toInt()

suspend fun ByteReadChannel.read40BitLong() = (readByte().toLong() shl 32) + (readByte().toLong() shl 24) + (readByte().toLong() shl 16) + (readByte().toLong() shl 8) + readByte().toLong()

suspend fun ByteReadChannel.read40BitULong() = (readUByte().toLong() shl 32) + (readUByte().toLong() shl 24) + (readUByte().toLong() shl 16) + (readUByte().toLong() shl 8) + readUByte().toLong()

suspend fun ByteReadChannel.readBoolean(): Boolean = readByte().toInt() == 1

suspend fun ByteReadChannel.readBooleanInverse() = readByteInverse() == 1

suspend fun ByteReadChannel.readBooleanSubtract() = readByteSubtract() == 1

suspend fun ByteReadChannel.readBooleanAdd() = readByteAdd() == 1

suspend fun ByteReadChannel.readByteAdd(): Int = (readByte() - 128).toByte().toInt()

suspend fun ByteReadChannel.readByteInverse(): Int = -readByte()

suspend fun ByteReadChannel.readByteSubtract(): Int = (readByteInverse() + 128).toByte().toInt()

suspend fun ByteReadChannel.readShortAdd(): Int = (readByte().toInt() shl 8) or readByteAdd()

suspend fun ByteReadChannel.readShortAddLittle(): Int = ((readByte().toInt() - 128) and 0xff) or ((readByte().toInt() shl 8) and 0xff00)

suspend fun ByteReadChannel.readUShortAdd(): Int = (readByte().toInt() shl 8) or ((readByte() - 128) and 0xff)

suspend fun ByteReadChannel.readUShortLittle(): Int = readUByte().toInt() or (readUByte().toInt() shl 8)

suspend fun ByteReadChannel.readShortLittle(): Int {
    val value = readUByte().toInt() or (readUByte().toInt() shl 8)
    if (value > 0x7FFF)
        return value - 0x10000
    return value
}

suspend fun ByteReadChannel.readUShortAddLittle(): Int = (readByte() - 128 and 0xff) + (readByte().toInt() shl 8 and 0xff00)

suspend fun ByteReadChannel.readUIntMiddle(): Int = (readUByte().toInt() shl 8) or readUByte().toInt() or (readUByte().toInt() shl 24) or (readUByte().toInt() shl 16)

suspend fun ByteReadChannel.readIntInverseMiddle(): Int = (readByte().toInt() shl 16) or (readByte().toInt() shl 24) or readUByte().toInt() or (readByte().toInt() shl 8)

suspend fun ByteReadChannel.readUIntInverseMiddle(): Int = (readUByte().toInt() shl 16) or (readUByte().toInt() shl 24) or readUByte().toInt() or (readUByte().toInt() shl 8)

suspend fun ByteReadChannel.readUIntLittle(): Int = (readUByte().toInt()) or (readUByte().toInt() shl 8) or (readUByte().toInt() shl 16) or (readUByte().toInt() shl 24)

suspend fun ByteReadChannel.readSmart(): Int {
    val peek = readUByte().toInt()
    return if (peek < 128) {
        peek and 0xFF
    } else {
        (peek shl 8 or readUByte().toInt()) - 32768
    }
}

suspend fun ByteWriteChannel.writeName(displayName: String, responseName: String = displayName) {
    val different = displayName != responseName
    writeBoolean(different)
    writeString(displayName)
    if (different)
        writeString(responseName)
}

/**
 * Writes a string as an RS long
 */
suspend fun ByteWriteChannel.writeLong(string: String) {
    var long = 0L
    for (i in 0 until string.length.coerceAtMost(12)) {
        val char = string[i].code
        long *= 37L
        when (char) {
            in 65..90 -> long += char - 64L
            in 97..122 -> long += char - 96L
            in 0..9 -> long += char - 21L
        }
    }
    while (long % 37L == 0L && long != 0L)
        long /= 37L
    writeLong(long)
}