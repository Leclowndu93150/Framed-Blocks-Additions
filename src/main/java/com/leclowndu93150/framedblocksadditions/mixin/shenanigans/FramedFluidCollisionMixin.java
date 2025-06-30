package com.leclowndu93150.framedblocksadditions.mixin.shenanigans;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import xfacthd.framedblocks.api.block.AbstractFramedBlock;
import xfacthd.framedblocks.api.block.FramedBlockEntity;
import xfacthd.framedblocks.api.camo.CamoContainer;
import xfacthd.framedblocks.common.data.camo.FluidCamoContainer;

@Mixin(AbstractFramedBlock.class)
public class FramedFluidCollisionMixin extends Block {

    public FramedFluidCollisionMixin(Properties p_49795_) {
        super(null);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx)
    {
        if (level.getBlockEntity(pos) instanceof FramedBlockEntity be) {
            CamoContainer camo = be.getCamo();
            if (camo instanceof FluidCamoContainer) {
                return Shapes.empty();
            }
        }

        return super.getCollisionShape(state, level, pos, ctx);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.getBlockEntity(pos) instanceof FramedBlockEntity be &&
                be.getCamo() instanceof FluidCamoContainer fluidCamo) {

            FluidState fluidState = fluidCamo.getFluid().defaultFluidState();

            if (!fluidState.isEmpty()) {
                fluidState.getType().defaultFluidState().createLegacyBlock().entityInside(level, pos, entity);
            }
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (level.getBlockEntity(pos) instanceof FramedBlockEntity be &&
                be.getCamo() instanceof FluidCamoContainer fluidCamo) {

            if (fluidCamo.getFluid() == Fluids.WATER || fluidCamo.getFluid() == Fluids.FLOWING_WATER) {
                entity.resetFallDistance();
                return;
            }
        }
        super.fallOn(level, state, pos, entity, fallDistance);
    }

    @Override
    public void updateEntityAfterFallOn(BlockGetter level, Entity entity) {
        if (level instanceof Level actualLevel) {
            BlockPos pos = entity.blockPosition();
            if (actualLevel.getBlockEntity(pos) instanceof FramedBlockEntity be &&
                    be.getCamo() instanceof FluidCamoContainer fluidCamo) {

                if (fluidCamo.getFluid() == Fluids.WATER || fluidCamo.getFluid() == Fluids.FLOWING_WATER) {
                    Vec3 motion = entity.getDeltaMovement();
                    entity.setDeltaMovement(motion.x * 0.5, motion.y, motion.z * 0.5);
                    return;
                }
            }
        }
        super.updateEntityAfterFallOn(level, entity);
    }

}