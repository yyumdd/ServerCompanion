package net.yumd.servercompanion.report;

import java.util.ArrayList;
import java.util.List;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.yumd.servercompanion.Config;
import net.yumd.servercompanion.ServerCompanion;
import net.yumd.servercompanion.integrity.HmacUtil;
import net.yumd.servercompanion.network.ResourcePackUpdatePayload;

public final class ResourcePackUpdateHandler implements IPayloadHandler<ResourcePackUpdatePayload> {
    public static final ResourcePackUpdateHandler INSTANCE = new ResourcePackUpdateHandler();

    private ResourcePackUpdateHandler() {
    }

    @Override
    public void handle(ResourcePackUpdatePayload payload, IPayloadContext context) {
        context.player().ifPresent(player -> {
            long ageMs = Math.abs(System.currentTimeMillis() - payload.timestamp());
            boolean fresh = ageMs <= (long) Config.MAX_RESPONSE_AGE_SECONDS.get() * 1000L;

            String canonical = HmacUtil.canonicalResourcePackUpdate(payload.timestamp(), payload.localResourcePacks());
            boolean signatureOk = HmacUtil.verify(Config.HMAC_SECRET.get(), canonical, payload.signatureHex());

            String playerName = player.getGameProfile().getName();

            if (!fresh || !signatureOk) {
                ServerCompanion.LOGGER.warn(
                        "ServerCompanion: dropped unverified resource pack update from {} (fresh={}, signatureOk={})",
                        playerName, fresh, signatureOk);
                return;
            }

            List<String> previous = ResourcePackStateTracker.getBaseline(player.getUUID());
            List<String> current = payload.localResourcePacks();
            ResourcePackStateTracker.setBaseline(player.getUUID(), current);

            List<String> added = new ArrayList<>(current);
            added.removeAll(previous);
            List<String> removed = new ArrayList<>(previous);
            removed.removeAll(current);

            if (added.isEmpty() && removed.isEmpty()) {
                return;
            }

            ServerCompanion.LOGGER.info("ServerCompanion: {} resource pack change -- added {}, removed {}",
                    playerName, added, removed);

            if (Config.RESOURCE_PACK_CHANGE_ALERTS.get()) {
                DiscordWebhookService.postResourcePackChange(playerName, added, removed);
            }
        });
    }
}
