package org.darkan.core.mongo

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import org.darkan.core.EnvVars

object MongoDB {
    private val settings = MongoClientSettings.builder()
        .applyConnectionString(ConnectionString(EnvVars.mongoUri))
        .codecRegistry(Codecs.codecRegistry)
        .build()

    val client = MongoClient.create(settings)
    val database = client.getDatabase(ConnectionString(EnvVars.mongoUri).database ?: "darkan-server")

    internal inline fun <reified T : Any> collection(name: String) = database.getCollection(name, T::class.java)
}

