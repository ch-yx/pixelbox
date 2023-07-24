package com.example.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.example.EntityGetter;
import com.example.LineEntity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import com.example.CubeEntity;
import com.example.PixelEntity;

@Mixin(net.minecraft.world.entity.EntityType.class)
public class RegEntityMixin implements EntityGetter {
	// @Inject(at = @At("TAIL"), method = "<clinit>")
	// private void init(CallbackInfo info)

	private static final EntityType<CubeEntity> CUBE = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			"pixelbox:cube",
			net.minecraft.world.entity.EntityType.Builder.of(CubeEntity::new, MobCategory.MISC).sized(1f, 1f)
					.build("pixelbox:cube"));
	private static final EntityType<PixelEntity> PIXEL = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			"pixelbox:pixel",
			net.minecraft.world.entity.EntityType.Builder.of(PixelEntity::new, MobCategory.MISC).noSummon().noSave().clientTrackingRange(255).updateInterval(1).sized(com.example.ExampleMod.pixsize, com.example.ExampleMod.pixsize)
					.build("pixelbox:pixel"));
	private static final EntityType<LineEntity> LINE = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			"pixelbox:lightning",
			net.minecraft.world.entity.EntityType.Builder.of(LineEntity::new, MobCategory.MISC).noSave().clientTrackingRange(255).updateInterval(1).sized(0,0)
					.build("pixelbox:lightning"));

	public EntityType<?> CUBE() {
		return CUBE;
	}

	public EntityType<?> PIXEL() {
		return PIXEL;
	}

	public EntityType<?> LINE() {
		return LINE;
	}
}