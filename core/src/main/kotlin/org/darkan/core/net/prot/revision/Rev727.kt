package org.darkan.core.net.prot.revision

import io.ktor.utils.io.*
import io.ktor.utils.io.core.remaining
import kotlinx.io.readByteArray
import kotlinx.io.readUByte
import kotlinx.io.readUShort
import org.darkan.core.clientwatch.MouseTrailStep
import org.darkan.core.clientwatch.MouseTrailStep.Type
import org.darkan.core.clientwatch.ReflectionCheckType
import org.darkan.core.clientwatch.ReflectionResponseCode
import org.darkan.core.net.prot.*
import org.darkan.core.social.QuickChatMessage
import org.darkan.core.type.Preference
import org.darkan.core.worldlist.World
import world.gregs.voidps.buffer.*
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.type.Tile
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import kotlin.io.use
import kotlin.math.max
import kotlin.math.min

fun register727() =
    Codec.register(727) {
        /*
         * Client protocol
         */
        //Utility/clientwatch
        clientProt<Ping>(opcode = 0)

        clientProt(opcode = 36, size = ProtSize.VarByte) {
            val responseCode = ReflectionResponseCode.fromId(readByte().toInt()) ?: throw IllegalArgumentException("Invalid response code")
            ReflectionResponse(responseCode, readByteArray(remaining.toInt()))
        }

        clientProt(opcode = 62, size = 1) { ClientFocus(readBoolean()) }

        clientProt(opcode = 84, size = 6) {
            ClientScreenSize(
                displayMode = readUByte().toInt(),
                width = readUShort().toInt(),
                height = readUShort().toInt(),
                switchDisplayMode = readBoolean()
            )
        }

        clientProt(opcode = 83, size = 4) {
            MoveCamera(angleX = readShortAddLittle().toInt(), angleY = readShortAdd().toInt())
        }

        clientProt(opcode = 44, size = 9) { //TODO test
            SendFps(
                key1 = readIntInverseMiddle(),
                key2 = readUIntMiddle(),
                fps = readByteAdd().toInt()
            )
        }

        clientProt(opcode = 59, size = 6) {
            val positionHash = readUIntLittle()
            val mouseHash = readShort()
            MouseClick(
                mouseButton = mouseHash.toInt() ushr 15,
                time = mouseHash.toInt() and 0x7FFF,
                x = positionHash and 0xFFFF,
                y = positionHash ushr 16
            )
        }

        clientProt(opcode = 57, size = 7) {
            val positionHash = readUIntLittle()
            val flags = readByteAdd()
            MouseButtonClick(
                mouseButton = flags ushr 1,
                time = readUShortLittle(),
                x = positionHash and 0xFFFF,
                y = positionHash ushr 16,
                hw = (flags and 1) == 1
            )
        }

        clientProt(opcodes = intArrayOf(40, 78), size = ProtSize.VarByte) { opcode ->
            val steps = mutableListOf<MouseTrailStep>()
            val frameSteps = readUByte().toInt()
            val frameCount = readUByte().toInt()
            while (remaining > 0) {
                val firstByte = readByte().toUByte().toInt()

                var frames: Int
                var dX: Int
                var dY: Int
                var type = Type.MOVE_OFFSET

                when {
                    firstByte >= 224 -> {
                        val secondByte = readByte().toUByte().toInt()
                        frames = ((firstByte shl 8) or secondByte) - 57344
                        val posHash = readInt()
                        dY = posHash shr 16
                        dX = posHash and 0xFFFF
                        type = Type.SET_POSITION
                    }
                    firstByte >= 192 -> {
                        frames = firstByte - 192
                        val posHash = readInt()
                        dY = posHash shr 16
                        dX = posHash and 0xFFFF
                        type = Type.SET_POSITION
                    }
                    firstByte >= 128 -> {
                        frames = firstByte - 128
                        val posHash = readUShort().toInt()
                        dY = posHash shr 8
                        dX = posHash and 0xFF
                    }
                    else -> {
                        val secondByte = readByte().toUByte().toInt()
                        val hash = (firstByte shl 8) or secondByte
                        frames = hash shr 12
                        dX = (hash shr 6) and 0x3F
                        dY = hash and 0x3F
                    }
                }

                steps.add(MouseTrailStep(type, frames, dX, dY, if (opcode == 40) readBoolean() else true))
            }
            MoveMouseData(
                steps = steps.toTypedArray(),
                frameCount = frameCount,
                frameSteps = frameSteps
            )
        }

        clientProt(opcode = 28, size = ProtSize.VarShort) { KeyPress(keyCode = readByte().toInt(), time = readMedium()) }

        clientProt(opcode = 85, size = ProtSize.VarByte) { ClientCheat(client = readBoolean(), command = readRSString()) }

        // Entity interactions
        val playerOpOpcodes = intArrayOf(66, 6, 31, 89, 103, 1, 51, 94, 53, 70)
        clientProt(opcodes = playerOpOpcodes, size = 3) { opcode ->
            OpPlayer(
                opNum = playerOpOpcodes.indexOf(opcode),
                playerIndex = readShort().toInt(),
                forceRun = readBooleanSubtract()
            )
        }

        val npcOpOpcodes = intArrayOf(65, 16, 50, 77, 95, 3)
        clientProt(opcodes = npcOpOpcodes, size = 3) { opcode ->
            OpNpc(
                opNum = npcOpOpcodes.indexOf(opcode),
                npcIndex = readUShort().toInt(),
                forceRun = readBoolean()
            )
        }

        val objOpOpcodes = intArrayOf(75, 93, 38, 32, 48, 73)
        clientProt(opcodes = objOpOpcodes, size = 9) { opcode ->
            OpObj(
                opNum = objOpOpcodes.indexOf(opcode),
                y = readUShort().toInt(),
                x = readUShort().toInt(),
                objectId = readInt(),
                forceRun = readBooleanAdd()
            )
        }

        val groundItemOpOpcodes = intArrayOf(24, 25, 54, 8, 43, 61)
        clientProt(opcodes = groundItemOpOpcodes, size = 7) { opcode ->
            OpGroundItem(
                opNum = groundItemOpOpcodes.indexOf(opcode),
                itemId = readUShortAddLittle(),
                forceRun = readBooleanInverse(),
                y = readUShort().toInt(),
                x = readUShortAdd()
            )
        }

        clientProt(opcode = 33, size = 5) {
            val forceRun = readBoolean()
            val x = readUShort().toInt()
            val y = readUShortLittle()
            Walk(x, y, forceRun, false)
        }

        clientProt(opcode = 42, size = 18) {
            val forceRun = readBoolean()
            val x = readUShort().toInt()
            val y = readUShortLittle()
            readByte()
            readByte() //always -1
            val camAngle = readUShort().toInt()
            readByte() //always 57
            val minimapRotation = readUByte() //TODO
            val minimapZoom = readUByte()
            readByte() //always 89
            val absX = readUShort().toInt()
            val absY = readUShort().toInt()
            readByte() //always 63
            Walk(x, y, forceRun, true)
        }

        // Interface interactions
        val ifButtonOpcodes = intArrayOf(96, 27, 68, 9, 72, 19, 23, 21, 22, 81)
        clientProt(opcodes = ifButtonOpcodes, size = 8) { opcode ->
            val interfaceHash = readUIntLittle()
            IfButton(
                opNum = ifButtonOpcodes.indexOf(opcode),
                interfaceId = interfaceHash shr 16,
                componentId = interfaceHash and 0xFFFF,
                slotId = readUShort().toInt(),
                itemId = readShortAdd()
            )
        }

        clientProt(opcode = 4, size = 16) {
            val toSlot = readShortAddLittle()
            val fromSlot = readUShortLittle()
            val toItemId = readShortAddLittle()
            val toHash = readUIntLittle()
            val fromHash = readIntInverseMiddle()
            val fromItemId = readUShortLittle()
            IfOnIf(
                fromInter = fromHash shr 16,
                toInter = toHash shr 16,
                fromComp = fromHash and 0xFFFF,
                toComp = toHash and 0xFFFF,
                fromSlot = fromSlot,
                toSlot = toSlot,
                fromItemId = fromItemId,
                toItemId = toItemId
            )
        }

        clientProt(opcode = 98, size = 17) {
            val x = readShortAddLittle()
            val forceRun = readBooleanAdd()
            val objectId = readUIntMiddle()
            val interfaceHash = readInt()
            val itemId = readUShortLittle()
            val slotId = readShortAdd()
            val y = readUShortLittle()
            IfOnObject(
                interfaceId = interfaceHash shr 16,
                componentId = interfaceHash and 0xFFFF,
                slotId = slotId,
                itemId = itemId,
                objectId = objectId,
                x = x,
                y = y,
                forceRun = forceRun
            )
        }

        clientProt(opcode = 67, size = 15) {
            val itemIdContainer = readShortAdd()
            val interfaceHash = readIntInverseMiddle()
            val itemId = readShort().toInt()
            val forceRun = readBooleanAdd()
            val slotId = readShortAddLittle()
            val y = readShortAddLittle()
            val x = readUShortLittle()
            IfOnGroundItem(
                interfaceId = interfaceHash shr 16,
                componentId = interfaceHash and 0xFFFF,
                slotId = slotId,
                itemIdContainer = itemIdContainer,
                itemId = itemId,
                x = x,
                y = y,
                forceRun = forceRun
            )
        }

        clientProt(opcode = 41, size = 11) {
            val interfaceHash = readIntInverseMiddle()
            val npcIndex = readUShortAddLittle()
            val forceRun = readBooleanSubtract()
            val itemId = readUShortAddLittle()
            val slotId = readUShortAdd()
            IfOnNpc(
                interfaceId = interfaceHash shr 16,
                componentId = interfaceHash and 0xFFFF,
                slotId = slotId,
                itemId = itemId,
                npcIndex = npcIndex,
                forceRun = forceRun
            )
        }

        clientProt(opcode = 13, size = 11) {
            val slotId = readUShort().toInt()
            val playerIndex = readUShortLittle()
            val forceRun = readBooleanSubtract()
            val interfaceHash = readIntInverseMiddle()
            val itemId = readUShortLittle()
            IfOnPlayer(
                interfaceId = interfaceHash shr 16,
                componentId = interfaceHash and 0xFFFF,
                slotId = slotId,
                itemId = itemId,
                playerIndex = playerIndex,
                forceRun = forceRun
            )
        }

        clientProt(opcode = 46, size = 12) {
            val itemId = readUShortLittle()
            val y = readUShortLittle()
            val interfaceHash = readIntInverseMiddle()
            val slotId = readShortAdd()
            val x = readUShortLittle()
            IfOnTile(
                interfaceId = interfaceHash shr 16,
                componentId = interfaceHash and 0xFFFF,
                slotId = slotId,
                itemId = itemId,
                x = x,
                y = y
            )
        }

        clientProt(opcode = 49, size = 6) {
            val interfaceHash = readUIntMiddle()
            val slotId = readShortAddLittle()
            IfContinue(
                interfaceId = interfaceHash shr 16,
                componentId = interfaceHash and 0xFFFF,
                slotId = slotId
            )
        }

        clientProt(opcode = 74, size = 16) {
            val toSlot = readShortAddLittle()
            val fromSlot = readUShortLittle()
            val toItemId = readShort().toInt()
            val fromItemId = readShortAddLittle()
            val fromInterfaceHash = readUIntMiddle()
            val toInterfaceHash = readUIntLittle()
            IfDragOntoIf(
                fromInter = fromInterfaceHash shr 16,
                toInter = toInterfaceHash shr 16,
                fromComp = fromInterfaceHash and 0xFFFF,
                toComp = toInterfaceHash and 0xFFFF,
                fromSlot = fromSlot,
                toSlot = toSlot,
                fromItemId = fromItemId,
                toItemId = toItemId
            )
        }

        clientProt<CloseInterface>(opcode = 60)

        // Dialog interactions
        clientProt(opcode = 87, size = ProtSize.VarByte) { ResumeTextDialog(readRSString()) }
        clientProt(opcode = 80, size = ProtSize.VarByte) { ResumeNameDialog(readRSString()) }
        clientProt(opcode = 69, size = ProtSize.VarByte) { ResumeClanForumQFCDialog(readRSString()) }
        clientProt(opcode = 11, size = 2) { ResumeHSLDialog(readUShort().toInt()) }
        clientProt(opcode = 58, size = 4) { ResumeCountDialog(readInt()) }
        clientProt(opcode = 17, size = 2) { ResumeItemSelect(readShort().toInt()) }

        // Communication
        clientProt(opcode = 86, size = ProtSize.VarByte) {
            Chat(
                color = min(0, max(readUByte().toInt(), 12)),
                effect = min(0, max(readUByte().toInt(), 5)),
                message = Cache.huffman.decompress(length = max(200, readSmart()), message = readByteArray(remaining.toInt())) ?: ""
            )
        }

        clientProt(opcode = 15, size = ProtSize.VarShort) {
            PrivateMessage(readRSString(), Cache.huffman.decompress(length = max(150, readSmart()), message = readByteArray(remaining.toInt())) ?: "")
        }

        clientProt(opcode = 64, size = ProtSize.VarByte) {
            QuickChatPublic(
                chatType = readByte().toInt(),
                qcId = readUShort().toInt(),
                messageData = if (remaining > 0) readByteArray(remaining.toInt()) else null
            )
        }

        clientProt(opcode = 14, size = ProtSize.VarByte) {
            QuickChatPrivate(
                toUsername = readRSString(),
                qcId = readUShort().toInt(),
                messageData = if (remaining > 0) readByteArray(remaining.toInt()) else null
            )
        }

        clientProt(opcode = 30, size = 1) { ChatType(readUByte().toInt()) }
        clientProt(opcode = 20, size = 3) { ChatSetFilter(readUByte().toInt(), readUByte().toInt(), readUByte().toInt()) }

        // Friends/Ignore List
        clientProt(opcode = 26, size = ProtSize.VarByte) { AddFriend(readRSString()) }
        clientProt(opcode = 29, size = ProtSize.VarByte) { RemoveFriend(readRSString()) }
        clientProt(opcode = 34, size = ProtSize.VarByte) { AddIgnoreReq(readRSString(), readBoolean()) }
        clientProt(opcode = 12, size = ProtSize.VarByte) { RemoveIgnore(readRSString()) }

        // Friend Chat
        clientProt(opcode = 71, size = ProtSize.VarByte) { FcJoin(if (remaining > 0) readRSString() else null) }
        clientProt(opcode = 91, size = ProtSize.VarByte) { FcKick(readRSString()) }
        clientProt(opcode = 7, size = ProtSize.VarByte) { FcSetRank(rank = readByteSubtract(), username = readRSString()) }

        // Clan Chat
        clientProt(opcode = 90, size = ProtSize.VarByte) {
            ClanChannelKickUser(
                guest = !readBoolean(),
                pid = readUShort().toInt(),
                username = readRSString()
            )
        }

        // Game State
        clientProt<RegionLoadedConfirm>(opcode = 76)
        clientProt(opcode = 2, size = 4) { SoundEffectMusicEnded(readInt()) }
        clientProt(opcode = 18, size = 4) { SongLoaded(readInt()) }
        clientProt(opcode = 45, size = 1) { CutsceneFinished(readBoolean()) }
        clientProt(opcode = 88, size = 2) { WritePing(readUShort().toInt()) }
        clientProt(opcode = 5, size = 4) { WorldMapClick(Tile(readUIntLittle())) }

        clientProt(opcode = 10, size = ProtSize.VarByte) {
            SendPreferences(buildMap {
                var currIdx = 0
                while (remaining > 0) {
                    Preference.forIndex(currIdx++)?.let {
                        put(it, readUByte().toInt())
                    } ?: readUByte()
                }
            })
        }

        clientProt(opcode = 55, size = 4) { TransmitvarVerifyId(readInt()) }
        clientProt(opcode = 47, size = 4) { RequestWorldList(readInt()) }

        // Reporting/Bug Tracking
        clientProt(opcode = 100, size = ProtSize.VarByte) {
            ReportAbuse(
                username = readRSString(),
                type = readUByte().toInt(),
                mute = readBoolean(),
                reason = readRSString()
            )
        }

        clientProt(opcode = 35, size = ProtSize.VarShort) {
            BugReport(
                category = readUByte().toInt(),
                body = readJagString(),
                reproSteps = readJagString()
            )
        }

        // Account/Login
        clientProt(opcode = 52, size = ProtSize.VarByte) { EmailValidationSubmitCode(readRSString()) }
        clientProt(opcode = 92, size = ProtSize.VarShort) { EmailValidationAddNewAddress(readRSString(), readByte().toInt()) }
        clientProt(opcode = 99, size = ProtSize.VarShort) { EmailValidationChangeAddress(readRSString(), readRSString()) }
        clientProt(opcode = 102, size = ProtSize.VarShort) { CheckEmailValidity(readByteArray(remaining.toInt())) }
        clientProt(opcode = 101, size = ProtSize.VarShort) { SendSignUpForm(readByteArray(remaining.toInt())) }
        clientProt(opcode = 79, size = 1) { AccountCreationStage(readUByte().toInt()) }

        clientProt(opcode = 56, size = ProtSize.VarShort) {
            LobbyHyperlink(
                service = readRSString(),
                page = readRSString(),
                query = readRSString(),
                flags = readByte().toInt()
            )
        }

        // Unknown/Misc
        clientProt(opcode = 37, size = 2) { PlayVorbis(readShort().toInt()) }
        clientProt(opcode = 63, size = 4) { AltWalk(x = readShortAdd(), y = readShort().toInt()) }
        clientProt(opcode = 82, size = 4) { AppletLoadingPleaseWait(readInt() /*Always 1057001181*/) }
        clientProt(opcode = 97, size = ProtSize.VarByte) { UnkCs2StringResponse(readRSString()) }

        /*
         * Server protocol
         */
        //Utility/clientwatch
        serverProt<Pong>(opcode = 29)

        serverProt<ReflectionRequest>(opcode = 98, size = ProtSize.VarShort) { out ->
            out.writeByte(checks.size)
            out.writeInt(id)
            for (check in checks) {
                out.writeByte(check.type.ordinal)
                when (check.type) {
                    ReflectionCheckType.GET_INT, ReflectionCheckType.SET_INT, ReflectionCheckType.GET_FIELD_MODIFIERS -> {
                        out.writeRSString(check.className)
                        out.writeRSString(check.methodName)
                        if (check.type == ReflectionCheckType.SET_INT)
                            check.fieldValue?.let { out.writeInt(it) }
                    }
                    ReflectionCheckType.GET_METHOD_RETURN_VALUE, ReflectionCheckType.GET_METHOD_MODIFIERS -> {
                        out.writeRSString(check.className)
                        out.writeRSString(check.methodName)
                        out.writeByte(check.paramTypes.size)
                        check.paramTypes.forEach { out.writeRSString(it) }
                        out.writeRSString(check.returnType ?: "void")
                        if (check.type == ReflectionCheckType.GET_METHOD_RETURN_VALUE) {
                            check.paramValues.forEach { param ->
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
        }

        serverProt<BuildRegion>(opcode = 85, size = ProtSize.VarShort) { out ->
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
            out.writeBoolean(forceMapRefresh)
            xteas.forEach { keys ->
                keys.forEach { key -> out.writeInt(key) }
            }
        }

        serverProt<BuildInstancedRegion>(opcode = 51, size = ProtSize.VarShort) { out ->
            if (localPlayerPid != null && localPlayerBaseTileHash != null && otherPlayerRegionIds != null) {
                out.bitAccess {
                    writeBits(30, localPlayerBaseTileHash)
                    otherPlayerRegionIds.forEachIndexed { index, region ->
                        if (index != localPlayerPid)
                            writeBits(18, region)
                    }
                }
            }
            out.writeBooleanInverse(forceMapRefresh)
            out.writeByteSubtract(instanceLoadType.index)
            out.writeByteAdd(mapSize.ordinal)
            out.writeShortAdd(chunkY)
            out.writeShort(chunkX)
            out.bitAccess {
                chunks.forEach {
                    if (it == -1) writeBit(false)
                    else {
                        writeBit(true)
                        writeBits(26, it)
                    }
                }
            }
            xteas.forEach { keys ->
                keys.forEach { key -> out.writeInt(key) }
            }
        }

        // Camera related protocols
        serverProt<CamLookAt>(opcode = 24, size = 6) { out ->
            out.writeByte(viewLocalX)
            out.writeShortAdd(viewZ shr 2)
            out.writeByteAdd(viewLocalY)
            out.writeByteInverse(speed1)
            out.writeByteSubtract(speed2)
        }

        serverProt<CamMoveTo>(opcode = 39, size = 6) { out ->
            out.writeByte(moveLocalY)
            out.writeByte(moveLocalX)
            out.writeByteSubtract(speed1)
            out.writeShortLittle(moveZ shr 2)
            out.writeByteAdd(speed2)
        }

        serverProt<CamRemoveRoof>(opcode = 40, size = 4) { out ->
            out.writeIntLittle(tile.id)
        }

        serverProt<CamResetHard>(opcode = 66)
        serverProt<CamResetSmooth>(opcode = 89)

        serverProt<CamShake>(opcode = 71, size = 6) { out ->
            out.writeByteSubtract(v2)
            out.writeShort(v4)
            out.writeByteInverse(slotId)
            out.writeByteInverse(v1)
            out.writeByteSubtract(v3)
        }

        serverProt<CamForceAngle>(opcode = 118, size = 4) { out ->
            out.writeShortAdd(angleY)
            out.writeShortAddLittle(angleX)
        }

        // Messaging protocols
        serverProt<MessagePrivate>(opcode = 10, size = ProtSize.VarShort) { out ->
            out.writeName(displayName, quickResponseName)
            out.writeHashedMessageTimestamp(message.take(210))
            out.writeByte(crown)
            out.writeBytes(Cache.huffman.compress(message.take(210)))
        }

        serverProt<MessageQuickChatPrivateEcho>(opcode = 22, size = ProtSize.VarByte) { out ->
            out.writeRSString(senderDisplayName)
            out.writeShort(message.fileId)
            message.data?.let { out.writeBytes(it) }
        }

        serverProt<MessageFriendsChat>(opcode = 25, size = ProtSize.VarByte) { out ->
            out.writeName(displayName, quickResponseName)
            out.writeLong(chatName)
            out.writeHashedMessageTimestamp(message.take(210))
            out.writeByte(crown)
            out.writeBytes(Cache.huffman.compress(message.take(210)))
        }

        serverProt<MessageQuickChatPrivate>(opcode = 31, size = ProtSize.VarByte) { out ->
            out.writeName(displayName, quickResponseName)
            out.writeHashedQCMessageTimestamp(message.fileId)
            out.writeByte(crown)
            out.writeShort(message.fileId)
            message.data?.let { out.writeBytes(it) }
        }

        serverProt<MessageClanChannel>(opcode = 81, size = ProtSize.VarByte) { out ->
            out.writeBoolean(!guest)
            out.writeRSString(displayName)
            out.writeHashedMessageTimestamp(message.take(210))
            out.writeByte(crown)
            out.writeBytes(Cache.huffman.compress(message.take(210)))
        }

        serverProt<MessageQuickChatPlayerGroup>(opcode = 83, size = ProtSize.VarByte) { out ->
            out.writeName(displayName, quickResponseName)
            out.writeByte(crown)
            out.writeShort(message.fileId)
            message.data?.let { out.writeBytes(it) }
        }

        serverProt<MessagePrivateEcho>(opcode = 92, size = ProtSize.VarShort) { out ->
            out.writeRSString(senderDisplayName)
            out.writeBytes(Cache.huffman.compress(message.take(210)))
        }

        serverProt<MessageQuickChatFriendsChat>(opcode = 142, size = ProtSize.VarByte) { out ->
            out.writeName(displayName, quickResponseName)
            out.writeLong(chatName)
            out.writeHashedQCMessageTimestamp(message.fileId)
            out.writeByte(crown)
            out.writeShort(message.fileId)
            message.data?.let { out.writeBytes(it) }
        }

        serverProt<MessageQuickChatClanChannel>(opcode = 131, size = ProtSize.VarByte) { out ->
            out.writeBoolean(!guest)
            out.writeRSString(displayName)
            out.writeHashedQCMessageTimestamp(message.fileId)
            out.writeByte(crown)
            out.writeShort(message.fileId)
            message.data?.let { out.writeBytes(it) }
        }

        serverProt<MessagePlayerGroup>(opcode = 133, size = ProtSize.VarByte) { out ->
            out.writeName(displayName, quickResponseName)
            out.writeByte(crown)
            out.writeBytes(Cache.huffman.compress(message.take(210)))
        }

        serverProt<MessagePublic>(opcode = 152, size = ProtSize.VarByte) { out ->
            out.writeShort(pid)
            out.writeShort(message.effects)
            out.writeByte(messageIcon)
            if (message is QuickChatMessage) {
                out.writeShort(message.fileId)
                message.data?.let { out.writeBytes(it) }
            } else
                out.writeBytes(Cache.huffman.compress(message.message.take(210)))
        }

        serverProt<GameMessage>(opcode = 160, size = ProtSize.VarByte) { out ->
            var maskData = 0
            if (targetDisplayName != null) {
                maskData = maskData or 0x1
                //maskData |= 0x2;
            }
            out.writeSmart(type.value)
            out.writeInt(effectFlags)
            out.writeByte(maskData)
            if (targetDisplayName != null) {
                out.writeRSString(targetDisplayName)
                //stream.writeRSString(target.getDisplayName());
            }
            out.writeRSString(message.take(248))
        }

        // Variable related protocols
        serverProt<VarpSmall>(opcode = 115, size = 3) { out ->
            out.writeByte(value)
            out.writeShortAddLittle(id)
        }

        serverProt<VarpLarge>(opcode = 8, size = 6) { out ->
            out.writeIntInverseMiddle(value)
            out.writeShortAddLittle(id)
        }

        serverProt<ClientSetVarcSmall>(opcode = 116, size = 3) { out ->
            out.writeByteSubtract(value)
            out.writeShortLittle(id)
        }

        serverProt<ClientSetVarcLarge>(opcode = 12, size = 6) { out ->
            out.writeShortLittle(id)
            out.writeIntInverseMiddle(value)
        }

        serverProt<VarbitSmall>(opcode = 68, size = 3) { out ->
            out.writeShort(id)
            out.writeByteAdd(value)
        }

        serverProt<VarbitLarge>(opcode = 108, size = 6) { out ->
            out.writeIntInverseMiddle(value)
            out.writeShortAdd(id)
        }

        serverProt<ClientSetVarcStrSmall>(opcode = 54, size = ProtSize.VarByte) { out ->
            out.writeRSString(value)
            out.writeShortAddLittle(id)
        }

        serverProt<ClientSetVarcStrLarge>(opcode = 119, size = ProtSize.VarShort) { out ->
            out.writeShortLittle(id)
            out.writeRSString(value)
        }

        serverProt<VarclanSetLong>(opcode = 26, size = 10) { out ->
            out.writeShort(id)
            out.writeLong(value)
        }

        serverProt<VarclanSetByte>(opcode = 123, size = 3) { out ->
            out.writeShort(id)
            out.writeByte(value)
        }

        serverProt<VarclanSetInt>(opcode = 141, size = 6) { out ->
            out.writeShort(id)
            out.writeInt(value)
        }

        serverProt<VarclanSetString>(opcode = 50, size = ProtSize.VarByte) { out ->
            out.writeShort(id)
            out.writeRSString(value)
        }

        serverProt<VarclanEnable>(opcode = 45)

        serverProt<VarclanDisable>(opcode = 94)

        serverProt<ClearVarps>(opcode = 100)

        // Player related protocols
        serverProt<PlayerWeight>(opcode = 14, size = 2) { out ->
            out.writeShort(weight)
        }

        serverProt<PlayerOption>(opcode = 111, size = ProtSize.VarByte) { out ->
            out.writeRSString(option)
            out.writeByteAdd(slot)
            out.writeShortAddLittle(cursor)
            out.writeByteInverse(if (top) 1 else 0)
        }

        serverProt<RunEnergy>(opcode = 64, size = 1) { out ->
            out.writeByte(energy)
        }

        serverProt<FriendStatus>(opcode = 74, size = ProtSize.VarShort) { out ->
            // TODO: Implement serialization
        }

        serverProt<FriendlistLoaded>(opcode = 101)

        serverProt<AddIgnore>(opcode = 138, size = ProtSize.VarByte) { out ->
            // TODO: Implement serialization
        }

        serverProt<ReduceAttackPriority>(opcode = 61, size = 1) { out ->
            // TODO: Implement serialization
        }

        serverProt<SetTarget>(opcode = 129, size = 2) { out ->
            // TODO: Implement serialization
        }

        // Friend and clan chat protocols
        serverProt<FriendsChatChannel>(opcode = 127, size = ProtSize.VarShort) { out ->
            // TODO: Implement serialization
        }

        // Login and system related protocols
        serverProt<LogoutLobby>(opcode = 9)

        serverProt<LogoutFull>(opcode = 62)

        serverProt<OpenUrl>(opcode = 16, size = ProtSize.VarShort) { out ->
            // TODO: Implement serialization
        }

        serverProt<WorldListPacket>(opcode = 103, size = ProtSize.VarShort) { out ->
            val worlds = worldList.getWorldArray()
            out.writeByte(1)
            out.writeByte(if (refreshOnClient) 2 else 0)
            if (!refreshOnlyPlayerCounts) {
                out.writeByte(1)
                val size: Int = worlds.size
                out.writeSmart(size)
                for (world in worlds) {
                    out.writeSmart(world.metadata.country.id)
                    out.writeJagString(world.metadata.activity)
                }
                out.writeSmart(0)
                out.writeSmart(worldList.maxWorlds+1)
                out.writeSmart(size)
                for (world in worlds) {
                    out.writeSmart(world.metadata.number)
                    out.writeByte(world.index)
                    var flags = 0
                    if (world.metadata.members) flags = flags or 0x1
                    if (world.metadata.quickchat) flags = flags or 0x2
                    if (world.metadata.pvp) flags = flags or 0x4
                    if (world.metadata.lootShare) flags = flags or 0x8
                    if (world.metadata.highlighted) flags = flags or 0x16
                    if (world.metadata.port != 43595) flags = flags or 0x40000000 // Custom port flag

                    out.writeInt(flags)
                    out.writeJagString("")
                    out.writeJagString(world.metadata.ipAddress)
                    if (world.metadata.port != 43595) out.writeInt(world.metadata.port)
                }
                out.writeInt(worldList.revision)
            } else
                out.writeByte(0)
            for (world in worlds) {
                out.writeSmart(world.metadata.number)
                out.writeShort(if (world.offline) -1 else world.playersOnline)
            }
        }

        serverProt<IdentifyHostName>(opcode = 147, size = 4) { out ->
            // TODO: Implement serialization
        }

        serverProt<UpdateRebootTimer>(opcode = 102, size = 2) { out ->
            // TODO: Implement serialization
        }

        serverProt<RequestFps>(opcode = 159, size = 8) { out ->
            // TODO: Implement serialization
        }

        serverProt<UpdateSitesettingsCookie>(opcode = 91, size = ProtSize.VarByte) { out ->
            // TODO: Implement serialization
        }

        serverProt<ChatFilterSettings>(opcode = 30, size = 2) { out ->
            // TODO: Implement serialization
        }

        serverProt<ChatFilterSettingsPrivateChat>(opcode = 67, size = 1) { out ->
            // TODO: Implement serialization
        }

        // Account related protocols
        serverProt<CreateCheckEmailReply>(opcode = 1, size = 1) { out ->
            // TODO: Implement serialization
        }

        serverProt<CreateAccountReply>(opcode = 87, size = 1) { out ->
            // TODO: Implement serialization
        }

        serverProt<UpdateDob>(opcode = 157, size = 4) { out ->
            // TODO: Implement serialization
        }

        // Miscellaneous protocols
        serverProt<ProcessDevConsoleCommand>(opcode = 2, size = ProtSize.VarByte) { out ->
            // TODO: Implement serialization
        }

        serverProt<RunCs2Script>(opcode = 99, size = ProtSize.VarShort) { out ->
            // TODO: Implement serialization
        }

        serverProt<SetCursor>(opcode = 153, size = ProtSize.VarByte) { out ->
            // TODO: Implement serialization
        }

        serverProt<Cutscene>(opcode = 110, size = ProtSize.VarShort) { out ->
            // TODO: Implement serialization
        }

        serverProt<TriggerOnDialogAbort>(opcode = 146)

        serverProt<KeepAlive>(opcode = 29)

        serverProt<LoyaltyUpdate>(opcode = 32, size = 5) { out ->
            // TODO: Implement serialization
        }

        serverProt<JCoinsUpdate>(opcode = 75, size = 4) { out ->
            // TODO: Implement serialization
        }

        serverProt<ApplyDebug>(opcode = 76, size = 2) { out ->
            // TODO: Implement serialization
        }

        serverProt<QuickHopWorlds>(opcode = 58, size = ProtSize.VarByte) { out ->
            // TODO: Implement serialization
        }

        serverProt<DebugServerTriggers>(opcode = 73, size = ProtSize.VarByte) { out ->
            // TODO: Implement serialization
        }

        // Interface/UI related
        serverProt<IfSetPlayerHead>(opcode = 0, size = 4) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfSetTextFont>(opcode = 7, size = 8) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfMoveSub>(opcode = 13, size = 8) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfOpenSubActiveObject>(opcode = 23, size = 32) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfSetHide>(opcode = 33, size = 5) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfOpenSubActivePlayer>(opcode = 59, size = 25) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfSetGraphic>(opcode = 35, size = 8) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfSetTextAntiMacro>(opcode = 36, size = 5) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfOpenTop>(opcode = 37, size = 19) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfOpenSub>(opcode = 38, size = 23) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfSetAngle>(opcode = 44, size = 10) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfSetPosition>(opcode = 48, size = 8) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfSetPlayerModelOther>(opcode = 53, size = 10) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfSetScrollPos>(opcode = 55, size = 6) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfOpenSubActiveNpc>(opcode = 34, size = 25) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfSetPlayerModel>(opcode = 72, size = 4) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfCloseSub>(opcode = 78, size = 4) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfSetAnim>(opcode = 82, size = 8) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfSetColor>(opcode = 105, size = 6) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfSetNpcHead>(opcode = 109, size = 8) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfSetItem>(opcode = 112, size = 10) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfSetEvents>(opcode = 121, size = 12) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfSetText>(opcode = 124, size = ProtSize.VarShort) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfSetPlayerHeadIgnoreWorn>(opcode = 130, size = 10) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfSetReTex>(opcode = 132, size = 9) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfSetModel>(opcode = 135, size = 8) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfSetReCol>(opcode = 144, size = 9) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfSetTargetParam>(opcode = 148, size = 10) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfSetClickMask>(opcode = 150, size = 5) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfOpenSubActiveGroundItem>(opcode = 155, size = 29) { out ->
            // TODO: Implement serialization
        }

        serverProt<IfSetPlayerHeadOther>(opcode = 158, size = 10) { out ->
            // TODO: Implement serialization
        }

        // Update and inventory protocols
        serverProt<UpdateInvPartial>(opcode = 4, size = ProtSize.VarShort) { out ->
            // TODO: Implement serialization
        }

        serverProt<UpdateInvFull>(opcode = 5, size = ProtSize.VarShort) { out ->
            // TODO: Implement serialization
        }

        serverProt<UpdateZoneFullFollows>(opcode = 15, size = 3) { out ->
            // TODO: Implement serialization
        }

        serverProt<TileMessage>(opcode = 114, size = ProtSize.VarByte) { out ->
            // TODO: Implement serialization
        }

        serverProt<UpdateFriendchatChannelSingleUser>(opcode = 19, size = ProtSize.VarByte) { out ->
            // TODO: Implement serialization
        }

        serverProt<UpdateZonePartialFollows>(opcode = 41, size = 3) { out ->
            // TODO: Implement serialization
        }

        serverProt<UpdateInvStopTransmit>(opcode = 42, size = 3) { out ->
            // TODO: Implement serialization
        }

        serverProt<PlayerUpdate>(opcode = 43, size = ProtSize.VarShort) { out ->
            // TODO: Implement serialization
        }

        serverProt<NpcUpdateLarge>(opcode = 47, size = ProtSize.VarShort) { out ->
            // TODO: Implement serialization
        }

        serverProt<NpcUpdate>(opcode = 6, size = ProtSize.VarShort) { out ->
            // TODO: Implement serialization
        }

        serverProt<ClanChannelDelta>(opcode = 28, size = ProtSize.VarShort) { out ->
            // TODO: Implement serialization
        }

        serverProt<ClanChannelFull>(opcode = 49, size = ProtSize.VarShort) { out ->
            // TODO: Implement serialization
        }

        serverProt<ClanSettingsDelta>(opcode = 46, size = ProtSize.VarShort) { out ->
            // TODO: Implement serialization
        }

        serverProt<ClanSettingsFull>(opcode = 137, size = ProtSize.VarShort) { out ->
            // TODO: Implement serialization
        }

        serverProt<UpdateZonePartialEnclosed>(opcode = 65, size = ProtSize.VarShort) { out ->
            // TODO: Implement serialization
        }

        serverProt<UpdateGESlot>(opcode = 57, size = 20) { out ->
            // TODO: Implement serialization
        }

        serverProt<UpdateUid192>(opcode = 90, size = 28) { out ->
            // TODO: Implement serialization
        }

        serverProt<UpdateIgnoreList>(opcode = 97, size = ProtSize.VarShort) { out ->
            // TODO: Implement serialization
        }

        serverProt<UpdateStat>(opcode = 140, size = 6) { out ->
            // TODO: Implement serialization
        }

        serverProt<ShowFaceHere>(opcode = 21, size = 1) { out ->
            // TODO: Implement serialization
        }

        serverProt<MinimapFlag>(opcode = 70, size = 2) { out ->
            // TODO: Implement serialization
        }

        serverProt<HintArrow>(opcode = 79, size = 14) { out ->
            // TODO: Implement serialization
        }

        serverProt<HintTrail>(opcode = 149, size = ProtSize.VarShort) { out ->
            // TODO: Implement serialization
        }

        serverProt<MapProjAnim>(opcode = 143, size = 16) { out ->
            // TODO: Implement serialization
        }

        serverProt<MapProjAnimHalfSq>(opcode = 95, size = 17) { out ->
            // TODO: Implement serialization
        }

        serverProt<SetDrawOrder>(opcode = 151, size = 1) { out ->
            // TODO: Implement serialization
        }

        serverProt<BlockMinimapState>(opcode = 154, size = 1) { out ->
            // TODO: Implement serialization
        }

        // Object and ground item protocols
        serverProt<GroundItemCount>(opcode = 3, size = 7) { out ->
            // TODO: Implement serialization
        }

        serverProt<CreateGroundItem>(opcode = 27, size = 5) { out ->
            // TODO: Implement serialization
        }

        serverProt<GroundItemReveal>(opcode = 107, size = 7) { out ->
            // TODO: Implement serialization
        }

        serverProt<RemoveGroundItem>(opcode = 125, size = 3) { out ->
            // TODO: Implement serialization
        }

        serverProt<DestroyObject>(opcode = 69, size = 2) { out ->
            // TODO: Implement serialization
        }

        serverProt<ObjAnim>(opcode = 113, size = 6) { out ->
            // TODO: Implement serialization
        }

        serverProt<ObjAnimSpecific>(opcode = 80, size = 9) { out ->
            // TODO: Implement serialization
        }

        serverProt<CreateObject>(opcode = 117, size = 6) { out ->
            // TODO: Implement serialization
        }

        serverProt<CustomizeObject>(opcode = 84, size = ProtSize.VarByte) { out ->
            // TODO: Implement serialization
        }

        serverProt<ObjectPrefetch>(opcode = 88, size = 5) { out ->
            // TODO: Implement serialization
        }

        // Animation related protocols
        serverProt<SpotAnim>(opcode = 139, size = 8) { out ->
            // TODO: Implement serialization
        }

        serverProt<SpotAnimSpecific>(opcode = 126, size = 12) { out ->
            // TODO: Implement serialization
        }

        serverProt<AnimateNpc>(opcode = 145, size = 19) { out ->
            // TODO: Implement serialization
        }

        serverProt<ProjAnimSpecific>(opcode = 56, size = 22) { out ->
            // TODO: Implement serialization
        }

        serverProt<ResetAllAnimations>(opcode = 122)

        // Audio related protocols
        serverProt<VorbisSound>(opcode = 18, size = 8) { out ->
            // TODO: Implement serialization
        }

        serverProt<SoundEffectTile>(opcode = 128, size = 6) { out ->
            // TODO: Implement serialization
        }

        serverProt<VorbisSpeechSound>(opcode = 104, size = 6) { out ->
            // TODO: Implement serialization
        }

        serverProt<SoundSynth>(opcode = 136, size = 8) { out ->
            // TODO: Implement serialization
        }

        serverProt<MusicEffect>(opcode = 60, size = 6) { out ->
            // TODO: Implement serialization
        }

        serverProt<MusicTrack>(opcode = 63, size = 4) { out ->
            // TODO: Implement serialization
        }

        serverProt<MusicTrackTile>(opcode = 93, size = 11) { out ->
            // TODO: Implement serialization
        }

        serverProt<PreloadSong>(opcode = 77, size = 2) { out ->
            // TODO: Implement serialization
        }

        serverProt<SoundMixbussSetLevel>(opcode = 134, size = 2) { out ->
            // TODO: Implement serialization
        }

        serverProt<ResetSounds>(opcode = 120)

        serverProt<LobbyLoginDetails>(opcode = 2, size = ProtSize.VarByte) { out ->
            var ipHash = 0
            out.writeByte(account.rights.crown) // rights
            out.writeByte(0) // PLAYER_MOD_LEVEL
            out.writeByte(0) // USERDETAIL_QUICKCHAT_ONLY
            out.writeMedium(0)
            out.writeByte(0) // Gender.
            out.writeByte(0) // VERIFIED_EMAIL_ADDRESS
            out.writeByte(1)
            out.writeLong(Long.Companion.MAX_VALUE) // members subscription end
            out.write5(12)
            out.writeByte(0x1) // 0x1 - if members, 0x2 - subscription
            out.writeInt(1) // jcoins?
            out.writeByte(1) // is loyalty member?
            out.writeInt(1) // loyalty points?
            out.writeShort(592) // recovery questions set date
            out.writeShort(0) // Messages add support for forum integration
            out.writeShort(0) // last logged in date
            if (account.lastIp != null) {
                val ipSplit = account.lastIp!!.split("\\.")
                ipHash = ipSplit[0].toInt() shl 24 or (ipSplit[1].toInt() shl 16) or (ipSplit[2].toInt() shl 8) or ipSplit[3].toInt()
            }
            out.writeInt(ipHash) // ip part
            out.writeRSString(worldLoginToken)
            out.writeByte(2) // email status (0 - no email, 1 - pending parental confirmation, 2 - pending confirmation, 3 - registered)
            out.writeShort(302)
            out.writeShort(1)
            out.writeByte(1)
            out.writeJagString(account.displayName)
            out.writeByte(1)
            out.writeInt(1)
            out.writeByte(0) // Removed on EOC revisions for some reason. idk
            //val defWorld: World? = Lobby.getWorlds().getDefault().getInfo()
            //out.writeShort(if (defWorld == null) Lobby.WORLD_INFO.number() else defWorld.number()) // Default world ID
            //out.writeJagString(if (defWorld == null) Lobby.WORLD_INFO.ipAddress() else defWorld.ipAddress())
            out.writeShort(0)
            out.writeJagString("127.0.0.1")
        }
    }
