package org.darkan.core.net.prot

import org.darkan.core.clientwatch.ReflectionCheck
import org.darkan.core.social.ChatMessage
import org.darkan.core.social.QuickChatMessage
import org.darkan.core.model.Account
import org.darkan.core.model.ChatMessageType
import org.darkan.core.model.RegionSize
import org.darkan.core.worldlist.WorldList
import world.gregs.voidps.type.Tile

interface ServerProt

data class IfSetPlayerHead(val interfaceId: Int, val componentId: Int) : ServerProt

data class IfSetPlayerHeadIgnoreWorn(val interfaceId: Int, val componentId: Int, val identiKit1: Int, val identiKit2: Int, val identiKit3: Int) : ServerProt

data class IfSetPlayerModel(val dummy: Int
    // TODO: Add fields based on analysis of opcode 72, size 4
) : ServerProt

data class IfSetTextFont(val interfaceId: Int, val componentId: Int, val fontId: Int) : ServerProt

data class IfMoveSub(val fromInterfaceId: Int, val fromComponentId: Int, val toInterfaceId: Int, val toComponentId: Int) : ServerProt

data class IfOpenSubActiveObject(val dummy: Int
    // TODO: Add fields based on analysis of opcode 23, size 32
) : ServerProt

data class IfSetHide(val dummy: Int
    // TODO: Add fields based on analysis of opcode 33, size 5
) : ServerProt

data class IfOpenSubActivePlayer(val dummy: Int
    // TODO: Add fields based on analysis of opcode 59, size 25
) : ServerProt

data class IfSetGraphic(val dummy: Int
    // TODO: Add fields based on analysis of opcode 35, size 8
) : ServerProt

data class IfSetTextAntiMacro(val dummy: Int
    // TODO: Add fields based on analysis of opcode 36, size 5
) : ServerProt

data class IfOpenTop(val dummy: Int
    // TODO: Add fields based on analysis of opcode 37, size 19
) : ServerProt

data class IfOpenSub(val dummy: Int
    // TODO: Add fields based on analysis of opcode 38, size 23
) : ServerProt

data class IfSetAngle(val dummy: Int
    // TODO: Add fields based on analysis of opcode 44, size 10
) : ServerProt

data class IfSetPosition(val dummy: Int
    // TODO: Add fields based on analysis of opcode 48, size 8
) : ServerProt

data class IfSetPlayerModelOther(val dummy: Int
    // TODO: Add fields based on analysis of opcode 53, size 10
) : ServerProt

data class IfSetScrollPos(val dummy: Int
    // TODO: Add fields based on analysis of opcode 55, size 6
) : ServerProt

data class IfOpenSubActiveNpc(val dummy: Int
    // TODO: Add fields based on analysis of opcode 34, size 25
) : ServerProt

data class IfCloseSub(val dummy: Int
    // TODO: Add fields based on analysis of opcode 78, size 4
) : ServerProt

data class IfSetAnim(val dummy: Int
    // TODO: Add fields based on analysis of opcode 82, size 8
) : ServerProt

data class IfSetColor(val dummy: Int
    // TODO: Add fields based on analysis of opcode 105, size 6
) : ServerProt

data class IfSetNpcHead(val dummy: Int
    // TODO: Add fields based on analysis of opcode 109, size 8
) : ServerProt

data class IfSetItem(val dummy: Int
    // TODO: Add fields based on analysis of opcode 112, size 10
) : ServerProt

data class IfSetEvents(val dummy: Int
    // TODO: Add fields based on analysis of opcode 121, size 12
) : ServerProt

data class IfSetText(val dummy: Int
    // TODO: Add fields based on analysis of opcode 124, size -2
) : ServerProt

data class IfSetReTex(val dummy: Int
    // TODO: Add fields based on analysis of opcode 132, size 9
) : ServerProt

data class IfSetModel(val dummy: Int
    // TODO: Add fields based on analysis of opcode 135, size 8
) : ServerProt

data class IfSetReCol(val dummy: Int
    // TODO: Add fields based on analysis of opcode 144, size 9
) : ServerProt

data class IfSetTargetParam(val dummy: Int
    // TODO: Add fields based on analysis of opcode 148, size 10
) : ServerProt

data class IfSetClickMask(val dummy: Int
    // TODO: Add fields based on analysis of opcode 150, size 5
) : ServerProt

data class IfOpenSubActiveGroundItem(val dummy: Int
    // TODO: Add fields based on analysis of opcode 155, size 29
) : ServerProt

data class IfSetPlayerHeadOther(val dummy: Int
    // TODO: Add fields based on analysis of opcode 158, size 10
) : ServerProt

/**
 * Update and inventory protocols
 */
data class UpdateInvPartial(val dummy: Int
    // TODO: Add fields based on analysis of opcode 4, size -2
) : ServerProt

data class UpdateInvFull(val dummy: Int
    // TODO: Add fields based on analysis of opcode 5, size -2
) : ServerProt

data class NpcUpdate(val dummy: Int
    // TODO: Add fields based on analysis of opcode 6, size -2
) : ServerProt

data class UpdateZoneFullFollows(val dummy: Int
    // TODO: Add fields based on analysis of opcode 15, size 3
) : ServerProt

data class UpdateFriendchatChannelSingleUser(val dummy: Int
    // TODO: Add fields based on analysis of opcode 19, size -1
) : ServerProt

data class UpdateZonePartialFollows(val dummy: Int
    // TODO: Add fields based on analysis of opcode 41, size 3
) : ServerProt

data class UpdateInvStopTransmit(val dummy: Int
    // TODO: Add fields based on analysis of opcode 42, size 3
) : ServerProt

data class PlayerUpdate(val dummy: Int
    // TODO: Add fields based on analysis of opcode 43, size -2
) : ServerProt

data class NpcUpdateLarge(val dummy: Int
    // TODO: Add fields based on analysis of opcode 47, size -2
) : ServerProt

data class ClanChannelDelta(val dummy: Int
    // TODO: Add fields based on analysis of opcode 28, size -2
) : ServerProt

data class ClanChannelFull(val dummy: Int
    // TODO: Add fields based on analysis of opcode 49, size -2
) : ServerProt

data class ClanSettingsDelta(val dummy: Int
    // TODO: Add fields based on analysis of opcode 46, size -2
) : ServerProt

data class ClanSettingsFull(val dummy: Int
    // TODO: Add fields based on analysis of opcode 137, size -2
) : ServerProt

data class UpdateZonePartialEnclosed(val dummy: Int
    // TODO: Add fields based on analysis of opcode 65, size -2
) : ServerProt

data class UpdateGESlot(val dummy: Int
    // TODO: Add fields based on analysis of opcode 57, size 20
) : ServerProt

@JvmInline value class UpdateUid192(val data: ByteArray) : ServerProt

data class UpdateIgnoreList(val dummy: Int
    // TODO: Add fields based on analysis of opcode 97, size -2
) : ServerProt

data class UpdateStat(val dummy: Int
    // TODO: Add fields based on analysis of opcode 140, size 6
) : ServerProt

/**
 * Region and map related protocols
 */
data class BuildRegion(
    val regionIds: IntArray,
    val mapSize: RegionSize = RegionSize.SIZE_104,
    val xteas: Array<IntArray>,
    val chunkX: Int = 0,
    val chunkY: Int = 0,
    val forceMapRefresh: Boolean = false,
    val localPlayerPid: Int? = null,
    val localPlayerBaseTileHash: Int? = null,
    val otherPlayerRegionIds: IntArray? = null
) : ServerProt {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BuildRegion

        if (chunkX != other.chunkX) return false
        if (chunkY != other.chunkY) return false
        if (forceMapRefresh != other.forceMapRefresh) return false
        if (localPlayerPid != other.localPlayerPid) return false
        if (localPlayerBaseTileHash != other.localPlayerBaseTileHash) return false
        if (!regionIds.contentEquals(other.regionIds)) return false
        if (mapSize != other.mapSize) return false
        if (!xteas.contentDeepEquals(other.xteas)) return false
        if (!otherPlayerRegionIds.contentEquals(other.otherPlayerRegionIds)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = chunkX
        result = 31 * result + chunkY
        result = 31 * result + forceMapRefresh.hashCode()
        result = 31 * result + (localPlayerPid ?: 0)
        result = 31 * result + (localPlayerBaseTileHash ?: 0)
        result = 31 * result + regionIds.contentHashCode()
        result = 31 * result + mapSize.hashCode()
        result = 31 * result + xteas.contentDeepHashCode()
        result = 31 * result + (otherPlayerRegionIds?.contentHashCode() ?: 0)
        return result
    }
}

enum class InstanceLoadType(val index: Int) {
    // not sure if zero is even a valid input to the packet glancing at the
    // client logic, it shouldn't really perform different than 1 as they both
    // set the loading type to the same value
    STANDARD(1),                   //No difference from non-dynamic logic essentially
    STANDARD_NO_ENTITY_RESET(2),   //Same as above except this one won't reset local entities on load (Matrix and most other bases default to this)
    LARGE(3),                      //Ignores render-distance restrictions on zone updates (current Void default)
    LARGE_NO_UPDATEZONE_RESET(4)   //Ignores render-distance restrictions on zone updates and doesn't reset objs, locs, etc on load
}

data class BuildInstancedRegion(
    val mapSize: RegionSize = RegionSize.SIZE_104,
    val chunks: IntArray,
    val xteas: Array<IntArray>,
    val chunkX: Int,
    val chunkY: Int,
    val forceMapRefresh: Boolean = false,
    val instanceLoadType: InstanceLoadType = InstanceLoadType.STANDARD,
    val localPlayerPid: Int? = null,
    val localPlayerBaseTileHash: Int? = null,
    val otherPlayerRegionIds: IntArray? = null
) : ServerProt {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BuildInstancedRegion
        if (chunkX != other.chunkX) return false
        if (chunkY != other.chunkY) return false
        if (forceMapRefresh != other.forceMapRefresh) return false
        if (localPlayerPid != other.localPlayerPid) return false
        if (localPlayerBaseTileHash != other.localPlayerBaseTileHash) return false
        if (mapSize != other.mapSize) return false
        if (!chunks.contentEquals(other.chunks)) return false
        if (!xteas.contentDeepEquals(other.xteas)) return false
        if (instanceLoadType != other.instanceLoadType) return false
        if (!otherPlayerRegionIds.contentEquals(other.otherPlayerRegionIds)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = chunkX
        result = 31 * result + chunkY
        result = 31 * result + forceMapRefresh.hashCode()
        result = 31 * result + (localPlayerPid ?: 0)
        result = 31 * result + (localPlayerBaseTileHash ?: 0)
        result = 31 * result + mapSize.hashCode()
        result = 31 * result + chunks.contentHashCode()
        result = 31 * result + xteas.contentDeepHashCode()
        result = 31 * result + instanceLoadType.hashCode()
        result = 31 * result + (otherPlayerRegionIds?.contentHashCode() ?: 0)
        return result
    }
}

data class ShowFaceHere(val dummy: Int
    // TODO: Add fields based on analysis of opcode 21, size 1
) : ServerProt

data class MinimapFlag(val dummy: Int
    // TODO: Add fields based on analysis of opcode 70, size 2
) : ServerProt

data class HintArrow(val dummy: Int
    // TODO: Add fields based on analysis of opcode 79, size 14
) : ServerProt

data class HintTrail(val dummy: Int
    // TODO: Add fields based on analysis of opcode 149, size -2
) : ServerProt

data class MapProjAnim(val dummy: Int
    // TODO: Add fields based on analysis of opcode 143, size 16
) : ServerProt

data class MapProjAnimHalfSq(val dummy: Int
    // TODO: Add fields based on analysis of opcode 95, size 17
) : ServerProt

data class SetDrawOrder(val dummy: Int
    // TODO: Add fields based on analysis of opcode 151, size 1
) : ServerProt

data class BlockMinimapState(val dummy: Int
    // TODO: Add fields based on analysis of opcode 154, size 1
) : ServerProt

/**
 * Object and ground item protocols
 */
data class GroundItemCount(val dummy: Int
    // TODO: Add fields based on analysis of opcode 3, size 7
) : ServerProt

data class CreateGroundItem(val dummy: Int
    // TODO: Add fields based on analysis of opcode 27, size 5
) : ServerProt

data class GroundItemReveal(val dummy: Int
    // TODO: Add fields based on analysis of opcode 107, size 7
) : ServerProt

data class RemoveGroundItem(val dummy: Int
    // TODO: Add fields based on analysis of opcode 125, size 3
) : ServerProt

data class DestroyObject(val dummy: Int
    // TODO: Add fields based on analysis of opcode 69, size 2
) : ServerProt

data class ObjAnim(val dummy: Int
    // TODO: Add fields based on analysis of opcode 113, size 6
) : ServerProt

data class ObjAnimSpecific(val dummy: Int
    // TODO: Add fields based on analysis of opcode 80, size 9
) : ServerProt

data class CreateObject(val dummy: Int
    // TODO: Add fields based on analysis of opcode 117, size 6
) : ServerProt

data class TileMessage(val dummy: Int
    // TODO: Add fields based on analysis of opcode 114, size -1
) : ServerProt

data class CustomizeObject(val dummy: Int
    // TODO: Add fields based on analysis of opcode 84, size -1
) : ServerProt

data class ObjectPrefetch(val dummy: Int
    // TODO: Add fields based on analysis of opcode 88, size 5
) : ServerProt

/**
 * Animation related protocols
 */
data class SpotAnim(val dummy: Int
    // TODO: Add fields based on analysis of opcode 139, size 8
) : ServerProt

data class SpotAnimSpecific(val dummy: Int
    // TODO: Add fields based on analysis of opcode 126, size 12
) : ServerProt

data class AnimateNpc(val dummy: Int
    // TODO: Add fields based on analysis of opcode 145, size 19
) : ServerProt

data class ProjAnimSpecific(val dummy: Int
    // TODO: Add fields based on analysis of opcode 56, size 22
) : ServerProt

data class ResetAllAnimations(val dummy: Int
    // TODO: Add fields based on analysis of opcode 122, size 0
) : ServerProt

/**
 * Audio related protocols
 */
data class VorbisSound(val dummy: Int
    // TODO: Add fields based on analysis of opcode 18, size 8
) : ServerProt

data class SoundEffectTile(val dummy: Int
    // TODO: Add fields based on analysis of opcode 128, size 6
) : ServerProt

data class VorbisSpeechSound(val dummy: Int
    // TODO: Add fields based on analysis of opcode 104, size 6
) : ServerProt

data class SoundSynth(val dummy: Int
    // TODO: Add fields based on analysis of opcode 136, size 8
) : ServerProt

data class MusicEffect(val dummy: Int
    // TODO: Add fields based on analysis of opcode 60, size 6
) : ServerProt

data class MusicTrack(val dummy: Int
    // TODO: Add fields based on analysis of opcode 63, size 4
) : ServerProt

data class MusicTrackTile(val dummy: Int
    // TODO: Add fields based on analysis of opcode 93, size 11
) : ServerProt

data class PreloadSong(val dummy: Int
    // TODO: Add fields based on analysis of opcode 77, size 2
) : ServerProt

data class SoundMixbussSetLevel(val dummy: Int
    // TODO: Add fields based on analysis of opcode 134, size 2
) : ServerProt

data class ResetSounds(val dummy: Int
    // TODO: Add fields based on analysis of opcode 120, size 0
) : ServerProt

/**
 * Camera related protocols
 */
data class CamLookAt(val viewLocalX: Int, val viewLocalY: Int, val viewZ: Int, val speed1: Int, val speed2: Int) : ServerProt
data class CamMoveTo(val moveLocalX: Int, val moveLocalY: Int, val moveZ: Int, val speed1: Int, val speed2: Int) : ServerProt
@JvmInline value class CamRemoveRoof(val tile: Tile) : ServerProt
@JvmInline value class CamResetHard(val dummy: Int = 0) : ServerProt
@JvmInline value class CamResetSmooth(val dummy: Int = 0) : ServerProt
data class CamShake(val slotId: Int, val v1: Int, val v2: Int, val v3: Int, val v4: Int) : ServerProt
data class CamForceAngle(val angleX: Int, val angleY: Int) : ServerProt

/**
 * Messaging protocols
 */
data class MessagePrivate(val crown: Int, val displayName: String, val quickResponseName: String = displayName, val message: String) : ServerProt
data class MessagePrivateEcho(val senderDisplayName: String, val message: String) : ServerProt

data class MessageQuickChatPrivate(val crown: Int, val displayName: String, val quickResponseName: String = displayName, val message: QuickChatMessage) : ServerProt
data class MessageQuickChatPrivateEcho(val senderDisplayName: String, val message: QuickChatMessage) : ServerProt

data class MessageFriendsChat(val crown: Int, val displayName: String, val quickResponseName: String = displayName, val chatName: String, val message: String) : ServerProt
data class MessageQuickChatFriendsChat(val chatName: String, val crown: Int, val displayName: String, val quickResponseName: String = displayName, val message: QuickChatMessage) : ServerProt

data class MessageClanChannel(val guest: Boolean, val crown: Int, val displayName: String, val message: String) : ServerProt
data class MessageQuickChatClanChannel(val guest: Boolean, val crown: Int, val displayName: String, val message: QuickChatMessage) : ServerProt

data class MessagePlayerGroup(val crown: Int, val displayName: String, val quickResponseName: String = displayName, val message: String) : ServerProt
data class MessageQuickChatPlayerGroup(val crown: Int, val displayName: String, val quickResponseName: String = displayName, val message: QuickChatMessage) : ServerProt

data class MessagePublic(val pid: Int, val messageIcon: Int, val message: ChatMessage) : ServerProt

data class GameMessage(val type: ChatMessageType, val message: String, val targetDisplayName: String? = null, val effectFlags: Int = 0) : ServerProt

/**
 * Variable related protocols
 */
data class VarpSmall(val id: Int, val value: Int) : ServerProt
data class VarpLarge(val id: Int, val value: Int) : ServerProt
fun setVarpPacket(id: Int, value: Int) = if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) VarpLarge(id, value) else VarpSmall(id, value)

data class ClientSetVarcSmall(val id: Int, val value: Int) : ServerProt
data class ClientSetVarcLarge(val id: Int, val value: Int) : ServerProt
fun setVarcPacket(id: Int, value: Int) = if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) ClientSetVarcLarge(id, value) else ClientSetVarcSmall(id, value)

data class VarbitSmall(val id: Int, val value: Int) : ServerProt
data class VarbitLarge(val id: Int, val value: Int) : ServerProt
fun setVarbitPacket(id: Int, value: Int) = if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) VarbitLarge(id, value) else VarbitSmall(id, value)

data class ClientSetVarcStrSmall(val id: Int, val value: String) : ServerProt
data class ClientSetVarcStrLarge(val id: Int, val value: String) : ServerProt
fun setVarcStrPacket(id: Int, value: String) = if (value.length+2 > Byte.MAX_VALUE) ClientSetVarcStrLarge(id, value) else ClientSetVarcStrSmall(id, value)

data class VarclanSetLong(val id: Int, val value: Long) : ServerProt
data class VarclanSetByte(val id: Int, val value: Int) : ServerProt
data class VarclanSetInt(val id: Int, val value: Int) : ServerProt
data class VarclanSetString(val id: Int, val value: String) : ServerProt

@JvmInline value class VarclanEnable(val dummy: Int = 0) : ServerProt
@JvmInline value class VarclanDisable(val dummy: Int = 0) : ServerProt
@JvmInline value class ClearVarps(val dummy: Int = 0) : ServerProt

/**
 * Player related protocols
 */
@JvmInline value class PlayerWeight(val weight: Int) : ServerProt

data class PlayerOption(val option: String, val slot: Int, val top: Boolean, val cursor: Int) : ServerProt

@JvmInline value class RunEnergy(val energy: Int) : ServerProt

data class FriendStatus(val dummy: Int
    // TODO: Add fields based on analysis of opcode 74, size -2
) : ServerProt

data class FriendlistLoaded(val dummy: Int
    // TODO: Add fields based on analysis of opcode 101, size 0
) : ServerProt

data class AddIgnore(val dummy: Int
    // TODO: Add fields based on analysis of opcode 138, size -1
) : ServerProt

data class ReduceAttackPriority(val dummy: Int
    // TODO: Add fields based on analysis of opcode 61, size 1
) : ServerProt

data class SetTarget(val dummy: Int
    // TODO: Add fields based on analysis of opcode 129, size 2
) : ServerProt

/**
 * Friend and clan chat protocols
 */
data class FriendsChatChannel(val dummy: Int
    // TODO: Add fields based on analysis of opcode 127, size -2
) : ServerProt

/**
 * Login and system related protocols
 */
data class LogoutLobby(val dummy: Int
    // TODO: Add fields based on analysis of opcode 9, size 0
) : ServerProt

data class LogoutFull(val dummy: Int
    // TODO: Add fields based on analysis of opcode 62, size 0
) : ServerProt

data class OpenUrl(val dummy: Int
    // TODO: Add fields based on analysis of opcode 16, size -2
) : ServerProt

data class WorldListPacket(val worldList: WorldList, val refreshOnClient: Boolean, val refreshOnlyPlayerCounts: Boolean) : ServerProt

data class IdentifyHostName(val dummy: Int
    // TODO: Add fields based on analysis of opcode 147, size 4
) : ServerProt

data class UpdateRebootTimer(val dummy: Int
    // TODO: Add fields based on analysis of opcode 102, size 2
) : ServerProt

data class RequestFps(val dummy: Int
    // TODO: Add fields based on analysis of opcode 159, size 8
) : ServerProt

data class UpdateSitesettingsCookie(val dummy: Int
    // TODO: Add fields based on analysis of opcode 91, size -1
) : ServerProt

data class ChatFilterSettings(val dummy: Int
    // TODO: Add fields based on analysis of opcode 30, size 2
) : ServerProt

data class ChatFilterSettingsPrivateChat(val dummy: Int
    // TODO: Add fields based on analysis of opcode 67, size 1
) : ServerProt

/**
 * Account related protocols
 */
@JvmInline value class CreateCheckEmailReply(val responseCode: Int) : ServerProt

enum class CreateAccountReplyOpcode(val value: Int) {
    ERROR_CONTACTING_SERVER(1),
    LOGIN(2),
    CANNOT_CREATE_ACCOUNT_ATM(9),
    UNEXPECTED_SERVER_RESPONSE(10),
    EMAIL_ALREADY_IN_USE(20),
    INVALID_EMAIL(21),
    PLEASE_SUPPLY_VALID_PASS(30),
    PASSWORDS_MAY_ONLY_CONTAIN_LETTERS_AND_NUMBERS(31),
    PASSWORD_TOO_EASY(32)
}

@JvmInline value class CreateAccountReply(val code: CreateAccountReplyOpcode) : ServerProt

data class UpdateDob(val dummy: Int
    // TODO: Add fields based on analysis of opcode 157, size 4
) : ServerProt

/**
 * Miscellaneous protocols
 */
class Pong : ServerProt

data class ProcessDevConsoleCommand(val dummy: Int
    // TODO: Add fields based on analysis of opcode 2, size -1
) : ServerProt

data class ReflectionRequest(val id: Int, val checks: Array<ReflectionCheck>) : ServerProt {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ReflectionRequest
        if (id != other.id) return false
        if (!checks.contentEquals(other.checks)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + checks.contentHashCode()
        return result
    }
}

data class RunCs2Script(val dummy: Int
    // TODO: Add fields based on analysis of opcode 99, size -2
) : ServerProt

data class SetCursor(val dummy: Int
    // TODO: Add fields based on analysis of opcode 153, size -1
) : ServerProt

data class Cutscene(val dummy: Int
    // TODO: Add fields based on analysis of opcode 110, size -2
) : ServerProt

data class TriggerOnDialogAbort(val dummy: Int
    // TODO: Add fields based on analysis of opcode 146, size 0
) : ServerProt

data class KeepAlive(val dummy: Int
    // TODO: Add fields based on analysis of opcode 29, size 0
) : ServerProt

data class LoyaltyUpdate(val dummy: Int
    // TODO: Add fields based on analysis of opcode 32, size 5
) : ServerProt

data class JCoinsUpdate(val dummy: Int
    // TODO: Add fields based on analysis of opcode 75, size 4
) : ServerProt

data class ApplyDebug(val dummy: Int
    // TODO: Add fields based on analysis of opcode 76, size 2
) : ServerProt

data class QuickHopWorlds(val dummy: Int
    // TODO: Add fields based on analysis of opcode 58, size -1
) : ServerProt

data class DebugServerTriggers(val dummy: Int
    // TODO: Add fields based on analysis of opcode 73, size -1
) : ServerProt

// Note: These pre-world packets would need special handling
data class WorldLoginDetails(val dummy: Int
    // TODO: Add fields based on analysis of pre-world opcode 2, size -1
) : ServerProt

data class LobbyLoginDetails(val account: Account, val worldLoginToken: String) : ServerProt