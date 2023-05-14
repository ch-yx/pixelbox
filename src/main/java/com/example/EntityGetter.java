package com.example;

import net.minecraft.world.entity.EntityType;

public interface EntityGetter {
    public EntityType<?> CUBE();

    public EntityType<?> PIXEL();

    public int id_get();
}
