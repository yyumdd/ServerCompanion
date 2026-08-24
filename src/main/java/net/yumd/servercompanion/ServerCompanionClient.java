package net.yumd.servercompanion;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.yumd.servercompanion.client.ClientReportHandler;
import net.yumd.servercompanion.network.ReportRequestPayload;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
// Deliberately kept separate from the common ServerCompanion class so that the client-bound
// (S2C) payload handler -- which touches Minecraft.getInstance() -- is never referenced from
// code that also runs on a dedicated server.
@Mod(value = ServerCompanion.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ServerCompanion.MOD_ID, value = Dist.CLIENT)
public class ServerCompanionClient {
    public ServerCompanionClient(ModContainer container) {

    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {

    }

    @SubscribeEvent
    static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(ReportRequestPayload.TYPE, ReportRequestPayload.STREAM_CODEC, ClientReportHandler.INSTANCE);
    }
}