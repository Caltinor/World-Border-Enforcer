package dicemc.worldborderenforcer.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dicemc.worldborderenforcer.config.Config;
import net.minecraft.world.level.border.WorldBorder;

@Mixin(WorldBorder.class)
public class BorderExtentMixin {
	
	@Inject(method= "Lnet/minecraft/world/level/border/WorldBorder;lerpSizeBetween(DDJ)V",
			at = @At("TAIL"))
	public void onLerpSizeBetween(double from, double to, long lerpDuration, CallbackInfo ci) {
		Config.WORLD_BORDER_CURRENT.set(((WorldBorder)(Object)(this)).getSize());
	}
	
	@Inject(method= "Lnet/minecraft/world/level/border/WorldBorder;setSize(D)V",
			at = @At("TAIL"))
	public void onSizeChange(double change, CallbackInfo ci) {
		Config.WORLD_BORDER_CURRENT.set(((WorldBorder)(Object)(this)).getSize());
	}
}
