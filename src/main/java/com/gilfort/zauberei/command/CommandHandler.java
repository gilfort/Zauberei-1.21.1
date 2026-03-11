package com.gilfort.zauberei.command;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;


public class CommandHandler {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();


    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {

    }
}