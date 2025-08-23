package com.gilfort.zauberei.command;

import com.gilfort.zauberei.helpers.PlayerDataHelper;
import com.gilfort.zauberei.item.armorbonus.ArmorSetDataRegistry;
import com.gilfort.zauberei.item.armorbonus.ZaubereiReloadListener;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;


public class CommandHandler {

    public static final SuggestionProvider<CommandSourceStack> MAJOR_SUGGESTIONS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(ArmorSetDataRegistry.getMajors(), builder);

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("zauberei")
                        .then(Commands.literal("setmajor")
                                .then(Commands.argument("major", StringArgumentType.word())
                                        .suggests(MAJOR_SUGGESTIONS)
                                        .executes(CommandHandler::setMajorCommand)))
                        .then(Commands.literal("checkmajor")
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .executes(CommandHandler::checkMajorCommand)))
                        .then(Commands.literal("setyear")
                                .then(Commands.argument("year", IntegerArgumentType.integer())
                                        .executes(CommandHandler::setYearCommand)))
                        .then(Commands.literal("checkyear")
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .executes(CommandHandler::checkYearCommand)))
                        .then(Commands.literal("reload")
                                .executes(context -> reloadArmorEffects(context.getSource()))));
    }


    private static int reloadArmorEffects(CommandSourceStack source){
        ZaubereiReloadListener.loadAllEffects();
        source.sendSuccess(()-> net.minecraft.network.chat.Component.literal("[Zauberei] Reloaded Set-Effects"), true);
        return 1;
    }


    private static int setMajorCommand(CommandContext<CommandSourceStack> context) {
        String major = StringArgumentType.getString(context, "major");
        CommandSourceStack source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Dieser Befehl kann nur von einem Spieler ausgeführt werden."));
            return Command.SINGLE_SUCCESS;
        }

        PlayerDataHelper.setMajor(player, major);
        source.sendSuccess(() -> Component.literal("Major-Typ gesetzt: " + major), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int setYearCommand(CommandContext<CommandSourceStack> context) {
        int year = IntegerArgumentType.getInteger(context, "year");
        CommandSourceStack source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Dieser Befehl kann nur von einem Spieler ausgeführt werden."));
            return Command.SINGLE_SUCCESS;
        }

        PlayerDataHelper.setYear(player, year);
        source.sendSuccess(() -> Component.literal("Year gesetzt: " + year), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int checkMajorCommand(CommandContext<CommandSourceStack> context) {
        String playerName = StringArgumentType.getString(context, "player");
        CommandSourceStack source = context.getSource();

        ServerPlayer targetPlayer = source.getServer().getPlayerList().getPlayerByName(playerName);
        if (targetPlayer == null) {
            source.sendFailure(Component.literal("Spieler " + playerName + " wurde nicht gefunden."));
            return Command.SINGLE_SUCCESS;
        }

        String major = PlayerDataHelper.getMajor(targetPlayer);

        source.sendSuccess(() -> Component.literal("Player:" + playerName + " hat den Major-Typ: " + major), true);

        return Command.SINGLE_SUCCESS;
    }

    private static int checkYearCommand(CommandContext<CommandSourceStack> context) {
        String playerName = StringArgumentType.getString(context, "player");
        CommandSourceStack source = context.getSource();

        ServerPlayer targetPlayer = source.getServer().getPlayerList().getPlayerByName(playerName);
        if (targetPlayer == null) {
            source.sendFailure(Component.literal("Spieler " + playerName + " wurde nicht gefunden."));
            return Command.SINGLE_SUCCESS;
        }

        int year = PlayerDataHelper.getYear(targetPlayer);
        source.sendSuccess(() -> Component.literal("Player:" + playerName + " ist im Jahr: " + year), true);

        return Command.SINGLE_SUCCESS;
    }

}
