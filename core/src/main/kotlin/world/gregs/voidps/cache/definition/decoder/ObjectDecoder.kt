package world.gregs.voidps.cache.definition.decoder

import org.darkan.core.model.ObjectShape
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Index.OBJECTS
import world.gregs.voidps.cache.definition.Parameters
import world.gregs.voidps.cache.definition.data.ObjectDefinition

open class ObjectDecoder(
    val member: Boolean,
    val lowDetail: Boolean,
    private val parameters: Parameters = Parameters.EMPTY
) : DefinitionDecoder<ObjectDefinition>(OBJECTS) {

    override fun create(size: Int) = Array(size) { ObjectDefinition(it) }

    override fun getFile(id: Int) = id and 0xff

    override fun getArchive(id: Int) = id ushr 8

    override fun ObjectDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> {
                val count = buffer.readUnsignedByte()
                shapes = Array(count) { ObjectShape.forId(buffer.readByte()) }
                modelIds = Array(count) { IntArray(0) }

                repeat(count) { shape ->
                    val models = buffer.readUnsignedByte()
                    modelIds?.set(shape, IntArray(models) { buffer.readBigSmart() })
                }
            }
            2 -> name = buffer.readString()
            14 -> sizeX = buffer.readUnsignedByte()
            15 -> sizeY = buffer.readUnsignedByte()
            17 -> {
                solid = 0
                block = block and ObjectDefinition.PROJECTILE.inv()
            }
            18 -> {
                block = block and ObjectDefinition.PROJECTILE.inv()
            }
            19 -> interactive = buffer.readUnsignedByte()
            21 -> {
                groundContourType = 1
            }
            22 -> {
                delayShading = true
            }
            23 -> {
                occlusionMode = 1
            }
            24 -> {
                val animation = buffer.readBigSmart()
                if (animation != -1) {
                    animations = intArrayOf(animation)
                }
            }
            27 -> solid = 1
            28 -> {
                decorDisplacement = buffer.readUnsignedByte() shl 2
            }
            29 -> {
                ambient = buffer.readByte()
            }
            39 -> {
                contrast = buffer.readByte() * 5
            }
            in 30..34 -> {
                if (options == null)
                    options = arrayOf(null, null, null, null, null, "Examine")
                options!![opcode - 30] = buffer.readString()
            }
            40 -> {
                val count = buffer.readUnsignedByte()
                recolorSrc = ShortArray(count)
                recolorDst = ShortArray(count)

                for (index in 0 until count) {
                    recolorSrc!![index] = buffer.readUnsignedShort().toShort()
                    recolorDst!![index] = buffer.readUnsignedShort().toShort()
                }
            }
            41 -> {
                val count = buffer.readUnsignedByte()
                retextureSrc = ShortArray(count)
                retextureDst = ShortArray(count)

                for (index in 0 until count) {
                    retextureSrc!![index] = buffer.readUnsignedShort().toShort()
                    retextureDst!![index] = buffer.readUnsignedShort().toShort()
                }
            }
            42 -> {
                val count = buffer.readUnsignedByte()
                recolorDPalette = ByteArray(count)

                for (index in 0 until count)
                    recolorDPalette!![index] = buffer.readByte().toByte()
            }
            62 -> mirrored = true
            64 -> {
                staticShadow = false
            }
            65 -> {
                resizeX = buffer.readUnsignedShort()
            }
            66 -> {
                resizeY = buffer.readUnsignedShort()
            }
            67 -> {
                resizeZ = buffer.readUnsignedShort()
            }
            69 -> {
                buffer.readUnsignedByte()
            }
            70 -> {
                offsetX = buffer.readShort() shl 2
            }
            71 -> {
                offsetY = buffer.readShort() shl 2
            }
            72 -> {
                offsetZ = buffer.readShort() shl 2
            }
            73 -> {
                forceDisplayDecoration = true
            }
            74 -> {
                block = block and ObjectDefinition.ROUTE.inv()
            }
            75 -> {
                supportsItems = buffer.readUnsignedByte()
            }
            77, 92 -> readTransforms(buffer, opcode == 92)
            78 -> {
                soundId = buffer.readUnsignedShort()
                soundRadius = buffer.readUnsignedByte()
            }
            79 -> {
                soundMinInterval = buffer.readUnsignedShort()
                soundMaxInterval = buffer.readUnsignedShort()
                soundRadius = buffer.readUnsignedByte()

                val count = buffer.readUnsignedByte()
                soundGroupIds = IntArray(count)

                for (index in 0 until count) {
                    soundGroupIds!![index] = buffer.readUnsignedShort()
                }
            }
            81 -> {
                groundContourType = 2
                groundContourModifier = buffer.readUnsignedByte() * 256
            }
            82 -> {
                requiresTextures = true
            }
            88 -> {
                dynamicShadow = false
            }
            89 -> {
                replaySequence = false
            }
            91 -> {
                members = true
            }
            93 -> {
                groundContourType = 3
                groundContourModifier = buffer.readUnsignedShort()
            }
            94 -> {
                groundContourType = 4
            }
            95 -> {
                groundContourType = 5
                groundContourModifier = buffer.readShort()
            }
            97 -> {
                mapIconRotates = true
            }
            98 -> {
                hasAnimation = true
            }
            99 -> {
                primaryCursorActionIndex = buffer.readUnsignedByte()
                primaryCursor = buffer.readUnsignedShort()
            }
            100 -> {
                secondaryCursorActionIndex = buffer.readUnsignedByte()
                secondaryCursor = buffer.readUnsignedShort()
            }
            101 -> {
                mapIconRotation = buffer.readUnsignedByte()
            }
            102 -> {
                mapIconId = buffer.readUnsignedShort()
            }
            103 -> {
                occlusionMode = 0
            }
            104 -> {
                soundVolume = buffer.readUnsignedByte()
            }
            105 -> {
                mapIconFlipped = true
            }
            106 -> {
                val numAnimations = buffer.readUnsignedByte()
                var sum = 0

                animations = IntArray(numAnimations)
                animationOdds = IntArray(numAnimations)

                for (index in 0 until numAnimations) {
                    animations!![index] = buffer.readBigSmart()
                    sum += buffer.readUnsignedByte().also { animationOdds!![index] = it }
                }

                for (index in 0 until numAnimations) {
                    animationOdds!![index] = animationOdds!![index] * 65535 / sum
                }
            }
            107 -> {
                mapCategoryId = buffer.readUnsignedShort()
            }
            in 150..154 -> {
                if (options == null) {
                    options = arrayOf(null, null, null, null, null, "Examine")
                }
                options!![opcode - 150] = buffer.readString()
                if (!member) {
                    options!![opcode - 150] = null
                }
            }
            160 -> {
                val count = buffer.readUnsignedByte()
                quests = IntArray(count)

                for (index in 0 until count) {
                    quests!![index] = buffer.readUnsignedShort()
                }
            }
            162 -> {
                groundContourType = 3
                groundContourModifier = buffer.readInt()
            }
            163 -> {
                tintHue = buffer.readByte().toByte()
                tintSaturation = buffer.readByte().toByte()
                tintLightness = buffer.readByte().toByte()
                tintOpacity = buffer.readByte().toByte()
            }
            164 -> {
                shadowOffsetX = buffer.readShort()
            }
            165 -> {
                shadowOffsetY = buffer.readShort()
            }
            166 -> {
                shadowOffsetZ = buffer.readShort()
            }
            167 -> {
                groundDecorationHeight = buffer.readUnsignedShort()
            }
            168 -> {
                instrumentSoundEffect = true
            }
            169 -> {
                instrumentAmbientSound = true
            }
            170 -> {
                cullY = buffer.readSmart()
            }
            171 -> {
                cullXZ = buffer.readSmart()
            }
            173 -> {
                ambientSoundMaxDelay = buffer.readUnsignedShort()
                ambientSoundMinDelay = buffer.readUnsignedShort()
            }
            177 -> {
                transformsFlag = true
            }
            178 -> {
                ambientSoundMaxHearDistance = buffer.readUnsignedByte()
            }
            189 -> {
                dynamicTint = true
            }
            249 -> readParameters(buffer, parameters)
        }
    }

    companion object {
        private fun skip(buffer: Reader) {
            val length = buffer.readUnsignedByte()
            for (i in 0 until length) {
                buffer.skip(1)
                buffer.skip(buffer.readUnsignedByte() * 2)
            }
        }
    }
}