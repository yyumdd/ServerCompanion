package net.yumd.servercompanion.client;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

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

        List<String> localPacks = LocalResourcePacks.collect();
        String selfJarHash = computeSelfJarHash();

        String canonical = HmacUtil.canonicalPayload(request.nonce(), request.timestamp(), mods, localPacks, selfJarHash);
        String signature = HmacUtil.sign(Config.HMAC_SECRET.get(), canonical);

        ReportResponsePayload response = new ReportResponsePayload(
                request.nonce(), request.timestamp(), mods, localPacks, selfJarHash, signature);

        PacketDistributor.sendToServer(response);
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
