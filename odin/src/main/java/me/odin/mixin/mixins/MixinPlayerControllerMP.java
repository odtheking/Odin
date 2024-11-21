package me.odin.mixin.mixins;

import me.odinmain.events.impl.GuiEvent;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.odinmain.utils.Utils.postAndCatch;

@Debug(export = true)
@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {

    @Inject(method = "windowClick", at = @At(value = "HEAD"), cancellable = true)
    private void onWindowClick(int windowId, int slotId, int mouseButtonClicked, int mode, EntityPlayer playerIn, CallbackInfoReturnable<ItemStack> cir) {
        if (postAndCatch(new GuiEvent.WindowClick(windowId, slotId, mouseButtonClicked, mode, playerIn))) cir.setReturnValue(null);
    }
}
