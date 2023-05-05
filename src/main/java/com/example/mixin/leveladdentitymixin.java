package com.example.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin({net.minecraft.world.level.entity.PersistentEntitySectionManager.class})
public class leveladdentitymixin {
    @Inject(method = "addEntity", at = @At("RETURN"), cancellable = true)
    public void xxxxxx(net.minecraft.world.level.entity.EntityAccess entity,boolean bool,CallbackInfoReturnable<Boolean> cir) {
        if(entity instanceof com.example.CubeEntity ce){
            //System.out.println("find a cube!!");
            ce.onaddtolevel(ce.level());
        }
        
    }
}
