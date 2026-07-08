package net.yumd.servercompanion.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import net.yumd.servercompanion.ServerCompanion;
import net.yumd.servercompanion.server.VerificationManager;

import java.util.function.Supplier;

public class HelloC2SPacket {
    private final String version;

    public HelloC2SPacket(String version) {
        this.version = version;
    }

    public HelloC2SPacket(FriendlyByteBuf buffer) {
        version = buffer.readUtf();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(version);
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
                VerificationManager.verify(sender.getUUID());
            } else {
                System.out.println("ServerCompanion received packet but sender was null");
            }

        });

        return true;
    }
}