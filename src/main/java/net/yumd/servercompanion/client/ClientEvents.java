package net.yumd.servercompanion.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yumd.servercompanion.ServerCompanion;
import net.yumd.servercompanion.network.ModMessages;
import net.yumd.servercompanion.network.packet.HelloC2SPacket;

@Mod.EventBusSubscriber(
        modid = ServerCompanion.MOD_ID,
        value = Dist.CLIENT
)
public class ClientEvents {

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {

        System.out.println("ServerCompanion sending hello packet!");

        ModMessages.INSTANCE.sendToServer(
                new HelloC2SPacket("0.1-1.20.1")
        );
    }
}