package org.darkan.core.net.prot

import org.darkan.core.clientwatch.ReflectionCheck
import org.darkan.core.type.RegionSize

interface ServerProt

data class IfSetPlayerHead(val dummy: Int
    // TODO: Add fields based on analysis of opcode 0, size 4
) : ServerProt

data class IfSetTextFont(val dummy: Int
    // TODO: Add fields based on analysis of opcode 7, size 8
) : ServerProt

data class IfMoveSub(val dummy: Int
    // TODO: Add fields based on analysis of opcode 13, size 8
) : ServerProt

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

data class IfSetPlayerModel(val dummy: Int
    // TODO: Add fields based on analysis of opcode 72, size 4
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

data class IfSetPlayerHeadIgnoreWorn(val dummy: Int
    // TODO: Add fields based on analysis of opcode 130, size 10
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

data class UpdateUid192(val dummy: Int
    // TODO: Add fields based on analysis of opcode 90, size 28
) : ServerProt

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
data class CamLookAt(val dummy: Int
    // TODO: Add fields based on analysis of opcode 24, size 6
) : ServerProt

data class CamMoveTo(val dummy: Int
    // TODO: Add fields based on analysis of opcode 39, size 6
) : ServerProt

data class CamRemoveRoof(val dummy: Int
    // TODO: Add fields based on analysis of opcode 40, size 4
) : ServerProt

data class CamResetHard(val dummy: Int
    // TODO: Add fields based on analysis of opcode 66, size 0
) : ServerProt

data class CamShake(val dummy: Int
    // TODO: Add fields based on analysis of opcode 71, size 6
) : ServerProt

data class CamResetSmooth(val dummy: Int
    // TODO: Add fields based on analysis of opcode 89, size 0
) : ServerProt

data class CamForceAngle(val dummy: Int
    // TODO: Add fields based on analysis of opcode 118, size 4
) : ServerProt

/**
 * Messaging protocols
 */
data class MessagePrivateEcho(val dummy: Int
    // TODO: Add fields based on analysis of opcode 10, size -2
) : ServerProt

data class MessageQuickChatPrivate(val dummy: Int
    // TODO: Add fields based on analysis of opcode 22, size -1
) : ServerProt

data class MessageFriendsChat(val dummy: Int
    // TODO: Add fields based on analysis of opcode 25, size -1
) : ServerProt

data class MessageQuickChatPrivateEcho(val dummy: Int
    // TODO: Add fields based on analysis of opcode 31, size -1
) : ServerProt

data class MessageClanChannel(val dummy: Int
    // TODO: Add fields based on analysis of opcode 81, size -1
) : ServerProt

data class MessageQuickChatPlayerGroup(val dummy: Int
    // TODO: Add fields based on analysis of opcode 83, size -1
) : ServerProt

data class SendPrivateMessage(val dummy: Int
    // TODO: Add fields based on analysis of opcode 92, size -2
) : ServerProt

data class MessageQuickChatFriendsChat(val dummy: Int
    // TODO: Add fields based on analysis of opcode 142, size -1
) : ServerProt

data class MessageQuickChatClanChannel(val dummy: Int
    // TODO: Add fields based on analysis of opcode 131, size -1
) : ServerProt

data class MessagePlayerGroup(val dummy: Int
    // TODO: Add fields based on analysis of opcode 133, size -1
) : ServerProt

data class MessagePublic(val dummy: Int
    // TODO: Add fields based on analysis of opcode 152, size -1
) : ServerProt

data class GameMessage(val dummy: Int
    // TODO: Add fields based on analysis of opcode 160, size -1
) : ServerProt

data class TileMessage(val dummy: Int
    // TODO: Add fields based on analysis of opcode 114, size -1
) : ServerProt

/**
 * Variable related protocols
 */
data class VarpLarge(val dummy: Int
    // TODO: Add fields based on analysis of opcode 8, size 6
) : ServerProt

data class ClientSetVarcLarge(val dummy: Int
    // TODO: Add fields based on analysis of opcode 12, size 6
) : ServerProt

data class ClientSetVarcSmall(val dummy: Int
    // TODO: Add fields based on analysis of opcode 116, size 3
) : ServerProt

data class VarbitSmall(val dummy: Int
    // TODO: Add fields based on analysis of opcode 68, size 3
) : ServerProt

data class VarbitLarge(val dummy: Int
    // TODO: Add fields based on analysis of opcode 108, size 6
) : ServerProt

data class VarpSmall(val dummy: Int
    // TODO: Add fields based on analysis of opcode 115, size 3
) : ServerProt

data class ClientSetVarcStrSmall(val dummy: Int
    // TODO: Add fields based on analysis of opcode 54, size -1
) : ServerProt

data class ClientSetVarcStrLarge(val dummy: Int
    // TODO: Add fields based on analysis of opcode 119, size -2
) : ServerProt

data class VarclanSetLong(val dummy: Int
    // TODO: Add fields based on analysis of opcode 26, size 10
) : ServerProt

data class VarclanSetByte(val dummy: Int
    // TODO: Add fields based on analysis of opcode 123, size 3
) : ServerProt

data class VarclanSetInt(val dummy: Int
    // TODO: Add fields based on analysis of opcode 141, size 6
) : ServerProt

data class VarclanEnable(val dummy: Int
    // TODO: Add fields based on analysis of opcode 45, size 0
) : ServerProt

data class VarclanDisable(val dummy: Int
    // TODO: Add fields based on analysis of opcode 94, size 0
) : ServerProt

data class ClearVarps(val dummy: Int
    // TODO: Add fields based on analysis of opcode 100, size 0
) : ServerProt

/**
 * Player related protocols
 */
data class PlayerWeight(val dummy: Int
    // TODO: Add fields based on analysis of opcode 14, size 2
) : ServerProt

data class PlayerOption(val dummy: Int
    // TODO: Add fields based on analysis of opcode 111, size -1
) : ServerProt

data class RunEnergy(val dummy: Int
    // TODO: Add fields based on analysis of opcode 64, size 1
) : ServerProt

data class FriendStatus(val dummy: Int
    // TODO: Add fields based on analysis of opcode 74, size -2
) : ServerProt

data class FriendlistLoaded(val dummy: Int
    // TODO: Add fields based on analysis of opcode 101, size 0
) : ServerProt

data class AddIgnoreReq(val dummy: Int
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

data class SetClanString(val dummy: Int
    // TODO: Add fields based on analysis of opcode 50, size -1
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

data class WorldList(val dummy: Int
    // TODO: Add fields based on analysis of opcode 103, size -2
) : ServerProt

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
data class CreateCheckEmailReply(val dummy: Int
    // TODO: Add fields based on analysis of opcode 1, size 1
) : ServerProt

data class CreateAccountReply(val dummy: Int
    // TODO: Add fields based on analysis of opcode 87, size 1
) : ServerProt

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

data class LobbyLoginDetails(val dummy: Int
    // TODO: Add fields based on analysis of pre-world opcode 2, size -1
) : ServerProt