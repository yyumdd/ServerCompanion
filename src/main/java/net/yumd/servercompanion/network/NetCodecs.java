package net.yumd.servercompanion.network;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Small hand-written StreamCodecs for the list types our payloads carry. Written manually
 * (rather than via ByteBufCodecs collection helpers) to keep this file self-contained and easy
 * to verify against FriendlyByteBuf's own read/write methods.
 */
public final class NetCodecs {
    private NetCodecs() {
    }

    // Sanity caps so a malformed/hostile packet can't make us allocate something absurd.
    private static final int MAX_LIST_SIZE = 4096;

    public static final StreamCodec<FriendlyByteBuf, List<String>> STRING_LIST = StreamCodec.of(
            (buf, list) -> {
                buf.writeVarInt(list.size());
                for (String s : list) {
                    buf.writeUtf(s, 32767);
                }
            },
            buf -> {
                int size = buf.readVarInt();
                if (size > MAX_LIST_SIZE) {
                    throw new IllegalArgumentException("String list too large: " + size);
                }
                List<String> list = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    list.add(buf.readUtf(32767));
                }
                return list;
            });

    public static final StreamCodec<FriendlyByteBuf, ModEntry> MOD_ENTRY = StreamCodec.of(
            (buf, entry) -> {
                buf.writeUtf(entry.id(), 256);
                buf.writeUtf(entry.name(), 256);
                buf.writeUtf(entry.version(), 128);
            },
            buf -> new ModEntry(buf.readUtf(256), buf.readUtf(256), buf.readUtf(128)));

    public static final StreamCodec<FriendlyByteBuf, List<ModEntry>> MOD_ENTRY_LIST = StreamCodec.of(
            (buf, list) -> {
                buf.writeVarInt(list.size());
                for (ModEntry entry : list) {
                    MOD_ENTRY.encode(buf, entry);
                }
            },
            buf -> {
                int size = buf.readVarInt();
                if (size > MAX_LIST_SIZE) {
                    throw new IllegalArgumentException("Mod entry list too large: " + size);
                }
                List<ModEntry> list = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    list.add(MOD_ENTRY.decode(buf));
                }
                return list;
            });
}
