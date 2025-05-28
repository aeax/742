package org.darkan.core.net.prot

import org.darkan.core.clientwatch.MouseTrailStep
import org.darkan.core.clientwatch.ReflectionResponseCode
import org.darkan.core.model.Preference
import world.gregs.voidps.type.Tile

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

data class KeyPress(val keyCode: Int, val time: Int) : ClientProt
data class ClientCheat(val client: Boolean, val command: String) : ClientProt

// Entity Interactions
data class OpPlayer(val opNum: Int, val playerIndex: Int, val forceRun: Boolean) : ClientProt
data class OpNpc(val opNum: Int, val npcIndex: Int, val forceRun: Boolean) : ClientProt
data class OpObj(val opNum: Int, val objectId: Int, val x: Int, val y: Int, val forceRun: Boolean) : ClientProt
data class OpGroundItem(val opNum: Int, val itemId: Int, val x: Int, val y: Int, val forceRun: Boolean) : ClientProt
data class Walk(val x: Int, val y: Int, val forceRun: Boolean, val minimap: Boolean) : ClientProt

// Interface Interactions
data class IfButton(val opNum: Int, val interfaceId: Int, val componentId: Int, val slotId: Int, val itemId: Int) : ClientProt
data class IfOnIf(val fromInter: Int, val fromComp: Int, val fromSlot: Int, val fromItemId: Int, val toInter: Int, val toComp: Int, val toSlot: Int, val toItemId: Int) : ClientProt
data class IfOnObject(val interfaceId: Int, val componentId: Int, val slotId: Int, val itemId: Int, val objectId: Int, val x: Int, val y: Int, val forceRun: Boolean) : ClientProt
data class IfOnGroundItem(val interfaceId: Int, val componentId: Int, val slotId: Int, val itemIdContainer: Int, val itemId: Int, val x: Int, val y: Int, val forceRun: Boolean) : ClientProt
data class IfOnNpc(val interfaceId: Int, val componentId: Int, val slotId: Int, val itemId: Int, val npcIndex: Int, val forceRun: Boolean) : ClientProt
data class IfOnPlayer(val interfaceId: Int, val componentId: Int, val slotId: Int, val itemId: Int, val playerIndex: Int, val forceRun: Boolean) : ClientProt
data class IfOnTile(val interfaceId: Int, val componentId: Int, val slotId: Int, val itemId: Int, val x: Int, val y: Int) : ClientProt
data class IfContinue(val interfaceId: Int, val componentId: Int, val slotId: Int) : ClientProt
data class IfDragOntoIf(val fromInter: Int, val fromComp: Int, val fromSlot: Int, val fromItemId: Int, val toInter: Int, val toComp: Int, val toSlot: Int, val toItemId: Int) : ClientProt
data class CloseInterface(val dummy: Int = 0) : ClientProt

// Dialog Interactions
@JvmInline value class ResumeItemSelect(val itemId: Int) : ClientProt
@JvmInline value class ResumeTextDialog(val text: String) : ClientProt
@JvmInline value class ResumeNameDialog(val name: String) : ClientProt
@JvmInline value class ResumeHSLDialog(val data: Int) : ClientProt
@JvmInline value class ResumeCountDialog(val count: Int) : ClientProt
@JvmInline value class ResumeClanForumQFCDialog(val forumQfc: String) : ClientProt

// Communication
data class Chat(val type: Int = -1, val color: Int, val effect: Int, val message: String) : ClientProt
data class PrivateMessage(val toDisplayName: String, val message: String) : ClientProt
@JvmInline value class ChatType(val type: Int) : ClientProt
data class ChatSetFilter(val publicFilter: Int, val privateFilter: Int, val tradeFilter: Int) : ClientProt
data class QuickChatPublic(val chatType: Int, val qcId: Int, val messageData: ByteArray?) : ClientProt {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as QuickChatPublic
        if (chatType != other.chatType) return false
        if (qcId != other.qcId) return false
        if (!messageData.contentEquals(other.messageData)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = chatType
        result = 31 * result + qcId
        result = 31 * result + messageData.contentHashCode()
        return result
    }
}

data class QuickChatPrivate(val toUsername: String, val qcId: Int, val messageData: ByteArray?) : ClientProt {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as QuickChatPrivate
        if (qcId != other.qcId) return false
        if (toUsername != other.toUsername) return false
        if (!messageData.contentEquals(other.messageData)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = qcId
        result = 31 * result + toUsername.hashCode()
        result = 31 * result + messageData.contentHashCode()
        return result
    }
}

// Friends/Ignore List
@JvmInline value class AddFriend(val name: String) : ClientProt
@JvmInline value class RemoveFriend(val name: String) : ClientProt
data class AddIgnoreReq(val username: String, val temporary: Boolean) : ClientProt
@JvmInline value class RemoveIgnore(val name: String) : ClientProt

// Friend Chat
@JvmInline value class FcJoin(val fcName: String?) : ClientProt
@JvmInline value class FcKick(val username: String) : ClientProt
data class FcSetRank(val username: String, val rank: Int) : ClientProt

// Clan Chat
data class ClanChannelKickUser(val guest: Boolean, val pid: Int, val username: String) : ClientProt

// Game State
@JvmInline value class RegionLoadedConfirm(val dummy: Int = 0) : ClientProt
@JvmInline value class SoundEffectMusicEnded(val musicId: Int) : ClientProt
@JvmInline value class SongLoaded(val songId: Int) : ClientProt
@JvmInline value class CutsceneFinished(val forced: Boolean) : ClientProt
@JvmInline value class WritePing(val ping: Int) : ClientProt
@JvmInline value class WorldMapClick(val tile: Tile) : ClientProt
@JvmInline value class SendPreferences(val preferences: Map<Preference, Int>) : ClientProt
@JvmInline value class TransmitvarVerifyId(val id: Int) : ClientProt
@JvmInline value class RequestWorldList(val worldlistVersion: Int) : ClientProt

// Reporting/Bug Tracking
data class ReportAbuse(val username: String, val type: Int, val mute: Boolean, val reason: String) : ClientProt
data class BugReport(val category: Int, val body: String, val reproSteps: String) : ClientProt

// Account/Login
@JvmInline value class EmailValidationSubmitCode(val code: String) : ClientProt
data class EmailValidationAddNewAddress(val email: String, val flags: Int) : ClientProt
data class EmailValidationChangeAddress(val email: String, val email2: String) : ClientProt
@JvmInline value class CheckEmailValidity(val encryptedData: ByteArray) : ClientProt
@JvmInline value class SendSignUpForm(val encryptedData: ByteArray) : ClientProt
@JvmInline value class AccountCreationStage(val stage: Int) : ClientProt
data class LobbyHyperlink(val service: String, val page: String, val query: String, val flags: Int) : ClientProt

// Unknown/Misc
@JvmInline value class PlayVorbis(val fileId: Int) : ClientProt
data class AltWalk(val x: Int, val y: Int) : ClientProt
@JvmInline value class AppletLoadingPleaseWait(val data: Int) : ClientProt
@JvmInline value class UnkCs2StringResponse(val str: String) : ClientProt