package com.example;

import net.minecraft.core.Vec3i;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.phys.AABB;

import java.util.Optional;
import org.joml.Vector3f;
import com.example.PixelEntity;

public class PixelEntity extends Entity {
    @Override
    public void tick() {
    }

    private CubeEntity owner;
    private boolean dielast;

    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    public CubeEntity getOwner() {
        owner = (CubeEntity) Optional.ofNullable((Entity) owner)
                .orElseGet(() -> this.level().getEntity(this.entityData.get(DATA_Owner)));
        return owner;
    }

    public void setOwner(CubeEntity owner) {
        this.entityData.set(DATA_Owner, owner.getId());
        this.owner = owner;
    }

    public PixelEntity getconer() {
        return (PixelEntity) this.level().getEntity(this.entityData.get(DATA_Coner));
    }

    private Integer conerindex = null;

    public void setconer(PixelEntity coner) {
        if (coner == null) {
            this.entityData.set(DATA_Coner, -1);
            conerindex = null;
        } else {
            this.entityData.set(DATA_Coner, coner.getId());
            conerindex = coner.index;
        }
    }

    public int index;

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return super.canCollideWith(entity) && !(entity instanceof PixelEntity);
    }

    private static final EntityDataAccessor<Vector3f> DATA_Pix_color = SynchedEntityData
            .defineId(PixelEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Boolean> DATA_Pusher = SynchedEntityData
            .defineId(PixelEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_Owner = SynchedEntityData.defineId(PixelEntity.class,
            EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_Coner = SynchedEntityData.defineId(PixelEntity.class,
            EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_Attacking = SynchedEntityData.defineId(PixelEntity.class,
            EntityDataSerializers.BOOLEAN);

    public PixelEntity(EntityType<? extends Entity> entityType, Level world) {
        super(entityType, world);
        this.blocksBuilding = true;
    }

    @Override
    public boolean isInvisible() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        return;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_Pix_color, new Vector3f());
        this.entityData.define(DATA_Pusher, false);
        this.entityData.define(DATA_Owner, 0);
        this.entityData.define(DATA_Coner, -1);
        this.entityData.define(DATA_Attacking, false);
    }

    public boolean isattacking() {
        return this.entityData.get(DATA_Attacking);
    }

    void setattacking(boolean b) {
        this.entityData.set(DATA_Attacking, b);
    }

    Vector3f getpixelcolor() {
        return this.entityData.get(DATA_Pix_color);
    }

    void setpixelcolor(int r, int g, int b) {
        this.entityData.set(DATA_Pix_color, new Vector3f(r / 255f, g / 255f, b / 255f));
        color = ARGB32.color(0, r, g, b);
    }

    void setpusher(boolean bl) {
        this.entityData.set(DATA_Pusher, bl);
    }

    public boolean getpusher() {
        return this.entityData.get(DATA_Pusher);
    }

    int color;

    @Override
    public int getTeamColor() {
        return color;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        return;
    }

    @Override
    public boolean isCurrentlyGlowing() {
        return true;

    }

    public void init(CompoundTag compound) {
        this.setPos(Vec3.CODEC.decode(NbtOps.INSTANCE, compound.get("pos")).result().get().getFirst());
        var c = compound.getInt("color");
        this.setpixelcolor(ARGB32.red(c), ARGB32.green(c), ARGB32.blue(c));
        this.state = State.valueOf(compound.getString("state"));
        if (compound.contains("goingdata", 10)) {
            var gdt = compound.getCompound("goingdata");
            this.goingdata = new Goingdata(gdt.getInt("cd1"),
                    Vec3i.CODEC.decode(NbtOps.INSTANCE, gdt.get("steps")).result().get().getFirst(),
                    Vec3.CODEC.decode(NbtOps.INSTANCE, gdt.get("target")).result().get().getFirst(), gdt.getInt("cd2"),
                    State.valueOf(gdt.getString("next")));
        }
        conercountdown = compound.getInt("conercountdown");
        if (compound.contains("coner")) {
            setconer(getOwner().children[compound.getInt("coner")]);
        }

    }

    public Tag write() {
        CompoundTag foo = new CompoundTag();
        Vec3.CODEC.encodeStart(NbtOps.INSTANCE, this.position()).result()
                .ifPresent(tag -> foo.put("pos", (Tag) tag));
        foo.putInt("color", this.getTeamColor());
        foo.putString("state", state.toString());
        if (goingdata != null) {
            var sub = new CompoundTag();
            sub.putInt("cd1", goingdata.cd1);
            sub.putInt("cd2", goingdata.cd2);
            sub.putString("next", goingdata.nextstage.toString());
            Vec3i.CODEC.encodeStart(NbtOps.INSTANCE, this.goingdata.targetstep).result()
                    .ifPresent(tag -> sub.put("steps", (Tag) tag));
            Vec3.CODEC.encodeStart(NbtOps.INSTANCE, this.goingdata.target).result()
                    .ifPresent(tag -> sub.put("target", (Tag) tag));
            foo.put("goingdata", sub);
        }
        foo.putInt("conercountdown", conercountdown);
        if (conerindex != null) {
            foo.putInt("coner", conerindex);
        }
        return foo;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand interactionHand) {
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (this.getOwner().iswinner || (this.getOwner().enemy.isEmpty() && !this.getOwner().allchildrenliving())) {
            if (!getOwner().isRemoved()) {
                this.getOwner().kill();
                var e = ((EntityGetter) (EntityType.ALLAY)).CUBE().create(level());
                e.setPos(getOwner().position());
                level().addFreshEntity(e);
            }
            return InteractionResult.CONSUME;
        }
        if (this.getOwner().enemy.isEmpty() && this.getOwner().allchildrenliving()) {
            getOwner().setEnemy(player);
            return InteractionResult.CONSUME;
        }
        getOwner().setEnemy(player);
        var list = this.level().getEntitiesOfClass(PixelEntity.class,
                this.getBoundingBox().inflate(com.example.ExampleMod.pixsize * 1.4));
        for (PixelEntity mob : list) {
            if (mob.getOwner() == this.getOwner()) {
                mob.kill();
                if (mob.dielast) {
                    mob.spawnAtLocation(Items.DIRT);
                }
            }

        }
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        if (this.getOwner() == null) {
            return false;
        }

        if (damageSource.isIndirect() && damageSource.getEntity() != null && damageSource.getEntity() != getOwner()) {
            if (this.getOwner().iswinner || (this.getOwner().enemy.isEmpty() && !this.getOwner().allchildrenliving())) {
                if (!getOwner().isRemoved()) {
                    this.getOwner().kill();
                    var e = ((EntityGetter) (EntityType.ALLAY)).CUBE().create(level());
                    e.setPos(getOwner().position());
                    level().addFreshEntity(e);
                }
                return false;
            }
            if (this.getOwner().enemy.isEmpty() && this.getOwner().allchildrenliving()) {
                getOwner().setEnemy(damageSource.getEntity());
            } else {
                getOwner().setEnemy(damageSource.getEntity());
                if (f > 0) {
                    var list = this.level().getEntitiesOfClass(PixelEntity.class,
                            this.getBoundingBox().inflate(com.example.ExampleMod.pixsize * 1.4));
                    for (PixelEntity mob : list) {
                        if (mob.getOwner() == this.getOwner()) {
                            mob.kill();
                            if (mob.dielast) {
                                mob.spawnAtLocation(Items.DIRT);
                            }
                        }

                    }
                }
            }
        }
        if (damageSource.getDirectEntity() instanceof Projectile pj) {
            pj.setOwner(this.getOwner());
        }
        return false;
    }

    @Override
    public void kill() {
        super.kill();
        this.getOwner().getlife();
        this.getOwner().children[this.index] = null;
        this.getOwner().lifecount -= 1;
        getOwner().updateprogress();
        if(this.state==State.connecting)getOwner().updateKIDSforC();
        if (this.getOwner().lifecount == 0) {
            this.getOwner().kill();
            this.dielast = true;
        }
    }

    void moveandpush(Vec3 v) {
        if (this.level().isClientSide) {
            return;
        }
        this.setpusher(true);
        this.getOwner().setpushing(true);
        var bbox = this.getBoundingBox().inflate(0.02).expandTowards(v);
        this.level().getEntities(this, bbox).stream()
                .filter(x -> !((x instanceof PixelEntity) || (x instanceof CubeEntity)))
                .forEach(x -> {
                    x.move(MoverType.PISTON, v);
                    // x.setPos(v.add(x.position()));
                    x.hasImpulse = true;
                    if (x instanceof ServerPlayer sp) {
                        sp.connection.teleport(sp.getX(), sp.getY(), sp.getZ(), sp.getYRot(), sp.getXRot(),net.minecraft.world.entity.RelativeMovement.ROTATION);
                    }
                });

        this.setpusher(false);
        this.getOwner().setpushing(false);
    }

    enum State {
        sleeping, IDLE, GOING, ERROR, connecting, GOING_with_TASK;
    }

    public State state = State.sleeping;

    public class Goingdata {

        int cd1;
        Vec3i targetstep;
        Vec3 target;
        int cd2;
        State nextstage;

        Goingdata(int cd1, Vec3i targetstep, Vec3 target, int cd2, State nextstage) {
            this.cd1 = cd1;
            this.cd2 = cd2;
            this.target = target;
            this.targetstep = targetstep;
            this.nextstage = nextstage;
        }
    }

    Goingdata goingdata;

    void setgoing(int cd1, int maxtargetstep, Vec3 target, int cd2) {
        setgoing(cd1, maxtargetstep, target, cd2, State.IDLE);
    }

    void setgoing(int cd1, int maxtargetstep, Vec3 target, int cd2, State state) {
        this.state = State.GOING;
        var d = target.subtract(position()).scale(4);
        int x = Mth.ceil(Math.abs(d.x));
        int y = Mth.ceil(Math.abs(d.y));
        int z = Mth.ceil(Math.abs(d.z));
        this.goingdata = this.new Goingdata(cd1, new Vec3i(Math.min(x,maxtargetstep), Math.min(y,maxtargetstep), Math.min(z,maxtargetstep)), target, cd2, state);
    }

    private int conercountdown;

    void setconnecting(PixelEntity other) {
        this.setattacking(false);
        other.setattacking(false);
        this.state = State.connecting;
        other.state = State.connecting;
        this.setconer(other);
        other.setconer(this);
        this.conercountdown = 32;
        other.conercountdown = 32;
        if ((Object)this.getOwner() instanceof CubeEntity ow) {
            ow.updateKIDSforC();
        }
    }

    void cutconnecting() {
        this.state = State.IDLE;
        this.setconer(null);
        this.setpixelcolor(0, 0, 0);
        if ((Object)this.getOwner() instanceof CubeEntity ow) {
            ow.updateKIDSforC();
        }
    }

    boolean og = false;

    public boolean onGround() {
        return og;
    }

    void loctick() {

        switch (state) {
            case GOING,GOING_with_TASK:
                switch (random.nextInt(16)) {
                    case 3:
                        setpixelcolor(0, 255, 0);
                        break;
                    case 7:
                        setpixelcolor(0, 0, 0);
                        break;
                    default:
                        break;
                }
                do {
                    if (goingdata.cd1-- > 0) {
                        break;
                    }
                    if ((goingdata.targetstep.getX() <= 0) && (goingdata.targetstep.getY() <= 0)
                            && (goingdata.targetstep.getZ() <= 0)) {
                    } else {

                        Axis d;
                        while (goingdata.targetstep.get(d = Axis.getRandom(random)) <= 0)
                            ;

                        switch (d) {
                            case X:
                                var v = new Vec3((goingdata.target.x - getX()) / goingdata.targetstep.getX(), 0,
                                        0);
                                this.moveandpush(v);
                                if (goingdata.targetstep.getX() <= 1) {
                                    this.setPos(goingdata.target.x, getY(), getZ());
                                    og = !og;
                                    // this.level.players().forEach(p->((ServerPlayer)p).connection.send( new
                                    // ClientboundTeleportEntityPacket(this)));
                                } else {
                                    this.setPos(v.add(this.position()));
                                }
                                break;
                            case Y:
                                v = new Vec3(0, (goingdata.target.y - getY()) / goingdata.targetstep.getY(),
                                        0);
                                this.moveandpush(v);
                                if (goingdata.targetstep.getY() <= 1) {
                                    this.setPos(getX(), goingdata.target.y, getZ());
                                    og = !og;
                                    // this.level.players().forEach(p->((ServerPlayer)p).connection.send( new
                                    // ClientboundTeleportEntityPacket(this)));
                                } else {
                                    this.setPos(v.add(this.position()));
                                }
                                break;
                            case Z:
                                v = new Vec3(0, 0, (goingdata.target.z - getZ()) / goingdata.targetstep.getZ());
                                this.moveandpush(v);
                                if (goingdata.targetstep.getZ() <= 1) {
                                    this.setPos(getX(), getY(), goingdata.target.z);
                                    og = !og;
                                    // this.level.players().forEach(p->((ServerPlayer)p).connection.send( new
                                    // ClientboundTeleportEntityPacket(this)));
                                } else {
                                    this.setPos(v.add(this.position()));
                                }
                                break;
                        }

                        goingdata.targetstep = goingdata.targetstep.relative(d, -1);
                        break;
                    }
                    if (goingdata.cd2-- > 0) {
                        break;
                    }
                    this.state = goingdata.nextstage;
                } while (false);
                // this.setPos(goingdata.target.x, goingdata.target.y, goingdata.target.z);
                break;
            case IDLE:
                switch (random.nextInt(70)) {
                    case 3:
                    case 7:
                    case 9:
                        setpixelcolor(0, 0, 0);
                        break;
                    case 1:
                        setpixelcolor(50, 69, 128);
                    default:
                        break;
                }
                if (this.getOwner() != null) {
                    this.getOwner().enemy.ifPresent(player -> {
                        // todo
                    });
                }

                break;
            case sleeping:
                if (getOwner().enemy.isPresent()) {
                    this.state = State.IDLE;
                } else {
                    setpixelcolor(0, 0, 0);
                }
                break;
            case connecting:
                if (getconer() == null || getOwner().children[getconer().index] == null) {
                    cutconnecting();
                    break;
                }
                this.conercountdown--;
                if (((conercountdown) & 8) != 0) {
                    setpixelcolor(255, 0, 0);
                } else {
                    setpixelcolor(255, 255, 255);
                }

                if (conercountdown < 0 && ((conercountdown & 4) == 0) && this.index < getconer().index) {
                    this.setattacking(true);
                    getconer().setattacking(true);
                    var v1 = this.position().add(0, ExampleMod.pixsize, 0);
                    var v2 = getconer().position().add(0, ExampleMod.pixsize, 0);
                    level().getEntitiesOfClass(LivingEntity.class, new AABB(v1, v2),
                            ent -> ent != getOwner() && ent.getBoundingBox().clip(v1, v2).isPresent())
                            .forEach(x -> {
                                if (x.hurt(level().damageSources().indirectMagic(this, this.getOwner()),
                                        x.getHealth() / 7f))
                                    doEnchantDamageEffects(getOwner(), x);
                            });
                    ;

                }
                if (conercountdown < -250) {
                    cutconnecting();
                }
                break;
            default:
                break;
        }
    }
    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }
}
