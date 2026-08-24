package net.yumd.servercompanion.report;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;

public final class PendingRequestTracker {

    public record Pending(UUID targetPlayerId, String targetPlayerName, CommandSourceStack source,
            MinecraftServer server) {
    }

    private static final Map<UUID, Pending> PENDING = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService TIMEOUTS = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "servercompanion-report-timeout");
        thread.setDaemon(true);
        return thread;
    });

    private PendingRequestTracker() {
    }

    public static void register(UUID nonce, Pending pending, int timeoutSeconds, Consumer<Pending> onTimeout) {
        PENDING.put(nonce, pending);
        TIMEOUTS.schedule(() -> {
            Pending removed = PENDING.remove(nonce);
            if (removed != null) {
                // Hop back onto the server thread before touching players/command sources.
                removed.server().execute(() -> onTimeout.accept(removed));
            }
        }, timeoutSeconds, TimeUnit.SECONDS);
    }

    /**
     * Removes and returns the pending entry for this nonce, or null if it's unknown/expired
     * (already timed out, or a duplicate/replayed response for a request we no longer track).
     */
    public static Pending complete(UUID nonce) {
        return PENDING.remove(nonce);
    }
}
