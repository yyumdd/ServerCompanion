package net.yumd.servercompanion.network;

import java.util.List;
import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.yumd.servercompanion.ServerCompanion;

/**
 * Client -> server. The actual report: full mod list, local (player-added) resource pack names,
 * a hash of this mod's own jar (best-effort self-tamper check), and an HMAC signature covering
 * everything above plus the request's nonce/timestamp.
 */
public record ReportResponsePayload(
        UUID nonce,
        long timestamp,
        List<ModEntry> mods,
        List<String> localResourcePacks,
        String selfJarHash,
        String signatureHex) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ReportResponsePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ServerCompanion.MOD_ID, "report_response"));

    public static final StreamCodec<FriendlyByteBuf, ReportResponsePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeUUID(payload.nonce());
                buf.writeLong(payload.timestamp());
                NetCodecs.MOD_ENTRY_LIST.encode(buf, payload.mods());
                NetCodecs.STRING_LIST.encode(buf, payload.localResourcePacks());
                buf.writeUtf(payload.selfJarHash(), 128);
                buf.writeUtf(payload.signatureHex(), 128);
            },
            buf -> new ReportResponsePayload(
                    buf.readUUID(),
                    buf.readLong(),
                    NetCodecs.MOD_ENTRY_LIST.decode(buf),
                    NetCodecs.STRING_LIST.decode(buf),
                    buf.readUtf(128),
                    buf.readUtf(128)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
