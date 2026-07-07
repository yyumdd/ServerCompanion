package net.yumd.servercompanion.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.yumd.servercompanion.ServerCompanion;

public class ModMessages {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE =
            NetworkRegistry.newSimpleChannel(
                    new ResourceLocation(ServerCompanion.MOD_ID, "main"),
                    () -> PROTOCOL_VERSION,
                    PROTOCOL_VERSION::equals,
                    PROTOCOL_VERSION::equals
            );

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {

    }
}