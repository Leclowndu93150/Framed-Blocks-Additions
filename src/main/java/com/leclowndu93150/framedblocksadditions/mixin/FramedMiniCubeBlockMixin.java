package com.leclowndu93150.framedblocksadditions.mixin;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xfacthd.framedblocks.api.block.FramedProperties;
import xfacthd.framedblocks.api.block.PlacementStateBuilder;
import xfacthd.framedblocks.common.block.cube.FramedMiniCubeBlock;

@Mixin(value = FramedMiniCubeBlock.class)
public class FramedMiniCubeBlockMixin
{
    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void addTopProperty(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci)
    {
        builder.add(FramedProperties.TOP);
    }

    @Inject(method = "getStateForPlacement", at = @At("HEAD"), cancellable = true)
    private void handleTopPlacement(BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> cir)
    {
        // Check if we should place on top (clicking upper half of block face)
        boolean top = ctx.getClickLocation().y - ctx.getClickedPos().getY() > 0.5;

        BlockState state = PlacementStateBuilder.of((FramedMiniCubeBlock)(Object)this, ctx)
                .withCustom((blockState, modCtx) -> blockState.setValue(
                        BlockStateProperties.ROTATION_16,
                        RotationSegment.convertToSegment(modCtx.getRotation() + 180F)
                ))
                .withCustom((blockState, modCtx) -> blockState.setValue(FramedProperties.TOP, top))
                .withWater()
                .build();

        cir.setReturnValue(state);
    }
}