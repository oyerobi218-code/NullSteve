package com.sahis.nullsteve.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class NullSteveEntity extends PathAwareEntity {

/** lifetime in ticks before this entity removes itself silently */
private int despawnTicks = 200; // 10 seconds default

/** if true, despawns the moment a player looks directly at it */
private boolean despawnOnSight = false;

public NullSteveEntity(EntityType<? extends PathAwareEntity> type, World world) {
    super(type, world);
    this.setPersistent();
    this.setAiDisabled(false);
    this.setSilent(true);
    this.setInvulnerable(true);
    this.noClip = false;
}

public static DefaultAttributeContainer.Builder createAttributes() {
    return PathAwareEntity.createMobAttributes()
        .add(EntityAttributes.GENERIC_MAX_HEALTH, 1024.0)
        .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.22)
        .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0)
        .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
        .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0.0);
}

public void setDespawnTicks(int ticks) {
    this.despawnTicks = ticks;
}

public void setDespawnOnSight(boolean v) {
    this.despawnOnSight = v;
}

@Override
public boolean damage(DamageSource source, float amount) {
    // Completely immune. Cannot be killed.
    return false;
}

@Override
public boolean cannotDespawn() {
    return false;
}

@Override
public boolean canBeHitByProjectile() {
    return false;
}

@Override
public boolean canTakeDamage() {
    return false;
}

@Override
public boolean pushedByFluids() {
    return false;
}

@Override
protected void pushAway(net.minecraft.entity.Entity entity) {
    // never push
}

@Override
public void pushAwayFrom(net.minecraft.entity.Entity entity) {
    // never push
}

@Override
public void tick() {
    super.tick();
    if (this.getWorld().isClient) return;

    // self-removal logic, fully safe
    if (despawnTicks > 0 && this.age >= despawnTicks) {
        this.discard();
        return;
    }
    if (despawnOnSight) {
        PlayerEntity nearest = this.getWorld().getClosestPlayer(this, 64.0);
        if (nearest != null && playerIsLookingAt(nearest)) {
            this.discard();
        }
    }
}

private boolean playerIsLookingAt(PlayerEntity player) {
    net.minecraft.util.math.Vec3d look = player.getRotationVec(1.0f).normalize();
    net.minecraft.util.math.Vec3d toEntity = this.getPos()
        .add(0, this.getHeight() * 0.5, 0)
        .subtract(player.getEyePos())
        .normalize();
    double dot = look.dotProduct(toEntity);
    return dot > 0.985 && player.canSee(this);
}

@Override
public boolean canBreatheInWater() {
    return true;
}

@Override
public boolean isFireImmune() {
    return true;
}

@Override
public boolean isPushable() {
    return false;
}

@Override
public boolean collides() {
    return true;
}

@Override
public void writeCustomDataToNbt(NbtCompound nbt) {
    super.writeCustomDataToNbt(nbt);
    nbt.putInt("DespawnTicks", despawnTicks);
    nbt.putBoolean("DespawnOnSight", despawnOnSight);
}

@Override
public void readCustomDataFromNbt(NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    if (nbt.contains("DespawnTicks")) despawnTicks = nbt.getInt("DespawnTicks");
    if (nbt.contains("DespawnOnSight")) despawnOnSight = nbt.getBoolean("DespawnOnSight");
}

@Override
protected @Nullable net.minecraft.sound.SoundEvent getAmbientSound() { return null; }

@Override
protected @Nullable net.minecraft.sound.SoundEvent getHurtSound(DamageSource source) { return null; }

@Override
protected @Nullable net.minecraft.sound.SoundEvent getDeathSound() { return null; }
}
