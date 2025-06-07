package org.darkan.core.mongo.collections

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.or
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Sorts.ascending
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.darkan.core.crypt.Crypto
import org.darkan.core.formatPlayerNameForProtocol
import org.darkan.core.mongo.MongoDB.database
import org.darkan.core.net.web.AccountCreateRequest
import org.darkan.core.net.web.ConflictException
import org.darkan.core.model.Account
import org.darkan.core.model.Social

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
        val account = collection.find(
            or(
                eq(Account::username.name, formatted),
                eq(Account::email.name, formatted)
            )
        ).firstOrNull()
        
        // Fix accounts with null social field
        if (account != null && account.social == null) {
            account.social = Social()
            save(account)
        }
        
        return account
    }

    suspend fun findByDisplayName(displayName: String): Account? {
        val formatted = displayName.formatPlayerNameForProtocol()
        val account = collection.find(eq(Account::displayName.name, formatted)).firstOrNull()
        
        // Fix accounts with null social field
        if (account != null && account.social == null) {
            account.social = Social()
            save(account)
        }
        
        return account
    }

    suspend fun exists(usernameOrEmail: String): Boolean {
        val formatted = usernameOrEmail.formatPlayerNameForProtocol()
        return collection.find(
            or(
                eq(Account::username.name, formatted),
                eq(Account::email.name, formatted)
            )
        ).firstOrNull() != null
    }

    suspend fun createLobby(email: String, password: String): Account? {
        val formattedEmail = email.formatPlayerNameForProtocol()
        val existingAccount = collection.find(
            or(
                eq(Account::email.name, formattedEmail)
            )
        ).firstOrNull()
        if (existingAccount != null) throw ConflictException("Account already exists")

        val newAccount = Account(
            username = formattedEmail,
            email = formattedEmail,
            passwordHash = Crypto.hashPasswordArgon2(password),
            displayName = "" // Empty displayName to trigger display name selection in lobby
        ).apply {
            // Ensure social field is properly initialized
            social = Social()
        }

        return try {
            collection.insertOne(newAccount)
            newAccount
        } catch (e: Exception) {
            null
        }
    }

    suspend fun create(req: AccountCreateRequest): Account? {
        val formattedUsername = req.username.formatPlayerNameForProtocol()
        val formattedEmail = req.email.formatPlayerNameForProtocol()

        val existingAccount = collection.find(
            or(
                eq(Account::username.name, formattedUsername),
                eq(Account::email.name, formattedEmail)
            )
        ).firstOrNull()
        if (existingAccount != null) throw ConflictException("Account already exists")

        val newAccount = Account(
            username = formattedUsername,
            email = formattedEmail,
            passwordHash = Crypto.hashPasswordArgon2(req.password),
            displayName = "" // Empty displayName to trigger display name selection in lobby
        ).apply {
            // Ensure social field is properly initialized
            social = Social()
        }

        return try {
            collection.insertOne(newAccount)
            newAccount
        } catch (e: Exception) {
            null
        }
    }

    suspend fun save(account: Account) = collection.replaceOne(eq(Account::username.name, account.username), account)
}