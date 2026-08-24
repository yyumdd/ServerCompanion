package net.yumd.servercompanion;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = ServerCompanion.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ServerCompanion.MOD_ID, value = Dist.CLIENT)
public class ServerCompanionClient {
    public ServerCompanionClient(ModContainer container) {

    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {

    }
}