package com.leclowndu93150.framedblocksadditions.mixin;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xfacthd.framedblocks.api.block.FramedProperties;
import xfacthd.framedblocks.api.model.quad.Modifiers;
import xfacthd.framedblocks.api.model.quad.QuadModifier;
import xfacthd.framedblocks.api.util.Utils;
import xfacthd.framedblocks.client.model.cube.FramedMiniCubeModel;

import java.util.List;
import java.util.Map;

@Mixin(value = FramedMiniCubeModel.class)
public class FramedMiniCubeModelMixin
{
    private static final Vector3f ORIGIN_BOTTOM = new Vector3f(.5F, 0F, .5F);
    private static final Vector3f ORIGIN_TOP = new Vector3f(.5F, 1F, .5F);

    @Shadow @Final @Mutable
    private float rotAngle;

    private Direction bottomFace;
    private Vector3f origin;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initTopBottomLogic(BlockState state, BakedModel baseModel, CallbackInfo ci)
    {
        boolean top = state.getValue(FramedProperties.TOP);
        this.bottomFace = top ? Direction.UP : Direction.DOWN;
        this.origin = top ? ORIGIN_TOP : ORIGIN_BOTTOM;
    }

    @Inject(method = "transformQuad", at = @At("HEAD"), cancellable = true, remap = false)
    private void transformQuadWithTopBottom(Map<Direction, List<BakedQuad>> quadMap, BakedQuad quad, CallbackInfo ci)
    {
        Direction quadDir = quad.getDirection();
        QuadModifier.geometry(quad)
                .apply(Modifiers.scaleFace(.5F, origin))
                .applyIf(Modifiers.setPosition(.5F), quadDir == bottomFace.getOpposite())
                .applyIf(Modifiers.setPosition(.75F), !Utils.isY(quadDir))
                .apply(Modifiers.rotate(Direction.Axis.Y, origin, rotAngle, false))
                .export(quadMap.get(quadDir == bottomFace ? quadDir : null));

        ci.cancel();
    }
}