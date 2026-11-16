package ru.herade.hardcore.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.GameType;

public final class HeradeHardcoreClient implements ClientModInitializer {
    private static final int PREPARATION_TICKS = 20 * 20;
    private static final int TRANSITION_TICKS = 20 * 3;
    private static final float PREPARATION_HEALTH = 10.0F;
    private static final float STARTING_RUN_HEALTH = 1.0F;
    private static final float EXTRA_EXHAUSTION_PER_TICK = 0.004F;

    private static volatile boolean preparing;
    private static int preparationTicks;
    private static int transitionTicks;
    private static volatile boolean runStarted;
    private static Object observedServer;

    @Override
    public void onInitializeClient() {
        ClientPreAttackCallback.EVENT.register((client, player, clickCount) -> preparing);

        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) ->
                preparing ? InteractionResult.FAIL : InteractionResult.PASS);

        AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) ->
                preparing ? InteractionResult.FAIL : InteractionResult.PASS);

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) ->
                preparing ? InteractionResult.FAIL : InteractionResult.PASS);

        UseItemCallback.EVENT.register((player, level, hand) ->
                preparing ? InteractionResult.FAIL : InteractionResult.PASS);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // The challenge is limited to a private singleplayer world.
            if (client.level == null || client.player == null
                    || client.getSingleplayerServer() == null || client.isMultiplayerServer()) {
                observedServer = null;
                preparing = false;
                preparationTicks = 0;
                transitionTicks = 0;
                runStarted = false;
                return;
            }

            var integratedServer = client.getSingleplayerServer();

            if (integratedServer != observedServer) {
                observedServer = integratedServer;
                preparing = true;
                preparationTicks = PREPARATION_TICKS;
                transitionTicks = 0;
                runStarted = false;

                var playerId = client.player.getUUID();
                integratedServer.execute(() -> {
                    var serverPlayer = integratedServer.getPlayerList().getPlayer(playerId);
                    if (serverPlayer != null) {
                        serverPlayer.setHealth(PREPARATION_HEALTH);
                        serverPlayer.setGameMode(GameType.SURVIVAL);
                    }
                });

                client.player.setHealth(PREPARATION_HEALTH);
                client.player.sendOverlayMessage(
                        Component.literal("Подготовка к марафону: 20 секунд"));
            }

            // Re-apply Survival so changing to Creative or another mode cannot bypass the run.
            var playerId = client.player.getUUID();
            integratedServer.execute(() -> {
                var serverPlayer = integratedServer.getPlayerList().getPlayer(playerId);
                if (serverPlayer != null && serverPlayer.gameMode.getGameModeForPlayer() != GameType.SURVIVAL) {
                    serverPlayer.setGameMode(GameType.SURVIVAL);
                }

                if (serverPlayer != null && runStarted) {
                    // Keep the challenge at exactly half a heart; healing cannot restore health.
                    if (serverPlayer.getHealth() > STARTING_RUN_HEALTH) {
                        serverPlayer.setHealth(STARTING_RUN_HEALTH);
                    }
                    // A small additional exhaustion drain makes hunger fall faster, but not instantly.
                    serverPlayer.getFoodData().addExhaustion(EXTRA_EXHAUSTION_PER_TICK);
                }
            });

            if (preparing) {
                preparationTicks--;

                if (preparationTicks <= 0) {
                    preparing = false;
                    transitionTicks = TRANSITION_TICKS;
                    client.player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.DARKNESS,
                            TRANSITION_TICKS, 0, false, false, false));
                    integratedServer.execute(() -> {
                        var serverPlayer = integratedServer.getPlayerList().getPlayer(playerId);
                        if (serverPlayer != null) {
                            serverPlayer.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                    net.minecraft.world.effect.MobEffects.DARKNESS,
                                    TRANSITION_TICKS, 0, false, false, false));
                        }
                    });
                } else if (preparationTicks % 20 == 0) {
                    int seconds = (preparationTicks + 19) / 20;
                    client.player.sendOverlayMessage(
                            Component.literal("Подготовка: " + seconds + " сек."));
                }
            } else if (transitionTicks > 0) {
                transitionTicks--;
                if (transitionTicks == 0) {
                    runStarted = true;
                    client.gui.hud.setTitle(
                            Component.literal("ХАРДКОР НАЧАЛСЯ!").withStyle(ChatFormatting.RED));
                    client.player.playSound(SoundEvents.WITHER_SPAWN, 1.0F, 0.75F);
                    client.player.setHealth(STARTING_RUN_HEALTH);
                    integratedServer.execute(() -> {
                        var serverPlayer = integratedServer.getPlayerList().getPlayer(playerId);
                        if (serverPlayer != null) {
                            serverPlayer.setHealth(STARTING_RUN_HEALTH);
                        }
                    });
                    client.player.sendSystemMessage(Component.literal("Задачи: пройти майнкрафт"));
                    client.player.sendSystemMessage(
                            Component.literal("ItsNikitinka: Удачи умереть бро)))"));
                }
            }
        });
    }

    public static boolean isPreparing() {
        return preparing;
    }

    public static boolean isRunStarted() {
        return runStarted;
    }
}
