package com.leclowndu93150.framedblocksadditions.mixin.shenanigans;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.ForgeMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xfacthd.framedblocks.api.block.IFramedBlock;
import xfacthd.framedblocks.api.block.FramedBlockEntity;
import xfacthd.framedblocks.common.data.camo.FluidCamoContainer;

@Mixin(Player.class)
public abstract class PlayerFramedFluidMixin extends LivingEntity {

    protected PlayerFramedFluidMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean isUnderWater() {
        BlockPos eyePos = BlockPos.containing(this.getX(), this.getEyeY(), this.getZ());
        if (hasFramedWater(eyePos)) {
            BlockPos feetPos = this.blockPosition();
            if (hasFramedWater(feetPos) || hasFramedWater(feetPos.above())) {
                return true;
            }
        }
        return super.isUnderWater();
    }

    @Override
    public boolean isInWater() {
        BlockPos pos = this.blockPosition();
        return hasFramedWater(pos) || hasFramedWater(pos.above()) || super.isInWater();
    }

    @Override
    public boolean canStartSwimming() {
        if (!this.onGround() && !this.isPassenger()) {
            BlockPos pos = this.blockPosition();
            if (hasFramedWater(pos) || hasFramedWater(pos.above())) {
                return true;
            }
        }
        return super.canStartSwimming();
    }

    @Inject(at = @At(value = "TAIL"), method = "tick")
    public void handleFramedFluidJump(CallbackInfo ci) {
        if (isInFramedWater() && jumping) {
            this.jumpInFluid(ForgeMod.WATER_TYPE.get());
        }
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        super.checkFallDamage(y, onGround, state, pos);
        if (hasFramedWater(pos) || hasFramedWater(this.blockPosition())) {
            this.resetFallDistance();
        }
    }

    private boolean hasFramedWater(BlockPos pos) {
        BlockState blockState = this.level().getBlockState(pos);

        if (blockState.getBlock() instanceof IFramedBlock &&
                this.level().getBlockEntity(pos) instanceof FramedBlockEntity be) {

            if (be.getCamo() instanceof FluidCamoContainer fluidCamo) {
                return fluidCamo.getFluid() == Fluids.WATER ||
                        fluidCamo.getFluid() == Fluids.FLOWING_WATER;
            }
        }
        return false;
    }

    private boolean isInFramedWater() {
        BlockPos pos = this.blockPosition();
        return hasFramedWater(pos) || hasFramedWater(pos.above());
    }
}