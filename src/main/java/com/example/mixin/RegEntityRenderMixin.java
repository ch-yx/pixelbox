package com.example.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import com.example.EntityGetter;

@Mixin(EntityRenderers.class)
public class RegEntityRenderMixin {
	@Shadow
	private static <T extends Entity> void register(EntityType<? extends T> entityType, EntityRendererProvider<T> entityRendererProvider) {}
	@Inject(at = @At("TAIL"), method = "<clinit>")
	private static void init(CallbackInfo info) {
		register(((EntityGetter)(EntityType.ALLAY)).CUBE(), com.example.CubeRenderer::new);
		register(((EntityGetter)(EntityType.ALLAY)).PIXEL(), com.example.PixRenderer::new);
	}

}
