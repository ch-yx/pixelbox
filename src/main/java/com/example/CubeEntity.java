package com.example;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.BossEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import com.example.CubeEntity;
import com.example.PixelEntity.State;

public class CubeEntity extends LivingEntity {
    public PixelEntity[] children = new PixelEntity[com.example.ExampleMod.pixcount * com.example.ExampleMod.pixcount
            * com.example.ExampleMod.pixcount];

    public CubeEntity(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
        first_time_spawn = true;
        has_spawn_children = false;
        enemy = Optional.empty();
        enemyuuid = Optional.empty();
        blocksBuilding = false;
        if (!world.isClientSide) {
            bossbar = new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.GREEN,
                    BossEvent.BossBarOverlay.PROGRESS);
        } else {
            bossbar = null;
        }
    }

    final ServerBossEvent bossbar;

    @Override
    public void startSeenByPlayer(ServerPlayer serverPlayer) {
        super.startSeenByPlayer(serverPlayer);
        this.bossbar.addPlayer(serverPlayer);
        if (this.lifecount != null) {
            updateprogress();
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer serverPlayer) {
        super.stopSeenByPlayer(serverPlayer);
        this.bossbar.removePlayer(serverPlayer);
    }

    private static final EntityDataAccessor<Boolean> DATA_Pushing = SynchedEntityData
            .defineId(CubeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<CompoundTag> DATA_KIDS = SynchedEntityData
            .defineId(CubeEntity.class, EntityDataSerializers.COMPOUND_TAG);

    public boolean first_time_spawn = true;
    public boolean has_spawn_children = false;
    private ListTag splist;

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putBoolean("won", iswinner);
        compoundTag.putBoolean("first_time_spawn", first_time_spawn);
        // System.out.println(enemy.orElse(null)+"==="+enemyuuid.orElse(null));
        enemy.map(Entity::getUUID).or(() -> enemyuuid).map(UUIDUtil::uuidToIntArray)
                .ifPresent(id -> compoundTag.putIntArray("enemy", id));
        // compoundTag.putBoolean("has_spawn_childrens", has_spawn_children);
        var list = new ListTag();
        for (PixelEntity child : children) {
            if (child != null) {
                list.add(child.write());
            } else {
                list.add(new CompoundTag());
            }
        }
        compoundTag.put("children", list);

    }

    public Optional<Entity> enemy;
    public Optional<UUID> enemyuuid;

    public void setEnemy(Optional<Entity> enemy) {
        this.enemy = enemy;
        this.enemyuuid = Optional.empty();
    }

    public void setEnemy(Entity enemy) {
        setEnemy(Optional.ofNullable(enemy));
    }

    public boolean iswinner;

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_Pushing, false);
        this.entityData.define(DATA_KIDS,new CompoundTag());
    }

    void updateKIDSforC(){   
        var k = new CompoundTag();
        k.putInt("t", this.tickCount);
        k.putIntArray("k", getlivechildren().mapToInt(x->x.getId()).toArray());
        this.entityData.set(DATA_KIDS, k);
    }

    int[] CgetKIDS(){
        return this.entityData.get(DATA_KIDS).getIntArray("k");
    }
    void setpushing(boolean bl) {
        this.entityData.set(DATA_Pushing, bl);
    }

    public boolean getpushing() {
        return this.entityData.get(DATA_Pushing);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        iswinner = compoundTag.getBoolean("won");

        if (compoundTag.contains("enemy")) {
            enemyuuid = Optional.of(UUIDUtil.uuidFromIntArray(compoundTag.getIntArray("enemy")));
        }

        if (compoundTag.contains("first_time_spawn", 99)) {
            first_time_spawn = compoundTag.getBoolean("first_time_spawn");
        }

        // has_spawn_children = compoundTag.getBoolean("has_spawn_childrens");
        if (!first_time_spawn) {
            this.splist = compoundTag.getList("children", 10);
        }
        // onaddtolevel(level());
    }

    double rtri(double a, double b) {
        return random.triangle(a, b - a);
    }

    static Vec3 defaultposVec3(int i) {
        return new Vec3(
                (((i % com.example.ExampleMod.pixcount) + .5) * com.example.ExampleMod.pixsize - .5),
                (i / com.example.ExampleMod.pixcount / com.example.ExampleMod.pixcount)
                        * com.example.ExampleMod.pixsize,
                (i / com.example.ExampleMod.pixcount % com.example.ExampleMod.pixcount)
                        * com.example.ExampleMod.pixsize + com.example.ExampleMod.pixsize / 2f - .5);

    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public void aiStep() {
    }

    @Override
    protected void onChangedBlock(BlockPos blockPos) {
        return;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    public boolean allchildrenliving() {
        return getlife() == this.children.length;
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    public void setPos(double d, double e, double f) {
        super.setPos(d, e, f);
        if (this.level().isClientSide()) {
            return;
        }
        this.level().players().forEach(player -> {
            ((ServerPlayer) player).connection.send(getAddEntityPacket());
            if(children!=null)updateKIDSforC();
        });
    }

    @Override
    public void tick() {

        if (this.level().isClientSide()) {
            // super.tick();
            return;
        }
        // super.tick();
        if (this.enemy.isEmpty() && this.enemyuuid.isPresent()) {
            var e = ((net.minecraft.server.level.ServerLevel) level()).getEntity(enemyuuid.get());
            if (e != null) {
                setEnemy(e);
            }
        }
        if (this.enemy.isPresent() && enemy.get().isRemoved()) {
            this.iswinner = true;
            this.setEnemy(Optional.empty());
        }
        if (this.enemy.isEmpty()) {
            this.getidlechildren().forEach(p -> {
                p.setgoing(0, 20, defaultposVec3(p.index).add(this.position()), 0, State.sleeping);
            });
        }
        if ((getId() - tickCount) % 300 == 0) {
            this.enemy.ifPresent(player -> {
                var t_ = player.position().add(0, player.getBbHeight() / 2, 0).scale(2).subtract(position())
                        .offsetRandom(random, 2);
                var t = new Vec3(Mth.clamp(t_.x, player.getX() - 9, player.getX() + 9),
                        Mth.clamp(t_.y, player.getEyeY() - 2, player.getEyeY() + 9),
                        Mth.clamp(t_.z, player.getZ() - 9, player.getZ() + 9));
                getidlechildren().forEach(pixelEntity -> {
                    pixelEntity.setgoing((int) (2 * random.triangle(3, 16)), 16,
                            t.add(random.nextBoolean() ? pixelEntity.position().subtract(position())
                                    : defaultposVec3(pixelEntity.index)),
                            32);
                });

                this.setPos(t);
            });

        } else {
            this.enemy.ifPresent(player -> {
                var ss = new AABB[] {
                        player.getBoundingBox().inflate(0, 0, 3).move(3, 0, 0),
                        player.getBoundingBox().inflate(0, 0, 3).move(-3, 0, 0),
                        player.getBoundingBox().inflate(3, 0, 0).move(0, 0, 3),
                        player.getBoundingBox().inflate(3, 0, 0).move(0, 0, -3) };

                ArrayList<PixelEntity>[] sss = new ArrayList[] { new ArrayList<PixelEntity>(),
                        new ArrayList<PixelEntity>(),
                        new ArrayList<PixelEntity>(), new ArrayList<PixelEntity>() };
                
                if (player instanceof LivingEntity livingplayer && livingplayer.getHealth() < 2 && !player.getType().is(EntityTypeTags.FALL_DAMAGE_IMMUNE)) {
                    var foot = player.getBoundingBox().setMinY(player.getY()-3).setMaxY(player.getY());
                    
                    getidlechildren().filter(pixel->pixel.getY()<player.getY()-.5).forEach(pixel->{
                        if(pixel.getBoundingBox().intersects(foot)){
                            pixel.setgoing(random.nextInt(4, 16), 30, pixel.position().add(0, 3, 0), 16);
                        }else{
                            pixel.setgoing(random.nextInt(4, 16), 30, new Vec3(player.getRandomX(1),player.getY()-0.5,player.getRandomZ(1)), 30);
                        }
                    });
        
                }
                
                getidleorgoingchildren()
                        .filter(x -> !x.getBoundingBox().intersects(ss[0]) ? true : (sss[0].add(x) && false))
                        .filter(x -> !x.getBoundingBox().intersects(ss[1]) ? true : (sss[1].add(x) && false))
                        .filter(x -> !x.getBoundingBox().intersects(ss[2]) ? true : (sss[2].add(x) && false))
                        .filter(x -> !x.getBoundingBox().intersects(ss[3]) ? true : (sss[3].add(x) && false))
                        .filter(x -> random.nextInt(500) <= 3)
                        .forEach(pix -> {
                            double x, y, z;
                            var rev = random.nextFloat() < 0.33;
                            if (random.nextBoolean()) {
                                var b = player.getBoundingBox().inflate(0, 0, 3);
                                if ((pix.getX() < player.getX()) ^ rev) {
                                    x = rtri(b.minX, b.maxX) - 3;
                                    y = rtri(b.minY, b.maxY);
                                    z = rtri(b.minZ, b.maxZ);
                                } else {
                                    x = rtri(b.minX, b.maxX) + 3;
                                    y = rtri(b.minY, b.maxY);
                                    z = rtri(b.minZ, b.maxZ);
                                }
                            } else {
                                var b = player.getBoundingBox().inflate(3, 0, 0);
                                if ((pix.getZ() < player.getZ()) ^ rev) {
                                    x = rtri(b.minX, b.maxX);
                                    y = rtri(b.minY, b.maxY);
                                    z = rtri(b.minZ, b.maxZ) - 3;
                                } else {
                                    x = rtri(b.minX, b.maxX);
                                    y = rtri(b.minY, b.maxY);
                                    z = rtri(b.minZ, b.maxZ) + 3;
                                }
                            }
                            pix.setgoing(0, 10, new Vec3(x, y, z), 32);
                        });
                if (!(sss[0].isEmpty() || sss[1].isEmpty())) {
                    var o1 = sss[0].get(random.nextInt(sss[0].size()));
                    var o2 = sss[1].get(random.nextInt(sss[1].size()));
                    var v1 = o1.position().add(0, ExampleMod.pixsize, 0);
                    var v2 = o2.position().add(0, ExampleMod.pixsize, 0);
                    if (player.getBoundingBox().clip(v1, v2).isPresent()) {
                        o1.setconnecting(o2);
                    }
                }
                if (!(sss[2].isEmpty() || sss[3].isEmpty())) {
                    var o1 = sss[2].get(random.nextInt(sss[2].size()));
                    var o2 = sss[3].get(random.nextInt(sss[3].size()));
                    var v1 = o1.position().add(0, ExampleMod.pixsize, 0);
                    var v2 = o2.position().add(0, ExampleMod.pixsize, 0);
                    if (player.getBoundingBox().clip(v1, v2).isPresent()) {
                        o1.setconnecting(o2);
                    }
                }
            });
        }
        if (cracking <= 0) {
            getlivechildren().forEach(PixelEntity::loctick);
        } else {
            cracking--;
        }
    }

    Stream<PixelEntity> getidlechildren() {
        return getlivechildren().filter(x -> (x.state == State.IDLE));
    }

    Stream<PixelEntity> getidleorgoingchildren() {
        return getlivechildren().filter(x -> (x.state == State.IDLE || x.state == State.GOING));
    }

    Stream<PixelEntity> getlivechildren() {
        return Arrays.asList(children).stream().filter(x -> (x != null));
    }

    public Long lifecount = null;

    public long getlife() {
        if (lifecount == null) {
            lifecount = getlivechildren().count();
        }
        return lifecount;
    }

    @Override
    public void kill() {
        this.remove(RemovalReason.KILLED);
        getlivechildren().forEach(PixelEntity::kill);
    }

    @Override
    public boolean isDeadOrDying() {
        return false;
    }

    int cracking = 0;

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        if (damageSource.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)
                || damageSource.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)
                || damageSource.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)
                || damageSource.is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK)) {
            return false;
        }
        this.cracking = 5;
        return false;
    }

    public void onaddtolevel(Level level) {
        // System.out.println(level);
        if (first_time_spawn) {
            first_time_spawn = false;
            for (int i = 0; i < children.length; i++) {

                children[i] = (PixelEntity) ((EntityGetter) (EntityType.ALLAY)).PIXEL().create(level);

                children[i].setPos(defaultposVec3(i).add(this.position()));
                level.addFreshEntity(children[i]);
                children[i].setOwner(this);
                children[i].index = i;

                children[i].setpixelcolor(0, 0, 0);
            }
            splist = new ListTag();
        } else {
            if (!has_spawn_children) {
                this.has_spawn_children = true;
                if ((splist == null)) {
                    this.kill();
                    return;
                }
                int i = 0;
                for (Tag compound : splist) {
                    if (i >= children.length) {
                        break;
                    }
                    if (((CompoundTag) compound).getAllKeys().isEmpty()) {
                        i++;
                        continue;
                    }
                    children[i] = (PixelEntity) ((EntityGetter) (EntityType.ALLAY)).PIXEL().create(level);
                    children[i].setOwner(this);
                    children[i].index = i;
                    i++;
                }
                i = 0;
                for (Tag compound : splist) {
                    if (i >= children.length) {
                        break;
                    }
                    if (((CompoundTag) compound).getAllKeys().isEmpty()) {
                        i++;
                        continue;
                    }

                    children[i].init((CompoundTag) compound);
                    level.addFreshEntity(children[i]);
                    i++;
                }
                splist = new ListTag();
            }
        }
        updateprogress();
    }

    public void updateprogress() {
        this.bossbar.setProgress(this.getlife() / (float) this.children.length);
        if (this.getlife() < this.children.length / 4) {
            bossbar.setName(net.minecraft.network.chat.Component.empty().append(this.getDisplayName())
                    .append(":  " + getlife() + "/" + children.length));
        }
        if(!level().isClientSide())updateKIDSforC();
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return new java.util.ArrayList<ItemStack>();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot var1) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot var1, ItemStack var2) {
        return;
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    public double getAttributeValue(Attribute attribute) {
        return 1.0f;
    }
    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }
}
