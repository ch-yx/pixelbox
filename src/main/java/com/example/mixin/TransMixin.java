package com.example.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.resources.language.ClientLanguage;


@Mixin(ClientLanguage.class)
public class TransMixin {
	@Inject(at = @At("HEAD"), method = "has",cancellable = true)
	public void has(String string,CallbackInfoReturnable cfr){
        if (string.equals("entity.pixelbox.cube")) {
            cfr.setReturnValue(true);
        }
        if (string.equals("entity.pixelbox.pixel")) {
            cfr.setReturnValue(true);
        }
    }
    @Inject(at = @At("HEAD"), method = "getOrDefault",cancellable = true)
	public void getOrDefault(String string,String string2,CallbackInfoReturnable cfr){
        if (string.equals("entity.pixelbox.cube")) {
            cfr.setReturnValue("Pixel Box");
        }
        if (string.equals("entity.pixelbox.pixel")) {
            cfr.setReturnValue("Pixel");
        }
    }

}