package org.darkan.core.mongo.collections

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.or
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Sorts.ascending
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.darkan.core.formatPlayerNameForProtocol
import org.darkan.core.mongo.MongoDB.database
import org.darkan.core.type.Account

object Accounts {
    private val collection by lazy {
        val col = database.getCollection<Account>("accounts")
        runBlocking {
            col.createIndex(ascending(Account::email.name), IndexOptions().unique(true).sparse(true))
            col.createIndex(ascending(Account::recoveryEmail.name), IndexOptions().sparse(true))
            col.createIndex(ascending(Account::displayName.name))
        }
        return@lazy col
    }

    suspend fun find(usernameOrEmail: String): Account? {
        val formatted = usernameOrEmail.formatPlayerNameForProtocol()
        return collection.find(
            or(
                eq(Account::username.name, formatted),
                eq(Account::email.name, formatted)
            )
        ).firstOrNull()
    }

    suspend fun save(account: Account) = collection.replaceOne(eq(Account::username.name, account.username), account)
}