package com.example.mixin;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
@Mixin(net.minecraft.client.renderer.RenderStateShard.class)
public class RenderStateShardMixin {
    @Shadow
    protected static  net.minecraft.client.renderer.RenderStateShard.TransparencyStateShard ADDITIVE_TRANSPARENCY;
    @Shadow
    protected static  net.minecraft.client.renderer.RenderStateShard.CullStateShard NO_CULL;
    @Shadow
    protected static  net.minecraft.client.renderer.RenderStateShard.OutputStateShard ITEM_ENTITY_TARGET;
    static{
        com.example.Renderer.beamrendertype = new net.minecraft.client.renderer.RenderType.CompositeRenderType("laser_beam", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 131072, false, true, net.minecraft.client.renderer.RenderType.CompositeState.builder().setShaderState(new ShaderStateShard(()->com.example.Renderer.beam_shader)).setTransparencyState(ADDITIVE_TRANSPARENCY).setCullState(NO_CULL).setOutputState(ITEM_ENTITY_TARGET).createCompositeState(false));
    }
}
