package com.example.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;

@Mixin(ClientLanguage.class)
public class TransMixin {
    @Inject(at = @At("HEAD"), method = "has", cancellable = true)
    public void has(String string, CallbackInfoReturnable<Boolean> cfr) {
        if (string.equals("entity.pixelbox.cube")) {
            cfr.setReturnValue(true);
        }
        if (string.equals("entity.pixelbox.pixel")) {
            cfr.setReturnValue(true);
        }
    }

    @Inject(at = @At("HEAD"), method = "getOrDefault", cancellable = true)
    public void getOrDefault(String string, String string2, CallbackInfoReturnable<String> cfr) {
        if (string.equals("entity.pixelbox.cube")) {
            var lang = Minecraft.getInstance().options.languageCode;
            if ("zh_cn".equals(lang)) {
                cfr.setReturnValue("像素盒");
            } else if ("lzh".equals(lang)) {
                cfr.setReturnValue("素方合");
            } else {
                cfr.setReturnValue("Pixel Box");
            }
            return;
        }
        if (string.equals("entity.pixelbox.pixel")) {
            var lang = Minecraft.getInstance().options.languageCode;
            if ("zh_cn".equals(lang)) {
                cfr.setReturnValue("像素");
            } else if ("lzh".equals(lang)) {
                cfr.setReturnValue("素方");
            } else {
                cfr.setReturnValue("Pixel");
            }
            return;
        }
    }

}