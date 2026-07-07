package net.yumd.servercompanion.server;

import java.util.HashSet;
import java.util.UUID;

public class VerificationManager {

    private static final HashSet<UUID> verifiedPlayers = new HashSet<>();

    public static void verify(UUID playerUUID) {
        verifiedPlayers.add(playerUUID);
    }

    public static boolean isVerified(UUID playerUUID) {
        return verifiedPlayers.contains(playerUUID);
    }

    public static void remove(UUID playerUUID) {
        verifiedPlayers.remove(playerUUID);
    }
}