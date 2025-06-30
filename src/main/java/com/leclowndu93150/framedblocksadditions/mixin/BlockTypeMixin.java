package com.leclowndu93150.framedblocksadditions.mixin;

import com.google.common.collect.ImmutableList;
import com.leclowndu93150.framedblocksadditions.shapes.MiniCubeShapes;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xfacthd.framedblocks.api.shapes.ShapeProvider;
import xfacthd.framedblocks.common.data.BlockType;

@Mixin(BlockType.class)
public class BlockTypeMixin
{
    @Inject(method = "generateShapes", at = @At("HEAD"), cancellable = true, remap = false)
    private void generateMiniCubeShapes(ImmutableList<BlockState> states, CallbackInfoReturnable<ShapeProvider> cir)
    {
        BlockType self = (BlockType)(Object)this;

        if (self == BlockType.FRAMED_MINI_CUBE)
        {
            cir.setReturnValue(MiniCubeShapes.generate(states));
        }
    }
}