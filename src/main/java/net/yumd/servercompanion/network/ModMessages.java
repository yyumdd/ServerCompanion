package net.yumd.servercompanion.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.yumd.servercompanion.ServerCompanion;
import net.yumd.servercompanion.network.packet.HelloC2SPacket;

public class ModMessages {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE =
            NetworkRegistry.newSimpleChannel(
                    ResourceLocation.fromNamespaceAndPath(ServerCompanion.MOD_ID, "main"),
                    () -> PROTOCOL_VERSION,
                    PROTOCOL_VERSION::equals,
                    PROTOCOL_VERSION::equals
            );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        INSTANCE.messageBuilder(
            HelloC2SPacket.class,
            id(),
            net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER
        )
        .encoder(HelloC2SPacket::encode)
        .decoder(HelloC2SPacket::new)
        .consumerMainThread(HelloC2SPacket::handle)
        .add();
    }
}