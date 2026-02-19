package com.odtheking.mixin.mixins;

import com.odtheking.odin.events.PlayerTeamEvent;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Scoreboard.class)
public class ScoreboardMixin {
    @Inject(
            method = "addPlayerTeam",
            at = @At("RETURN")
    )
    private void onAddPlayerTeam(String string, CallbackInfoReturnable<PlayerTeam> cir) {
        PlayerTeam team = cir.getReturnValue();
        new PlayerTeamEvent.AddTeam(string, team).postAndCatch();
    }

    @Inject(
            method = "onTeamChanged",
            at = @At("HEAD")
    )
    private void onTeamChanged(PlayerTeam playerTeam, CallbackInfo ci) {
        new PlayerTeamEvent.UpdateParameters(playerTeam).postAndCatch();
    }

    @Inject(
            method = "removePlayerTeam",
            at = @At("HEAD")
    )
    private void onRemovePlayerTeam(PlayerTeam playerTeam, CallbackInfo ci) {
        new PlayerTeamEvent.RemoveTeam(playerTeam).postAndCatch();
    }
}
