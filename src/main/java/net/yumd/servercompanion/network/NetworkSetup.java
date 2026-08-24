package net.yumd.servercompanion.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.yumd.servercompanion.ServerCompanion;
import net.yumd.servercompanion.report.ServerReportHandler;

/**
 * Registers the server-bound (C2S) report payload. Lives in common code (loads on both sides)
 * but the handler it points at never references client-only classes, so it's safe here.
 * The client-bound (S2C) request payload is registered separately from ServerCompanionClient,
 * which is annotated Dist.CLIENT -- keeping the two apart avoids ever loading a class that
 * touches Minecraft.getInstance() on a dedicated server.
 */
@EventBusSubscriber(modid = ServerCompanion.MOD_ID)
public final class NetworkSetup {
    private NetworkSetup() {
    }

    @SubscribeEvent
    static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(ReportResponsePayload.TYPE, ReportResponsePayload.STREAM_CODEC, ServerReportHandler.INSTANCE);
    }
}
