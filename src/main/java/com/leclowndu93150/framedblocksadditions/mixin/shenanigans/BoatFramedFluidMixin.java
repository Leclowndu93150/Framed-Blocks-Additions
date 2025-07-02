package com.leclowndu93150.framedblocksadditions.mixin.shenanigans;

import com.leclowndu93150.framedblocksadditions.network.BoatBuoyancyPacket;
import com.leclowndu93150.framedblocksadditions.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.extensions.IForgeBoat;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xfacthd.framedblocks.api.block.IFramedBlock;
import xfacthd.framedblocks.api.block.FramedBlockEntity;
import xfacthd.framedblocks.common.data.camo.FluidCamoContainer;

@Mixin(Boat.class)
public abstract class BoatFramedFluidMixin extends Entity implements IForgeBoat {

    public BoatFramedFluidMixin(EntityType<?> p_19870_, Level p_19871_) {
        super(null, null);
    }

    @Redirect(
            method = {"checkInWater", "getWaterLevelAbove"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"
            )
    )
    private FluidState redirectGetFluidStateForWater(Level level, BlockPos pos) {
        return getFluidStateOrFramedFluid(level, pos);
    }

    @Redirect(
            method = "isUnderwater",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"
            )
    )
    private FluidState redirectGetFluidStateForUnderwater(Level level, BlockPos pos) {
        return getFluidStateOrFramedFluid(level, pos);
    }

    @Redirect(
            method = {"checkInWater", "getWaterLevelAbove", "isUnderwater"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FluidState;getHeight(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"
            )
    )
    private float redirectFluidHeight(FluidState fluidState, BlockGetter blockGetter, BlockPos pos) {
        if (hasFramedFluid(pos)) {
            return 1.2F;
        }
        return fluidState.getHeight(blockGetter, pos);
    }

    @Redirect(
            method = {"checkInWater", "getWaterLevelAbove", "isUnderwater"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/vehicle/Boat;canBoatInFluid(Lnet/minecraft/world/level/material/FluidState;)Z",
                    remap = false
            )
    )
    private boolean redirectCanBoatInFluid(Boat instance, FluidState fluidState) {
        if (fluidState.is(Fluids.WATER) || fluidState.is(Fluids.FLOWING_WATER)) {
            return true;
        }
        return instance.canBoatInFluid(fluidState);
    }

    @Inject(
            method = "getGroundFriction",
            at = @At("HEAD"),
            cancellable = true
    )
    private void checkFramedFluidFriction(CallbackInfoReturnable<Float> cir) {
        Boat boat = (Boat)(Object)this;
        AABB aabb = boat.getBoundingBox();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        int minX = (int)Math.floor(aabb.minX);
        int maxX = (int)Math.ceil(aabb.maxX);
        int minY = (int)Math.floor(aabb.minY - 0.001);
        int maxY = (int)Math.ceil(aabb.minY);
        int minZ = (int)Math.floor(aabb.minZ);
        int maxZ = (int)Math.ceil(aabb.maxZ);

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    mutablePos.set(x, y, z);
                    if (hasFramedFluid(mutablePos)) {
                        cir.setReturnValue(0.0F);
                        return;
                    }
                }
            }
        }
    }

    /*
    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void applyFramedFluidBuoyancy(CallbackInfo ci) {
        if (!this.level().isClientSide) {
            Boat boat = (Boat)(Object)this;

            if (boat.isUnderWater() && isInFramedWaterFluid() && boat.getDeltaMovement().y < 0.05) {
                Vec3 deltaMovement = boat.getDeltaMovement();
                double buoyancyForce = boat.getPassengers().isEmpty() ? 0.15 : 2;
                boat.setDeltaMovement(deltaMovement.x, deltaMovement.y + buoyancyForce, deltaMovement.z);
            }
        }
    }
     */

    @Inject(
            method = "floatBoat",
            at = @At("TAIL")
    )
    private void applyFramedFluidBuoyancy(CallbackInfo ci) {
        Boat boat = (Boat)(Object)this;

        if (boat.isUnderWater() && isInFramedWaterFluid()) {
            Vec3 deltaMovement = boat.getDeltaMovement();

            double buoyancyForce = 0;
            if (boat.getControllingPassenger() instanceof Player) {
                if (deltaMovement.y < 0.1) {
                    buoyancyForce = 0.10;
                }
            } else {
                if (deltaMovement.y < 0.05) {
                    buoyancyForce = 0.15;
                }
            }

            if (buoyancyForce > 0) {
                Vec3 newMovement = new Vec3(deltaMovement.x, deltaMovement.y + buoyancyForce, deltaMovement.z);
                boat.setDeltaMovement(newMovement);

                if (!this.level().isClientSide && boat.getControllingPassenger() instanceof ServerPlayer player) {
                    NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new BoatBuoyancyPacket(boat.getId(), newMovement));
                }
            }
        }
    }

    private boolean isInFramedWaterFluid() {
        Boat boat = (Boat)(Object)this;
        AABB aabb = boat.getBoundingBox();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        // Check the center and slightly above center of the boat
        double centerX = (aabb.minX + aabb.maxX) / 2.0;
        double centerY = aabb.minY + (aabb.maxY - aabb.minY) * 0.6; // 60% up from bottom
        double centerZ = (aabb.minZ + aabb.maxZ) / 2.0;

        mutablePos.set((int)Math.floor(centerX), (int)Math.floor(centerY), (int)Math.floor(centerZ));
        return hasFramedWaterFluid(mutablePos);
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

    private boolean hasFramedFluid(BlockPos pos) {
        BlockState blockState = this.level().getBlockState(pos);

        if (blockState.getBlock() instanceof IFramedBlock &&
                this.level().getBlockEntity(pos) instanceof FramedBlockEntity be) {

            return be.getCamo() instanceof FluidCamoContainer;
        }
        return false;
    }

    private boolean hasFramedWaterFluid(BlockPos pos) {
        BlockState blockState = this.level().getBlockState(pos);

        if (blockState.getBlock() instanceof IFramedBlock &&
                this.level().getBlockEntity(pos) instanceof FramedBlockEntity be) {

            if (be.getCamo() instanceof FluidCamoContainer fluidCamo) {
                FluidState fluid = fluidCamo.getFluid().defaultFluidState();
                return fluid.is(Fluids.WATER) || fluid.is(Fluids.FLOWING_WATER);
            }
        }
        return false;
    }
}