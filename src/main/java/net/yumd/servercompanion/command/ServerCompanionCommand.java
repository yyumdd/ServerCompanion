package net.yumd.servercompanion.command;

import java.util.List;
import java.util.stream.Collectors;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.yumd.servercompanion.Config;
import net.yumd.servercompanion.report.ReportService;

public final class ServerCompanionCommand {
    private ServerCompanionCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("servercompanion")
                .requires(src -> src.hasPermission(Config.COMMAND_PERMISSION_LEVEL.get()))
                .then(Commands.literal("report")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ServerCompanionCommand::runReport)))
                .then(Commands.literal("whitelist")
                        .then(Commands.literal("dump")
                                .executes(ServerCompanionCommand::dumpWhitelist))));
    }

    private static int runReport(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        ReportService.requestReport(target, ctx.getSource());
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Requested report from " + target.getGameProfile().getName() + "..."), false);
        return 1;
    }

    // Reads the SERVER's own loaded mods (i.e. the modpack) and prints a ready-to-paste
    // modWhitelist array, so you don't have to hand-type every mod ID.
    private static int dumpWhitelist(CommandContext<CommandSourceStack> ctx) {
        List<String> ids = ModList.get().getMods().stream()
                .map(mod -> mod.getModId())
                .sorted()
                .toList();

        String arrayLiteral = ids.stream()
                .map(id -> "\"" + id + "\"")
                .collect(Collectors.joining(", "));
        String line = "modWhitelist = [" + arrayLiteral + "]";

        MutableComponent clickToCopy = Component.literal(line)
                .setStyle(Style.EMPTY
                        .withColor(ChatFormatting.AQUA)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, line))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.literal("Click to copy to clipboard"))));

        ctx.getSource().sendSuccess(() -> clickToCopy, false);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "^ click that line to copy it, then paste into config/servercompanion-common.toml "
                        + "(replacing the existing modWhitelist line), then /reload or restart the server."), false);
        return 1;
    }
}