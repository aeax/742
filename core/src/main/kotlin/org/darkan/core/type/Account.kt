package org.darkan.core.type

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.darkan.core.formatPlayerNameForDisplay
import org.darkan.core.formatPlayerNameForProtocol
import java.text.SimpleDateFormat
import java.util.*

@Serializable
enum class Rights(val crown: Int) {
    PLAYER(0),
    MOD(1),
    ADMIN(2),
    DEVELOPER(2),
    OWNER(2);
}

@Serializable
class Account {
    var username: String
    var email: String
    var recoveryEmail: String
    var displayName: String
    var prevDisplayName: String = ""
    var rights: Rights = Rights.PLAYER
    @Contextual
    var previousPasswords: MutableSet<String> = mutableSetOf()
    @Contextual
    var password: IntArray? = null
    var passwordHash: String
    var legacyPass: String? = null
    var social: Social = Social()
    var banned: Long = 0
    var muted: Long = 0
    var lastIp: String? = null

    constructor(username: String, email: String, passwordHash: String, displayName: String = username.formatPlayerNameForDisplay()) {
        this.username = username
        this.email = email
        this.recoveryEmail = email
        this.displayName = displayName
        this.passwordHash = passwordHash
    }

    constructor() {
        this.username = ""
        this.email = ""
        this.recoveryEmail = ""
        this.displayName = ""
        this.passwordHash = ""
    }

    fun isMuted() = System.currentTimeMillis() < muted
    fun muteSpecific(ms: Long) { muted = System.currentTimeMillis() + ms }
    fun muteDays(days: Int) = muteSpecific(days * (24 * 60 * 60 * 1000L))
    fun mutePerm() { muted = Long.MAX_VALUE }
    fun unmute() { muted = 0 }
    fun getUnmuteDate() = SimpleDateFormat("dd MMM yyyy HH:mm:ss:SSS Z").format(Date(muted))

    fun isBanned() = System.currentTimeMillis() < banned
    fun banSpecific(ms: Long) { banned = System.currentTimeMillis() + ms }
    fun banDays(days: Int) = banSpecific(days * (24 * 60 * 60 * 1000L))
    fun banPerm() { banned = Long.MAX_VALUE }
    fun unban() { banned = 0 }
    fun getUnbanDate() = SimpleDateFormat("dd MMM yyyy HH:mm:ss:SSS Z").format(Date(banned))

    fun copyPunishments(account: Account) {
        banned = account.banned
        muted = account.muted
    }

    fun hasRights(checkRights: Rights) = rights.ordinal >= checkRights.ordinal

    fun onlineTo(other: Account) =
        !(other.social.status.toInt() == 2 || (other.social.status.toInt() == 1 && !other.social.friends.contains(username)))

    fun changePassword(newPasswordHash: String) {
        newPasswordHash.let { previousPasswords.add(it) }
        passwordHash = newPasswordHash
    }

    // NEVER CALL THIS EVER. ONLY TO BE USED FOR ACCOUNT CREATION WHEN PLAYER SELECTS A DISPLAY NAME
    @Deprecated("Only for initial account creation")
    fun initializeUsername(newUsername: String) {
        username = newUsername.formatPlayerNameForProtocol()
        displayName = username.formatPlayerNameForDisplay()
    }
}

@Serializable
data class Social(
    var friends: MutableSet<String> = HashSet(),
    var ignores: MutableSet<String> = HashSet(),
    var tillLogoutIgnores: MutableSet<String>? = null,
    var status: Byte = 0,
    var fcStatus: Byte = 0,
    var clanName: String? = null,
    var connectedToClan: Boolean = false,
    var currentFriendsChat: String = "help",
    var guestedClanChat: String? = null,
    var friendsChat: FriendsChat = FriendsChat()
) {
    fun addFriend(account: Account) = friends.add(account.username)
    fun addIgnore(account: Account) = ignores.add(account.username)
    fun removeFriend(account: Account) = friends.remove(account.username)
    fun removeIgnore(account: Account) = ignores.remove(account.username)
}

@Serializable
data class FriendsChat(
    var name: String? = null,
    var friendsChatRanks: MutableMap<String, FriendsChatRank> = HashMap(),
    var rankToEnter: FriendsChatRank = FriendsChatRank.UNRANKED,
    var rankToSpeak: FriendsChatRank = FriendsChatRank.UNRANKED,
    var rankToKick: FriendsChatRank = FriendsChatRank.OWNER,
    var rankToLS: FriendsChatRank = FriendsChatRank.UNRANKED,
    var coinshare: Boolean = false
) {
    fun getRank(username: String) = friendsChatRanks[username] ?: FriendsChatRank.UNRANKED

    fun setRank(name: String, rank: FriendsChatRank?) {
        if (rank == null) {
            friendsChatRanks.remove(name)
            return
        }
        friendsChatRanks[name] = rank
    }
}

@Serializable
enum class FriendsChatRank(val id: Int) {
    UNRANKED(-1),
    FRIEND(0),
    RECRUIT(1),
    CORPORAL(2),
    SERGEANT(3),
    LIEUTENANT(4),
    CAPTAIN(5),
    GENERAL(6),
    OWNER(7),
    JMOD(127);

    companion object {
        private val idMap = entries.associateBy { it.id }
        fun fromId(id: Int): FriendsChatRank = idMap[id] ?: UNRANKED
    }
}