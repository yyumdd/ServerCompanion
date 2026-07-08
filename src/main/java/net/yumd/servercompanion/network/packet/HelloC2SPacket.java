package net.yumd.servercompanion.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import net.yumd.servercompanion.ServerCompanion;
import net.yumd.servercompanion.server.VerificationManager;

import java.util.function.Supplier;
import java.util.List;
import java.util.ArrayList;

public class HelloC2SPacket {
    private final String version;
    private final List<String> mods;

    public HelloC2SPacket(String version, List<String> mods) {
        this.version = version;
        this.mods = mods;
    }

    public HelloC2SPacket(FriendlyByteBuf buffer) {

        version = buffer.readUtf();

        int modCount = buffer.readInt();

        mods = new ArrayList<>();

        for (int i = 0; i < modCount; i++) {
            mods.add(buffer.readUtf());
        }
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(version);
        buffer.writeInt(mods.size());
        for (var mod : mods) {
            buffer.writeUtf(mod);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {

        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {

            var sender = context.getSender();

            if (sender != null) {
                ServerCompanion.LOGGER.info(
                        "Received hello from {}",
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
                VerificationManager.verify(sender.getUUID());
            } else {
                System.out.println("ServerCompanion received packet but sender was null");
            }

        });

        return true;
    }
}