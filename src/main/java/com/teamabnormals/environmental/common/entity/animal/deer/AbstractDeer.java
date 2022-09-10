package com.teamabnormals.environmental.common.entity.animal.deer;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.teamabnormals.environmental.core.other.EnvironmentalTags;
import com.teamabnormals.environmental.core.registry.EnvironmentalSoundEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractDeer extends Animal {
	private static final EntityDataAccessor<Integer> TARGET_NECK_ANGLE = SynchedEntityData.defineId(AbstractDeer.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> HAS_ANTLERS = SynchedEntityData.defineId(AbstractDeer.class, EntityDataSerializers.BOOLEAN);

	private static final EntityDimensions GRAZING_DIMENSIONS = EntityDimensions.scalable(0.8F, 1.2F);

	private static final Predicate<LivingEntity> AVOID_ENTITY_PREDICATE = (entity) -> {
		return entity.getType().is(EnvironmentalTags.EntityTypes.SCARES_DEER) && !entity.isDiscrete() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity);
	};
	private static final Predicate<LivingEntity> TRUSTING_AVOID_ENTITY_PREDICATE = (entity) -> {
		if (!entity.getType().is(EnvironmentalTags.EntityTypes.SCARES_TRUSTING_DEER) || (entity instanceof TamableAnimal && ((TamableAnimal) entity).isTame())) {
			return false;
		} else {
			return !entity.isDiscrete() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity);
		}
	};
	private static final TargetingConditions AVOID_ENTITY_TARGETING = TargetingConditions.forCombat().range(10.0D).selector(AVOID_ENTITY_PREDICATE);
	private static final TargetingConditions TRUSTING_AVOID_ENTITY_TARGETING = TargetingConditions.forCombat().range(10.0D).selector(TRUSTING_AVOID_ENTITY_PREDICATE);

	private float neckAngle;
	private float neckAngleO;
	private float sprintAmount;
	private float sprintAmountO;

	public AbstractDeer(EntityType<? extends Animal> type, Level level) {
		super(type, level);
		this.neckAngle = 15F;
		this.neckAngleO = 15F;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(TARGET_NECK_ANGLE, 15);
		this.entityData.define(HAS_ANTLERS, true);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putBoolean("Antlers", this.hasAntlers());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		this.setHasAntlers(compound.getBoolean("Antlers"));
	}

	@Override
	public void customServerAiStep() {
		this.setSprinting(!this.isInWater() && this.getMoveControl().hasWanted() && this.getMoveControl().getSpeedModifier() >= 1.75D);
		super.customServerAiStep();
	}

	@Override
	public void tick() {
		super.tick();

		this.updateNeckAngle();
		this.updateSprintAnimation();
	}

	private void updateNeckAngle() {
		this.neckAngleO = this.neckAngle;
		int i = this.getTargetNeckAngle();
		float f = this.neckAngle + (i - this.neckAngle) * 0.3F;
		if (this.neckAngle < i == f > i) {
			this.neckAngle = i;
		} else {
			this.neckAngle = f;
		}
	}

	private void updateSprintAnimation() {
		this.sprintAmountO = this.sprintAmount;
		if (this.isSprinting()) {
			this.sprintAmount = Math.min(1.0F, this.sprintAmount + 0.2F);
		} else {
			this.sprintAmount = Math.max(0.0F, this.sprintAmount - 0.2F);
		}
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
		return this.isGrazing() ? dimensions.height * 0.3F : dimensions.height * 0.95F;
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		if (this.isGrazing()) {
			return GRAZING_DIMENSIONS.scale(this.getScale());
		} else {
			return super.getDimensions(pose);
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return EnvironmentalSoundEvents.DEER_AMBIENT.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return EnvironmentalSoundEvents.DEER_HURT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return EnvironmentalSoundEvents.DEER_DEATH.get();
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState blockIn) {
		this.playSound(EnvironmentalSoundEvents.DEER_STEP.get(), 0.15F, 1.0F);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		if (TARGET_NECK_ANGLE.equals(key)) {
			this.refreshDimensions();
		}

		super.onSyncedDataUpdated(key);
	}

	private int getTargetNeckAngle() {
		return this.entityData.get(TARGET_NECK_ANGLE);
	}

	public void setTargetNeckAngle(int angle) {
		this.entityData.set(TARGET_NECK_ANGLE, angle);
	}

	public void resetTargetNeckAngle() {
		this.setTargetNeckAngle(15);
	}

	public float getNeckAngle(float partialTick) {
		return Mth.lerp(partialTick, this.neckAngleO, this.neckAngle);
	}

	private boolean isGrazing() {
		return this.getTargetNeckAngle() >= 90;
	}

	public float getSprintAmount(float partialTick) {
		return Mth.lerp(partialTick, this.sprintAmountO, this.sprintAmount);
	}

	public void setHasAntlers(boolean antlers) {
		this.entityData.set(HAS_ANTLERS, antlers);
	}

	public boolean hasAntlers() {
		return this.entityData.get(HAS_ANTLERS);
	}

	public boolean isTrusting() {
		return false;
	}

	public LivingEntity getNearestScaryEntity() {
		return this.level.getNearestEntity(this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(10.0D, 4.0D, 10.0D), (p_148124_) -> {
			return true;
		}), this.isTrusting() ? TRUSTING_AVOID_ENTITY_TARGETING : AVOID_ENTITY_TARGETING, this, this.getX(), this.getY(), this.getZ());
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
		spawnDataIn = super.finalizeSpawn(worldIn, difficulty, reason, spawnDataIn, dataTag);
		this.setHasAntlers(this.random.nextBoolean());
		return super.finalizeSpawn(worldIn, difficulty, reason, spawnDataIn, dataTag);
	}
}
