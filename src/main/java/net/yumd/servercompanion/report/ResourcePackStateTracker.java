package net.yumd.servercompanion.report;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ResourcePackStateTracker {
    private static final Map<UUID, List<String>> KNOWN_PACKS = new ConcurrentHashMap<>();

    private ResourcePackStateTracker() {
    }

    public static void setBaseline(UUID playerId, List<String> packs) {
        KNOWN_PACKS.put(playerId, List.copyOf(packs));
    }

    public static List<String> getBaseline(UUID playerId) {
        return KNOWN_PACKS.getOrDefault(playerId, List.of());
    }

    // Called on logout so a stale baseline from one session never leaks into a later one.
    public static void clear(UUID playerId) {
        KNOWN_PACKS.remove(playerId);
    }
}
