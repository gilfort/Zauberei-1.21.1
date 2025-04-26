package com.gilfort.zauberei.command;

import com.gilfort.zauberei.helpers.PlayerDataHelper;
import com.gilfort.zauberei.item.armorbonus.ArmorSetDataRegistry;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.WorldData;

import java.util.Optional;
import java.util.Set;


public class CommandHandler {
    private static final Set<String> VALID_MAJORS = Set.of(
            "summoning", "alchemy", "arcane", "elemental",
            "herbomancy", "magicalcombat", "dark_arts"
    );

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("zauberei")
                        .then(Commands.literal("setmajor")
                                .then(Commands.argument("major", StringArgumentType.word())
                                        .executes(CommandHandler::setMajorCommand)))
                        .then(Commands.literal("checktag")
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .executes(CommandHandler::checkTagCommand)))
                        .then(Commands.literal("setyear")
                                .then(Commands.argument("year", IntegerArgumentType.integer())
                                        .executes(CommandHandler::setYearCommand)))
                        .then(Commands.literal("checkyeartag")
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .executes(CommandHandler::checkYearTagCommand)))
                        .then(Commands.literal("test_sets")
                                .then(Commands.argument("major", StringArgumentType.word())
                                        .then(Commands.argument("year", IntegerArgumentType.integer())
                                                .then(Commands.argument("armorMaterial", StringArgumentType.word())
                                                        .executes(CommandHandler::testSetsCommand))))));
    }

    private  static int testSetsCommand(CommandContext<CommandSourceStack> context){
        String major = StringArgumentType.getString(context, "major");
        int year = IntegerArgumentType.getInteger(context, "year");
        String armorMaterial = StringArgumentType.getString(context, "armorMaterial");
        CommandSourceStack source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Dieser Befehl kann nur von einem Spieler ausgeführt werden."));
            return Command.SINGLE_SUCCESS;
        }

        if (!VALID_MAJORS.contains(major)) {
            source.sendFailure(Component.literal("Ungültiger Major-Typ. Gültige Optionen: " + VALID_MAJORS));
            return Command.SINGLE_SUCCESS;
        }

        ArmorSetDataRegistry.debugPrintData(major, year, armorMaterial);

        return Command.SINGLE_SUCCESS;
    }

    private static int setMajorCommand(CommandContext<CommandSourceStack> context) {
        String major = StringArgumentType.getString(context, "major");
        CommandSourceStack source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Dieser Befehl kann nur von einem Spieler ausgeführt werden."));
            return Command.SINGLE_SUCCESS;
        }

        if (!VALID_MAJORS.contains(major)) {
            source.sendFailure(Component.literal("Ungültiger Major-Typ. Gültige Optionen: " + VALID_MAJORS));
            return Command.SINGLE_SUCCESS;
        }

        PlayerDataHelper.setMajor(player, major);
        source.sendSuccess(() -> Component.literal("Mayor-Typ gesetzt: " + major), true);

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

    private static int checkTagCommand(CommandContext<CommandSourceStack> context) {
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

    private static int checkYearTagCommand(CommandContext<CommandSourceStack> context) {
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
