package net.yumd.servercompanion.client;

import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.yumd.servercompanion.ServerCompanion;

/**
 * Shared by ClientReportHandler and ResourcePackWatcher so both read the exact same set of
 * "local" packs.
 */
public final class LocalResourcePacks {
    private LocalResourcePacks() {
    }

    // Packs known to be non-user-added but which still carry PackSource.DEFAULT (so the source
    // check alone doesn't exclude them). Confirmed via testing: "mod_resources" is NeoForge's
    // combined pack aggregating every mod's built-in resources.
    private static final Set<String> NON_USER_PACK_IDS = Set.of(
            "vanilla", "mod_resources", "programmer_art", "high_contrast");

    // Only packs with PackSource.DEFAULT are included -- that's the source used for ordinary
    // packs a player dropped into their resourcepacks folder, as opposed to mod-provided packs
    // or the server's own pushed pack (which carry other sources).
    public static List<String> collect() {
        try {
            return Minecraft.getInstance().getResourcePackRepository().getSelectedPacks().stream()
                    .filter(pack -> pack.getPackSource() == PackSource.DEFAULT)
                    .map(Pack::getId)
                    .filter(id -> !NON_USER_PACK_IDS.contains(id))
                    .toList();
        } catch (Exception e) {
            ServerCompanion.LOGGER.warn("ServerCompanion: failed to read local resource packs", e);
            return List.of();
        }
    }
}
