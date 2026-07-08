package net.yumd.servercompanion.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yumd.servercompanion.ServerCompanion;
import net.yumd.servercompanion.network.ModMessages;
import net.yumd.servercompanion.network.packet.HelloC2SPacket;
import net.minecraftforge.fml.ModList;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(
        modid = ServerCompanion.MOD_ID,
        value = Dist.CLIENT
)
public class ClientEvents {

    private static List<String> getInstalledMods() {
        var mods = ModList.get().getMods();
        var modList = new ArrayList<String>();

        for (var mod : mods) {
            modList.add(mod.getModId());
        }

        return modList;
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {

        System.out.println("ServerCompanion sending hello packet!");
        var mods = getInstalledMods();

        System.out.println(mods);

        ModMessages.INSTANCE.sendToServer(
                new HelloC2SPacket(ServerCompanion.VERSION, mods)
        );
    }
}