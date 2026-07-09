package net.yumd.servercompanion.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yumd.servercompanion.ServerCompanion;
import net.yumd.servercompanion.network.ModMessages;
import net.yumd.servercompanion.network.c2s.ClientHelloC2SPacket;
import net.yumd.servercompanion.network.c2s.ClientInfoC2SPacket;
import net.minecraftforge.fml.ModList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;

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

    private static List<String> getInstalledResourcePacks() {

        var packs = Minecraft.getInstance()
                .getResourcePackRepository()
                .getAvailablePacks();

        var resourcePacks = new ArrayList<String>();

        for (var pack : packs) {
            resourcePacks.add(pack.getTitle().getString());
        }

        return resourcePacks;
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {

        System.out.println("ServerCompanion sending hello packet!");

        var mods = getInstalledMods();
        var resourcePacks = getInstalledResourcePacks();

        System.out.println(mods);
        System.out.println(resourcePacks);

        System.out.println("Sending ClientInfoPacket");

        ModMessages.INSTANCE.sendToServer(
                new ClientHelloC2SPacket(ServerCompanion.VERSION)
        );
    }
}