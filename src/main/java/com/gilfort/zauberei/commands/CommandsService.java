package com.gilfort.zauberei.commands;

import com.gilfort.zauberei.Zauberei;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** Service managing per-player timers and command execution. */
public class CommandsService {
    private static final CommandsService INSTANCE = new CommandsService();

    private CommandsConfig config = new CommandsConfig();
    private final Map<UUID, GwState> states = new HashMap<>();
    private int tickCounter = 0;

    public static void init() {
        NeoForge.EVENT_BUS.register(INSTANCE);
    }

    @SubscribeEvent
    public void onServerStart(ServerStartingEvent e) {
        config = CommandsConfig.load();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("zauberei").requires(cs -> cs.hasPermission(2));
        LiteralArgumentBuilder<CommandSourceStack> cmds = Commands.literal("commands");

        cmds.then(Commands.literal("reload").executes(ctx -> {
            config = CommandsConfig.load();
            ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("Commands config reloaded"), false);
            return 1;
        }));

        cmds.then(Commands.literal("dump").executes(ctx -> {
            CommandSourceStack src = ctx.getSource();
            ServerPlayer player = src.getPlayer();
            if (player == null) return 0;
            GwState st = states.get(player.getUUID());
            if (st == null) return 0;
            Optional<CommandWithPos> next = findMatchingCommand(player, st);
            src.sendSuccess(() -> net.minecraft.network.chat.Component.literal(
                    "tier=" + st.tier + " next=" + st.nextInTicks + " tags=" + st.activeTags +
                            " nextCmd=" + next.map(c -> c.command).orElse("<none>")), false);
            return 1;
        }));

        cmds.then(Commands.literal("now").executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayer();
            if (player == null) return 0;
            GwState st = states.get(player.getUUID());
            if (st != null) {
                st.nextInTicks = 0;
            }
            return 1;
        }));

        root.then(cmds);
        dispatcher.register(root);
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        GwState state = CommandsStateCodec.load(player);
        updateState(player, state);
        if (state.nextInTicks <= 0) {
            state.nextInTicks = randomWindow(state.tier);
        }
        states.put(player.getUUID(), state);
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        GwState state = states.remove(player.getUUID());
        if (state != null) {
            CommandsStateCodec.save(player, state);
        }
    }

    @SubscribeEvent
    public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        GwState state = CommandsStateCodec.load(player);
        updateState(player, state);
        states.put(player.getUUID(), state);
    }

    @SubscribeEvent
    public void onAdvancement(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        GwState state = states.get(player.getUUID());
        if (state == null) return;
        updateState(player, state);
        state.nextInTicks = Math.min(state.nextInTicks, 40); // trigger soon
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;
        if (tickCounter < 20) return;
        tickCounter = 0;
        if (event.getServer() == null) return;
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            GwState st = states.get(player.getUUID());
            if (st == null) continue;
            st.nextInTicks -= 20;
            if (st.nextInTicks <= 0) {
                runForPlayer(player, st);
                st.nextInTicks = randomWindow(st.tier);
                CommandsStateCodec.save(player, st);
            }
        }
    }

    private void updateState(ServerPlayer player, GwState state) {
        state.activeTags = computeTags(player);
        state.tier = computeTier(player, state.activeTags);
    }

    private Set<String> computeTags(ServerPlayer player) {
        Set<String> tags = new HashSet<>();
        for (CommandsConfig.TagRule rule : config.tagRules) {
            if (rule.tag == null) continue;
            boolean ok = true;
            if (rule.anyOf != null && !rule.anyOf.isEmpty()) {
                ok = false;
                for (String alias : rule.anyOf) {
                    if (aliasCompleted(player, alias)) { ok = true; break; }
                }
            }
            if (ok && rule.allOf != null) {
                for (String alias : rule.allOf) {
                    if (!aliasCompleted(player, alias)) { ok = false; break; }
                }
            }
            if (ok && rule.noneOf != null) {
                for (String alias : rule.noneOf) {
                    if (aliasCompleted(player, alias)) { ok = false; break; }
                }
            }
            if (ok) {
                tags.add(rule.tag);
            }
        }
        return tags;
    }

    private int computeTier(ServerPlayer player, Set<String> tags) {
        int tier = 1;
        for (CommandsConfig.TierRule rule : config.tierRules) {
            boolean ok = true;
            if (rule.anyOf != null && !rule.anyOf.isEmpty()) {
                ok = false;
                for (String alias : rule.anyOf) {
                    if (aliasCompleted(player, alias)) { ok = true; break; }
                }
            }
            if (ok && rule.allOf != null) {
                for (String alias : rule.allOf) {
                    if (!aliasCompleted(player, alias)) { ok = false; break; }
                }
            }
            if (ok && rule.noneOf != null) {
                for (String alias : rule.noneOf) {
                    if (aliasCompleted(player, alias)) { ok = false; break; }
                }
            }
            if (ok) {
                tier = Math.max(tier, rule.tier);
            }
        }
        return tier;
    }

    private boolean aliasCompleted(ServerPlayer player, String alias) {
        List<ResourceLocation> ids = config.aliases.get(alias);
        if (ids == null) return false;
        for (ResourceLocation id : ids) {
            if (CommandsAdvancementUtil.hasAdv(player, id)) {
                return true;
            }
        }
        return false;
    }

    private void runForPlayer(ServerPlayer player, GwState st) {
        Optional<CommandWithPos> opt = findMatchingCommand(player, st);
        if (opt.isEmpty()) {
            return;
        }
        CommandWithPos c = opt.get();
        BlockPos pos = computePosition(player, c.position);
        String cmd = CommandsPlaceholderUtil.apply(c.command, player, pos);
        CommandSourceStack src = player.createCommandSourceStack().withPermission(2).withSuppressedOutput();
        try {
            player.getServer().getCommands().performPrefixedCommand(src, cmd);
        } catch (Exception e) {
            Zauberei.LOGGER.error("Command failed: {}", e.getMessage());
        }
    }

    private Optional<CommandWithPos> findMatchingCommand(ServerPlayer player, GwState st) {
        List<CommandWithPos> pool = new ArrayList<>();
        if (config.commands != null) {
            for (CommandsConfig.CommandEntry entry : config.commands) {
                if (entry == null || entry.when == null || entry.pool == null) continue;
                if (!matches(entry.when, st)) continue;
                for (String cmd : entry.pool) {
                    pool.add(new CommandWithPos(cmd, entry.position));
                }
            }
        }
        if (pool.isEmpty()) return Optional.empty();
        return Optional.of(pool.get(ThreadLocalRandom.current().nextInt(pool.size())));
    }

    private boolean matches(CommandsConfig.When when, GwState st) {
        if (when == null) return false;
        if (when.minTier > st.tier) return false;
        if (when.anyTags != null && !when.anyTags.isEmpty()) {
            boolean ok = false;
            for (String tag : when.anyTags) {
                if (st.activeTags.contains(tag)) { ok = true; break; }
            }
            if (!ok) return false;
        }
        if (when.allTags != null) {
            for (String tag : when.allTags) {
                if (!st.activeTags.contains(tag)) return false;
            }
        }
        return true;
    }

    private int randomWindow(int tier) {
        int[] win = config.tierWindows.getOrDefault(tier, config.tierWindows.getOrDefault(1, new int[]{1,2}));
        int minutes = ThreadLocalRandom.current().nextInt(win[0], win[1] + 1);
        return minutes * 20 * 60;
    }

    private BlockPos computePosition(ServerPlayer player, String mode) {
        BlockPos base = player.blockPosition();
        if ("onPosition".equalsIgnoreCase(mode)) {
            return base;
        }
        int min = 5;
        int max = 15;
        if ("away".equalsIgnoreCase(mode)) {
            min = 25; max = 35;
        }
        double angle = ThreadLocalRandom.current().nextDouble() * Math.PI * 2;
        double radius = ThreadLocalRandom.current().nextDouble(min, max + 1);
        int dx = (int)Math.round(Math.cos(angle) * radius);
        int dz = (int)Math.round(Math.sin(angle) * radius);
        return base.offset(dx, 0, dz);
    }

    private static class CommandWithPos {
        final String command;
        final String position;
        CommandWithPos(String cmd, String pos){ this.command = cmd; this.position = pos; }
    }
}
