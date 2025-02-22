package org.darkan.core.net.prot

import org.darkan.core.clientwatch.MouseTrailStep
import org.darkan.core.clientwatch.ReflectionResponseCode

interface ClientProt

// Utility/Clientwatch packets
@JvmInline value class Ping(val dummy: Int = 0) : ClientProt // KEEPALIVE

data class ReflectionResponse(val code: ReflectionResponseCode, val data: ByteArray) : ClientProt {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReflectionResponse

        if (code != other.code) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = code.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}

@JvmInline value class ClientFocus(val focused: Boolean) : ClientProt
data class ClientScreenSize(val displayMode: Int, val width: Int, val height: Int, val switchDisplayMode: Boolean) : ClientProt
data class MoveCamera(val angleX: Int, val angleY: Int) : ClientProt
data class SendFps(val key1: Int, val key2: Int, val fps: Int) : ClientProt
data class MouseClick(val mouseButton: Int, val time: Int, val x: Int, val y: Int) : ClientProt
data class MouseButtonClick(val mouseButton: Int, val time: Int, val x: Int, val y: Int, val hw: Boolean) : ClientProt
data class MoveMouseData(val frameCount: Int, val frameSteps: Int, val steps: Array<MouseTrailStep>) : ClientProt {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MoveMouseData
        if (frameCount != other.frameCount) return false
        if (frameSteps != other.frameSteps) return false
        if (!steps.contentEquals(other.steps)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = frameCount
        result = 31 * result + frameSteps
        result = 31 * result + steps.contentHashCode()
        return result
    }
}

data class KeyPress(val keyData: ByteArray) : ClientProt { // TODO: Define data fields
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as KeyPress
        return keyData.contentEquals(other.keyData)
    }
    override fun hashCode() = keyData.contentHashCode()
}

data class ClientCheat(val command: String) : ClientProt // TODO: Define data fields

// Entity Interactions
data class OpPlayer(val opNum: Int, val playerIndex: Int, val forceRun: Boolean) : ClientProt
data class OpNpc(val opNum: Int, val npcIndex: Int, val forceRun: Boolean) : ClientProt
data class OpObj(val opNum: Int, val objectId: Int, val x: Int, val y: Int, val forceRun: Boolean) : ClientProt
data class OpGroundItem(val opNum: Int, val itemId: Int, val x: Int, val y: Int, val forceRun: Boolean) : ClientProt
data class Walk(val x: Int, val y: Int, val forceRun: Boolean) : ClientProt // TODO: Define data fields
data class MiniWalk(val x: Int, val y: Int, val forceRun: Boolean) : ClientProt // TODO: Define data fields

// Item Interactions
data class IfOnGroundItem(val interfaceId: Int, val componentId: Int, val itemId: Int, val x: Int, val y: Int) : ClientProt // TODO: Define data fields
data class GroundItemExamine(val itemId: Int, val x: Int, val y: Int) : ClientProt // TODO: Define data fields
data class GeItemSelect(val itemId: Int) : ClientProt // TODO: Define data fields

// NPC Interactions
data class NpcExamine(val npcIndex: Int) : ClientProt // TODO: Define data fields

// Object Interactions
data class ObjectExamine(val objectId: Int, val x: Int, val y: Int) : ClientProt // TODO: Define data fields
data class IfOnObject(val interfaceId: Int, val componentId: Int, val objectId: Int, val x: Int, val y: Int) : ClientProt // TODO: Define data fields

// Interface Interactions
data class IfButton(val opNum: Int, val interfaceId: Int, val componentId: Int, val slotId: Int) : ClientProt
data class IfOnIf(val sourceInterfaceId: Int, val sourceComponentId: Int, val targetInterfaceId: Int, val targetComponentId: Int) : ClientProt // TODO: Define data fields
data class IfOnNpc(val interfaceId: Int, val componentId: Int, val npcIndex: Int) : ClientProt // TODO: Define data fields
data class IfOnPlayer(val interfaceId: Int, val componentId: Int, val playerIndex: Int) : ClientProt // TODO: Define data fields
data class IfOnTile(val interfaceId: Int, val componentId: Int, val x: Int, val y: Int) : ClientProt // TODO: Define data fields
data class IfContinue(val interfaceId: Int, val componentId: Int) : ClientProt // TODO: Define data fields
data class IfDragOntoIf(val sourceInterfaceId: Int, val sourceComponentId: Int, val sourceSlotId: Int, val targetInterfaceId: Int, val targetComponentId: Int, val targetSlotId: Int) : ClientProt // TODO: Define data fields
data class CloseInterface(val dummy: Int = 0) : ClientProt

// Dialog Interactions
data class ResumeTextDialog(val text: String) : ClientProt // TODO: Define data fields
data class ResumeNameDialog(val name: String) : ClientProt // TODO: Define data fields
data class ResumeHSLDialog(val data: Int) : ClientProt // TODO: Define data fields
data class ResumeCountDialog(val count: Int) : ClientProt // TODO: Define data fields
data class ResumeClanForumQFCDialog(val data: ByteArray) : ClientProt { // TODO: Define data fields
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ResumeClanForumQFCDialog
        if (!data.contentEquals(other.data)) return false
        return true
    }
    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}

// Communication
data class Chat(val message: String, val effects: Int) : ClientProt // TODO: Define data fields
data class PrivateMessage(val username: String, val message: String) : ClientProt // TODO: Define data fields
data class ChatType(val type: Int) : ClientProt // TODO: Define data fields
data class ChatSetFilter(val publicMode: Int, val privateMode: Int, val tradeMode: Int) : ClientProt // TODO: Define data fields
data class QuickChatPublic(val data: ByteArray) : ClientProt { // TODO: Define data fields
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as QuickChatPublic
        if (!data.contentEquals(other.data)) return false
        return true
    }
    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}
data class QuickChatPrivate(val data: ByteArray) : ClientProt { // TODO: Define data fields
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as QuickChatPrivate
        if (!data.contentEquals(other.data)) return false
        return true
    }
    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}

// Friends/Ignore List
data class AddFriend(val username: String) : ClientProt // TODO: Define data fields
data class RemoveFriend(val username: String) : ClientProt // TODO: Define data fields
data class AddIgnore(val username: String) : ClientProt // TODO: Define data fields
data class RemoveIgnore(val username: String) : ClientProt // TODO: Define data fields

// Friend Chat
data class FcJoin(val fcName: String) : ClientProt // TODO: Define data fields
data class FcKick(val username: String) : ClientProt // TODO: Define data fields
data class FcSetRank(val username: String, val rank: Int) : ClientProt // TODO: Define data fields

// Clan Chat
data class ClanChannelKickUser(val username: String) : ClientProt // TODO: Define data fields

// Custom Clan Chat Opcodes
data class CcJoin(val dummy: Int = 0) : ClientProt
data class CcLeave(val dummy: Int = 0) : ClientProt
data class CcBan(val username: String) : ClientProt // TODO: Define data fields
data class ClanCheckName(val name: String) : ClientProt // TODO: Define data fields
data class ClanCreate(val name: String) : ClientProt // TODO: Define data fields
data class ClanLeave(val dummy: Int = 0) : ClientProt
data class ClanAddMember(val username: String) : ClientProt // TODO: Define data fields
data class ClanKickMember(val username: String) : ClientProt // TODO: Define data fields

// Game State
@JvmInline value class RegionLoadedConfirm(val dummy: Int = 0) : ClientProt
@JvmInline value class SoundEffectMusicEnded(val musicId: Int) : ClientProt // TODO: Define data fields
@JvmInline value class SongLoaded(val songId: Int) : ClientProt // TODO: Define data fields
data class CutsceneFinished(val dummy: Int = 0) : ClientProt // TODO: Define data fields
data class WritePing(val ping: Int) : ClientProt // TODO: Define data fields
data class WorldMapClick(val data: Int) : ClientProt // TODO: Define data fields
data class SendPreferences(val data: ByteArray) : ClientProt { // TODO: Define data fields
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SendPreferences
        if (!data.contentEquals(other.data)) return false
        return true
    }
    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}
data class TransmitvarVerifyid(val id: Int) : ClientProt // TODO: Define data fields
data class RequestWorldList(val version: Int) : ClientProt // TODO: Define data fields

// Reporting/Bug Tracking
data class ReportAbuse(val username: String) : ClientProt // TODO: Define data fields
data class BugReport(val data: ByteArray) : ClientProt { // TODO: Define data fields
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BugReport
        if (!data.contentEquals(other.data)) return false
        return true
    }
    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}

// Account/Login
data class EmailValidationSubmitCode(val code: String) : ClientProt // TODO: Define data fields
data class EmailValidationAddNewAddress(val email: String) : ClientProt // TODO: Define data fields
data class EmailValidationChangeAddress(val email: String) : ClientProt // TODO: Define data fields
data class CheckEmailValidity(val email: String) : ClientProt // TODO: Define data fields
data class SendSignUpForm(val data: ByteArray) : ClientProt { // TODO: Define data fields
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SendSignUpForm
        if (!data.contentEquals(other.data)) return false
        return true
    }
    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}
data class AccountCreationStage(val stage: Int) : ClientProt // TODO: Define data fields
data class LobbyHyperlink(val url: String) : ClientProt // TODO: Define data fields

// Unknown/Misc
data class Unk37(val data: Int) : ClientProt // TODO: Define data fields
data class Unk63(val data: Int) : ClientProt // TODO: Define data fields
data class Unk82(val data: Int) : ClientProt // TODO: Define data fields
data class Unk97(val data: ByteArray) : ClientProt { // TODO: Define data fields
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Unk97
        if (!data.contentEquals(other.data)) return false
        return true
    }
    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}