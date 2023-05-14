package com.example.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.world.entity.EntityType;

@Mixin(com.mojang.blaze3d.shaders.Program.class)
public class ProgramIdMixin implements com.example.EntityGetter{
    
    @Shadow
    private int id;

    public int id_get(){
        return id;
    }

    @Override
    public EntityType<?> CUBE() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'CUBE'");
    }

    @Override
    public EntityType<?> PIXEL() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'PIXEL'");
    }
}
