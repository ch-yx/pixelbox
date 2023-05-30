package com.example.mixin;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
@Mixin(net.minecraft.client.renderer.RenderStateShard.class)
public interface RenderStateShardMixin {
    @Accessor("ADDITIVE_TRANSPARENCY")
    public static  net.minecraft.client.renderer.RenderStateShard.TransparencyStateShard trans_para(){
        throw new AssertionError();};
    @Accessor("NO_CULL")
    public static  net.minecraft.client.renderer.RenderStateShard.CullStateShard cull_para(){
        throw new AssertionError();};
    @Accessor("ITEM_ENTITY_TARGET")//"TRANSLUCENT_TARGET")
    public static  net.minecraft.client.renderer.RenderStateShard.OutputStateShard target_para(){
        throw new AssertionError();};
    // public static void iiint(){
    // }
}
