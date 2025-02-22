package org.darkan.core.net.prot.revision

import io.ktor.utils.io.*
import kotlinx.io.readByteArray
import org.darkan.core.clientwatch.MouseTrailStep
import org.darkan.core.clientwatch.MouseTrailStep.Type
import org.darkan.core.clientwatch.ReflectionCheckType
import org.darkan.core.clientwatch.ReflectionResponseCode
import org.darkan.core.net.prot.*
import world.gregs.voidps.buffer.*
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream

fun register727() =
Codec.register(727) {
    /*
     * Client protocol
     */
    //Utility/clientwatch
    clientProt<Ping>(opcode = 0)

    clientProt(opcode = 36, size = ProtSize.VarByte) {
        val responseCode = ReflectionResponseCode.fromId(readByte().toInt()) ?: throw IllegalArgumentException("Invalid response code")
        ReflectionResponse(responseCode, readRemaining().readByteArray())
    }

    clientProt(opcode = 62, size = 1) { ClientFocus(readBoolean()) }

    clientProt(opcode = 84, size = 6) {
        ClientScreenSize(
            displayMode = readUByte().toInt(),
            width = readUShort(),
            height = readUShort(),
            switchDisplayMode = readBoolean()
        )
    }

    clientProt(opcode = 83, size = 4) {
        MoveCamera(angleX = readShortAddLittle().toInt(), angleY = readShortAdd().toInt())
    }

    clientProt(opcode = 44, size = 9) { //TODO test
        SendFps(
            key1 = readIntInverseMiddle(),
            key2 = readUnsignedIntMiddle(),
            fps = readByteAdd().toInt(),
        )
    }

    clientProt(opcode = 59, size = 6) {
        val positionHash = readUnsignedIntLittle()
        val mouseHash = readShort()
        MouseClick(
            mouseButton = mouseHash.toInt() ushr 15,
            time = mouseHash.toInt() and 0x7FFF,
            x = positionHash and 0xFFFF,
            y = positionHash ushr 16
        )
    }

    clientProt(opcode = 57, size = 7) {
        val positionHash = readUnsignedIntLittle()
        val flags = readByteAdd()
        MouseButtonClick(
            mouseButton = flags ushr 1,
            time = readUnsignedShortLittle(),
            x = positionHash and 0xFFFF,
            y = positionHash ushr 16,
            hw = (flags and 1) == 1
        )
    }

    clientProt(opcodes = intArrayOf(40, 78), size = ProtSize.VarByte) { opcode ->
        val steps = mutableListOf<MouseTrailStep>()
        val frameSteps = readUByte().toInt()
        val frameCount = readUByte().toInt()
        while (!isClosedForRead) {
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

    clientProt(opcode = 28, size = ProtSize.VarShort) {
        //TODO
        KeyPress(readRemaining().readByteArray())
    }

    // Entity interactions
    val playerOpOpcodes = intArrayOf(66, 6, 31, 89, 103, 1, 51, 94, 53, 70)
    clientProt(opcodes = playerOpOpcodes, size = 3) { opcode ->
        OpPlayer(
            opNum = playerOpOpcodes.indexOf(opcode),
            playerIndex = readShort().toInt(),
            forceRun = readBooleanSubtract()
        )
    }

    val npcOpOpcodes = intArrayOf(65, 16, 50, 77, 95)
    clientProt(opcodes = npcOpOpcodes, size = 3) { opcode ->
        OpNpc(
            opNum = npcOpOpcodes.indexOf(opcode),
            npcIndex = readUShort().toInt(),
            forceRun = readBoolean()
        )
    }

    val objOpOpcodes = intArrayOf(75, 93, 38, 32, 48)
    clientProt(opcodes = objOpOpcodes, size = 9) { opcode ->
        OpObj(
            opNum = objOpOpcodes.indexOf(opcode),
            y = readUShort().toInt(),
            x = readUShort().toInt(),
            objectId = readInt(),
            forceRun = readBooleanAdd()
        )
    }

    val groundItemOpOpcodes = intArrayOf(24, 25, 54, 8, 43)
    clientProt(opcodes = groundItemOpOpcodes, size = 7) { opcode ->
        // TODO: Implement decode
        OpGroundItem(
            opNum = groundItemOpOpcodes.indexOf(opcode),
            itemId = 0,
            x = 0,
            y = 0,
            forceRun = false
        )
    }

    clientProt(opcode = 33, size = 5) {
        // TODO: Implement decode
        Walk(0, 0, false)
    }

    clientProt(opcode = 42, size = 18) {
        // TODO: Implement decode
        MiniWalk(0, 0, false)
    }

    // Item interactions
    clientProt(opcode = 67, size = 15) {
        // TODO: Implement decode
        IfOnGroundItem(0, 0, 0, 0, 0)
    }

    clientProt(opcode = 61, size = 7) {
        // TODO: Implement decode
        GroundItemExamine(0, 0, 0)
    }

    clientProt(opcode = 17, size = 2) {
        // TODO: Implement decode
        GeItemSelect(0)
    }

    // NPC interactions
    clientProt(opcode = 3, size = 3) {
        // TODO: Implement decode
        NpcExamine(0)
    }

    // Object interactions
    clientProt(opcode = 73, size = 9) {
        // TODO: Implement decode
        ObjectExamine(0, 0, 0)
    }

    clientProt(opcode = 98, size = 17) {
        // TODO: Implement decode
        IfOnObject(0, 0, 0, 0, 0)
    }

    // Interface interactions
    val ifButtonOpcodes = intArrayOf(96, 27, 68, 9, 72, 19, 23, 21, 22, 81)
    clientProt(opcodes = ifButtonOpcodes, size = 8) { opcode ->
        // TODO: Implement decode
        IfButton(opNum = ifButtonOpcodes.indexOf(opcode), interfaceId = 0, componentId = 0, slotId = 0)
    }

    clientProt(opcode = 4, size = 16) {
        // TODO: Implement decode
        IfOnIf(0, 0, 0, 0)
    }

    clientProt(opcode = 41, size = 11) {
        // TODO: Implement decode
        IfOnNpc(0, 0, 0)
    }

    clientProt(opcode = 13, size = 11) {
        // TODO: Implement decode
        IfOnPlayer(0, 0, 0)
    }

    clientProt(opcode = 46, size = 12) {
        // TODO: Implement decode
        IfOnTile(0, 0, 0, 0)
    }

    clientProt(opcode = 49, size = 6) {
        // TODO: Implement decode
        IfContinue(0, 0)
    }

    clientProt(opcode = 74, size = 16) {
        // TODO: Implement decode
        IfDragOntoIf(0, 0, 0, 0, 0, 0)
    }

    clientProt<CloseInterface>(opcode = 60)

    // Dialog interactions
    clientProt(opcode = 87, size = ProtSize.VarByte) {
        // TODO: Implement decode
        ResumeTextDialog("")
    }

    clientProt(opcode = 80, size = ProtSize.VarByte) {
        // TODO: Implement decode
        ResumeNameDialog("")
    }

    clientProt(opcode = 11, size = 2) {
        // TODO: Implement decode
        ResumeHSLDialog(0)
    }

    clientProt(opcode = 58, size = 4) {
        // TODO: Implement decode
        ResumeCountDialog(0)
    }

    clientProt(opcode = 69, size = ProtSize.VarByte) {
        // TODO: Implement decode
        ResumeClanForumQFCDialog(readRemaining().readByteArray())
    }

    // Communication
    clientProt(opcode = 86, size = ProtSize.VarByte) {
        // TODO: Implement decode
        Chat("", 0)
    }

    clientProt(opcode = 15, size = ProtSize.VarShort) {
        // TODO: Implement decode
        PrivateMessage("", "")
    }

    clientProt(opcode = 30, size = 1) {
        // TODO: Implement decode
        ChatType(0)
    }

    clientProt(opcode = 20, size = 3) {
        // TODO: Implement decode
        ChatSetFilter(0, 0, 0)
    }

    clientProt(opcode = 64, size = ProtSize.VarByte) {
        // TODO: Implement decode
        QuickChatPublic(readRemaining().readByteArray())
    }

    clientProt(opcode = 14, size = ProtSize.VarByte) {
        // TODO: Implement decode
        QuickChatPrivate(readRemaining().readByteArray())
    }

    // Friends/Ignore List
    clientProt(opcode = 26, size = ProtSize.VarByte) {
        // TODO: Implement decode
        AddFriend("")
    }

    clientProt(opcode = 29, size = ProtSize.VarByte) {
        // TODO: Implement decode
        RemoveFriend("")
    }

    clientProt(opcode = 34, size = ProtSize.VarByte) {
        // TODO: Implement decode
        AddIgnore("")
    }

    clientProt(opcode = 12, size = ProtSize.VarByte) {
        // TODO: Implement decode
        RemoveIgnore("")
    }

    // Friend Chat
    clientProt(opcode = 71, size = ProtSize.VarByte) {
        // TODO: Implement decode
        FcJoin("")
    }

    clientProt(opcode = 91, size = ProtSize.VarByte) {
        // TODO: Implement decode
        FcKick("")
    }

    clientProt(opcode = 7, size = ProtSize.VarByte) {
        // TODO: Implement decode
        FcSetRank("", 0)
    }

    // Clan Chat
    clientProt(opcode = 90, size = ProtSize.VarByte) {
        // TODO: Implement decode
        ClanChannelKickUser("")
    }

    // Game State
    clientProt<RegionLoadedConfirm>(opcode = 76)

    clientProt(opcode = 2, size = 4) {
        // TODO: Implement decode
        SoundEffectMusicEnded(0)
    }

    clientProt(opcode = 18, size = 4) {
        // TODO: Implement decode
        SongLoaded(0)
    }

    clientProt(opcode = 45, size = 1) {
        CutsceneFinished()
    }

    clientProt(opcode = 88, size = 2) {
        // TODO: Implement decode
        WritePing(0)
    }

    clientProt(opcode = 5, size = 4) {
        // TODO: Implement decode
        WorldMapClick(0)
    }

    clientProt(opcode = 10, size = ProtSize.VarByte) {
        SendPreferences(readRemaining().readByteArray())
    }

    clientProt(opcode = 55, size = 4) {
        // TODO: Implement decode
        TransmitvarVerifyid(0)
    }

    clientProt(opcode = 47, size = 4) {
        // TODO: Implement decode
        RequestWorldList(0)
    }

    // Reporting/Bug Tracking
    clientProt(opcode = 100, size = ProtSize.VarByte) {
        // TODO: Implement decode
        ReportAbuse("")
    }

    clientProt(opcode = 35, size = ProtSize.VarShort) {
        BugReport(readRemaining().readByteArray())
    }

    // Account/Login
    clientProt(opcode = 52, size = ProtSize.VarByte) {
        // TODO: Implement decode
        EmailValidationSubmitCode("")
    }

    clientProt(opcode = 92, size = ProtSize.VarShort) {
        // TODO: Implement decode
        EmailValidationAddNewAddress("")
    }

    clientProt(opcode = 99, size = ProtSize.VarShort) {
        // TODO: Implement decode
        EmailValidationChangeAddress("")
    }

    clientProt(opcode = 102, size = ProtSize.VarShort) {
        // TODO: Implement decode
        CheckEmailValidity("")
    }

    clientProt(opcode = 101, size = ProtSize.VarShort) {
        SendSignUpForm(readRemaining().readByteArray())
    }

    clientProt(opcode = 79, size = 1) {
        // TODO: Implement decode
        AccountCreationStage(0)
    }

    clientProt(opcode = 56, size = ProtSize.VarShort) {
        // TODO: Implement decode
        LobbyHyperlink("")
    }

    // Unknown/Misc
    clientProt(opcode = 37, size = 2) {
        // TODO: Implement decode
        Unk37(0)
    }

    clientProt(opcode = 63, size = 4) {
        // TODO: Implement decode
        Unk63(0)
    }

    clientProt(opcode = 82, size = 4) {
        // TODO: Implement decode
        Unk82(0)
    }

    clientProt(opcode = 97, size = ProtSize.VarByte) {
        Unk97(readRemaining().readByteArray())
    }

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
                    out.writeString(check.className)
                    out.writeString(check.methodName)
                    if (check.type == ReflectionCheckType.SET_INT)
                        check.fieldValue?.let { out.writeInt(it) }
                }
                ReflectionCheckType.GET_METHOD_RETURN_VALUE, ReflectionCheckType.GET_METHOD_MODIFIERS -> {
                    out.writeString(check.className)
                    out.writeString(check.methodName)
                    out.writeByte(check.paramTypes.size)
                    check.paramTypes.forEach { out.writeString(it) }
                    out.writeString(check.returnType ?: "void")
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