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
    private static final int TITLE_TICKS = 70;

    private static boolean preparing;
    private static int preparationTicks;
    private static int titleTicks;
    private static Object observedLevel;

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
            if (client.level == null || client.player == null) {
                observedLevel = null;
                preparing = false;
                preparationTicks = 0;
                titleTicks = 0;
                return;
            }

            if (client.level != observedLevel) {
                observedLevel = client.level;
                preparing = true;
                preparationTicks = PREPARATION_TICKS;
                titleTicks = 0;
                client.player.setHealth(20.0F);
                client.player.displayClientMessage(
                        Component.literal("Подготовка к марафону: 20 секунд"), true);
            }

            if (preparing) {
                preparationTicks--;

                if (preparationTicks <= 0) {
                    preparing = false;
                    titleTicks = TITLE_TICKS;
                    client.gui.setTitle(Component.literal("ХАРДКОР НАЧАЛСЯ!"));
                    client.player.displayClientMessage(
                            Component.literal("Задача: пройти Minecraft или смерть!"), false);
                } else if (preparationTicks % 20 == 0) {
                    int seconds = (preparationTicks + 19) / 20;
                    client.player.displayClientMessage(
                            Component.literal("Подготовка: " + seconds + " сек."), true);
                }
            } else if (titleTicks > 0) {
                titleTicks--;
            }
        });
    }

    public static boolean isPreparing() {
        return preparing;
    }
}
