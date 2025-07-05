package org.darkan.core.mongo.codec

import org.bson.BsonReader
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.darkan.core.model.*

class AccountCodec : Codec<Account> {
    override fun getEncoderClass(): Class<Account> = Account::class.java

    override fun encode(writer: BsonWriter, value: Account, encoderContext: EncoderContext) {
        writer.writeStartDocument()
        writer.writeString("username", value.username)
        writer.writeString("email", value.email)
        writer.writeString("recoveryEmail", value.recoveryEmail)
        writer.writeString("displayName", value.displayName)
        writer.writeString("prevDisplayName", value.prevDisplayName)
        writer.writeString("rights", value.rights.name)
        writer.writeString("passwordHash", value.passwordHash)
        
        if (value.password != null) {
            writer.writeStartArray("password")
            value.password!!.forEach { writer.writeInt32(it) }
            writer.writeEndArray()
        }
        
        if (value.legacyPass != null) {
            writer.writeString("legacyPass", value.legacyPass!!)
        }
        
        writer.writeInt64("banned", value.banned)
        writer.writeInt64("muted", value.muted)
        
        if (value.lastIp != null) {
            writer.writeString("lastIp", value.lastIp!!)
        }
        
        writer.writeStartArray("previousPasswords")
        value.previousPasswords.forEach { writer.writeString(it) }
        writer.writeEndArray()
        
        // Write social
        writer.writeStartDocument("social")
        encodeSocial(writer, value.social, encoderContext)
        writer.writeEndDocument()
        
        writer.writeEndDocument()
    }

    override fun decode(reader: BsonReader, decoderContext: DecoderContext): Account {
        reader.readStartDocument()
        
        var username = ""
        var email = ""
        var recoveryEmail = ""
        var displayName = ""
        var prevDisplayName = ""
        var rights = Rights.PLAYER
        var passwordHash = ""
        var password: IntArray? = null
        var legacyPass: String? = null
        var banned = 0L
        var muted = 0L
        var lastIp: String? = null
        val previousPasswords = mutableSetOf<String>()
        var social = Social()
        
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            val fieldName = reader.readName()
            when (fieldName) {
                "username" -> username = reader.readString()
                "email" -> email = reader.readString()
                "recoveryEmail" -> recoveryEmail = reader.readString()
                "displayName" -> displayName = reader.readString()
                "prevDisplayName" -> prevDisplayName = reader.readString()
                "rights" -> rights = Rights.valueOf(reader.readString())
                "passwordHash" -> passwordHash = reader.readString()
                "password" -> {
                    val list = mutableListOf<Int>()
                    reader.readStartArray()
                    while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                        list.add(reader.readInt32())
                    }
                    reader.readEndArray()
                    password = list.toIntArray()
                }
                "legacyPass" -> legacyPass = reader.readString()
                "banned" -> {
                    when (reader.currentBsonType) {
                        BsonType.BOOLEAN -> banned = if (reader.readBoolean()) Long.MAX_VALUE else 0L
                        BsonType.INT64 -> banned = reader.readInt64()
                        BsonType.INT32 -> banned = reader.readInt32().toLong()
                        else -> reader.skipValue()
                    }
                }
                "muted" -> {
                    when (reader.currentBsonType) {
                        BsonType.BOOLEAN -> muted = if (reader.readBoolean()) Long.MAX_VALUE else 0L
                        BsonType.INT64 -> muted = reader.readInt64()
                        BsonType.INT32 -> muted = reader.readInt32().toLong()
                        else -> reader.skipValue()
                    }
                }
                "lastIp" -> lastIp = reader.readString()
                "previousPasswords" -> {
                    reader.readStartArray()
                    while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                        previousPasswords.add(reader.readString())
                    }
                    reader.readEndArray()
                }
                "social" -> {
                    reader.readStartDocument()
                    social = decodeSocial(reader, decoderContext)
                    reader.readEndDocument()
                }
                else -> reader.skipValue()
            }
        }
        
        reader.readEndDocument()
        
        val account = Account(username, email, passwordHash, displayName).apply {
            this.recoveryEmail = recoveryEmail
            this.prevDisplayName = prevDisplayName
            this.rights = rights
            this.password = password
            this.legacyPass = legacyPass
            this.banned = banned
            this.muted = muted
            this.lastIp = lastIp
            this.previousPasswords = previousPasswords
            this.social = social
        }
        
        return account
    }
    
    private fun encodeSocial(writer: BsonWriter, social: Social, encoderContext: EncoderContext) {
        writer.writeStartArray("friends")
        social.friends.forEach { writer.writeString(it) }
        writer.writeEndArray()
        
        writer.writeStartArray("ignores")
        social.ignores.forEach { writer.writeString(it) }
        writer.writeEndArray()
        
        writer.writeInt32("status", social.status.toInt())
        writer.writeInt32("fcStatus", social.fcStatus.toInt())
        writer.writeBoolean("connectedToClan", social.connectedToClan)
        writer.writeString("currentFriendsChat", social.currentFriendsChat)
        
        if (social.clanName != null) {
            writer.writeString("clanName", social.clanName!!)
        }
        
        if (social.guestedClanChat != null) {
            writer.writeString("guestedClanChat", social.guestedClanChat!!)
        }
        
        // Write friends chat
        writer.writeStartDocument("friendsChat")
        encodeFriendsChat(writer, social.friendsChat, encoderContext)
        writer.writeEndDocument()
    }
    
    private fun decodeSocial(reader: BsonReader, decoderContext: DecoderContext): Social {
        val friends = mutableSetOf<String>()
        val ignores = mutableSetOf<String>()
        var status: Byte = 0
        var fcStatus: Byte = 0
        var clanName: String? = null
        var connectedToClan = false
        var currentFriendsChat = "help"
        var guestedClanChat: String? = null
        var friendsChat = FriendsChat()
        
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            val fieldName = reader.readName()
            when (fieldName) {
                "friends" -> {
                    reader.readStartArray()
                    while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                        friends.add(reader.readString())
                    }
                    reader.readEndArray()
                }
                "ignores" -> {
                    reader.readStartArray()
                    while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                        ignores.add(reader.readString())
                    }
                    reader.readEndArray()
                }
                "status" -> status = reader.readInt32().toByte()
                "fcStatus" -> fcStatus = reader.readInt32().toByte()
                "clanName" -> clanName = reader.readString()
                "connectedToClan" -> connectedToClan = reader.readBoolean()
                "currentFriendsChat" -> currentFriendsChat = reader.readString()
                "guestedClanChat" -> guestedClanChat = reader.readString()
                "friendsChat" -> {
                    reader.readStartDocument()
                    friendsChat = decodeFriendsChat(reader, decoderContext)
                    reader.readEndDocument()
                }
                else -> reader.skipValue()
            }
        }
        
        return Social(friends, ignores, null, status, fcStatus, clanName, connectedToClan, currentFriendsChat, guestedClanChat, friendsChat)
    }
    
    private fun encodeFriendsChat(writer: BsonWriter, fc: FriendsChat, encoderContext: EncoderContext) {
        if (fc.name != null) {
            writer.writeString("name", fc.name!!)
        }
        
        writer.writeStartDocument("friendsChatRanks")
        fc.friendsChatRanks.forEach { (name, rank) ->
            writer.writeString(name, rank.name)
        }
        writer.writeEndDocument()
        
        writer.writeString("rankToEnter", fc.rankToEnter.name)
        writer.writeString("rankToSpeak", fc.rankToSpeak.name)
        writer.writeString("rankToKick", fc.rankToKick.name)
        writer.writeString("rankToLS", fc.rankToLS.name)
        writer.writeBoolean("coinshare", fc.coinshare)
    }
    
    private fun decodeFriendsChat(reader: BsonReader, decoderContext: DecoderContext): FriendsChat {
        var name: String? = null
        val friendsChatRanks = mutableMapOf<String, FriendsChatRank>()
        var rankToEnter = FriendsChatRank.UNRANKED
        var rankToSpeak = FriendsChatRank.UNRANKED
        var rankToKick = FriendsChatRank.OWNER
        var rankToLS = FriendsChatRank.UNRANKED
        var coinshare = false
        
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            val fieldName = reader.readName()
            when (fieldName) {
                "name" -> name = reader.readString()
                "friendsChatRanks" -> {
                    reader.readStartDocument()
                    while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                        val playerName = reader.readName()
                        val rank = FriendsChatRank.valueOf(reader.readString())
                        friendsChatRanks[playerName] = rank
                    }
                    reader.readEndDocument()
                }
                "rankToEnter" -> rankToEnter = FriendsChatRank.valueOf(reader.readString())
                "rankToSpeak" -> rankToSpeak = FriendsChatRank.valueOf(reader.readString())
                "rankToKick" -> rankToKick = FriendsChatRank.valueOf(reader.readString())
                "rankToLS" -> rankToLS = FriendsChatRank.valueOf(reader.readString())
                "coinshare" -> coinshare = reader.readBoolean()
                else -> reader.skipValue()
            }
        }
        
        return FriendsChat(name, friendsChatRanks, rankToEnter, rankToSpeak, rankToKick, rankToLS, coinshare)
    }
}