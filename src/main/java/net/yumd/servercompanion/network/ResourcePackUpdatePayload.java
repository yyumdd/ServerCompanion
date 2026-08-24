package net.yumd.servercompanion.network;

import java.util.List;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.yumd.servercompanion.ServerCompanion;

/**
 * Client -> server, sent unsolicited whenever the client notices its local resource pack
 * selection changed (checked periodically, not on every tick -- see ResourcePackWatcher).
 * Unlike ReportRequestPayload/ReportResponsePayload this isn't a request/response, so there's no
 * server-issued nonce; freshness is checked via timestamp only, same lightweight integrity tier
 * as the rest of this mod.
 */
public record ResourcePackUpdatePayload(long timestamp, List<String> localResourcePacks, String signatureHex)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ResourcePackUpdatePayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(ServerCompanion.MOD_ID, "resource_pack_update"));

    public static final StreamCodec<FriendlyByteBuf, ResourcePackUpdatePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeLong(payload.timestamp());
                NetCodecs.STRING_LIST.encode(buf, payload.localResourcePacks());
                buf.writeUtf(payload.signatureHex(), 128);
            },
            buf -> new ResourcePackUpdatePayload(
                    buf.readLong(),
                    NetCodecs.STRING_LIST.decode(buf),
                    buf.readUtf(128)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
