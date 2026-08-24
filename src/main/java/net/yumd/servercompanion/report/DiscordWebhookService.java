package net.yumd.servercompanion.report;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.yumd.servercompanion.Config;
import net.yumd.servercompanion.ServerCompanion;
import net.yumd.servercompanion.network.ModEntry;

public final class DiscordWebhookService {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // Discord embed field values are capped at 1024 characters.
    private static final int FIELD_VALUE_LIMIT = 1000;

    private DiscordWebhookService() {
    }

    public static void postReport(String playerName, List<ModEntry> unlistedMods, List<String> localPacks, boolean verified) {
        String modsField = unlistedMods.isEmpty()
                ? "None"
                : truncate(unlistedMods.stream()
                        .map(m -> m.id() + " (" + m.version() + ")")
                        .reduce((a, b) -> a + "\n" + b)
                        .orElse(""), FIELD_VALUE_LIMIT);

        String packsField = localPacks.isEmpty() ? "None" : truncate(String.join(", ", localPacks), FIELD_VALUE_LIMIT);

        int color = verified ? 0x2ECC71 : 0xE67E22; // green = verified, orange = unverified

        String json = "{"
                + "\"embeds\":[{"
                + "\"title\":\"ServerCompanion report: " + escape(playerName) + "\","
                + "\"color\":" + color + ","
                + "\"fields\":["
                + "{\"name\":\"Unlisted mods\",\"value\":\"" + escape(modsField) + "\"},"
                + "{\"name\":\"Local resource packs\",\"value\":\"" + escape(packsField) + "\"},"
                + "{\"name\":\"Integrity\",\"value\":\"" + (verified ? "Verified" : "UNVERIFIED") + "\"}"
                + "],"
                + "\"timestamp\":\"" + Instant.now() + "\""
                + "}]"
                + "}";

        postJson(json);
    }

    public static void postResourcePackChange(String playerName, List<String> added, List<String> removed) {
        String addedField = added.isEmpty() ? "None" : truncate(String.join("\n", added), FIELD_VALUE_LIMIT);
        String removedField = removed.isEmpty() ? "None" : truncate(String.join("\n", removed), FIELD_VALUE_LIMIT);

        String json = "{"
                + "\"embeds\":[{"
                + "\"title\":\"Resource pack change: " + escape(playerName) + "\","
                + "\"color\":3447003,"
                + "\"fields\":["
                + "{\"name\":\"Added\",\"value\":\"" + escape(addedField) + "\"},"
                + "{\"name\":\"Removed\",\"value\":\"" + escape(removedField) + "\"}"
                + "],"
                + "\"timestamp\":\"" + Instant.now() + "\""
                + "}]"
                + "}";

        postJson(json);
    }

    private static void postJson(String json) {
        String url = Config.DISCORD_WEBHOOK_URL.get();
        if (url == null || url.isBlank()) {
            return;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        // Fire-and-forget on a background thread -- never let a slow/unreachable webhook stall
        // the server thread.
        CompletableFuture.runAsync(() -> {
            try {
                HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 300) {
                    ServerCompanion.LOGGER.warn("ServerCompanion: Discord webhook returned status {}: {}",
                            response.statusCode(), truncate(response.body(), 300));
                }
            } catch (Exception e) {
                ServerCompanion.LOGGER.warn("ServerCompanion: failed to post to Discord webhook", e);
            }
        });
    }

    private static String truncate(String s, int limit) {
        if (s.length() <= limit) {
            return s;
        }
        return s.substring(0, limit - 3) + "...";
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
