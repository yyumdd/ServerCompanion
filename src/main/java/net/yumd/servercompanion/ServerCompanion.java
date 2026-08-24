package net.yumd.servercompanion;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.yumd.servercompanion.command.ServerCompanionCommand;
import net.yumd.servercompanion.report.ReportService;
import net.yumd.servercompanion.report.ResourcePackStateTracker;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ServerCompanion.MOD_ID)
public class ServerCompanion {
    public static final String MOD_ID = "servercompanion";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ServerCompanion(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        if (Config.HMAC_SECRET.get().equals("CHANGE-ME-BEFORE-DISTRIBUTING")) {
            LOGGER.warn("ServerCompanion: hmacSecret is still set to the default value. "
                    + "Change it in config/servercompanion-common.toml before distributing your modpack.");
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ServerCompanionCommand.register(event.getDispatcher());
    }

    // Auto-trigger a report the moment a player finishes logging in. No command sender is
    // attached (source = null), so the result is only logged and posted to Discord, never
    // printed to anyone's chat.
    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ReportService.requestReport(player, null);
        }
    }

    // Clears the resource pack change baseline so a later session never diffs against stale
    // state from an earlier one.
    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ResourcePackStateTracker.clear(player.getUUID());
        }
    }
}
