package net.yumd.servercompanion;

import java.util.List;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * COMMON config: each side (client / server) reads its own local copy of this file and NeoForge
 * does NOT sync it between them. That's intentional here:
 *  - The server's copy holds the real webhook URL, whitelist, and HMAC secret.
 *  - The client's copy of webhook URL / whitelist is irrelevant (the client never uses them).
 *  - The HMAC secret DOES need to match on both sides. Since this mod is meant for a private
 *    modpack, the expected setup is: bundle a pre-filled config/servercompanion-common.toml
 *    (with the same hmacSecret) inside the modpack you distribute to your friends, so nobody
 *    has to manually edit anything.
 */
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // ---- Mod whitelist ----
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MOD_WHITELIST = BUILDER
            .comment(
                    "Mod IDs that are expected/part of the modpack. Mods a player has that are NOT",
                    "in this list will be flagged in reports. Generate this list with the in-game",
                    "command: /servercompanion whitelist dump (run on the server, once your pack",
                    "is finalized), then paste the printed array here.")
            .defineListAllowEmpty("modWhitelist", List.of(), () -> "", obj -> obj instanceof String);

    // ---- Discord webhook ----
    public static final ModConfigSpec.ConfigValue<String> DISCORD_WEBHOOK_URL = BUILDER
            .comment("Discord webhook URL that reports are posted to. Leave empty to disable Discord posting.")
            .define("discordWebhookUrl", "");

    // ---- Integrity ----
    public static final ModConfigSpec.ConfigValue<String> HMAC_SECRET = BUILDER
            .comment(
                    "Shared secret used to sign client reports (HMAC-SHA256). Must be identical in",
                    "the client's and server's config for signatures to verify. This is a lightweight",
                    "check: it catches accidental corruption, replay of stale reports, and casual",
                    "tampering, but it is NOT a strong security boundary -- a sufficiently motivated",
                    "person can extract this from the client mod jar. Change this from the default",
                    "before distributing your modpack.")
            .define("hmacSecret", "CHANGE-ME-BEFORE-DISTRIBUTING");

    public static final ModConfigSpec.IntValue MAX_RESPONSE_AGE_SECONDS = BUILDER
            .comment("Reports with a timestamp older than this (clock skew / replay protection) are flagged as unverified.")
            .defineInRange("maxResponseAgeSeconds", 30, 5, 300);

    public static final ModConfigSpec.IntValue RESPONSE_TIMEOUT_SECONDS = BUILDER
            .comment("How long the server waits for a client to respond to a report request before giving up.")
            .defineInRange("responseTimeoutSeconds", 5, 1, 60);

    // ---- Permissions ----
    public static final ModConfigSpec.IntValue COMMAND_PERMISSION_LEVEL = BUILDER
            .comment("Permission level required to run /servercompanion commands. 2 = ops (default), 4 = server owner only.")
            .defineInRange("commandPermissionLevel", 2, 0, 4);

    static final ModConfigSpec SPEC = BUILDER.build();
}