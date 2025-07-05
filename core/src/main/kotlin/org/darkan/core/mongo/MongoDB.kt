package org.darkan.core.mongo

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.pojo.PojoCodecProvider
import org.darkan.core.EnvVars
import org.darkan.core.model.Account
import org.darkan.core.mongo.codec.AccountCodec

object MongoDB {
    private val accountCodecRegistry: CodecRegistry = CodecRegistries.fromCodecs(AccountCodec())
    
    private val settings = MongoClientSettings.builder()
        .applyConnectionString(ConnectionString(EnvVars.mongoUri))
        .codecRegistry(CodecRegistries.fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            accountCodecRegistry,
            CodecRegistries.fromProviders(PojoCodecProvider.builder()
                .automatic(true)
                .build())
        ))
        .build()

    val client = MongoClient.create(settings)
    val database = client.getDatabase(ConnectionString(EnvVars.mongoUri).database ?: "darkan-server")
}

