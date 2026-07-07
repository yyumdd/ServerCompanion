package net.yumd.servercompanion.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.yumd.servercompanion.ServerCompanion;

import java.util.function.Supplier;

public class HelloC2SPacket {

    public HelloC2SPacket() {

    }

    public HelloC2SPacket(FriendlyByteBuf buffer) {

    }

    public void encode(FriendlyByteBuf buffer) {

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
            } else {
                System.out.println("ServerCompanion received packet but sender was null");
            }

        });

        return true;
    }
}