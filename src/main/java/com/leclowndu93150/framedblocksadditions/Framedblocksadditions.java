package com.leclowndu93150.framedblocksadditions;

import com.leclowndu93150.framedblocksadditions.network.NetworkHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Framedblocksadditions.MODID)
public class Framedblocksadditions {

    public static final String MODID = "framedblocksadditions";
    private static final Logger LOGGER = LogUtils.getLogger();


    public Framedblocksadditions() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::init);
    }
}
