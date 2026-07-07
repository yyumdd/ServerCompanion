package net.yumd.servercompanion.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
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
    public static void onJoinServer(PlayerEvent.PlayerLoggedInEvent event) {
        ModMessages.INSTANCE.sendToServer(
                new HelloC2SPacket()
        );
    }
}