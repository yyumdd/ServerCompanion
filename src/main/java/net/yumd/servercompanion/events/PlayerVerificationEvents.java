package net.yumd.servercompanion.events;

import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yumd.servercompanion.ServerCompanion;
import net.yumd.servercompanion.server.VerificationManager;

import java.util.HashMap;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ServerCompanion.MOD_ID)
public class PlayerVerificationEvents {

    private static final HashMap<UUID, Long> pendingPlayers = new HashMap<>();

    private static final long TIMEOUT = 100; // 5 seconds (20 ticks = 1 second)

    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event) {

        UUID uuid = event.getEntity().getUUID();

        pendingPlayers.put(
                uuid,
                event.getEntity().level().getGameTime()
        );
    }
    @SubscribeEvent
    public static void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent event) {

        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END)
            return;

        var server = event.getServer();

        long currentTime = server.overworld().getGameTime();

        pendingPlayers.entrySet().removeIf(entry -> {

            UUID uuid = entry.getKey();

            if (VerificationManager.isVerified(uuid)) {
                return true;
            }

            if (currentTime - entry.getValue() > TIMEOUT) {

                var player = server.getPlayerList()
                        .getPlayer(uuid);

                if (player != null) {

                    player.connection.disconnect(
                            Component.literal(
                                    "You need ServerCompanion installed to join this server."
                            )
                    );

                    ServerCompanion.LOGGER.info(
                            "Kicked {} for missing ServerCompanion",
                            player.getName().getString()
                    );
                }

                return true;
            }

            return false;
        });
    }
    @SubscribeEvent
    public static void onLeave(PlayerEvent.PlayerLoggedOutEvent event) {

        VerificationManager.remove(
                event.getEntity().getUUID()
        );

    }
}