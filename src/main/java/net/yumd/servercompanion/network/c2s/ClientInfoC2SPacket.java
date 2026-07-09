package net.yumd.servercompanion.network.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import net.yumd.servercompanion.ServerCompanion;
import net.yumd.servercompanion.server.VerificationManager;

import java.util.function.Supplier;
import java.util.List;
import java.util.ArrayList;

public class ClientInfoC2SPacket {
    private final String version;
    private final List<String> mods;
    private final List<String> resourcePacks;

    public ClientInfoC2SPacket(String version, List<String> mods, List<String> resourcePacks) {
        this.version = version;
        this.mods = mods;
        this.resourcePacks = resourcePacks;
    }

    public ClientInfoC2SPacket(FriendlyByteBuf buffer) {

        ServerCompanion.LOGGER.info("Decoding ClientInfoC2SPacket");

        version = buffer.readUtf();

        int modCount = buffer.readInt();

        mods = new ArrayList<>();

        for (int i = 0; i < modCount; i++) {
            mods.add(buffer.readUtf());
        }

        int resourcePackCount = buffer.readInt();

        resourcePacks = new ArrayList<>();

        for (int i = 0; i < resourcePackCount; i++) {
            resourcePacks.add(buffer.readUtf());
        }
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(version);
        buffer.writeInt(mods.size());
        for (var mod : mods) {
            buffer.writeUtf(mod);
        }
        buffer.writeInt(resourcePacks.size());
        for (var resourcePack : resourcePacks) {
            buffer.writeUtf(resourcePack);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {

        ServerCompanion.LOGGER.info("ClientInfoC2SPacket received!");

        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {

            var sender = context.getSender();

            if (sender != null) {
                ServerCompanion.LOGGER.info(
                        "Received client info from {}",
                        sender.getName().getString()
                );
                ServerCompanion.LOGGER.info(
                        "ServerCompanion version: {}",
                        version
                );
                ServerCompanion.LOGGER.info(
                        "Client mods: {}",
                        mods
                );
                ServerCompanion.LOGGER.info(
                        "Client resource packs: {}",
                        resourcePacks
                );
                VerificationManager.verify(sender.getUUID());
            } else {
                System.out.println("ServerCompanion received packet but sender was null");
            }

        });

        return true;
    }
}