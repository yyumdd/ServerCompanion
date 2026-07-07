package net.yumd.servercompanion.events;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yumd.servercompanion.ServerCompanion;
import org.slf4j.Logger;

@Mod.EventBusSubscriber(modid = ServerCompanion.MOD_ID)
public class PlayerEvents {

    private static final Logger LOGGER = ServerCompanion.LOGGER;

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        LOGGER.info("{} joined the server!", event.getEntity().getName().getString());
    }
}