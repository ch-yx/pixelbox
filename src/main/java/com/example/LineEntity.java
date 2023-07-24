package com.example;

import java.util.Optional;
import java.util.UUID;

import org.joml.Vector3f;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LineEntity extends Entity implements TraceableEntity {
    public LineEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        
        axis = new Vec3(2, -4, 6);
        setTarget(randomtarget(axis));
        setMvprogress1(0);
        setMvprogress2(-10);
        setSp(0);
        setEp(10);
        owneruuid = Optional.empty();
    }

    public Vector3f randomtarget(Vec3 axis) {
        var a = new Vector3f((float) axis.x, (float) axis.y, (float) axis.z);
        a.mul((float)this.random.nextGaussian() * 0.3f + 1f);
        Vector3f v1 = new Vector3f().orthogonalizeUnit(a);
        Vector3f v2 = new Vector3f();
        v1.cross(a, v2);
        v2.normalize(2*(float)this.random.nextGaussian());
        v1.mul(2*(float)this.random.nextGaussian());
        return a.add(v2).add(v1);

    }

    public boolean hadkid;
    public int age;
    public Vec3 axis;

    public int getMvprogress1() {
        return this.entityData.get(DATA_mvprogress1);
    }

    public void setMvprogress1(int mvprogress1) {
        this.entityData.set(DATA_mvprogress1, mvprogress1);
    }

    public int getMvprogress2() {
        return this.entityData.get(DATA_mvprogress2);
    }

    public void setMvprogress2(int mvprogress2) {
        this.entityData.set(DATA_mvprogress2, mvprogress2);
    }

    public Vec3 getTarget() {
        var x = this.entityData.get(DATA_target);
        return new Vec3(x.x, x.y, x.z);
    }

    public void setTarget(Vector3f target) {
        this.entityData.set(DATA_target, target);
    }

    public double getSp() {
        return Double.longBitsToDouble(this.entityData.get(DATA_sp));
    }

    public void setSp(double sp) {
        this.entityData.set(DATA_sp, Double.doubleToLongBits(sp));
    }

    public double getEp() {
        return Double.longBitsToDouble(this.entityData.get(DATA_ep));
    }

    public void setEp(double ep) {
        this.entityData.set(DATA_ep, Double.doubleToLongBits(ep));
    }

    public Optional<UUID> owneruuid;

    static Vec3 fp(double p, double sp, double ep, Vec3 target) {
        return target.scale((p - sp) / (ep - sp));
    }

    @Override
    public void tick() {
        if (this.level().isClientSide()) {
            return;
        }
        this.setMvprogress1(this.getMvprogress1() + 1);
        this.setMvprogress2(this.getMvprogress2() + 1);

        if (this.getMvprogress2() > this.getEp()) {
            this.kill();
            return;
        }

        if (this.getMvprogress1() > this.getEp() && !hadkid) {
            createnext();
        }
        var t1 = fp(Math.min(getMvprogress1(), getEp()), getSp(), getEp(), getTarget());
        var t2 = fp(Math.max(getMvprogress2(), getSp()), getSp(), getEp(), getTarget());
        var v1 = position().add(t1);
        var v2 = position().add(t2);
        level().getEntitiesOfClass(LivingEntity.class, new AABB(v1, v2),
                ent -> !(ent instanceof CubeEntity) && ent.getBoundingBox().clip(v1, v2).isPresent())
                .forEach(x -> {
                    if (x.hurt(level().damageSources().indirectMagic(this, this.getOwner()),
                            x.getHealth() / 7f))
                        doEnchantDamageEffects(getOwner() instanceof LivingEntity l ? l : null, x);
                });
        ;
        // System.out.println(mvprogress1 + "#" + this + this.level().getGameTime() +
        // "tking");
    }

    LineEntity createnext() {
        if (age > 7) {
            return null;
        }
        var slave = new LineEntity(getType(), level());
        slave.setPos(position().add(getTarget()));
        slave.axis = axis;
        slave.setTarget(slave.randomtarget(axis));
        slave.setSp(getEp());
        slave.setEp(slave.getSp() + slave.getTarget().length());
        slave.age = age + 1;
        slave.setMvprogress1(getMvprogress1());
        slave.setMvprogress2(getMvprogress2());
        slave.owneruuid = owneruuid;
        this.level().addFreshEntity(slave);
        hadkid = true;
        return slave;
    }

    private static final EntityDataAccessor<Vector3f> DATA_target = SynchedEntityData.defineId(LineEntity.class,
            EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Integer> DATA_mvprogress1 = SynchedEntityData.defineId(LineEntity.class,
            EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_mvprogress2 = SynchedEntityData.defineId(LineEntity.class,
            EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> DATA_sp = SynchedEntityData.defineId(LineEntity.class,
            EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> DATA_ep = SynchedEntityData.defineId(LineEntity.class,
            EntityDataSerializers.LONG);

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_target, new Vector3f());
        this.entityData.define(DATA_mvprogress1, 0);
        this.entityData.define(DATA_mvprogress2, 0);
        this.entityData.define(DATA_sp, 0L);
        this.entityData.define(DATA_ep, 0L);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag var1) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag var1) {
    }

    @Override
    public Entity getOwner() {
        return owneruuid.map(((ServerLevel) level())::getEntity).orElse(this);
    }
}