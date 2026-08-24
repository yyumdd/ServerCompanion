package net.yumd.servercompanion.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.yumd.servercompanion.ServerCompanion;
import net.yumd.servercompanion.client.ClientReportHandler;
import net.yumd.servercompanion.report.ResourcePackUpdateHandler;
import net.yumd.servercompanion.report.ServerReportHandler;

/**
 * Registers all payload directions from common code. Both sides need to know about every
 * channel for the handshake to succeed, even though each handler only ever actually runs on the
 * side that receives that packet -- e.g. ClientReportHandler is never invoked on the server, the
 * server only sends ReportRequestPayload, it never receives it.
 */
@EventBusSubscriber(modid = ServerCompanion.MOD_ID)
public final class NetworkSetup {
    private NetworkSetup() {
    }

    @SubscribeEvent
    static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(ReportResponsePayload.TYPE, ReportResponsePayload.STREAM_CODEC, ServerReportHandler.INSTANCE);
        registrar.playToServer(ResourcePackUpdatePayload.TYPE, ResourcePackUpdatePayload.STREAM_CODEC, ResourcePackUpdateHandler.INSTANCE);
        registrar.playToClient(ReportRequestPayload.TYPE, ReportRequestPayload.STREAM_CODEC, ClientReportHandler.INSTANCE);
    }
}
