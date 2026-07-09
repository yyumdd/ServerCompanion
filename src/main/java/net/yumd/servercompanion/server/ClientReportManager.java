package net.yumd.servercompanion.server;

import net.minecraft.server.MinecraftServer;
import net.yumd.servercompanion.ServerCompanion;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientReportManager {

    private static final Map<UUID, ClientReport> reports = new HashMap<>();

    private static void saveToDisk(ClientReport report, MinecraftServer server) {

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