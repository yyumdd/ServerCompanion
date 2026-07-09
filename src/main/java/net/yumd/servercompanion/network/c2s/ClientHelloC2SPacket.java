package net.yumd.servercompanion.network.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.yumd.servercompanion.ServerCompanion;
import net.yumd.servercompanion.network.ModMessages;
import net.yumd.servercompanion.network.s2c.RequestClientInfoS2CPacket;

import java.util.function.Supplier;

public class ClientHelloC2SPacket {

    private final String version;

    public ClientHelloC2SPacket(String version) {
        this.version = version;
    }

    public ClientHelloC2SPacket(FriendlyByteBuf buffer) {
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
                        "{} sent hello. Version {}",
                        sender.getName().getString(),
                        version
                );
                ModMessages.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> sender),
                        new RequestClientInfoS2CPacket()
                );
            }

        });

        return true;
    }
}