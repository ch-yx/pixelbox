package com.example.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.example.PixelEntity;

import net.minecraft.world.entity.Entity;

@Mixin(Entity.class)
public class collidemixin {
    @Inject(method = "canCollideWith", at = @At("HEAD"), cancellable = true)
    public void canCollideWith(Entity entity,CallbackInfoReturnable<Boolean> cir) {

        if(entity instanceof PixelEntity pe){
            if(((Entity)(Object)this).getBoundingBox().inflate(-1.0E-7).intersects(pe.getBoundingBox())){
                cir.setReturnValue(false);
                return;
            }
            if(pe.getOwner()==null){
                cir.setReturnValue(true);
                return;
            }
            boolean result;
            if (pe.getOwner().getpushing()) {
                result=pe.getpusher();
            }else{
                result=true;
            }
            cir.setReturnValue(result);
        }
        
    }
}
