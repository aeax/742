package org.darkan.core.net.prot.revision

import io.ktor.utils.io.*
import org.darkan.core.clientwatch.ReflectionCheckType
import org.darkan.core.clientwatch.ReflectionData
import org.darkan.core.clientwatch.ReflectionResponseCode
import org.darkan.core.net.prot.*
import world.gregs.voidps.buffer.bitAccess
import world.gregs.voidps.buffer.readByteSubtract
import world.gregs.voidps.buffer.readString
import world.gregs.voidps.buffer.writeByte
import world.gregs.voidps.buffer.writeBytes
import world.gregs.voidps.buffer.writeShort
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream

fun register727() = Codec.register(727) {
    /*
     * Client protocol
     */
    //Utility/clientwatch
    clientProt<Ping>(opcode = 0)

    clientProt<ReflectionResponse>(opcode = 36, size = ProtSize.VarByte) {
        val responseCode = ReflectionResponseCode.fromId(readByte().toInt())
            ?: throw IllegalArgumentException("Invalid response code")

        val data = when (responseCode) {
//            ReflectionResponseCode.SUCCESS,
//            ReflectionResponseCode.GET_FIELD_MODIFIERS,
//            ReflectionResponseCode.GET_METHOD_MODIFIERS -> ReflectionData(numericalData = readInt().toLong())
//            ReflectionResponseCode.NUMBER -> ReflectionData(numericalData = readLong())
//            ReflectionResponseCode.STRING -> ReflectionData(stringData = readString())
            else -> ReflectionData()
        }

        ReflectionResponse(responseCode, data)
    }

    val playerOpOpcodes = intArrayOf(66, 6, 31, 89, 103, 1, 51, 94, 53, 70)
    clientProt<OpPlayer>(opcodes = playerOpOpcodes, size = ProtSize.Fixed(3)) { opcode ->
        OpPlayer(opNum = playerOpOpcodes.indexOf(opcode), playerIndex = readShort().toInt(), forceRun = readByteSubtract() == 0)
    }

    /*
     * Server protocol
     */
    //Utility/clientwatch
    serverProt<Pong>(opcode = 29)

    serverProt<ReflectionRequest>(opcode = 98, size = ProtSize.VarShort) { out ->
        out.writeByte(type.ordinal)
        when (type) {
            ReflectionCheckType.GET_INT, ReflectionCheckType.SET_INT, ReflectionCheckType.GET_FIELD_MODIFIERS -> {
                out.writeString(className)
                out.writeString(methodName)
                if (type == ReflectionCheckType.SET_INT)
                    fieldValue?.let { out.writeInt(it) }
            }
            ReflectionCheckType.GET_METHOD_RETURN_VALUE, ReflectionCheckType.GET_METHOD_MODIFIERS -> {
                out.writeString(className)
                out.writeString(methodName)
                out.writeByte(paramTypes.size)
                paramTypes.forEach { out.writeString(it) }
                out.writeString(returnType ?: "void")
                if (type == ReflectionCheckType.GET_METHOD_RETURN_VALUE) {
                    paramValues.forEach { param ->
                        ByteArrayOutputStream().use { bos ->
                            ObjectOutputStream(bos).use { oos ->
                                oos.writeObject(param)
                                val data = bos.toByteArray()
                                out.writeInt(data.size)
                                out.writeBytes(data)
                            }
                        }
                    }
                }
            }
        }
    }

    serverProt<MapRegion>(opcode = 85, size = ProtSize.VarShort) { out ->
        if (localPlayerPid != null && localPlayerBaseTileHash != null && otherPlayerRegionIds != null) {
            out.bitAccess {
                writeBits(30, localPlayerBaseTileHash)
                otherPlayerRegionIds.forEachIndexed { index, region ->
                    if (index != localPlayerPid)
                        writeBits(18, region)
                }
            }
        }
        out.writeByte(mapSize.ordinal)
        out.writeShort(chunkX)
        out.writeShort(chunkY)
        out.writeByte(if (forceMapRefresh) 1 else 0)
        xteas.forEach { keys ->
            keys.forEach { key -> out.writeInt(key) }
        }
    }
}