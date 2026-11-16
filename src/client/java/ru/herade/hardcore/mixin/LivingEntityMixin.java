package ru.herade.hardcore.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.herade.hardcore.client.HeradeHardcoreClient;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void herade$blockHealingDuringRun(float amount, CallbackInfo ci) {
        if ((Object) this instanceof Player && HeradeHardcoreClient.isRunStarted()) {
            ci.cancel();
        }
    }
}
