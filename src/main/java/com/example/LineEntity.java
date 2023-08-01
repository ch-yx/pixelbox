package com.example;

import java.util.Optional;
import java.util.UUID;

import org.joml.Vector3f;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
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
    public Vector3f randomtargetoffest;

    public LineEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public static LineEntity create_line(Level level, Vec3 axis, boolean b) {
        var t =new LineEntity(((EntityGetter)(EntityType.ALLAY)).LINE(), level);
        t.axis=axis;
        t.precise=b;
        t.readAdditionalSaveData(new CompoundTag());
        level.addFreshEntity(t);
        return t;
    }


    public Vector3f conv(Vec3 axis) {
        return new Vector3f((float) axis.x, (float) axis.y, (float) axis.z);
    }

    public Vec3 conv(Vector3f axis) {
        return new Vec3(axis.x, axis.y, axis.z);
    }

    public Vector3f randomtarget(Vec3 axis) {
        var a = conv(axis);
        a.mul((float) this.random.nextGaussian() * 0.3f + 1f);
        return a;
    }

    public Vector3f randomtargetoffest(Vector3f a) {
        Vector3f v1 = new Vector3f().orthogonalizeUnit(a);
        Vector3f v2 = new Vector3f();
        v1.cross(a, v2);
        v2.normalize(2 * (float) this.random.nextGaussian());
        v1.mul(2 * (float) this.random.nextGaussian());
        return v2.add(v1);

    }

    public boolean hadkid;
    public int age;
    public Vec3 axis;
    public boolean precise;

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
        if (target == null || target.lengthSquared()<0.002) {
            target=new Vector3f(0f, 0.3f, 0f);
        }
        this.entityData.set(DATA_target, target);
    }

    public void setTarget(Vec3 first) {
        setTarget(conv(first));
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
            var ___ = (this.random.nextFloat() < 0.2) ? createnext() : null;
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
        Vector3f randomtarget = slave.randomtarget(slave.axis);
        slave.randomtargetoffest = slave.randomtargetoffest(randomtarget);
        slave.setTarget(randomtarget.add(this.randomtargetoffest).sub(slave.randomtargetoffest));
        slave.setSp(getEp());
        slave.setEp(slave.getSp() + slave.getTarget().length());
        slave.age = age + 1;
        slave.setMvprogress1(getMvprogress1());
        slave.setMvprogress2(getMvprogress2());
        slave.owneruuid = owneruuid;
        this.level().addFreshEntity(slave);
        hadkid = true;
        slave.precise = precise;
        if (!precise) {
            slave.randomtargetoffest = new Vector3f();
        }
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
    protected void readAdditionalSaveData(CompoundTag compound) {
        //System.out.println("creating" + this);
        if (compound.contains("axis"))
            axis = Vec3.CODEC.decode(NbtOps.INSTANCE, compound.get("axis")).result().get().getFirst();
        if (axis==null||axis.lengthSqr() < 0.002) {
            axis = new Vec3(2, -4, 6);
        }
        if (compound.contains("target") && compound.contains("offset")) {
            setTarget(Vec3.CODEC.decode(NbtOps.INSTANCE, compound.get("target")).result().get().getFirst());
            randomtargetoffest = conv(Vec3.CODEC.decode(NbtOps.INSTANCE, compound.get("offset")).result().get().getFirst());
        } else {
            Vector3f randomtarget = randomtarget(axis);
            randomtargetoffest = randomtargetoffest(randomtarget);
            setTarget(randomtarget.sub(randomtargetoffest));
        }

        ;
        setMvprogress1(compound.contains("mv1", 99)?compound.getInt("mv1"):0);
        setMvprogress2(compound.contains("mv2", 99)?compound.getInt("mv2"):-10);
        setSp(compound.contains("sp", 99)?compound.getDouble("sp"):0);
        setEp(compound.contains("ep", 99)?compound.getDouble("ep"):10);
        owneruuid = Optional.ofNullable(compound.hasUUID("owner")?compound.getUUID("owner"):null);
        if(compound.contains("precise", 99))
            precise=compound.getBoolean("precise");
        if (!precise) {
            randomtargetoffest = new Vector3f();
        }
        hadkid=compound.getBoolean("hadkid");
        age=compound.getInt("age");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        //System.out.println("saving" + this);
        Vec3.CODEC.encodeStart(NbtOps.INSTANCE, axis).result()
                .ifPresent(tag -> compound.put("axis", tag));
        Vec3.CODEC.encodeStart(NbtOps.INSTANCE, getTarget()).result()
                .ifPresent(tag -> compound.put("target", tag));
        Vec3.CODEC.encodeStart(NbtOps.INSTANCE, conv(randomtargetoffest)).result()
                .ifPresent(tag -> compound.put("offset", tag));
        compound.putInt("mv1", getMvprogress1());
        compound.putInt("mv2", getMvprogress2());
        compound.putDouble("sp", getSp());
        compound.putDouble("ep", getEp());
        owneruuid.ifPresent(uuid->compound.putUUID("owner", uuid));
        compound.putBoolean("precise", precise);
        compound.putBoolean("hadkid", hadkid);
        compound.putInt("age", age);
    }

    @Override
    public Entity getOwner() {
        return owneruuid.map(((ServerLevel) level())::getEntity).orElse(this);
    }
}