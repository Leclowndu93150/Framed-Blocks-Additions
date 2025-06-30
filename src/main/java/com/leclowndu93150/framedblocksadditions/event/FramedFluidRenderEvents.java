package com.leclowndu93150.framedblocksadditions.event;

import com.leclowndu93150.framedblocksadditions.Framedblocksadditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xfacthd.framedblocks.api.block.FramedBlockEntity;
import xfacthd.framedblocks.api.block.IFramedBlock;
import xfacthd.framedblocks.api.camo.CamoContainer;
import xfacthd.framedblocks.common.data.camo.FluidCamoContainer;

@Mod.EventBusSubscriber(modid = Framedblocksadditions.MODID, value = Dist.CLIENT)
public class FramedFluidRenderEvents {

    @SubscribeEvent
    public static void onRenderBlockOverlay(RenderBlockScreenEffectEvent event) {
        if (event.getOverlayType() == RenderBlockScreenEffectEvent.OverlayType.BLOCK) {
            Player player = event.getPlayer();
            Level level = player.level();
            BlockPos pos = event.getBlockPos();
            BlockState state = level.getBlockState(pos);

            if (state.getBlock() instanceof IFramedBlock &&
                    level.getBlockEntity(pos) instanceof FramedBlockEntity be) {

                CamoContainer camo = be.getCamo();
                if (camo instanceof FluidCamoContainer fluidCamo) {
                    event.setCanceled(true);

                    Fluid fluid = fluidCamo.getFluid();
                    if (fluid.is(FluidTags.WATER)) {
                        ScreenEffectRenderer.renderFluid(
                                Minecraft.getInstance(),
                                event.getPoseStack(),
                                new ResourceLocation("textures/misc/underwater.png")
                        );
                    } else if (!fluid.getFluidType().isAir()) {
                        IClientFluidTypeExtensions.of(fluid.getFluidType()).renderOverlay(
                                Minecraft.getInstance(),
                                event.getPoseStack()
                        );
                    }
                }
            }
        }
    }
}
