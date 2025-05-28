package world.gregs.voidps.cache.definition.data

import org.darkan.core.model.ObjectShape
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.definition.Extra
import world.gregs.voidps.cache.definition.Transforms

data class ObjectDefinition(
    override var id: Int = -1,
    var name: String = "null",
    var sizeX: Int = 1,
    var sizeY: Int = 1,
    var solid: Int = 2,
    var interactive: Int = -1,
    var options: Array<String?>? = null,
    var mirrored: Boolean = false,
    var blockFlag: Int = 0,
    override var varbit: Int = -1,
    override var varp: Int = -1,
    override var transforms: IntArray? = null,
    override var stringId: String = "",
    override var extras: Map<String, Any>? = null,

    var shapes: Array<ObjectShape>? = null,
    var modelIds: Array<IntArray>? = null,

    var groundContourType: Byte = 0,
    var groundContourModifier: Int = -1,
    var delayShading: Boolean = false,
    var occlusionMode: Int = -1,
    var decorDisplacement: Int = 64,
    var ambient: Int = 0,
    var contrast: Int = 0,
    var staticShadow: Boolean = true,
    var dynamicShadow: Boolean = true,

    var resizeX: Int = 128,
    var resizeY: Int = 128,
    var resizeZ: Int = 128,
    var offsetX: Int = 0,
    var offsetY: Int = 0,
    var offsetZ: Int = 0,

    var recolorSrc: ShortArray? = null,
    var recolorDst: ShortArray? = null,
    var retextureSrc: ShortArray? = null,
    var retextureDst: ShortArray? = null,
    var recolorDPalette: ByteArray? = null,
    var tintHue: Byte = 0,
    var tintSaturation: Byte = 0,
    var tintLightness: Byte = 0,
    var tintOpacity: Byte = 0,
    var dynamicTint: Boolean = false,

    var animations: IntArray? = null,
    var animationOdds: IntArray? = null,
    var hasAnimation: Boolean = false,
    var replaySequence: Boolean = true,

    var soundId: Int = -1,
    var soundRadius: Int = 0,
    var soundMinInterval: Int = 0,
    var soundMaxInterval: Int = 0,
    var soundGroupIds: IntArray? = null,
    var soundVolume: Int = 255,
    var instrumentSoundEffect: Boolean = false,
    var instrumentAmbientSound: Boolean = false,
    var ambientSoundMaxDelay: Int = 256,
    var ambientSoundMinDelay: Int = 256,
    var ambientSoundMaxHearDistance: Int = 0,

    var mapIconId: Int = -1,
    var mapIconRotation: Int = 0,
    var mapIconRotates: Boolean = false,
    var mapIconFlipped: Boolean = false,
    var mapCategoryId: Int = -1,

    var primaryCursorActionIndex: Int = -1,
    var primaryCursor: Int = -1,
    var secondaryCursorActionIndex: Int = -1,
    var secondaryCursor: Int = -1,

    var shadowOffsetX: Int = 0,
    var shadowOffsetY: Int = 0,
    var shadowOffsetZ: Int = 0,

    var forceDisplayDecoration: Boolean = false,
    var supportsItems: Int = -1,
    var requiresTextures: Boolean = false,
    var members: Boolean = false,
    var quests: IntArray? = null,
    var groundDecorationHeight: Int = 0,
    var cullY: Int = 960,
    var cullXZ: Int = 0,
    var transformsFlag: Boolean = false
) : Definition, Transforms, Extra {
    var block: Int = PROJECTILE or ROUTE

    fun optionsIndex(option: String) = if (options != null) options!!.indexOf(option) else -1
    fun containsOption(option: String) = if (options != null) options!!.contains(option) else false
    fun containsOption(index: Int, option: String) = if (options != null) options!![index] == option else false

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ObjectDefinition

        if (id != other.id) return false
        if (name != other.name) return false
        if (sizeX != other.sizeX) return false
        if (sizeY != other.sizeY) return false
        if (solid != other.solid) return false
        if (interactive != other.interactive) return false
        if (options != null) {
            if (other.options == null) return false
            if (!options.contentEquals(other.options)) return false
        } else if (other.options != null) return false
        if (mirrored != other.mirrored) return false
        if (blockFlag != other.blockFlag) return false
        if (varbit != other.varbit) return false
        if (varp != other.varp) return false
        if (transforms != null) {
            if (other.transforms == null) return false
            if (!transforms.contentEquals(other.transforms)) return false
        } else if (other.transforms != null) return false
        if (stringId != other.stringId) return false
        if (extras != other.extras) return false
        if (shapes != null) {
            if (other.shapes == null) return false
            if (!shapes.contentEquals(other.shapes)) return false
        } else if (other.shapes != null) return false
        if (modelIds != null) {
            if (other.modelIds == null) return false
            if (!modelIds.contentDeepEquals(other.modelIds)) return false
        } else if (other.modelIds != null) return false
        if (groundContourType != other.groundContourType) return false
        if (groundContourModifier != other.groundContourModifier) return false
        if (delayShading != other.delayShading) return false
        if (occlusionMode != other.occlusionMode) return false
        if (decorDisplacement != other.decorDisplacement) return false
        if (ambient != other.ambient) return false
        if (contrast != other.contrast) return false
        if (staticShadow != other.staticShadow) return false
        if (dynamicShadow != other.dynamicShadow) return false
        if (resizeX != other.resizeX) return false
        if (resizeY != other.resizeY) return false
        if (resizeZ != other.resizeZ) return false
        if (offsetX != other.offsetX) return false
        if (offsetY != other.offsetY) return false
        if (offsetZ != other.offsetZ) return false
        if (recolorSrc != null) {
            if (other.recolorSrc == null) return false
            if (!recolorSrc.contentEquals(other.recolorSrc)) return false
        } else if (other.recolorSrc != null) return false
        if (recolorDst != null) {
            if (other.recolorDst == null) return false
            if (!recolorDst.contentEquals(other.recolorDst)) return false
        } else if (other.recolorDst != null) return false
        if (retextureSrc != null) {
            if (other.retextureSrc == null) return false
            if (!retextureSrc.contentEquals(other.retextureSrc)) return false
        } else if (other.retextureSrc != null) return false
        if (retextureDst != null) {
            if (other.retextureDst == null) return false
            if (!retextureDst.contentEquals(other.retextureDst)) return false
        } else if (other.retextureDst != null) return false
        if (recolorDPalette != null) {
            if (other.recolorDPalette == null) return false
            if (!recolorDPalette.contentEquals(other.recolorDPalette)) return false
        } else if (other.recolorDPalette != null) return false
        if (tintHue != other.tintHue) return false
        if (tintSaturation != other.tintSaturation) return false
        if (tintLightness != other.tintLightness) return false
        if (tintOpacity != other.tintOpacity) return false
        if (dynamicTint != other.dynamicTint) return false
        if (animations != null) {
            if (other.animations == null) return false
            if (!animations.contentEquals(other.animations)) return false
        } else if (other.animations != null) return false
        if (animationOdds != null) {
            if (other.animationOdds == null) return false
            if (!animationOdds.contentEquals(other.animationOdds)) return false
        } else if (other.animationOdds != null) return false
        if (hasAnimation != other.hasAnimation) return false
        if (replaySequence != other.replaySequence) return false
        if (soundId != other.soundId) return false
        if (soundRadius != other.soundRadius) return false
        if (soundMinInterval != other.soundMinInterval) return false
        if (soundMaxInterval != other.soundMaxInterval) return false
        if (soundGroupIds != null) {
            if (other.soundGroupIds == null) return false
            if (!soundGroupIds.contentEquals(other.soundGroupIds)) return false
        } else if (other.soundGroupIds != null) return false
        if (soundVolume != other.soundVolume) return false
        if (instrumentSoundEffect != other.instrumentSoundEffect) return false
        if (instrumentAmbientSound != other.instrumentAmbientSound) return false
        if (ambientSoundMaxDelay != other.ambientSoundMaxDelay) return false
        if (ambientSoundMinDelay != other.ambientSoundMinDelay) return false
        if (ambientSoundMaxHearDistance != other.ambientSoundMaxHearDistance) return false
        if (mapIconId != other.mapIconId) return false
        if (mapIconRotation != other.mapIconRotation) return false
        if (mapIconRotates != other.mapIconRotates) return false
        if (mapIconFlipped != other.mapIconFlipped) return false
        if (mapCategoryId != other.mapCategoryId) return false
        if (primaryCursorActionIndex != other.primaryCursorActionIndex) return false
        if (primaryCursor != other.primaryCursor) return false
        if (secondaryCursorActionIndex != other.secondaryCursorActionIndex) return false
        if (secondaryCursor != other.secondaryCursor) return false
        if (shadowOffsetX != other.shadowOffsetX) return false
        if (shadowOffsetY != other.shadowOffsetY) return false
        if (shadowOffsetZ != other.shadowOffsetZ) return false
        if (forceDisplayDecoration != other.forceDisplayDecoration) return false
        if (supportsItems != other.supportsItems) return false
        if (requiresTextures != other.requiresTextures) return false
        if (members != other.members) return false
        if (quests != null) {
            if (other.quests == null) return false
            if (!quests.contentEquals(other.quests)) return false
        } else if (other.quests != null) return false
        if (groundDecorationHeight != other.groundDecorationHeight) return false
        if (cullY != other.cullY) return false
        if (cullXZ != other.cullXZ) return false
        if (transformsFlag != other.transformsFlag) return false
        return block == other.block
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + sizeX
        result = 31 * result + sizeY
        result = 31 * result + solid
        result = 31 * result + interactive
        result = 31 * result + (options?.contentHashCode() ?: 0)
        result = 31 * result + mirrored.hashCode()
        result = 31 * result + blockFlag
        result = 31 * result + varbit
        result = 31 * result + varp
        result = 31 * result + (transforms?.contentHashCode() ?: 0)
        result = 31 * result + stringId.hashCode()
        result = 31 * result + (extras?.hashCode() ?: 0)
        result = 31 * result + (shapes?.contentHashCode() ?: 0)
        result = 31 * result + (modelIds?.contentDeepHashCode() ?: 0)
        result = 31 * result + groundContourType
        result = 31 * result + groundContourModifier
        result = 31 * result + delayShading.hashCode()
        result = 31 * result + occlusionMode
        result = 31 * result + decorDisplacement
        result = 31 * result + ambient
        result = 31 * result + contrast
        result = 31 * result + staticShadow.hashCode()
        result = 31 * result + dynamicShadow.hashCode()
        result = 31 * result + resizeX
        result = 31 * result + resizeY
        result = 31 * result + resizeZ
        result = 31 * result + offsetX
        result = 31 * result + offsetY
        result = 31 * result + offsetZ
        result = 31 * result + (recolorSrc?.contentHashCode() ?: 0)
        result = 31 * result + (recolorDst?.contentHashCode() ?: 0)
        result = 31 * result + (retextureSrc?.contentHashCode() ?: 0)
        result = 31 * result + (retextureDst?.contentHashCode() ?: 0)
        result = 31 * result + (recolorDPalette?.contentHashCode() ?: 0)
        result = 31 * result + tintHue
        result = 31 * result + tintSaturation
        result = 31 * result + tintLightness
        result = 31 * result + tintOpacity
        result = 31 * result + dynamicTint.hashCode()
        result = 31 * result + (animations?.contentHashCode() ?: 0)
        result = 31 * result + (animationOdds?.contentHashCode() ?: 0)
        result = 31 * result + hasAnimation.hashCode()
        result = 31 * result + replaySequence.hashCode()
        result = 31 * result + soundId
        result = 31 * result + soundRadius
        result = 31 * result + soundMinInterval
        result = 31 * result + soundMaxInterval
        result = 31 * result + (soundGroupIds?.contentHashCode() ?: 0)
        result = 31 * result + soundVolume
        result = 31 * result + instrumentSoundEffect.hashCode()
        result = 31 * result + instrumentAmbientSound.hashCode()
        result = 31 * result + ambientSoundMaxDelay
        result = 31 * result + ambientSoundMinDelay
        result = 31 * result + ambientSoundMaxHearDistance
        result = 31 * result + mapIconId
        result = 31 * result + mapIconRotation
        result = 31 * result + mapIconRotates.hashCode()
        result = 31 * result + mapIconFlipped.hashCode()
        result = 31 * result + mapCategoryId
        result = 31 * result + primaryCursorActionIndex
        result = 31 * result + primaryCursor
        result = 31 * result + secondaryCursorActionIndex
        result = 31 * result + secondaryCursor
        result = 31 * result + shadowOffsetX
        result = 31 * result + shadowOffsetY
        result = 31 * result + shadowOffsetZ
        result = 31 * result + forceDisplayDecoration.hashCode()
        result = 31 * result + supportsItems
        result = 31 * result + requiresTextures.hashCode()
        result = 31 * result + members.hashCode()
        result = 31 * result + (quests?.contentHashCode() ?: 0)
        result = 31 * result + groundDecorationHeight
        result = 31 * result + cullY
        result = 31 * result + cullXZ
        result = 31 * result + transformsFlag.hashCode()
        result = 31 * result + block
        return result
    }

    companion object {
        const val ROUTE = 0x10
        const val PROJECTILE = 0x8
        val EMPTY = ObjectDefinition()
    }
}