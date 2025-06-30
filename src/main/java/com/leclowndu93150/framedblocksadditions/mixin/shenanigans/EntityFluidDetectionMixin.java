package com.leclowndu93150.framedblocksadditions.mixin.shenanigans;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xfacthd.framedblocks.api.block.IFramedBlock;
import xfacthd.framedblocks.api.block.FramedBlockEntity;
import xfacthd.framedblocks.common.data.camo.FluidCamoContainer;

@Mixin(Entity.class)
public abstract class EntityFluidDetectionMixin {
    

    @Shadow
    public abstract Vec3 position();

    @Shadow
    public abstract double getEyeY();

    @Shadow private Level level;

    @Redirect(
            method = "updateFluidHeightAndDoFluidPushing(Ljava/util/function/Predicate;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"
            )
    )
    private FluidState redirectGetFluidState(Level level, BlockPos pos) {
        return getFluidStateOrFramedFluid(level, pos);
    }

    @Inject(
            method = "updateFluidOnEyes()V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void updateFluidOnEyes(CallbackInfo ci) {
        double eyeY = this.getEyeY() - 0.11111111F;
        BlockPos eyePos = BlockPos.containing(this.position().x, eyeY, this.position().z);
        BlockState blockState = this.level.getBlockState(eyePos);

        if (blockState.getBlock() instanceof IFramedBlock &&
                this.level.getBlockEntity(eyePos) instanceof FramedBlockEntity be) {

            if (be.getCamo() instanceof FluidCamoContainer fluidCamo) {
                FluidState fluidState = fluidCamo.getFluid().defaultFluidState();
                double fluidHeight = (double) ((float) eyePos.getY() + fluidState.getHeight(this.level, eyePos));

                if (fluidHeight > eyeY) {
                    Entity entity = (Entity) (Object) this;
                    try {
                        var wasEyeInWaterField = Entity.class.getDeclaredField("wasEyeInWater");
                        wasEyeInWaterField.setAccessible(true);
                        wasEyeInWaterField.setBoolean(entity, fluidCamo.getFluid() == Fluids.WATER);

                        var fluidOnEyesField = Entity.class.getDeclaredField("fluidOnEyes");
                        fluidOnEyesField.setAccessible(true);
                        var fluidOnEyes = (java.util.Set<TagKey<Fluid>>) fluidOnEyesField.get(entity);
                        fluidOnEyes.clear();
                        if (fluidCamo.getFluid() == Fluids.WATER) {
                            fluidOnEyes.add(net.minecraft.tags.FluidTags.WATER);
                        } else if (fluidCamo.getFluid() == Fluids.LAVA) {
                            fluidOnEyes.add(net.minecraft.tags.FluidTags.LAVA);
                        }

                        ci.cancel();
                        return;
                    } catch (Exception e) {
                        // Fall back to normal behavior if reflection fails
                    }
                }
            }
        }
    }

    @Inject(method = "isInWater()Z", at = @At("HEAD"), cancellable = true)
    private void checkFramedWater(CallbackInfoReturnable<Boolean> cir) {
        BlockPos entityPos = BlockPos.containing(this.position());

        if (hasFramedFluid(entityPos, Fluids.WATER) ||
                hasFramedFluid(entityPos.above(), Fluids.WATER)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isInLava()Z", at = @At("HEAD"), cancellable = true)
    private void checkFramedLava(CallbackInfoReturnable<Boolean> cir) {
        BlockPos entityPos = BlockPos.containing(this.position());

        if (hasFramedFluid(entityPos, Fluids.LAVA) ||
                hasFramedFluid(entityPos.above(), Fluids.LAVA)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z", at = @At("HEAD"), cancellable = true)
    private void checkEyeInFramedFluid(TagKey<Fluid> fluidTag, CallbackInfoReturnable<Boolean> cir) {
        BlockPos eyePos = BlockPos.containing(this.position().x, this.getEyeY(), this.position().z);
        BlockState blockState = this.level.getBlockState(eyePos);

        if (blockState.getBlock() instanceof IFramedBlock &&
                this.level.getBlockEntity(eyePos) instanceof FramedBlockEntity be) {

            if (be.getCamo() instanceof FluidCamoContainer fluidCamo) {
                Fluid fluid = fluidCamo.getFluid();
                if (fluid.defaultFluidState().is(fluidTag)) {
                    cir.setReturnValue(true);
                }
            }
        }
    }

    @Inject(method = "isUnderWater()Z", at = @At("HEAD"), cancellable = true)
    private void checkFramedUnderWater(CallbackInfoReturnable<Boolean> cir) {
        BlockPos eyePos = BlockPos.containing(this.position().x, this.getEyeY(), this.position().z);
        BlockState blockState = this.level.getBlockState(eyePos);

        if (blockState.getBlock() instanceof IFramedBlock &&
                this.level.getBlockEntity(eyePos) instanceof FramedBlockEntity be) {

            if (be.getCamo() instanceof FluidCamoContainer fluidCamo) {
                if (fluidCamo.getFluid() == Fluids.WATER) {
                    BlockPos entityPos = BlockPos.containing(this.position());
                    if (hasFramedFluid(entityPos, Fluids.WATER) || hasFramedFluid(entityPos.above(), Fluids.WATER)) {
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }

    @Inject(method = "isInWall()Z", at = @At("HEAD"), cancellable = true)
    private void preventFramedFluidSuffocation(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        if (entity.noPhysics) {
            cir.setReturnValue(false);
            return;
        }

        AABB aabb = entity.getBoundingBox().deflate(0.001);
        int minX = (int) Math.floor(aabb.minX);
        int maxX = (int) Math.ceil(aabb.maxX);
        int minY = (int) Math.floor(aabb.minY);
        int maxY = (int) Math.ceil(aabb.maxY);
        int minZ = (int) Math.floor(aabb.minZ);
        int maxZ = (int) Math.ceil(aabb.maxZ);

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    mutablePos.set(x, y, z);
                    BlockState blockState = this.level.getBlockState(mutablePos);

                    if (blockState.getBlock() instanceof IFramedBlock &&
                            this.level.getBlockEntity(mutablePos) instanceof FramedBlockEntity be &&
                            be.getCamo() instanceof FluidCamoContainer) {
                        continue;
                    }

                    if (blockState.isSuffocating(this.level, mutablePos) &&
                            blockState.isCollisionShapeFullBlock(this.level, mutablePos)) {
                        cir.setReturnValue(true);
                        return;
                    }
                }
            }
        }

        cir.setReturnValue(false);
    }


    private boolean hasFramedFluid(BlockPos pos, Fluid targetFluid) {
        BlockState blockState = this.level.getBlockState(pos);

        if (blockState.getBlock() instanceof IFramedBlock &&
                this.level.getBlockEntity(pos) instanceof FramedBlockEntity be) {

            if (be.getCamo() instanceof FluidCamoContainer fluidCamo) {
                return fluidCamo.getFluid() == targetFluid;
            }
        }
        return false;
    }

    private FluidState getFluidStateOrFramedFluid(Level level, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos);

        if (blockState.getBlock() instanceof IFramedBlock &&
                level.getBlockEntity(pos) instanceof FramedBlockEntity be) {

            if (be.getCamo() instanceof FluidCamoContainer fluidCamo) {
                return fluidCamo.getFluid().defaultFluidState();
            }
        }

        return level.getFluidState(pos);
    }
}