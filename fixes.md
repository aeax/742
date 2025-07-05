## Code Changes

### 1. Player.java - Added null checks for clan-related methods

**File**: `/src/main/java/com/rs/game/model/entity/player/Player.java`

**Lines changed**: 3802-3828

```java
// Before:
public Clan getClan() {
    return ClansManager.getClan(getAccount().getSocial().getClanName());
}

// After:
public Clan getClan() {
    if (getAccount() == null || getAccount().getSocial() == null)
        return null;
    return ClansManager.getClan(getAccount().getSocial().getClanName());
}
```

Similar changes were made to:
- `getClan(Consumer<Clan> cb)` 
- `getGuestClan()`
- `getGuestClan(Consumer<Clan> cb)`

### 2. Player.java - Added Social initialization in init() method

**File**: `/src/main/java/com/rs/game/model/entity/player/Player.java`

**Lines changed**: 678-679 (added)

```java
if (account != null && account.getSocial() == null)
    account.setSocial(new Social());
```

This ensures that any account loaded without a Social object gets one initialized during player initialization.

### 3. WorldEncoder.java - Added null check for FC status

**File**: `/src/main/java/com/rs/net/encoders/WorldEncoder.java`

**Line changed**: 530

```java
// Before:
sendVar(2159, player.getAccount().getSocial().getFcStatus());

// After:
sendVar(2159, player.getAccount() != null && player.getAccount().getSocial() != null ? player.getAccount().getSocial().getFcStatus() : 0);
```

### 4. WorldEncoder.java - Added null check for chat filter settings

**File**: `/src/main/java/com/rs/net/encoders/WorldEncoder.java`

**Line changed**: 356

```java
// Before:
session.writeToQueue(new ChatFilterSettingsPriv(player.getAccount().getSocial().getStatus()));

// After:
session.writeToQueue(new ChatFilterSettingsPriv(player.getAccount() != null && player.getAccount().getSocial() != null ? player.getAccount().getSocial().getStatus() : 0));
```
