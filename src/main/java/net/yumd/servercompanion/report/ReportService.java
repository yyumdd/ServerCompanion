package net.yumd.servercompanion.report;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yumd.servercompanion.Config;
import net.yumd.servercompanion.ServerCompanion;
import net.yumd.servercompanion.integrity.HmacUtil;
import net.yumd.servercompanion.network.ModEntry;
import net.yumd.servercompanion.network.ReportRequestPayload;
import net.yumd.servercompanion.network.ReportResponsePayload;

public final class ReportService {
    private ReportService() {
    }

    /**
     * Sends a report request to a player. {@code source} may be null (e.g. the automatic
     * on-join check) -- in that case the result is only logged/posted to Discord, never printed
     * to anyone's chat.
     */
    public static void requestReport(ServerPlayer target, CommandSourceStack source) {
        UUID nonce = UUID.randomUUID();
        long timestamp = System.currentTimeMillis();

        PendingRequestTracker.register(
                nonce,
                new PendingRequestTracker.Pending(target.getUUID(), target.getGameProfile().getName(), source, target.getServer()),
                Config.RESPONSE_TIMEOUT_SECONDS.get(),
                ReportService::onTimeout);

        PacketDistributor.sendToPlayer(target, new ReportRequestPayload(nonce, timestamp));
    }

    private static void onTimeout(PendingRequestTracker.Pending pending) {
        ServerCompanion.LOGGER.warn("ServerCompanion: report request to {} timed out (mod not installed, or client unresponsive).",
                pending.targetPlayerName());
        if (pending.source() != null) {
            pending.source().sendFailure(Component.literal(
                    "No report from " + pending.targetPlayerName() + " -- they may not have the mod installed, or timed out."));
        }
    }

    public static void handleResponse(PendingRequestTracker.Pending pending, ReportResponsePayload payload) {
        long ageMs = Math.abs(System.currentTimeMillis() - payload.timestamp());
        boolean fresh = ageMs <= (long) Config.MAX_RESPONSE_AGE_SECONDS.get() * 1000L;

        String canonical = HmacUtil.canonicalPayload(
                payload.nonce(), payload.timestamp(), payload.mods(), payload.localResourcePacks(), payload.selfJarHash());
        boolean signatureOk = HmacUtil.verify(Config.HMAC_SECRET.get(), canonical, payload.signatureHex());
        boolean verified = fresh && signatureOk;

        List<String> whitelist = new ArrayList<>(Config.MOD_WHITELIST.get());
        List<ModEntry> unlisted = payload.mods().stream()
                .filter(mod -> !whitelist.contains(mod.id()))
                .sorted(Comparator.comparing(ModEntry::id))
                .toList();

        // Seed the resource pack change tracker with this report's list, so the next mid-session
        // change (from ResourcePackWatcher) diffs against an accurate baseline instead of nothing.
        ResourcePackStateTracker.setBaseline(pending.targetPlayerId(), payload.localResourcePacks());

        if (pending.source() != null) {
            String summary = buildChatSummary(pending.targetPlayerName(), unlisted, payload.localResourcePacks(), verified);
            pending.source().sendSuccess(() -> Component.literal(summary), false);
        }

        ServerCompanion.LOGGER.info(
                "ServerCompanion report for {}: {} unlisted mod(s), {} local resource pack(s), verified={}",
                pending.targetPlayerName(), unlisted.size(), payload.localResourcePacks().size(), verified);

        DiscordWebhookService.postReport(pending.targetPlayerName(), unlisted, payload.localResourcePacks(), verified);
    }

    private static String buildChatSummary(String playerName, List<ModEntry> unlisted, List<String> localPacks, boolean verified) {
        StringBuilder sb = new StringBuilder();
        sb.append("ServerCompanion report for ").append(playerName).append('\n');

        if (unlisted.isEmpty()) {
            sb.append("Unlisted mods: none\n");
        } else {
            sb.append("Unlisted mods (").append(unlisted.size()).append("): ")
                    .append(unlisted.stream().map(m -> m.id() + " (" + m.version() + ")").collect(Collectors.joining(", ")))
                    .append('\n');
        }

        if (localPacks.isEmpty()) {
            sb.append("Local resource packs: none\n");
        } else {
            sb.append("Local resource packs (").append(localPacks.size()).append("): ")
                    .append(String.join(", ", localPacks))
                    .append('\n');
        }

        sb.append("Integrity: ").append(verified ? "verified" : "UNVERIFIED (signature or timestamp mismatch)");
        return sb.toString();
    }
}
