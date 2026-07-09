package net.yumd.servercompanion.server;

import net.minecraft.server.MinecraftServer;
import net.yumd.servercompanion.ServerCompanion;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClientReportManager {

    private static final Map<UUID, ClientReport> reports = new HashMap<>();

    private static void saveToDisk(ClientReport report, MinecraftServer server) {

        try {

            Path folder = server
                    .getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                    .resolve("servercompanion")
                    .resolve("reports");


            Files.createDirectories(folder);


            Path file = folder.resolve(
                    report.getPlayerUUID() + ".json"
            );


            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();


            String json = gson.toJson(report);


            Files.writeString(
                    file,
                    json
            );


            ServerCompanion.LOGGER.info(
                    "Saved client report for {} to {}",
                    report.getPlayerName(),
                    file
            );

        } catch (IOException e) {

            ServerCompanion.LOGGER.error(
                    "Failed to save client report",
                    e
            );

        }
    }

    public static void saveReport(ClientReport report, MinecraftServer server) {

        reports.put(
                report.getPlayerUUID(),
                report
        );

        saveToDisk(report, server);
    }


    public static ClientReport getReport(UUID uuid) {

        return reports.get(uuid);

    }


    public static void removeReport(UUID uuid) {

        reports.remove(uuid);

    }
}