package ru.herade.hardcore.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.herade.hardcore.client.HeradeHardcoreClient;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void herade$blockPickupDuringPreparation(Player player, CallbackInfo ci) {
        if (player instanceof ServerPlayer && HeradeHardcoreClient.isPreparing()) {
            ci.cancel();
        }
    }
}
