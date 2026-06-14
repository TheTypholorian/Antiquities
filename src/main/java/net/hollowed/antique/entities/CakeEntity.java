package net.hollowed.antique.entities;

import net.hollowed.antique.index.AntiqueSounds;
import net.hollowed.antique.index.AntiqueParticles;
import net.hollowed.antique.util.delay.TickDelayScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class CakeEntity extends AbstractArrow {
    private static final EntityDataAccessor<Boolean> FROZEN = SynchedEntityData.defineId(CakeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> YAW = SynchedEntityData.defineId(CakeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> PITCH = SynchedEntityData.defineId(CakeEntity.class, EntityDataSerializers.FLOAT);

    public CakeEntity(EntityType<? extends AbstractArrow> entityType, Level world) {
        super(entityType, world);
        this.setBaseDamage(0);
        this.entityData.set(FROZEN, false);
        this.entityData.set(PITCH, this.getXRot());
        this.entityData.set(YAW, this.getYRot());
    }

    @Override
    public void handleEntityEvent(byte status) {
        if (status == EntityEvent.DEATH) {
            this.level().addDestroyBlockEffect(this.blockPosition(), Blocks.CAKE.defaultBlockState());
        }
        if (status == EntityEvent.POOF) {
            BlockPos entityPos = this.blockPosition();
            Level world = this.level();

            for (Direction direction : Direction.values()) {
                BlockPos offsetPos = entityPos.relative(direction);
                if (world.getBlockState(offsetPos).isCollisionShapeFullBlock(world, offsetPos) && direction.getAxis().isHorizontal()) {
                    Vec3 vec = Vec3.ZERO;
                    switch (direction) {
                        case NORTH, SOUTH -> vec = new Vec3(0, 0, 1);
                        case EAST, WEST -> vec = new Vec3(1, 0, 0);
                    }

                    Vec3 particlePos = this.position();

                    world.addParticle(
                            AntiqueParticles.CAKE_SMEAR,
                            particlePos.x + direction.getStepX() * (Math.random() / 10.0),
                            particlePos.y + direction.getStepY() * (Math.random() / 10.0),
                            particlePos.z + direction.getStepZ() * (Math.random() / 10.0),
                            vec.x,
                            vec.y,
                            vec.z
                    );
                    break;
                }
            }
        }
    }

    public ItemStack getPickResult() {
        return Items.CAKE.getDefaultInstance();
    }

    public boolean noRotate() {
        return this.entityData.get(FROZEN);
    }

    public float getStoredPitch() {
        return this.entityData.get(PITCH);
    }

    public float getStoredYaw() {
        return this.entityData.get(YAW);
    }

    @Override
    protected void defineSynchedData(@NotNull SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FROZEN, false);
        builder.define(PITCH, this.getXRot());
        builder.define(YAW, this.getYRot());
    }

    private int lowGravityTicksRemaining = 0;
    private int lastParticleTime = 0;

    @Override
    public void tick() {

        if (lastParticleTime > 0) {
            lastParticleTime--;
        }
        if (this.tickCount > 400) {
            this.discard();
        }
        if (lowGravityTicksRemaining > 0 && this.getDeltaMovement().y() < -0.05) {
            this.push(0, 0.04, 0);
            lowGravityTicksRemaining--;
        }
        if (!this.noRotate()) {
            this.entityData.set(PITCH, this.getXRot());
            this.entityData.set(YAW, this.getYRot());
        }
        if (this.getDeltaMovement().y() < 0 && this.getDeltaMovement().horizontalDistance() < 0.2 && lastParticleTime <= 0) {
            this.level().broadcastEntityEvent(this, EntityEvent.POOF);
            if (this.getDeltaMovement().y() < -0.5) {
                this.lastParticleTime = 2;
            } else if (this.getDeltaMovement().y() < -0.2) {
                this.lastParticleTime = 6;
            } else {
                this.lastParticleTime = 10;
            }
        }

        super.tick();
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult entityHitResult) {
        if (!this.level().isClientSide() && !(entityHitResult.getEntity() instanceof CakeEntity)) {
            this.playSound(this.getHitGroundSoundEvent(), 1.0F, 1.0F / (this.random.nextFloat() * 0.2F + 0.9F));
            this.level().broadcastEntityEvent(this, EntityEvent.DEATH);
            this.setDeltaMovement(Vec3.ZERO);
            this.setNoGravity(true);
            this.entityData.set(FROZEN, true);

            TickDelayScheduler.schedule(5, () -> {
                this.setNoGravity(false);
                this.lowGravityTicksRemaining = 20;
            });
        }
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult blockHitResult) {
        if (!this.level().isClientSide()) {
            this.playSound(this.getHitGroundSoundEvent(), 1.0F, 1.0F / (this.random.nextFloat() * 0.2F + 0.9F));
            this.level().broadcastEntityEvent(this, EntityEvent.DEATH);
            this.setDeltaMovement(0, 0, 0);
            this.setNoGravity(true);
            this.entityData.set(FROZEN, true);

            Vector3f normal = blockHitResult.getDirection().step();
            this.setPos(
                    this.getX() + normal.x * 0.1,
                    this.getY(),
                    this.getZ() + normal.z * 0.1
            );

            TickDelayScheduler.schedule(5, () -> {
                this.setNoGravity(false);
                this.lowGravityTicksRemaining = 20;
            });
            TickDelayScheduler.schedule(60, this::discard);
        }

        super.onHitBlock(blockHitResult);
        this.setSoundEvent(AntiqueSounds.CAKE_SPLAT);
    }

    @Override
    protected @NotNull ItemStack getDefaultPickupItem() {
        return Items.CAKE.getDefaultInstance();
    }

    @Override
    protected @NotNull SoundEvent getDefaultHitGroundSoundEvent() {
        return AntiqueSounds.CAKE_SPLAT;
    }
}
