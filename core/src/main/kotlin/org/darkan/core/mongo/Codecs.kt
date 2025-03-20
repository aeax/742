package org.darkan.core.mongo

import kotlinx.serialization.json.Json
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider

object Codecs {
    private val pojoCodecProvider = PojoCodecProvider.builder()
        .automatic(true)
        .build()

    val codecRegistry = CodecRegistries.fromRegistries(
        com.mongodb.MongoClientSettings.getDefaultCodecRegistry(),
        CodecRegistries.fromProviders(pojoCodecProvider)
    )

    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }
}