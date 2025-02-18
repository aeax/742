package org.darkan.core.net.prot

import org.darkan.core.clientwatch.ReflectionData
import org.darkan.core.clientwatch.ReflectionResponseCode

interface ClientProt

data class OpPlayer(val opNum: Int, val playerIndex: Int, val forceRun: Boolean) : ClientProt
data class OpNpc(val opNum: Int, val npcIndex: Int, val forceRun: Boolean) : ClientProt
data class OpObj(val opNum: Int, val objectId: Int, val x: Int, val y: Int, val forceRun: Boolean) : ClientProt
data class OpGroundItem(val opNum: Int, val itemId: Int, val x: Int, val y: Int, val forceRun: Boolean) : ClientProt

data class IfButton(val opNum: Int, val interfaceId: Int, val componentId: Int, val slotId: Int) : ClientProt

data class Chat(val message: String, val effects: Int) : ClientProt
data class PrivateMessage(val username: String, val message: String) : ClientProt

//ClientWatch/Monitoring
@JvmInline value class Ping(val dummy: Int = 0) : ClientProt
@JvmInline value class PingResponse(val ping: Int) : ClientProt
@JvmInline value class FPSResponse(val fps: Int) : ClientProt
data class ReflectionResponse(val code: ReflectionResponseCode, val data: ReflectionData = ReflectionData()) : ClientProt
@JvmInline value class ClientFocus(val focused: Boolean) : ClientProt
data class ClientScreenSize(val width: Int, val height: Int) : ClientProt
data class MoveCamera(val angleX: Int, val angleY: Int) : ClientProt

@JvmInline value class RegionLoadedConfirm(val dummy: Int = 0) : ClientProt
@JvmInline value class SoundEffectMusicEnded(val musicId: Int) : ClientProt
@JvmInline value class SongLoaded(val songId: Int) : ClientProt
