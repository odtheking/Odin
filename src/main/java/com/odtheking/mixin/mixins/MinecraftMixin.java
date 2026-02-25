package com.odtheking.mixin.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.odtheking.odin.events.BlockInteractEvent;
import com.odtheking.odin.events.EntityInteractEvent;
import com.odtheking.odin.features.impl.floor7.TerminalSolver;
import com.odtheking.odin.utils.skyblock.dungeon.terminals.TerminalUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    @Nullable
    public HitResult hitResult;

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;useItemOn(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"), cancellable = true)
    private void cancelBlockUse(CallbackInfo ci) {
        if (!(this.hitResult instanceof BlockHitResult blockHitResult)) return;
        if ((new BlockInteractEvent(blockHitResult.getBlockPos()).postAndCatch())) ci.cancel();
    }

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;interactAt(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/EntityHitResult;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"), cancellable = true)
    private void cancelEntityUse(CallbackInfo ci) {
        if (!(this.hitResult instanceof EntityHitResult entityHitResult)) return;
        if (new EntityInteractEvent(entityHitResult.getLocation(), entityHitResult.getEntity()).postAndCatch()) ci.cancel();
    }

    @ModifyExpressionValue(method = "resizeDisplay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"))
    private Object modifyGuiScaleValue(Object original) {
        if (TerminalUtils.getCurrentTerm() != null && TerminalSolver.getTermSize() != (Integer) original) return TerminalSolver.getTermSize();
        return original;
    }
}