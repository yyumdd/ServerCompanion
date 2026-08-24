package net.yumd.servercompanion.network;

import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.yumd.servercompanion.ServerCompanion;

/**
 * Server -> client. Asks the client to send back its mod/resource-pack report.
 * The nonce is echoed back by the client and included in its HMAC signature, so a stale or
 * replayed response won't verify against a fresh request.
 */
public record ReportRequestPayload(UUID nonce, long timestamp) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ReportRequestPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ServerCompanion.MOD_ID, "report_request"));

    public static final StreamCodec<FriendlyByteBuf, ReportRequestPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeUUID(payload.nonce());
                buf.writeLong(payload.timestamp());
            },
            buf -> new ReportRequestPayload(buf.readUUID(), buf.readLong()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
