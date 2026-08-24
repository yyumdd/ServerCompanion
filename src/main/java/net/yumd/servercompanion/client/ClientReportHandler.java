package net.yumd.servercompanion.client;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.yumd.servercompanion.Config;
import net.yumd.servercompanion.ServerCompanion;
import net.yumd.servercompanion.integrity.HmacUtil;
import net.yumd.servercompanion.network.ModEntry;
import net.yumd.servercompanion.network.ReportRequestPayload;
import net.yumd.servercompanion.network.ReportResponsePayload;

public final class ClientReportHandler implements IPayloadHandler<ReportRequestPayload> {
    public static final ClientReportHandler INSTANCE = new ClientReportHandler();

    private ClientReportHandler() {
    }

    @Override
    public void handle(ReportRequestPayload request, IPayloadContext context) {
        List<ModEntry> mods = ModList.get().getMods().stream()
                .map(mod -> new ModEntry(mod.getModId(), mod.getDisplayName(), mod.getVersion().toString()))
                .toList();

        List<String> localPacks = collectLocalResourcePacks();
        String selfJarHash = computeSelfJarHash();

        String canonical = HmacUtil.canonicalPayload(request.nonce(), request.timestamp(), mods, localPacks, selfJarHash);
        String signature = HmacUtil.sign(Config.HMAC_SECRET.get(), canonical);

        ReportResponsePayload response = new ReportResponsePayload(
                request.nonce(), request.timestamp(), mods, localPacks, selfJarHash, signature);

        PacketDistributor.sendToServer(response);
    }

    // Only packs with PackSource.DEFAULT are included -- that's the source used for ordinary
    // packs a player dropped into their resourcepacks folder, as opposed to the base game pack,
    // mod-provided packs, or the server's own pushed pack (all of which carry other sources and
    // are excluded so the report only shows what the player actually added).
    // NOTE: verify this in-game against a test resourcepacks folder before relying on it --
    // I couldn't compile/run this against the real client to double-check the exact filtering.
    private static List<String> collectLocalResourcePacks() {
        try {
            return Minecraft.getInstance().getResourcePackRepository().getSelectedPacks().stream()
                    .filter(pack -> pack.getPackSource() == PackSource.DEFAULT)
                    .map(Pack::getId)
                    .toList();
        } catch (Exception e) {
            ServerCompanion.LOGGER.warn("ServerCompanion: failed to read local resource packs", e);
            return List.of();
        }
    }

    private static String computeSelfJarHash() {
        try {
            URL location = ServerCompanion.class.getProtectionDomain().getCodeSource().getLocation();
            Path path = Path.of(location.toURI());
            if (Files.isDirectory(path)) {
                // Running from exploded classes in a Gradle dev environment, not a real jar.
                return "dev-environment";
            }
            byte[] bytes = Files.readAllBytes(path);
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            return "unavailable";
        }
    }
}
