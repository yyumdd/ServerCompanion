package net.yumd.servercompanion.report;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.yumd.servercompanion.network.ReportResponsePayload;

public final class ServerReportHandler implements IPayloadHandler<ReportResponsePayload> {
    public static final ServerReportHandler INSTANCE = new ServerReportHandler();

    private ServerReportHandler() {
    }

    @Override
    public void handle(ReportResponsePayload payload, IPayloadContext context) {
        // If the nonce isn't recognized, it's either a duplicate/replayed response or one that
        // already timed out -- silently ignore rather than acting on it twice.
        PendingRequestTracker.Pending pending = PendingRequestTracker.complete(payload.nonce());
        if (pending == null) {
            return;
        }
        ReportService.handleResponse(pending, payload);
    }
}
