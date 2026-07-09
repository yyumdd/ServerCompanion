package net.yumd.servercompanion.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.yumd.servercompanion.ServerCompanion;
import net.yumd.servercompanion.network.c2s.ClientHelloC2SPacket;
import net.yumd.servercompanion.network.c2s.ClientInfoC2SPacket;
import net.yumd.servercompanion.network.s2c.RequestClientInfoS2CPacket;

public class ModMessages {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE =
            NetworkRegistry.newSimpleChannel(
                    ResourceLocation.fromNamespaceAndPath(ServerCompanion.MOD_ID, "main"),
                    () -> PROTOCOL_VERSION,
                    clientAcceptedVersions -> true,
                    serverAcceptedVersions -> true
            );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        ServerCompanion.LOGGER.info("Registering ClientInfoPacket");

        INSTANCE.messageBuilder(
                        ClientHelloC2SPacket.class,
                        id(),
                        NetworkDirection.PLAY_TO_SERVER
                )
                .encoder(ClientHelloC2SPacket::encode)
                .decoder(ClientHelloC2SPacket::new)
                .consumerMainThread(ClientHelloC2SPacket::handle)
                .add();


        INSTANCE.messageBuilder(
                        ClientInfoC2SPacket.class,
                        id(),
                        NetworkDirection.PLAY_TO_SERVER
                )
                .encoder(ClientInfoC2SPacket::encode)
                .decoder(ClientInfoC2SPacket::new)
                .consumerMainThread(ClientInfoC2SPacket::handle)
                .add();


        INSTANCE.messageBuilder(
                        RequestClientInfoS2CPacket.class,
                        id(),
                        NetworkDirection.PLAY_TO_CLIENT
                )
                .encoder(RequestClientInfoS2CPacket::encode)
                .decoder(RequestClientInfoS2CPacket::new)
                .consumerMainThread(RequestClientInfoS2CPacket::handle)
                .add();
    }
}