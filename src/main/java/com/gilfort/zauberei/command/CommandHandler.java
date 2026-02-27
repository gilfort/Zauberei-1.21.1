package com.gilfort.zauberei.command;

import com.gilfort.zauberei.Zauberei;
import com.gilfort.zauberei.helpers.PlayerDataHelper;
import com.gilfort.zauberei.item.armorbonus.ArmorSetData;
import com.gilfort.zauberei.item.armorbonus.ArmorSetDataRegistry;
import com.gilfort.zauberei.item.armorbonus.ZaubereiReloadListener;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

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
                        .then(Commands.literal("debug")
                                .requires(src -> src.hasPermission(2))
                                .then(Commands.literal("tag")
                                        .then(Commands.argument("tag", StringArgumentType.string())
                                                .executes(ctx -> debugTag(ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "tag")))))
                                .then(Commands.literal("sets")
                                        .executes(ctx -> debugSets(ctx.getSource())))
                        .then(Commands.literal("reload")
                                .executes(context -> reloadArmorEffects(context.getSource())))));
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

    private static int debugSets(CommandSourceStack source) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("Player only."));
            return 0;
        }

        String major = PlayerDataHelper.getMajor(player);
        int year = PlayerDataHelper.getYear(player);

        source.sendSystemMessage(Component.literal("[Zauberei Debug] ")
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal("major=" + major + ", year=" + year)));

        Set<String> tags = ArmorSetDataRegistry.getRegisteredTags(major.toLowerCase(), year);
        if (tags.isEmpty()) {
            source.sendSystemMessage(Component.literal("No set tags registered for this major/year.")
                    .withStyle(ChatFormatting.YELLOW));
            return 1;
        }

        source.sendSystemMessage(Component.literal("Registered tags (" + tags.size() + "):")
                .withStyle(ChatFormatting.GRAY));

        for (String tagString : tags) {
            debugTagInternal(source, player, tagString);
        }

        return 1;
    }

    /**
     * /zauberei debug tag <namespace:tagpath>
     * Example: /zauberei debug tag zauberei:magiccloth_armor
     */
    private static int debugTag(CommandSourceStack source, String tagStringRaw) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("Player only."));
            return 0;
        }

        // allow "magiccloth_armor" shorthand -> assume zauberei namespace
        String tagString = tagStringRaw.contains(":") ? tagStringRaw : (Zauberei.MODID + ":" + tagStringRaw);

        debugTagInternal(source, player, tagString);
        return 1;
    }

    private static void debugTagInternal(CommandSourceStack source, ServerPlayer player, String tagString) {
        ResourceLocation tagLoc = ResourceLocation.tryParse(tagString);
        if (tagLoc == null) {
            source.sendFailure(Component.literal("Invalid tag ResourceLocation: " + tagString));
            return;
        }

        String major = PlayerDataHelper.getMajor(player);
        int year = PlayerDataHelper.getYear(player);

        TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagLoc);

        int count = 0;

        // Armor slots are iterable; we print each stack + match status
        source.sendSystemMessage(Component.literal("")
                .append(Component.literal("Tag ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(tagString).withStyle(ChatFormatting.GOLD)));

        for (ItemStack armorStack : player.getArmorSlots()) {
            boolean matches = !armorStack.isEmpty() && armorStack.is(tagKey);
            if (matches) count++;

            String itemId = armorStack.isEmpty()
                    ? "<empty>"
                    : String.valueOf(BuiltInRegistries.ITEM.getKey(armorStack.getItem()));

            source.sendSystemMessage(Component.literal(" - ")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(itemId).withStyle(ChatFormatting.WHITE))
                    .append(Component.literal("  matches=").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(String.valueOf(matches))
                            .withStyle(matches ? ChatFormatting.GREEN : ChatFormatting.RED)));
        }

        source.sendSystemMessage(Component.literal(" => wornParts=" + count + "/4")
                .withStyle(ChatFormatting.AQUA));

        ArmorSetData data = ArmorSetDataRegistry.getData(major.toLowerCase(), year, tagString);
        if (data == null) {
            source.sendSystemMessage(Component.literal("No set definition loaded for this tag at major/year.")
                    .withStyle(ChatFormatting.YELLOW));
        } else {
            source.sendSystemMessage(Component.literal("Set definition: FOUND (parts keys=" + data.getParts().keySet() + ")")
                    .withStyle(ChatFormatting.GREEN));
        }
    }

}
