package net.yumd.servercompanion.server;

import java.util.List;
import java.util.UUID;

public class ClientReport {

    private final UUID playerUUID;
    private final String playerName;
    private final String version;
    private final List<String> mods;
    private final List<String> resourcePacks;
    private final long timestamp;

    public ClientReport(
            UUID playerUUID,
            String playerName,
            String version,
            List<String> mods,
            List<String> resourcePacks
    ) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.version = version;
        this.mods = mods;
        this.resourcePacks = resourcePacks;
        this.timestamp = System.currentTimeMillis();
    }


    public UUID getPlayerUUID() {
        return playerUUID;
    }


    public String getPlayerName() {
        return playerName;
    }


    public String getVersion() {
        return version;
    }


    public List<String> getMods() {
        return mods;
    }


    public List<String> getResourcePacks() {
        return resourcePacks;
    }


    public long getTimestamp() {
        return timestamp;
    }
}