package com.leclowndu93150.framedblocksadditions.event;

import com.leclowndu93150.framedblocksadditions.Framedblocksadditions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.event.entity.living.LivingBreatheEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xfacthd.framedblocks.api.block.IFramedBlock;
import xfacthd.framedblocks.api.block.FramedBlockEntity;
import xfacthd.framedblocks.common.data.camo.FluidCamoContainer;

@Mod.EventBusSubscriber(modid = Framedblocksadditions.MODID)
public class FramedFluidBreathingHandler {

    @SubscribeEvent
    public static void onLivingBreathe(LivingBreatheEvent event) {
        LivingEntity entity = event.getEntity();
        BlockPos eyePos = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
        BlockState blockState = entity.level().getBlockState(eyePos);

        if (blockState.getBlock() instanceof IFramedBlock &&
                entity.level().getBlockEntity(eyePos) instanceof FramedBlockEntity be &&
                be.getCamo() instanceof FluidCamoContainer fluidCamo) {

            if (fluidCamo.getFluid() == Fluids.WATER || fluidCamo.getFluid() == Fluids.FLOWING_WATER) {
                event.setCanBreathe(false);
            }
        }
    }
}