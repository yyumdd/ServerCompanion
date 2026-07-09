package net.yumd.servercompanion.network.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.ModList;
import net.minecraft.client.Minecraft;
import net.yumd.servercompanion.ServerCompanion;
import net.yumd.servercompanion.network.ModMessages;
import net.yumd.servercompanion.network.c2s.ClientInfoC2SPacket;

import java.util.ArrayList;
import java.util.function.Supplier;

public class RequestClientInfoS2CPacket {

    public RequestClientInfoS2CPacket() {

    }

    public RequestClientInfoS2CPacket(FriendlyByteBuf buffer) {

    }

    public void encode(FriendlyByteBuf buffer) {

    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {

        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {

            ServerCompanion.LOGGER.info("Server requested client information.");

            var mods = new ArrayList<String>();

            for (var mod : ModList.get().getMods()) {
                mods.add(mod.getModId());
            }

            var packs = new ArrayList<String>();

            for (var pack : Minecraft.getInstance()
                    .getResourcePackRepository()
                    .getAvailablePacks()) {

                packs.add(pack.getTitle().getString());
            }

            ModMessages.INSTANCE.sendToServer(
                    new ClientInfoC2SPacket(
                            ServerCompanion.VERSION,
                            mods,
                            packs
                    )
            );

        });

        return true;
    }
}