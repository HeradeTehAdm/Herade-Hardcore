package ru.herade.hardcore.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;

public final class HeradeHardcoreClient implements ClientModInitializer {
    private static final int PREPARATION_TICKS = 20 * 20;

    private static boolean preparing;
    private static int preparationTicks;
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
            // This challenge is deliberately limited to a private singleplayer world.
            // Opening the world to LAN disables the challenge rather than affecting other players.
            if (client.level == null || client.player == null
                    || client.getSingleplayerServer() == null || client.isMultiplayerServer()) {
                observedServer = null;
                preparing = false;
                preparationTicks = 0;
                return;
            }

            Object currentServer = client.getSingleplayerServer();
            if (currentServer != observedServer) {
                observedServer = currentServer;
                preparing = true;
                preparationTicks = PREPARATION_TICKS;
                client.player.setHealth(20.0F);
                client.player.displayClientMessage(
                        Component.literal("Подготовка к марафону: 20 секунд"), true);
            }

            if (preparing) {
                preparationTicks--;

                if (preparationTicks <= 0) {
                    preparing = false;
                    client.gui.setTitle(Component.literal("ХАРДКОР НАЧАЛСЯ!"));
                    client.player.displayClientMessage(
                            Component.literal("Задача: пройти Minecraft или смерть!"), false);
                } else if (preparationTicks % 20 == 0) {
                    int seconds = (preparationTicks + 19) / 20;
                    client.player.displayClientMessage(
                            Component.literal("Подготовка: " + seconds + " сек."), true);
                }
            }
        });
    }

    public static boolean isPreparing() {
        return preparing;
    }
}
