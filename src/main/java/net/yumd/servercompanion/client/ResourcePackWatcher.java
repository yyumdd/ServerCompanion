package net.yumd.servercompanion.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yumd.servercompanion.Config;
import net.yumd.servercompanion.integrity.HmacUtil;
import net.yumd.servercompanion.network.ResourcePackUpdatePayload;
import net.yumd.servercompanion.ServerCompanion;

/**
 * Polls the client's active local resource packs roughly every 5 seconds (not every tick -- no
 * need to be instant, and this keeps overhead negligible) and pushes a signed update to the
 * server whenever the set changes.
 */
@EventBusSubscriber(modid = ServerCompanion.MOD_ID, value = Dist.CLIENT)
public final class ResourcePackWatcher {
    private ResourcePackWatcher() {
    }

    private static final int CHECK_INTERVAL_TICKS = 100; // ~5 seconds at 20 tps

    private static int tickCounter = 0;
    // null = not yet established a baseline this session (e.g. just joined, or just disconnected).
    private static List<String> lastKnownPacks = null;

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().getConnection() == null) {
            // Not connected to a server -- reset so the next join starts with a fresh baseline
            // instead of comparing against packs from a previous session/server.
            lastKnownPacks = null;
            tickCounter = 0;
            return;
        }

        if (++tickCounter < CHECK_INTERVAL_TICKS) {
            return;
        }
        tickCounter = 0;

        List<String> current = LocalResourcePacks.collect();

        if (lastKnownPacks == null) {
            // First check this session. The server already gets a full baseline from the normal
            // join report, so just start tracking locally without sending anything redundant.
            lastKnownPacks = current;
            return;
        }

        if (current.equals(lastKnownPacks)) {
            return;
        }
        lastKnownPacks = current;

        long timestamp = System.currentTimeMillis();
        String canonical = HmacUtil.canonicalResourcePackUpdate(timestamp, current);
        String signature = HmacUtil.sign(Config.HMAC_SECRET.get(), canonical);

        PacketDistributor.sendToServer(new ResourcePackUpdatePayload(timestamp, current, signature));
    }
}
